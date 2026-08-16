package org.example.islamicapp.feature.quran.ui

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Plays a surah's recitation ayah-by-ayah with a playlist of local MP3 files,
 * exposing the index of the currently playing ayah so the reader can
 * highlight/scroll in sync (PROJECT_PROMPT.md §6 Phase 2: "تتبّع صوتي
 * متزامن مع النص"). Owned by the reader's ViewModel; released on exit.
 */
class RecitationPlayer(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _isPlaying = MutableStateFlow(false)
    private val _currentIndex = MutableStateFlow(-1)
    private val _error = MutableStateFlow<String?>(null)

    /** True while playback is active. */
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** Index (into the surah's ayah list) of the ayah currently being played. */
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentIndex.value = player.currentMediaItemIndex
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    _currentIndex.value = -1
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _error.value = error.message ?: "playback_error"
                _isPlaying.value = false
            }
        })
    }

    /** Loads [ayahFiles] and starts playing from [startIndex]. */
    fun play(ayahFiles: List<File>, startIndex: Int = 0) {
        if (ayahFiles.isEmpty()) return
        val clampedStart = startIndex.coerceIn(0, ayahFiles.lastIndex)
        _error.value = null
        player.setMediaItems(
            ayahFiles.map { MediaItem.fromUri(Uri.fromFile(it)) },
            clampedStart,
            0L,
        )
        _currentIndex.value = clampedStart
        player.prepare()
        player.play()
        _isPlaying.value = true
    }

    fun pause() {
        player.pause()
        _isPlaying.value = false
    }

    fun resume() {
        if (player.mediaItemCount > 0) {
            player.play()
            _isPlaying.value = true
        }
    }

    fun stop() {
        player.stop()
        _isPlaying.value = false
        _currentIndex.value = -1
    }

    /** True when a surah is loaded (even if paused). */
    val hasContent: Boolean
        get() = player.mediaItemCount > 0

    fun release() {
        player.release()
    }
}
