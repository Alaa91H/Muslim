package org.muslim.app.core.common.prayer

import java.time.LocalDate
import java.time.LocalTime

/**
 * Result of a prayer-times computation.
 *
 * @param date       the date the times were computed for
 * @param times      wall-clock time of each prayer
 * @param epochMillis the same times as UTC instants (used for alarm scheduling)
 */
data class PrayerTimesResult(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
    val epochMillis: Map<Prayer, Long>,
) {
    val isValid: Boolean get() = times.size == Prayer.entries.size

    fun timeFor(prayer: Prayer): LocalTime? = times[prayer]

    companion object {
        val Empty = PrayerTimesResult(LocalDate.MIN, emptyMap(), emptyMap())
    }
}
