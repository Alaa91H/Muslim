package org.example.islamicapp.feature.prayertimes.domain

/**
 * Pure logic for "next prayer" and the live countdown shown on the home
 * screen (PROJECT_PROMPT.md §6: "عدّاد تنازلي مباشر للصلاة القادمة").
 */
object NextPrayer {

    data class Next(
        val prayer: Prayer,
        val atEpochMillis: Long,
    )

    /**
     * Returns the next prayer at or after [nowMillis] among [times], or null
     * when all prayers of the day have passed (caller then computes tomorrow's
     * times — typically Fajr).
     *
     * [times] maps each prayer to its epoch-millisecond instant (see
     * [PrayerTimesResult.epochMillis]). Sunrise is skipped: it is not a prayer.
     */
    fun nextPrayer(times: Map<Prayer, Long>, nowMillis: Long): Next? {
        val upcoming = times
            .filterKeys { it != Prayer.Sunrise }
            .filterValues { it >= nowMillis }
            .minByOrNull { it.value }
            ?: return null
        return Next(upcoming.key, upcoming.value)
    }

    /** Seconds remaining until [targetMillis], never negative. */
    fun countdownSeconds(targetMillis: Long, nowMillis: Long): Long =
        ((targetMillis - nowMillis).coerceAtLeast(0) + 999) / 1000
}
