package org.muslim.app.feature.quran.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Simple playback state shared with the reader UI. */
enum class PlaybackState { Idle, Playing, Paused }

/**
 * Plays one ayah's downloaded recitation with an optional repeat count
 * (PROJECT_PROMPT.md §6 Phase 2: وضع التكرار للحفظ). One instance is shared
 * app-wide so starting a new ayah stops the previous playback.
 */
@Singleton
class QuranAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var mediaPlayer: MediaPlayer? = null
    private var remainingRepeats = 0

    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    /** The ayah (global number) currently loaded, or null. */
    private val _currentAyah = MutableStateFlow<Int?>(null)
    val currentAyah: StateFlow<Int?> = _currentAyah.asStateFlow()

    /** True while the currently loaded ayah is playing its configured repeats. */
    val isPlaying: Boolean get() = _playbackState.value == PlaybackState.Playing

    /**
     * Plays [file] (one ayah). [repeatCount] >= 1 loops the file that many
     * times, then stops.
     */
    fun play(file: File, globalNumber: Int, repeatCount: Int, onFinished: () -> Unit = {}) {
        stop()
        _currentAyah.value = globalNumber
        remainingRepeats = repeatCount.coerceAtLeast(1)

        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        player.setDataSource(file.absolutePath)
        player.setOnPreparedListener { p ->
            _playbackState.value = PlaybackState.Playing
            p.start()
        }
        player.setOnCompletionListener {
            remainingRepeats--
            if (remainingRepeats > 0) {
                it.seekTo(0)
                it.start()
            } else {
                _playbackState.value = PlaybackState.Idle
                onFinished()
            }
        }
        player.setOnErrorListener { _, _, _ ->
            _playbackState.value = PlaybackState.Idle
            onFinished()
            true
        }
        runCatching { player.prepare() }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (_playbackState.value == PlaybackState.Playing) {
            player.pause()
            _playbackState.value = PlaybackState.Paused
        }
    }

    fun resume() {
        val player = mediaPlayer ?: return
        if (_playbackState.value == PlaybackState.Paused) {
            player.start()
            _playbackState.value = PlaybackState.Playing
        }
    }

    fun stop() {
        mediaPlayer?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        mediaPlayer = null
        _playbackState.value = PlaybackState.Idle
    }
}
