package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.muslim.app.core.network.FileDownloader
import org.muslim.app.feature.quran.domain.Reciter
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
     * Resolves the actual on-server size (bytes) of one ayah's audio via a
     * ranged probe (`bytes=0-0`), so download sizes are verified rather than
     * estimated. Returns null when the server doesn't report a length.
     */
    suspend fun verifiedAyahSize(reciter: Reciter, surahNumber: Int, ayahNumberInSurah: Int): Long? =
        fileDownloader.contentLength(reciter.urlFor(surahNumber, ayahNumberInSurah))

    /** Deletes in-flight `.part` files for [reciterId] (optionally one surah). */
    suspend fun deletePartials(reciterId: String, surahNumber: Int?): Unit =
        withContext(Dispatchers.IO) {
            val base = if (surahNumber != null) {
                File(reciterDir(reciterId), surahNumber.toString())
            } else {
                reciterDir(reciterId)
            }
            if (!base.exists()) return@withContext
            base.walkTopDown().filter { it.name.endsWith(".part") }.forEach { it.delete() }
        }

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

    /** Removes every downloaded surah for [reciterId]; true when anything was deleted. */
    suspend fun deleteReciter(reciterId: String): Boolean =
        withContext(Dispatchers.IO) {
            val dir = reciterDir(reciterId)
            if (!dir.exists()) return@withContext false
            val files = dir.listFiles().orEmpty()
            files.forEach { it.deleteRecursively() }
            files.isNotEmpty()
        }

    /** Total size of downloaded recitation audio in bytes (shown to the user). */
    fun downloadedBytes(): Long =
        recitationsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    /**
     * Scans disk for [reciterId] and returns what is downloaded: total ayah
     * files, bytes, and per-surah counts. Drives the per-reciter download
     * state shown in the downloads screen / reader dialog.
     */
    suspend fun downloadState(reciterId: String): ReciterDownloadState =
        withContext(Dispatchers.IO) {
            val dir = File(recitationsDir, reciterId)
            if (!dir.exists()) return@withContext ReciterDownloadState(reciterId, emptyMap(), 0L)
            val perSurah = dir.listFiles().orEmpty()
                .filter { it.isDirectory }
                .mapNotNull { surahDir ->
                    val number = surahDir.name.toIntOrNull() ?: return@mapNotNull null
                    val ayahs = surahDir.listFiles().orEmpty().count { it.isFile && it.name.endsWith(".mp3") }
                    number to ayahs
                }
                .filter { it.second > 0 }
                .toMap()
            val bytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            ReciterDownloadState(reciterId, perSurah, bytes)
        }

    /** True when every ayah of [surahNumber] is downloaded for [reciterId]. */
    suspend fun isSurahComplete(reciterId: String, surahNumber: Int, expectedAyahs: Int): Boolean =
        withContext(Dispatchers.IO) {
            val dir = File(reciterDir(reciterId), surahNumber.toString())
            if (!dir.exists()) return@withContext false
            val count = dir.listFiles().orEmpty().count { it.isFile && it.name.endsWith(".mp3") }
            count >= expectedAyahs
        }
}

/** Snapshot of what audio is downloaded for one reciter. */
data class ReciterDownloadState(
    val reciterId: String,
    /** surahNumber -> downloaded ayah count. */
    val surahCounts: Map<Int, Int>,
    val totalBytes: Long,
) {
    val downloadedAyahs: Int get() = surahCounts.values.sum()
    val downloadedSurahs: Int get() = surahCounts.keys.size
}
