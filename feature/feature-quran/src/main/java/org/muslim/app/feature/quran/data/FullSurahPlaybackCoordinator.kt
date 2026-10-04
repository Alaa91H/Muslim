package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.FullSurahRecitation
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import java.io.File
import javax.inject.Inject

/** Routes whole-surah recordings through the same player and durable session as ayah recitations. */
class FullSurahPlaybackCoordinator @Inject constructor(
    private val player: QuranAudioPlayer,
    private val sessionRuntime: RecitationSessionRuntime,
    private val recitationRepository: RecitationRepository,
) {
    fun start(recording: FullSurahRecitation, surahNumber: Int): RecitationSessionIntent? {
        val audioUrl = recording.audioUrl(surahNumber) ?: return null
        val firstGlobal = QuranAyahIndex.globalNumber(surahNumber, 1).takeIf { it > 0 } ?: return null
        val intent = RecitationSessionIntent(
            reciterId = recording.id,
            surahNumber = surahNumber,
            globalNumbers = listOf(firstGlobal),
            repeatCount = 1,
            continuous = false,
            advanceToNext = false,
            toEndOfQuran = false,
            fullSurahAudioUrl = audioUrl,
        )
        return intent.takeIf { play(it) }
    }

    fun play(
        intent: RecitationSessionIntent,
        startPositionMs: Long = 0L,
        remainingRepeats: Int? = null,
    ): Boolean {
        if (player.isRemotelyControlled) return false
        val audioUrl = intent.fullSurahAudioUrl ?: return false
        val anchorGlobal = intent.globalNumbers.singleOrNull() ?: return false
        if (intent.surahNumber !in 1..114 || QuranAyahIndex.surahOf(anchorGlobal) != intent.surahNumber) return false
        if (!RecitationStreamSourcePolicy.accepts(audioUrl, RecitationPlaybackScope.FullSurah)) return false

        startQueue(
            items = queueItem(intent, anchorGlobal),
            intent = intent,
            startPositionMs = startPositionMs,
            remainingRepeats = remainingRepeats,
        )
        return true
    }

    private fun queueItem(intent: RecitationSessionIntent, anchorGlobal: Int): List<RecitationQueueItem> {
        val local = recitationRepository.localFullSurahFile(intent.reciterId, intent.surahNumber)
        return listOf(
            RecitationQueueItem(
                file = local ?: File("full-surah.mp3"),
                globalNumber = anchorGlobal,
                streamUrl = if (local == null) intent.fullSurahAudioUrl else null,
                playbackScope = RecitationPlaybackScope.FullSurah,
            ),
        )
    }

    fun startQueue(
        items: List<RecitationQueueItem>,
        intent: RecitationSessionIntent,
        startPositionMs: Long = 0L,
        remainingRepeats: Int? = null,
        onAdvanceToNext: ((repeat: Int, toEndOfQuran: Boolean) -> Unit)? = null,
    ) {
        val continuousMode = intent.continuous || intent.repeatCount <= 0
        val effectiveRepeat = if (continuousMode) 1 else intent.repeatCount.coerceAtLeast(1)
        sessionRuntime.begin(intent, startPositionMs, remainingRepeats)
        player.onQueueCompleted = if (intent.advanceToNext && onAdvanceToNext != null) {
            { onAdvanceToNext(effectiveRepeat, intent.toEndOfQuran) }
        } else null
        player.playQueue(
            items = items,
            startIndex = 0,
            repeatCount = effectiveRepeat,
            continuous = intent.advanceToNext,
            startPositionMs = startPositionMs,
            remainingRepeatsForCurrent = remainingRepeats,
        )
    }
}
