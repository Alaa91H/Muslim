package org.example.islamicapp.feature.tasbih.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class TasbihCounterTest {

    private val today = LocalDate.of(2026, 8, 16)
    private val yesterday = today.minusDays(1)

    @Test
    fun `effectiveCount is the stored count on the same day`() {
        assertThat(TasbihCounter.effectiveCount(12, today, today)).isEqualTo(12)
    }

    @Test
    fun `effectiveCount is zero after a day change`() {
        assertThat(TasbihCounter.effectiveCount(12, yesterday, today)).isEqualTo(0)
    }

    @Test
    fun `increment same day just increments`() {
        val result = TasbihCounter.increment(5, today, today, emptyList())
        assertThat(result.count).isEqualTo(6)
        assertThat(result.date).isEqualTo(today)
        assertThat(result.history).isEmpty()
    }

    @Test
    fun `increment after day change rolls the previous day into history`() {
        val result = TasbihCounter.increment(30, yesterday, today, emptyList())
        assertThat(result.count).isEqualTo(1)
        assertThat(result.date).isEqualTo(today)
        assertThat(result.history).containsExactly(DailyCount(yesterday, 30))
    }

    @Test
    fun `increment after day change with zero count keeps history untouched`() {
        val history = listOf(DailyCount(yesterday.minusDays(1), 50))
        val result = TasbihCounter.increment(0, yesterday, today, history)
        assertThat(result.count).isEqualTo(1)
        assertThat(result.history).containsExactlyElementsIn(history)
    }

    @Test
    fun `history is trimmed to the configured limit`() {
        val history = (1..29).map { DailyCount(today.minusDays(it.toLong() + 1), it * 10) }
        val result = TasbihCounter.increment(99, yesterday, today, history, historyLimit = 30)
        assertThat(result.history).hasSize(30)
        assertThat(result.history.first()).isEqualTo(DailyCount(yesterday, 99))
    }

    @Test
    fun `targetReached reflects the goal`() {
        assertThat(TasbihState(33, 33, TasbihPhrase.SubhanAllah, emptyList()).targetReached).isTrue()
        assertThat(TasbihState(32, 33, TasbihPhrase.SubhanAllah, emptyList()).targetReached).isFalse()
        assertThat(TasbihState(50, 33, TasbihPhrase.SubhanAllah, emptyList()).targetReached).isTrue()
    }
}
