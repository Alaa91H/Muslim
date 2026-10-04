package org.muslim.app.feature.quran.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter

internal data class ReciterSelectionEnvironment(
    val audioPlayer: QuranAudioPlayer,
    val prefsRepository: QuranPrefsRepository,
    val repository: QuranRepository,
    val scope: CoroutineScope,
    val previewPlayer: ReciterPreviewPlayer,
)

internal data class ReciterSelectionContext(
    val selectedReciter: () -> Reciter,
    val currentSurahNumber: () -> Int,
    val setSurahNumber: (Int) -> Unit,
    val currentSurahAyahs: () -> List<Ayah>,
    val repeatCount: () -> Int,
    val range: () -> RecitationRange,
    val playAyahWithRange: (Ayah, Int, RecitationRange) -> Unit,
)

/** Keeps reciter changes and active-queue recovery out of the reader UI model. */
internal class ReciterSelectionController(
    private val environment: ReciterSelectionEnvironment,
    private val context: ReciterSelectionContext,
) {
    val previewingReciterId: StateFlow<String?> get() = environment.previewPlayer.previewingReciterId

    fun togglePreview(reciter: Reciter) = environment.previewPlayer.toggle(reciter)

    fun stopPreview() {
        environment.previewPlayer.stop()
    }

    fun selectReciter(reciter: Reciter) {
        val player = environment.audioPlayer
        val resumeAfterPreview = environment.previewPlayer.stop(resumePlayback = false)
        if (reciter.id == context.selectedReciter().id) {
            if (resumeAfterPreview) player.resume()
            return
        }

        val wasActive = resumeAfterPreview || shouldResumeAfterReciterChange(
            player.playbackState.value,
            player.currentAyah.value,
        )
        val ayahGlobal = player.currentAyah.value
        val repeat = context.repeatCount()
        val range = context.range()
        player.stop()
        environment.scope.launch {
            environment.prefsRepository.setSelectedReciterId(reciter.id)
            if (wasActive && ayahGlobal != null) {
                val targetSurah = QuranAyahIndex.surahOf(ayahGlobal)
                val ayahs = if (targetSurah in 1..114 && targetSurah != context.currentSurahNumber()) {
                    context.setSurahNumber(targetSurah)
                    environment.repository.observeSurah(targetSurah).first()
                } else {
                    context.currentSurahAyahs()
                }
                ayahs.firstOrNull { it.globalNumber == ayahGlobal }?.let { ayah ->
                    context.playAyahWithRange(ayah, repeat, range)
                }
            }
        }
    }
}
