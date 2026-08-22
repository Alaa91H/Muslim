package org.muslim.app.feature.ramadan.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class HabitTrackerTest {
    private val today = LocalDate.of(2026, 3, 10)

    @Test
    fun `toggle adds and removes one daily habit without changing other dates`() {
        val first = HabitTrackerCalculator.toggle(HabitTrackerState(), today, HabitId.Duha)
        assertThat(first.records[today]).containsExactly(HabitId.Duha)

        val second = HabitTrackerCalculator.toggle(first, today, HabitId.Duha)
        assertThat(second.records).doesNotContainKey(today)
    }

    @Test
    fun `summary calculates weekly and monthly completion percentages`() {
        var state = HabitTrackerState()
        HabitId.entries.forEach { state = HabitTrackerCalculator.toggle(state, today, it) }
        state = HabitTrackerCalculator.toggle(state, today.minusDays(1), HabitId.Duha)

        val summary = HabitTrackerCalculator.summary(state, today)

        assertThat(summary.today.completedCount).isEqualTo(4)
        assertThat(summary.weeklyCompletionPercent).isEqualTo(17)
        assertThat(summary.monthlyCompletionPercent).isEqualTo(4)
        assertThat(summary.currentStreak).isEqualTo(1)
    }

    @Test
    fun `current streak requires every daily habit and stops at the first gap`() {
        val all = HabitId.entries.toSet()
        val state = HabitTrackerState(
            records = mapOf(
                today to all,
                today.minusDays(1) to all,
                today.minusDays(2) to setOf(HabitId.Duha),
                today.minusDays(3) to all,
            ),
        )

        assertThat(HabitTrackerCalculator.currentStreak(state, today)).isEqualTo(2)
    }

    @Test
    fun `Ramadan plan clamps khatma and toggles taraweeh and itikaf`() {
        val initial = HabitTrackerState()
        val atMaximum = HabitTrackerCalculator.setKhatmaJuz(initial, 100)
        assertThat(atMaximum.khatmaJuz).isEqualTo(30)

        val withTaraweeh = HabitTrackerCalculator.toggleTaraweeh(atMaximum, today)
        assertThat(withTaraweeh.taraweehDates).containsExactly(today)
        assertThat(HabitTrackerCalculator.toggleTaraweeh(withTaraweeh, today).taraweehDates).isEmpty()
    }

    @Test
    fun `points include habits taraweeh and completed juz with a level and badge`() {
        val state = HabitTrackerState(
            records = (0..29).associate { offset ->
                today.minusDays(offset.toLong()) to HabitId.entries.toSet()
            },
            khatmaJuz = 30,
            taraweehDates = (0..29).map { today.minusDays(it.toLong()) }.toSet(),
        )

        val summary = HabitTrackerCalculator.summary(state, today)

        assertThat(summary.monthlyCompletionPercent).isEqualTo(100)
        assertThat(summary.currentStreak).isEqualTo(30)
        assertThat(summary.monthlyPoints).isEqualTo(210)
        assertThat(summary.level).isEqualTo(11)
        assertThat(summary.badge).isEqualTo(HabitBadge.Excellent)
    }
}
