package org.example.islamicapp.feature.tasbih.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Pure counter logic for the electronic tasbih (Phase 4). */
class TasbihStateTest {

    @Test
    fun `tap increments count and today total`() {
        val state = TasbihState(target = 33).tap().tap()
        assertThat(state.count).isEqualTo(2)
        assertThat(state.todayTotal).isEqualTo(2)
        assertThat(state.cycles).isEqualTo(0)
    }

    @Test
    fun `cycle rolls over exactly at the target`() {
        var state = TasbihState(target = 3)
        repeat(3) { state = state.tap() }
        assertThat(state.cycles).isEqualTo(1)
        assertThat(state.count).isEqualTo(0)
        assertThat(state.todayTotal).isEqualTo(3)
    }

    @Test
    fun `progress stays within 0-1`() {
        val state = TasbihState(target = 10).tap()
        assertThat(state.progress).isWithin(1e-6f).of(0.1f)
    }

    @Test
    fun `target is clamped to sane bounds`() {
        assertThat(TasbihState().withTarget(0).target).isEqualTo(1)
        assertThat(TasbihState().withTarget(1_000_000).target).isEqualTo(10_000)
    }

    @Test
    fun `reset cycle keeps the daily total`() {
        var state = TasbihState(target = 5).tap().tap()
        state = state.resetCycle()
        assertThat(state.count).isEqualTo(0)
        assertThat(state.todayTotal).isEqualTo(2)
    }
}
