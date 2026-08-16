package org.example.islamicapp.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.example.islamicapp.feature.quran.domain.Reciter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages on-demand recitation downloads (PROJECT_PROMPT.md §6 Phase 2).
 *
 * Surahs are downloaded ayah-by-ayah (which is what makes ayah-synced
 * playback possible without external timing metadata) into
 * `filesDir/quran_audio/<reciter>/`, bounded to 4 concurrent requests, with
 * progress, cancellation, and per-surah deletion. Fully offline afterwards.
 */
@Singleton
class RecitationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
) {

    enum class DownloadState {
        /** No audio for this (reciter, surah). */
        None,

        /** Download in progress; see [RecitationStatus.downloaded]/[RecitationStatus.total]. */
        Downloading,

        /** All ayahs present locally. */
        Downloaded,

        /** A previous download failed; retry is allowed. */
        Error,
    }

    data class RecitationStatus(
        val state: DownloadState = DownloadState.None,
        val downloaded: Int = 0,
        val total: Int = 0,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _statuses = MutableStateFlow<Map<String, RecitationStatus>>(emptyMap())

    /** Status keyed by `"<reciter>|<surahNumber>"`. */
    val statuses: StateFlow<Map<String, RecitationStatus>> = _statuses.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()

    private fun key(reciter: Reciter, surahNumber: Int) = "${reciter.name}|$surahNumber"

    /** Public form of the status-map key for (reciter, surah). */
    fun statusKey(reciter: Reciter, surahNumber: Int): String = key(reciter, surahNumber)

    /** Snapshot status for one (reciter, surah). */
    fun statusOf(reciter: Reciter, surahNumber: Int): RecitationStatus =
        _statuses.value[key(reciter, surahNumber)] ?: RecitationStatus()

    /** Local file for one ayah (may not exist yet). */
    fun audioFile(reciter: Reciter, globalAyahNumber: Int): File =
        RecitationAudio.fileFor(context.filesDir, reciter, globalAyahNumber)

    /** True when every ayah of the surah is already on disk. */
    fun isSurahDownloaded(reciter: Reciter, ayahGlobals: List<Int>): Boolean =
        ayahGlobals.isNotEmpty() && ayahGlobals.all { file ->
            val f = audioFile(reciter, file)
            f.isFile && f.length() > 0
        }

    /**
     * Downloads the surah's [ayahGlobals] for [reciter] with progress.
     * Cancels any running download for the same (reciter, surah).
     */
    fun download(reciter: Reciter, surahNumber: Int, ayahGlobals: List<Int>) {
        val key = key(reciter, surahNumber)
        activeJobs.remove(key)?.cancel()

        val alreadyDownloaded = ayahGlobals.count { audioFile(reciter, it).isFile && it > 0 }
        val job = scope.launch {
            _statuses.update { it + (key to RecitationStatus(
                state = DownloadState.Downloading,
                downloaded = alreadyDownloaded,
                total = ayahGlobals.size,
            )) }
            try {
                // Download in bounded batches of 4 to avoid connection floods.
                ayahGlobals.chunked(4).forEach { batch ->
                    batch.map { global ->
                        async { downloadAyah(reciter, global) }
                    }.awaitAll()
                    _statuses.update { all ->
                        all + (key to RecitationStatus(
                            state = DownloadState.Downloading,
                            downloaded = ayahGlobals.count { audioFile(reciter, it).isFile && it > 0 },
                            total = ayahGlobals.size,
                        ))
                    }
                }
                _statuses.update { it + (key to RecitationStatus(
                    state = DownloadState.Downloaded,
                    downloaded = ayahGlobals.size,
                    total = ayahGlobals.size,
                )) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _statuses.update { it + (key to RecitationStatus(
                    state = DownloadState.Error,
                    downloaded = ayahGlobals.count { audioFile(reciter, it).isFile && it > 0 },
                    total = ayahGlobals.size,
                )) }
            }
        }
        activeJobs[key] = job
    }

    /** Cancels a running download for (reciter, surah) and clears its status. */
    suspend fun cancel(reciter: Reciter, surahNumber: Int) {
        val key = key(reciter, surahNumber)
        activeJobs.remove(key)?.cancelAndJoin()
        _statuses.update { it - key }
    }

    /** Removes the downloaded ayahs of one surah from disk. */
    fun deleteSurah(reciter: Reciter, surahNumber: Int, ayahGlobals: List<Int>) {
        ayahGlobals.forEach { global -> audioFile(reciter, global).delete() }
        _statuses.update { it - key(reciter, surahNumber) }
    }

    /** Total on-disk size of the reciter's downloads (for storage management). */
    fun totalSizeBytes(reciter: Reciter): Long {
        val dir = RecitationAudio.reciterDir(context.filesDir, reciter)
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun downloadAyah(reciter: Reciter, globalAyahNumber: Int) {
        val target = audioFile(reciter, globalAyahNumber)
        if (target.isFile && target.length() > 0) return
        val request = Request.Builder().url(RecitationAudio.url(reciter, globalAyahNumber)).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("HTTP ${response.code} for ayah $globalAyahNumber (${reciter.id})")
            }
            val body = response.body ?: error("Empty body for ayah $globalAyahNumber")
            target.parentFile?.mkdirs()
            target.writeBytes(body.bytes())
        }
    }
}
