package org.muslim.app.feature.quran.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.muslim.app.feature.quran.data.PlaybackState
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.domain.Reciter

/** Plays one ayah as a sample without changing the selected reciter or queue. */
internal class ReciterPreviewPlayer(
    private val audioPlayer: QuranAudioPlayer,
) {
    private var player: MediaPlayer? = null
    private var resumeRecitationAfterPreview = false
    private val _previewingReciterId = MutableStateFlow<String?>(null)
    val previewingReciterId: StateFlow<String?> = _previewingReciterId.asStateFlow()

    fun toggle(reciter: Reciter) {
        if (_previewingReciterId.value == reciter.id) {
            stop()
            return
        }

        val shouldResumePlayback = resumeRecitationAfterPreview ||
            audioPlayer.playbackState.value == PlaybackState.Playing
        stop(resumePlayback = false)
        resumeRecitationAfterPreview = shouldResumePlayback
        if (audioPlayer.playbackState.value == PlaybackState.Playing) audioPlayer.pause()

        val sample = MediaPlayer()
        player = sample
        _previewingReciterId.value = reciter.id
        sample.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        sample.setOnCompletionListener { completed ->
            if (player === completed) stop()
        }
        sample.setOnErrorListener { failed, _, _ ->
            if (player === failed) stop()
            true
        }
        sample.setOnPreparedListener { prepared ->
            if (player === prepared) prepared.start()
        }
        runCatching {
            sample.setDataSource(reciter.urlFor(surahNumber = 1, ayahNumberInSurah = 1))
            sample.prepareAsync()
        }.onFailure { stop() }
    }

    /** Returns whether the Quran queue had been playing before the preview. */
    fun stop(resumePlayback: Boolean = true): Boolean {
        val shouldResume = resumeRecitationAfterPreview
        resumeRecitationAfterPreview = false
        val activePlayer = player
        player = null
        _previewingReciterId.value = null
        if (activePlayer != null) {
            runCatching { activePlayer.stop() }
            runCatching { activePlayer.release() }
        }
        if (resumePlayback && shouldResume) audioPlayer.resume()
        return shouldResume
    }
}
