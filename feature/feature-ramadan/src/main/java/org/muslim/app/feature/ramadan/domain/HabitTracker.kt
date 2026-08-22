package org.muslim.app.feature.ramadan.domain

import java.time.LocalDate

/** Daily acts tracked by the habit dashboard. */
enum class HabitId {
    Rawatib,
    Duha,
    Qiyam,
    Congregation,
}

/** Local, device-only habit and Ramadan-plan state. */
data class HabitTrackerState(
    val records: Map<LocalDate, Set<HabitId>> = emptyMap(),
    val khatmaJuz: Int = 0,
    val taraweehDates: Set<LocalDate> = emptySet(),
    val itikafEnabled: Boolean = false,
)

data class HabitDaySummary(
    val date: LocalDate,
    val completed: Set<HabitId>,
) {
    val completedCount: Int get() = completed.size
    val isComplete: Boolean get() = completed.containsAll(HabitId.entries)
}

enum class HabitBadge {
    Starting,
    Building,
    Consistent,
    Excellent,
}

data class HabitSummary(
    val today: HabitDaySummary,
    val week: List<HabitDaySummary>,
    val month: List<HabitDaySummary>,
    val weeklyCompletionPercent: Int,
    val monthlyCompletionPercent: Int,
    val currentStreak: Int,
    val monthlyPoints: Int,
    val level: Int,
    val badge: HabitBadge,
)

/** Pure operations kept separate from DataStore so every rule is testable. */
object HabitTrackerCalculator {
    const val DAILY_HABIT_COUNT = 4
    const val RAMADAN_JUZ_COUNT = 30

    fun toggle(
        state: HabitTrackerState,
        date: LocalDate,
        habit: HabitId,
    ): HabitTrackerState {
        val current = state.records[date].orEmpty().toMutableSet()
        if (!current.add(habit)) current.remove(habit)
        val records = state.records.toMutableMap()
        if (current.isEmpty()) records.remove(date) else records[date] = current
        return state.copy(records = records)
    }

    fun setKhatmaJuz(state: HabitTrackerState, juz: Int): HabitTrackerState =
        state.copy(khatmaJuz = juz.coerceIn(0, RAMADAN_JUZ_COUNT))

    fun toggleTaraweeh(state: HabitTrackerState, date: LocalDate): HabitTrackerState {
        val dates = state.taraweehDates.toMutableSet()
        if (!dates.add(date)) dates.remove(date)
        return state.copy(taraweehDates = dates)
    }

    fun summary(state: HabitTrackerState, today: LocalDate): HabitSummary {
        val week = dayRange(today, 7).map { date ->
            HabitDaySummary(date, state.records[date].orEmpty().intersect(HabitId.entries.toSet()))
        }
        val month = dayRange(today, 30).map { date ->
            HabitDaySummary(date, state.records[date].orEmpty().intersect(HabitId.entries.toSet()))
        }
        val weeklyDone = week.sumOf { it.completedCount }
        val monthlyDone = month.sumOf { it.completedCount }
        val weeklyPercent = percentage(weeklyDone, week.size * DAILY_HABIT_COUNT)
        val monthlyPercent = percentage(monthlyDone, month.size * DAILY_HABIT_COUNT)
        val monthlyTaraweeh = month.count { it.date in state.taraweehDates }
        val points = monthlyDone + monthlyTaraweeh * 2 + state.khatmaJuz
        val level = (points / 20) + 1
        val badge = when {
            monthlyPercent >= 85 -> HabitBadge.Excellent
            monthlyPercent >= 60 -> HabitBadge.Consistent
            monthlyPercent >= 25 -> HabitBadge.Building
            else -> HabitBadge.Starting
        }
        return HabitSummary(
            today = HabitDaySummary(today, state.records[today].orEmpty().intersect(HabitId.entries.toSet())),
            week = week,
            month = month,
            weeklyCompletionPercent = weeklyPercent,
            monthlyCompletionPercent = monthlyPercent,
            currentStreak = currentStreak(state, today),
            monthlyPoints = points,
            level = level,
            badge = badge,
        )
    }

    /** A streak counts consecutive days with all four daily habits complete. */
    fun currentStreak(state: HabitTrackerState, today: LocalDate): Int {
        var streak = 0
        var date = today
        val all = HabitId.entries.toSet()
        while (state.records[date].orEmpty().containsAll(all)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun dayRange(today: LocalDate, count: Int): List<LocalDate> =
        (count - 1 downTo 0).map { today.minusDays(it.toLong()) }

    private fun percentage(value: Int, total: Int): Int =
        if (total == 0) 0 else ((value * 100f) / total).toInt().coerceIn(0, 100)
}
