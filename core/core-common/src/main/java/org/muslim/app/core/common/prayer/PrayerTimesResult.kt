package org.muslim.app.core.common.prayer

import java.time.LocalDate
import java.time.LocalTime

/**
 * Result of a prayer-times computation.
 *
 * @param date       the date the times were computed for
 * @param times      wall-clock time of each prayer
 * @param epochMillis final UTC instants rounded once to the civil minute;
 *   used both by rendering and alarm scheduling so they remain identical.
 * @param rawEpochMillis astronomical UTC instants after explicit method and
 *   user offsets but before final minute rounding; intended for validation and
 *   diagnostics, not direct scheduling.
 */
data class PrayerTimesResult(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
    val epochMillis: Map<Prayer, Long>,
    val rawEpochMillis: Map<Prayer, Long> = epochMillis,
) {
    val isValid: Boolean get() = times.size == Prayer.entries.size

    fun timeFor(prayer: Prayer): LocalTime? = times[prayer]

    companion object {
        val Empty = PrayerTimesResult(LocalDate.MIN, emptyMap(), emptyMap())
    }
}
