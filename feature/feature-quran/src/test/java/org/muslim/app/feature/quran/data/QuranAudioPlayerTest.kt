package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Unit tests for [QuranAudioPlayer]'s queue, repeat and state-transition logic.
 * [RecitationAudioEngine] is faked so the decision logic runs on the JVM
 * without the Android [android.media.MediaPlayer] framework.
 */
class QuranAudioPlayerTest {

    private class FakeEngine : RecitationAudioEngine {
        var started = false
        var paused = false
        var stopped = false
        var released = false
        var prepareAsyncCalls = 0
        var seekToCalls = 0
        var position = 0

        private var preparedListener: (() -> Unit)? = null
        private var completionListener: (() -> Unit)? = null
        private var errorListener: (() -> Unit)? = null

        override val durationMs: Int get() = 1000
        override val positionMs: Int get() = position

        override fun prepareAsync() {
            prepareAsyncCalls++
        }

        override fun start() {
            started = true
            paused = false
        }

        override fun pause() {
            paused = true
        }

        override fun seekTo(msec: Int) {
            seekToCalls++
        }

        override fun stop() {
            stopped = true
        }

        override fun release() {
            released = true
        }

        override fun setOnPreparedListener(listener: () -> Unit) {
            preparedListener = listener
        }

        override fun setOnCompletionListener(listener: () -> Unit) {
            completionListener = listener
        }

        override fun setOnErrorListener(listener: () -> Unit) {
            errorListener = listener
        }

        fun firePrepared() = preparedListener?.invoke()
        fun fireCompletion() = completionListener?.invoke()
        fun fireError() = errorListener?.invoke()
    }

    private class FakeFactory : RecitationEngineFactory {
        val engines = mutableListOf<FakeEngine>()
        override fun create(file: File): RecitationAudioEngine =
            FakeEngine().also { engines.add(it) }
    }

    private fun item(global: Int) = RecitationQueueItem(File("ayah-$global.mp3"), global)

    private fun player(factory: FakeFactory) = QuranAudioPlayer(factory)

    @Test
    fun `empty queue is a no-op`() {
        val factory = FakeFactory()
        val player = player(factory)

        player.playQueue(emptyList(), startIndex = 0, repeatCount = 1)

        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.currentAyah.value).isNull()
        assertThat(player.hasNext.value).isFalse()
        assertThat(player.hasPrevious.value).isFalse()
        assertThat(factory.engines).isEmpty()
    }

    @Test
    fun `playQueue starts at the requested index`() {
        val factory = FakeFactory()
        val player = player(factory)

        player.playQueue(listOf(item(1), item(2), item(3)), startIndex = 1, repeatCount = 1)

        assertThat(player.currentAyah.value).isEqualTo(2)
        assertThat(player.hasPrevious.value).isTrue()
        assertThat(player.hasNext.value).isTrue()
    }

    @Test
    fun `preparing starts playback`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1)), startIndex = 0, repeatCount = 1)
        val engine = factory.engines.single()

        engine.firePrepared()

        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Playing)
        assertThat(engine.started).isTrue()
        assertThat(engine.prepareAsyncCalls).isEqualTo(1)
    }

    @Test
    fun `completion auto-advances through the queue and finishes at the end`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1), item(2), item(3)), startIndex = 0, repeatCount = 1)

        factory.engines[0].fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(2)
        assertThat(factory.engines).hasSize(2)

        factory.engines[1].fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(3)
        assertThat(factory.engines).hasSize(3)

        factory.engines[2].fireCompletion()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.currentAyah.value).isNull()
        assertThat(player.hasNext.value).isFalse()
        // The finished queue still allows stepping back to the previous ayah.
        assertThat(player.hasPrevious.value).isTrue()
    }

    @Test
    fun `repeat count replays the same ayah before advancing`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1), item(2)), startIndex = 0, repeatCount = 3)
        val engine = factory.engines[0]

        // Repeat #1 (remainingRepeats 3 -> 2).
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(1)
        assertThat(engine.seekToCalls).isEqualTo(1)
        assertThat(factory.engines).hasSize(1)

        // Repeat #2 (remainingRepeats 2 -> 1).
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(1)
        assertThat(engine.seekToCalls).isEqualTo(2)

        // Third completion exhausts the repeats -> advance to ayah 2.
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(2)
        assertThat(factory.engines).hasSize(2)
    }

    @Test
    fun `pausing in the middle of repeats preserves the repeat count and resumes`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1), item(2)), startIndex = 0, repeatCount = 3)
        val engine = factory.engines[0]
        engine.firePrepared()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Playing)

        // Repeat #1 finishes -> the same ayah keeps playing.
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(1)
        assertThat(engine.seekToCalls).isEqualTo(1)

        // Pause in the middle of repeat #2.
        player.pause()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Paused)
        assertThat(engine.paused).isTrue()

        // Resume: state returns to Playing, pause is cleared, and the repeat
        // counter is untouched.
        player.resume()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Playing)
        assertThat(engine.paused).isFalse()
        assertThat(engine.started).isTrue()

        // Repeat #2 finishes -> still the same ayah.
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(1)
        assertThat(engine.seekToCalls).isEqualTo(2)

        // Third completion exhausts the repeats -> advance to ayah 2.
        engine.fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(2)
        assertThat(factory.engines).hasSize(2)
    }

    @Test
    fun `pause and resume toggle the state`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1)), startIndex = 0, repeatCount = 1)
        val engine = factory.engines.single()
        engine.firePrepared()

        player.pause()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Paused)
        assertThat(engine.paused).isTrue()

        player.resume()
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Playing)
        assertThat(engine.started).isTrue()
    }

    @Test
    fun `stop resets state and releases the engine`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1)), startIndex = 0, repeatCount = 1)
        val engine = factory.engines.single()
        engine.firePrepared()

        player.stop()

        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.currentAyah.value).isNull()
        assertThat(player.hasNext.value).isFalse()
        assertThat(player.hasPrevious.value).isFalse()
        assertThat(engine.stopped).isTrue()
        assertThat(engine.released).isTrue()
    }

    @Test
    fun `next and previous navigate within the queue`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1), item(2), item(3)), startIndex = 1, repeatCount = 1)

        player.next()
        assertThat(player.currentAyah.value).isEqualTo(3)
        assertThat(player.hasNext.value).isFalse()

        player.previous()
        assertThat(player.currentAyah.value).isEqualTo(2)

        player.previous()
        assertThat(player.currentAyah.value).isEqualTo(1)
        assertThat(player.hasPrevious.value).isFalse()
    }

    @Test
    fun `engine error fails playback and bumps the error counter`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1)), startIndex = 0, repeatCount = 1)
        val engine = factory.engines.single()
        engine.firePrepared()

        engine.fireError()

        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.currentAyah.value).isNull()
        assertThat(player.errorCount.value).isEqualTo(1)
        assertThat(engine.released).isTrue()
    }

    @Test
    fun `refreshPosition reads the engine position`() {
        val factory = FakeFactory()
        val player = player(factory)
        player.playQueue(listOf(item(1)), startIndex = 0, repeatCount = 1)
        factory.engines.single().position = 1234

        player.refreshPosition()

        assertThat(player.positionMs.value).isEqualTo(1234L)
    }

    @Test
    fun `continuous queue fires onQueueCompleted at the end instead of finishing`() {
        val factory = FakeFactory()
        val player = player(factory)
        var completed = 0
        player.onQueueCompleted = { completed++ }

        player.playQueue(
            listOf(item(1), item(2)),
            startIndex = 0,
            repeatCount = 1,
            continuous = true,
        )

        factory.engines[0].fireCompletion()
        assertThat(player.currentAyah.value).isEqualTo(2)
        assertThat(completed).isEqualTo(0)

        factory.engines[1].fireCompletion()
        assertThat(completed).isEqualTo(1)
        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.currentAyah.value).isNull()
        // The flag is consumed so a second completion cannot double-fire.
        factory.engines[1].fireCompletion()
        assertThat(completed).isEqualTo(1)
    }

    @Test
    fun `stop clears continuous mode and the completion callback`() {
        val factory = FakeFactory()
        val player = player(factory)
        var completed = 0
        player.onQueueCompleted = { completed++ }

        player.playQueue(
            listOf(item(1), item(2)),
            startIndex = 0,
            repeatCount = 1,
            continuous = true,
        )
        player.stop()

        assertThat(player.playbackState.value).isEqualTo(PlaybackState.Idle)
        assertThat(player.onQueueCompleted).isNull()
    }
}
