package org.muslim.app.feature.quran.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Simple playback state shared with the reader UI. */
enum class PlaybackState { Idle, Playing, Paused }

/** Stable failure reasons surfaced to the reader for recovery. */
enum class RecitationFailureReason {
    DownloadFailed,
    AudioFileUnavailable,
    EngineUnavailable,
    PreparationFailed,
    StartFailed,
    EngineError,
}

/** One playback/preparation failure. Sequence makes repeated identical failures observable. */
data class RecitationFailureEvent(
    val sequence: Long,
    val reason: RecitationFailureReason,
    val globalNumber: Int?,
)

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
    private val playbackBridge: RecitationPlaybackBridge,
) {

    private var currentEngine: RecitationAudioEngine? = null
    private var queue: List<RecitationQueueItem> = emptyList()
    private var queueIndex = -1
    private var repeatPerAyah = 1
    private val _remainingRepeats = MutableStateFlow(0)
    val remainingRepeats: StateFlow<Int> = _remainingRepeats.asStateFlow()
    private var pendingStartPositionMs = 0L
    private var pendingRemainingRepeats: Int? = null

    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    /** The global number of the ayah currently playing, or null. */
    private val _currentAyah = MutableStateFlow<Int?>(null)
    val currentAyah: StateFlow<Int?> = _currentAyah.asStateFlow()

    private var failureSequence = 0L
    private val _lastFailure = MutableStateFlow<RecitationFailureEvent?>(null)
    val lastFailure: StateFlow<RecitationFailureEvent?> = _lastFailure.asStateFlow()

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

    /**
     * Invoked when a queue started with [continuous] finishes (its last item
     * completed), so the owner can start the next queue (e.g. auto-advance to
     * the next surah). Cleared when a new queue is started or playback stops.
     */
    var onQueueCompleted: (() -> Unit)? = null
    private var continuous = false

    /** True while the loaded ayah is playing its configured repeats. */
    val isPlaying: Boolean get() = _playbackState.value == PlaybackState.Playing

    /**
     * Starts [items] at [startIndex]; each item repeats [repeatCount] times
     * before advancing to the next one. Preparation is asynchronous so the
     * caller's thread (usually the main thread) never blocks.
     */
    fun playQueue(
        items: List<RecitationQueueItem>,
        startIndex: Int,
        repeatCount: Int,
        continuous: Boolean = false,
        startPositionMs: Long = 0L,
        remainingRepeatsForCurrent: Int? = null,
    ) {
        if (items.isEmpty()) return
        _lastFailure.value = null
        queue = items
        repeatPerAyah = repeatCount.coerceAtLeast(1)
        this.continuous = continuous
        pendingStartPositionMs = startPositionMs.coerceAtLeast(0L)
        pendingRemainingRepeats = remainingRepeatsForCurrent
            ?.coerceIn(1, repeatPerAyah)
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
        if (_playbackState.value != PlaybackState.Playing) return
        runCatching {
            currentEngine?.pause() ?: error("No active recitation engine")
        }.onSuccess {
            _playbackState.value = PlaybackState.Paused
        }.onFailure {
            fail(RecitationFailureReason.EngineError)
        }
    }

    fun resume() {
        if (_playbackState.value != PlaybackState.Paused) return
        runCatching {
            currentEngine?.start() ?: error("No active recitation engine")
        }.onSuccess {
            _playbackState.value = PlaybackState.Playing
        }.onFailure {
            fail(RecitationFailureReason.StartFailed)
        }
    }

    fun stop() {
        releaseEngine()
        queue = emptyList()
        queueIndex = -1
        continuous = false
        onQueueCompleted = null
        pendingStartPositionMs = 0L
        pendingRemainingRepeats = null
        _remainingRepeats.value = 0
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
        playbackBridge.onPlaybackActiveChanged(false, PlaybackDeactivationReason.Stopped)
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
        _remainingRepeats.value = pendingRemainingRepeats ?: repeatPerAyah
        pendingRemainingRepeats = null
        resetProgress()
        updateNavState()

        val engine = engineFactory.create(item.file)
        if (engine == null) {
            fail(RecitationFailureReason.EngineUnavailable)
            return
        }
        currentEngine = engine
        engine.setOnPreparedListener {
            _durationMs.value = engine.durationMs.toLong()
            _positionMs.value = 0L
            val requestedStart = pendingStartPositionMs
            pendingStartPositionMs = 0L
            runCatching {
                if (requestedStart > 0L && engine.durationMs > 0) {
                    val maxPosition = (engine.durationMs - 1).coerceAtLeast(0)
                    val seekPosition = requestedStart.coerceAtMost(maxPosition.toLong()).toInt()
                    if (seekPosition > 0) {
                        engine.seekTo(seekPosition)
                        _positionMs.value = seekPosition.toLong()
                    }
                }
                engine.start()
            }
                .onSuccess {
                    _playbackState.value = PlaybackState.Playing
                    // Keep the process alive in the background only after the
                    // engine really started; never publish a false Playing state.
                    playbackBridge.onPlaybackActiveChanged(true, null)
                }
                .onFailure {
                    fail(RecitationFailureReason.StartFailed)
                }
        }
        engine.setOnCompletionListener {
            _remainingRepeats.value -= 1
            if (_remainingRepeats.value > 0) {
                runCatching {
                    engine.seekTo(0)
                    engine.start()
                }.onFailure {
                    fail(RecitationFailureReason.StartFailed)
                }
            } else if (queueIndex < queue.lastIndex) {
                queueIndex++
                loadCurrent()
            } else {
                finish()
            }
        }
        engine.setOnErrorListener {
            fail(RecitationFailureReason.EngineError)
        }
        runCatching { engine.prepareAsync() }.onFailure {
            fail(RecitationFailureReason.PreparationFailed)
        }
    }

    private fun finish() {
        releaseEngine()
        if (continuous) {
            // Continuous mode: hand control back to the owner (e.g. the reader
            // advances to the next surah) instead of just going idle.
            val callback = onQueueCompleted
            continuous = false
            queue = emptyList()
            queueIndex = -1
            _playbackState.value = PlaybackState.Idle
            _currentAyah.value = null
            resetProgress()
            updateNavState()
            playbackBridge.onPlaybackActiveChanged(false, PlaybackDeactivationReason.Completed)
            callback?.invoke()
            return
        }
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
        playbackBridge.onPlaybackActiveChanged(false, PlaybackDeactivationReason.Completed)
    }

    private fun fail(reason: RecitationFailureReason) {
        val globalNumber = _currentAyah.value
        releaseEngine()
        failureSequence += 1
        _lastFailure.value = RecitationFailureEvent(
            sequence = failureSequence,
            reason = reason,
            globalNumber = globalNumber,
        )
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        resetProgress()
        updateNavState()
        playbackBridge.onPlaybackActiveChanged(false, PlaybackDeactivationReason.Failed)
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
