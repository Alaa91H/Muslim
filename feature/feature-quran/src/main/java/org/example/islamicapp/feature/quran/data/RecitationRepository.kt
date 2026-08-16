package org.example.islamicapp.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.example.islamicapp.core.network.FileDownloader
import org.example.islamicapp.feature.quran.domain.Reciter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-demand Quran recitation audio (PROJECT_PROMPT.md §6 Phase 2: قرّاء
 * متعددون + تنزيل). Files live in app-private storage under
 * `quran_recitations/<reciter>/<surah>/<global>.mp3`, so everything is
 * offline after download and removable by the user.
 */
@Singleton
class RecitationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloader: FileDownloader,
    private val prefsRepository: QuranPrefsRepository,
) {

    private val recitationsDir: File
        get() = File(context.filesDir, "quran_recitations").apply { mkdirs() }

    fun reciterDir(reciterId: String): File =
        File(recitationsDir, reciterId).apply { mkdirs() }

    fun fileFor(reciterId: String, surahNumber: Int, globalNumber: Int): File =
        File(File(reciterDir(reciterId), surahNumber.toString()), "$globalNumber.mp3")

    /** The currently selected reciter (bundled list fallback). */
    suspend fun selectedReciter(): Reciter =
        Reciter.Bundled.firstOrNull { it.id == prefsRepository.selectedReciterId.first() }
            ?: Reciter.Bundled.first()

    suspend fun isDownloaded(reciterId: String, surahNumber: Int, globalNumber: Int): Boolean =
        withContext(Dispatchers.IO) { fileFor(reciterId, surahNumber, globalNumber).exists() }

    /**
     * Downloads the whole surah for [reciter]. [ayahs] maps in-surah numbers
     * to global numbers. Reports aggregate progress (0..1).
     */
    suspend fun downloadSurah(
        reciter: Reciter,
        surahNumber: Int,
        ayahs: Map<Int, Int>, // numberInSurah -> globalNumber
        onProgress: (Float) -> Unit = {},
    ): FileDownloader.Result {
        val dir = File(reciterDir(reciter.id), surahNumber.toString()).apply { mkdirs() }
        var completed = 0
        val total = ayahs.size
        for ((inSurah, global) in ayahs) {
            val target = File(dir, "$global.mp3")
            if (target.exists()) {
                completed++
                continue
            }
            val url = reciter.urlFor(surahNumber, inSurah)
            when (val result = fileDownloader.download(url, target)) {
                is FileDownloader.Result.Success -> completed++
                is FileDownloader.Result.Failure -> {
                    target.delete()
                    onProgress(completed.toFloat() / total)
                    return result
                }
            }
            if (total > 0) onProgress(completed.toFloat() / total)
        }
        return FileDownloader.Result.Success(dir)
    }

    /** Removes the downloaded audio for one surah (storage management). */
    suspend fun deleteSurah(reciterId: String, surahNumber: Int): Boolean =
        withContext(Dispatchers.IO) {
            val dir = File(reciterDir(reciterId), surahNumber.toString())
            if (!dir.exists()) return@withContext false
            dir.listFiles()?.forEach { it.delete() }
            dir.delete()
        }

    /** Total size of downloaded recitation audio in bytes (shown to the user). */
    fun downloadedBytes(): Long =
        recitationsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}
