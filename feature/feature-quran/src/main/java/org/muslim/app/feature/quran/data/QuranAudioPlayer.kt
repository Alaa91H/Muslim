package org.muslim.app.feature.quran.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Simple playback state shared with the reader UI. */
enum class PlaybackState { Idle, Playing, Paused }

/** One ayah entry in the playback queue. */
data class RecitationQueueItem(
    val file: File,
    val globalNumber: Int,
)

/**
 * A small sequential audio player for Quran recitation. It plays a queue of
 * ayahs one after another (auto-advancing at the end of each) with a per-ayah
 * repeat count for memorisation, and exposes the currently-playing ayah so the
 * reader can highlight it. One instance is shared app-wide so starting a new
 * queue stops the previous playback.
 *
 * The heavy lifting is delegated to a [RecitationAudioEngine] so the
 * queue/repeat/state logic can be unit-tested on the JVM.
 */
@Singleton
class QuranAudioPlayer @Inject constructor(
    private val engineFactory: RecitationEngineFactory,
) {

    private var currentEngine: RecitationAudioEngine? = null
    private var queue: List<RecitationQueueItem> = emptyList()
    private var queueIndex = -1
    private var repeatPerAyah = 1
    private var remainingRepeats = 0

    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    /** The global number of the ayah currently playing, or null. */
    private val _currentAyah = MutableStateFlow<Int?>(null)
    val currentAyah: StateFlow<Int?> = _currentAyah.asStateFlow()

    /** Increments on every playback failure so the UI can surface a hint. */
    private val _errorCount = MutableStateFlow(0)
    val errorCount: StateFlow<Int> = _errorCount.asStateFlow()

    /** Elapsed position of the current ayah's audio, in milliseconds. */
    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    /** Total duration of the current ayah's audio, in milliseconds. */
    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()

    private val _hasPrevious = MutableStateFlow(false)
    val hasPrevious: StateFlow<Boolean> = _hasPrevious.asStateFlow()

    /** True while the loaded ayah is playing its configured repeats. */
    val isPlaying: Boolean get() = _playbackState.value == PlaybackState.Playing

    /**
     * Starts [items] at [startIndex]; each item repeats [repeatCount] times
     * before advancing to the next one. Preparation is asynchronous so the
     * caller's thread (usually the main thread) never blocks.
     */
    fun playQueue(items: List<RecitationQueueItem>, startIndex: Int, repeatCount: Int) {
        if (items.isEmpty()) return
        queue = items
        repeatPerAyah = repeatCount.coerceAtLeast(1)
        queueIndex = startIndex.coerceIn(0, items.lastIndex)
        loadCurrent()
    }

    fun next() {
        if (queueIndex < 0 || queueIndex >= queue.lastIndex) return
        queueIndex++
        loadCurrent()
    }

    fun previous() {
        if (queueIndex <= 0) return
        queueIndex--
        loadCurrent()
    }

    fun pause() {
        if (_playbackState.value == PlaybackState.Playing) {
            runCatching { currentEngine?.pause() }
            _playbackState.value = PlaybackState.Paused
        }
    }

    fun resume() {
        if (_playbackState.value == PlaybackState.Paused) {
            runCatching { currentEngine?.start() }
            _playbackState.value = PlaybackState.Playing
        }
    }

    fun stop() {
        releaseEngine()
        queue = emptyList()
        queueIndex = -1
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
    }

    /** Reads the current media position; called by the UI's progress poller. */
    fun refreshPosition() {
        _positionMs.value = runCatching { currentEngine?.positionMs?.toLong() ?: 0L }.getOrDefault(0L)
    }

    private fun loadCurrent() {
        val item = queue.getOrNull(queueIndex)
        if (item == null) {
            finish()
            return
        }
        releaseEngine()
        _currentAyah.value = item.globalNumber
        remainingRepeats = repeatPerAyah
        resetProgress()
        updateNavState()

        val engine = engineFactory.create(item.file)
        if (engine == null) {
            fail()
            return
        }
        currentEngine = engine
        engine.setOnPreparedListener {
            _durationMs.value = engine.durationMs.toLong()
            _positionMs.value = 0L
            _playbackState.value = PlaybackState.Playing
            engine.start()
        }
        engine.setOnCompletionListener {
            remainingRepeats--
            if (remainingRepeats > 0) {
                engine.seekTo(0)
                engine.start()
            } else if (queueIndex < queue.lastIndex) {
                queueIndex++
                loadCurrent()
            } else {
                finish()
            }
        }
        engine.setOnErrorListener {
            fail()
        }
        runCatching { engine.prepareAsync() }.onFailure {
            fail()
        }
    }

    private fun finish() {
        releaseEngine()
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
    }

    private fun fail() {
        releaseEngine()
        _errorCount.value += 1
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
    }

    private fun releaseEngine() {
        currentEngine?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        currentEngine = null
    }

    private fun resetProgress() {
        _positionMs.value = 0L
        _durationMs.value = 0L
    }

    private fun updateNavState() {
        _hasNext.value = queueIndex in 0 until queue.lastIndex
        _hasPrevious.value = queueIndex > 0
    }
}
