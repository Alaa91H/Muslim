package org.muslim.app.feature.tasbih.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TasbihSessionEngineTest {

    @Test
    fun `free session never auto completes`() {
        var state = TasbihSessionEngine.start(
            TasbihSessionConfig(
                phraseId = TasbihPhrase.SubhanAllah.storageId,
                mode = TasbihSessionMode.Free,
            ),
            nowEpochMillis = 100L,
        )

        repeat(100) {
            state = TasbihSessionEngine.increment(state, 101L + it).state
        }

        assertThat(state.count).isEqualTo(100L)
        assertThat(state.status).isEqualTo(TasbihSessionStatus.Active)
        assertThat(state.goalReached).isFalse()
    }

    @Test
    fun `target session completes exactly at target`() {
        var state = TasbihSessionEngine.start(
            TasbihSessionConfig(
                phraseId = TasbihPhrase.SubhanAllah.storageId,
                mode = TasbihSessionMode.Target,
                target = 3,
            ),
            nowEpochMillis = 100L,
        )

        val first = TasbihSessionEngine.increment(state, 101L)
        state = first.state
        val second = TasbihSessionEngine.increment(state, 102L)
        state = second.state
        val third = TasbihSessionEngine.increment(state, 103L)

        assertThat(first.goalCompleted).isFalse()
        assertThat(second.goalCompleted).isFalse()
        assertThat(third.goalCompleted).isTrue()
        assertThat(third.roundCompleted).isTrue()
        assertThat(third.state.status).isEqualTo(TasbihSessionStatus.Completed)
        assertThat(third.state.completedAtEpochMillis).isEqualTo(103L)
    }

    @Test
    fun `rounds session reports each round and completes final round`() {
        var state = TasbihSessionEngine.start(
            TasbihSessionConfig(
                phraseId = TasbihPhrase.Alhamdulillah.storageId,
                mode = TasbihSessionMode.Rounds,
                target = 2,
                roundsGoal = 2,
            ),
            nowEpochMillis = 0L,
        )

        val transitions = mutableListOf<TasbihSessionTransition>()
        repeat(4) { index ->
            val transition = TasbihSessionEngine.increment(state, index.toLong() + 1L)
            transitions += transition
            state = transition.state
        }

        assertThat(transitions[1].roundCompleted).isTrue()
        assertThat(transitions[1].goalCompleted).isFalse()
        assertThat(transitions[3].roundCompleted).isTrue()
        assertThat(transitions[3].goalCompleted).isTrue()
        assertThat(state.completedRounds).isEqualTo(2L)
        assertThat(state.progress).isEqualTo(1f)
    }

    @Test
    fun `completed target ignores additional taps until a new session starts`() {
        var state = TasbihSessionEngine.start(
            TasbihSessionConfig(
                phraseId = TasbihPhrase.AllahuAkbar.storageId,
                target = 1,
            ),
            nowEpochMillis = 10L,
        )

        state = TasbihSessionEngine.increment(state, 11L).state
        val extra = TasbihSessionEngine.increment(state, 12L)

        assertThat(extra.state.count).isEqualTo(1L)
        assertThat(extra.goalCompleted).isFalse()
    }

    @Test
    fun `decrement floors at zero and reopens completed session`() {
        val initial = TasbihSessionEngine.start(
            TasbihSessionConfig(
                phraseId = TasbihPhrase.Astaghfirullah.storageId,
                target = 1,
            ),
            nowEpochMillis = 0L,
        )
        val completed = TasbihSessionEngine.increment(initial, 1L).state
        val decremented = TasbihSessionEngine.decrement(completed, 2L)
        val floor = TasbihSessionEngine.decrement(decremented, 3L)

        assertThat(decremented.count).isEqualTo(0L)
        assertThat(decremented.status).isEqualTo(TasbihSessionStatus.Active)
        assertThat(floor.count).isEqualTo(0L)
    }
}
