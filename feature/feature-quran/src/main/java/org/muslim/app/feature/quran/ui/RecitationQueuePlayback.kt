package org.muslim.app.feature.quran.ui

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.muslim.app.core.network.FileDownloader
import org.muslim.app.feature.quran.data.FullSurahPlaybackCoordinator
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.data.RecitationDownloadNotifier
import org.muslim.app.feature.quran.data.RecitationQueueItem
import org.muslim.app.feature.quran.data.RecitationRepository
import org.muslim.app.feature.quran.data.RecitationSessionIntent
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.Reciter

internal data class RecitationQueuePlaybackDependencies(
    val scope: CoroutineScope,
    val repository: RecitationRepository,
    val audioPlayer: QuranAudioPlayer,
    val fullSurahPlayback: FullSurahPlaybackCoordinator,
    val notifier: RecitationDownloadNotifier,
)

internal data class RecitationQueuePlaybackCallbacks(
    val currentSurahName: () -> String,
    val isDownloading: () -> Boolean,
    val isCurrentRequest: (Long) -> Boolean,
    val advanceToNext: (repeat: Int, toEndOfQuran: Boolean) -> Unit,
    val setDownloading: (Boolean) -> Unit,
    val setProgress: (Float?) -> Unit,
    val onDownloadFailed: (globalNumber: Int) -> Unit,
)

/** Coordinates fast first-ayah playback and background completion of the selected queue. */
internal class RecitationQueuePlayback(
    private val dependencies: RecitationQueuePlaybackDependencies,
    private val callbacks: RecitationQueuePlaybackCallbacks,
) {
    fun play(ayahs: List<Ayah>, intent: RecitationSessionIntent, reciter: Reciter, requestSequence: Long) {
        if (ayahs.isEmpty() || callbacks.isDownloading()) return
        val surahNumber = ayahs.first().surahNumber
        dependencies.scope.launch {
            val existingQueue = dependencies.repository.localQueue(reciter.id, surahNumber, intent.globalNumbers)
            if (existingQueue != null) {
                startQueue(existingQueue, intent)
                return@launch
            }
            val firstItem = downloadFirstAyah(reciter, ayahs.first()) ?: return@launch
            if (!callbacks.isCurrentRequest(requestSequence)) return@launch
            startQueue(listOf(firstItem), intent)
            dependencies.audioPlayer.setQueueLoading(true)
            downloadRemaining(ayahs.drop(1), reciter, intent, requestSequence)
        }
    }

    private suspend fun downloadFirstAyah(
        reciter: Reciter,
        ayah: Ayah,
    ) = dependencies.repository.localQueue(reciter.id, ayah.surahNumber, listOf(ayah.globalNumber))
        ?.firstOrNull() ?: downloadFirstMissingAyah(reciter, ayah)

    private suspend fun downloadFirstMissingAyah(
        reciter: Reciter,
        ayah: Ayah,
    ) = try {
        callbacks.setDownloading(true)
        callbacks.setProgress(0f)
        val result = dependencies.repository.downloadAyah(reciter, ayah.surahNumber, ayah.numberInSurah, ayah.globalNumber)
        if (result !is FileDownloader.Result.Success) {
            callbacks.onDownloadFailed(ayah.globalNumber)
            null
        } else {
            dependencies.repository.localQueue(reciter.id, ayah.surahNumber, listOf(ayah.globalNumber))?.firstOrNull()
        }
    } finally {
        callbacks.setDownloading(false)
        callbacks.setProgress(null)
    }

    private fun startQueue(items: List<RecitationQueueItem>, intent: RecitationSessionIntent) {
        dependencies.fullSurahPlayback.startQueue(
            items = items,
            intent = intent,
            onAdvanceToNext = if (intent.advanceToNext) callbacks.advanceToNext else null,
        )
    }

    private suspend fun downloadRemaining(
        ayahs: List<Ayah>,
        reciter: Reciter,
        intent: RecitationSessionIntent,
        requestSequence: Long,
    ) {
        val startedAt = SystemClock.elapsedRealtime()
        var lastNotifiedPercent = -1
        callbacks.setProgress(0f)
        try {
            ayahs.forEachIndexed { index, ayah ->
                if (!callbacks.isCurrentRequest(requestSequence)) return
                val result = dependencies.repository.downloadAyah(
                    reciter,
                    intent.surahNumber,
                    ayah.numberInSurah,
                    ayah.globalNumber,
                )
                if (result !is FileDownloader.Result.Success) {
                    callbacks.onDownloadFailed(ayah.globalNumber)
                    return
                }
                if (!callbacks.isCurrentRequest(requestSequence)) return
                dependencies.repository.localQueue(reciter.id, intent.surahNumber, listOf(ayah.globalNumber))
                    ?.let(dependencies.audioPlayer::appendQueueItems)
                val progress = (index + 1).toFloat() / ayahs.size.coerceAtLeast(1)
                callbacks.setProgress(progress)
                lastNotifiedPercent = publishProgressIfChanged(
                    progress = progress,
                    previousPercent = lastNotifiedPercent,
                    startedAt = startedAt,
                    ayahCount = ayahs.size,
                    reciter = reciter,
                )
            }
        } finally {
            if (callbacks.isCurrentRequest(requestSequence)) {
                dependencies.audioPlayer.setQueueLoading(false)
                callbacks.setProgress(null)
                dependencies.notifier.dismiss()
            }
        }
    }

    private fun publishProgressIfChanged(
        progress: Float,
        previousPercent: Int,
        startedAt: Long,
        ayahCount: Int,
        reciter: Reciter,
    ): Int {
        val percent = (progress * 100).toInt()
        if (percent == previousPercent || progress >= 1f) return previousPercent
        val elapsedSeconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000f).coerceAtLeast(1f)
        val completedAyahsPerSecond = ((progress * ayahCount).toInt() / elapsedSeconds)
        val remainingSeconds = if (completedAyahsPerSecond > 0f) {
            ((ayahCount - (progress * ayahCount).toInt()) / completedAyahsPerSecond).toLong()
        } else {
            0L
        }
        dependencies.notifier.show(
            surahName = callbacks.currentSurahName(),
            percent = percent,
            remainingSeconds = remainingSeconds,
            bytesPerSecond = (completedAyahsPerSecond * reciter.estimatedBytesPerAyah()).toLong(),
        )
        return percent
    }
}
