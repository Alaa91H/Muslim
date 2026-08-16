package org.example.islamicapp.feature.hadith.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** A book in the hadith library catalog. */
data class HadithBook(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val authorAr: String,
    /** Bundled inside the APK (small collections). */
    val bundled: Boolean,
    /** File name on the CDN and in local storage, e.g. "ara-bukhari.json". */
    val fileName: String,
)

/** A fully loaded book with parsed hadiths. */
data class LoadedBook(
    val book: HadithBook,
    val hadiths: List<HadithDto>,
    /** Section number → section title (kutub/abwab). */
    val sections: Map<String, String>,
) {
    /** Hadiths grouped by their kitab, in book order. */
    fun bySection(): List<Pair<String, List<HadithDto>>> =
        hadiths.groupBy { it.reference?.book?.toString() ?: "0" }
            .map { (section, list) -> sections[section].orEmpty() to list }
}

data class DownloadState(
    val bookId: String? = null,
    val downloading: Boolean = false,
    val error: String? = null,
)

/**
 * Complete hadith library (طلب الإضافة "جميع الكتب كاملة"): the small
 * essential collections ship inside the APK; the full six books download
 * once from the open hadith-api CDN and are cached in app storage —
 * after the first download everything works fully offline forever.
 */
@Singleton
class HadithLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()
    private val mutex = Mutex()

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    /** Books already cached locally (bundled or downloaded). */
    private val cache = mutableMapOf<String, LoadedBook>()

    val catalog: List<HadithBook> = listOf(
        HadithBook("nawawi", "الأربعون النووية", "An-Nawawi's Forty", "الإمام النووي", true, "ara-nawawi.json"),
        HadithBook("qudsi", "الأربعون القدسية", "Al-Qudsi's Forty", "أحاديث قدسية معروفة", true, "ara-qudsi.json"),
        HadithBook("bukhari", "صحيح البخاري", "Sahih al-Bukhari", "الإمام البخاري", false, "ara-bukhari.json"),
        HadithBook("muslim", "صحيح مسلم", "Sahih Muslim", "الإمام مسلم", false, "ara-muslim.json"),
        HadithBook("abudawud", "سنن أبي داود", "Sunan Abu Dawud", "الإمام أبو داود", false, "ara-abudawud.json"),
        HadithBook("tirmidhi", "جامع الترمذي", "Jami' at-Tirmidhi", "الإمام الترمذي", false, "ara-tirmidhi.json"),
        HadithBook("nasai", "سنن النسائي", "Sunan an-Nasa'i", "الإمام النسائي", false, "ara-nasai.json"),
        HadithBook("ibnmajah", "سنن ابن ماجه", "Sunan Ibn Majah", "الإمام ابن ماجه", false, "ara-ibnmajah.json"),
    )

    fun isDownloaded(book: HadithBook): Boolean =
        book.bundled || localFile(book).exists()

    /**
     * Loads [bookId]: bundled asset or the cached download; downloads on
     * demand when missing. Safe to call repeatedly.
     */
    suspend fun load(bookId: String): Result<LoadedBook> = mutex.withLock {
        cache[bookId]?.let { return Result.success(it) }
        val book = catalog.firstOrNull { it.id == bookId }
            ?: return Result.failure(IllegalArgumentException("Unknown book $bookId"))
        return runCatching {
            val text = if (book.bundled) {
                context.assets.open("hadith_books/${book.fileName}").bufferedReader().use { it.readText() }
            } else {
                val file = localFile(book)
                if (!file.exists()) download(book)
                file.readText()
            }
            val parsed = withContext(Dispatchers.Default) { json.decodeFromString<BookFileDto>(text) }
            val loaded = LoadedBook(
                book = book,
                hadiths = parsed.hadiths,
                sections = parsed.metadata.sections,
            )
            cache[bookId] = loaded
            loaded
        }
    }

    private fun download(book: HadithBook) {
        _downloadState.value = DownloadState(bookId = book.id, downloading = true)
        try {
            val request = Request.Builder()
                .url("$CDN_BASE/editions/${book.fileName}")
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val body = response.body ?: error("Empty body")
                localFile(book).outputStream().use { out ->
                    body.byteStream().copyTo(out)
                }
            }
            _downloadState.value = DownloadState(bookId = book.id, downloading = false)
        } catch (t: Throwable) {
            _downloadState.value = DownloadState(
                bookId = book.id,
                downloading = false,
                error = t.message,
            )
            throw t
        }
    }

    /** Removes a downloaded book to free space (bundled books stay). */
    fun delete(book: HadithBook): Boolean =
        if (book.bundled) false else localFile(book).delete().also { if (it) cache.remove(book.id) }

    private fun localFile(book: HadithBook): File =
        File(File(context.filesDir, "hadith_books"), book.fileName)

    companion object {
        private const val CDN_BASE = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1"
    }
}
