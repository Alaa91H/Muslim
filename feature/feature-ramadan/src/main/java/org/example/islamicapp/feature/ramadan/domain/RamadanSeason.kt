package org.example.islamicapp.feature.ramadan.domain

import org.example.islamicapp.core.common.time.HijriDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Ramadan season logic (PROJECT_PROMPT.md §6 Phase 6): start/end dates from
 * the app-wide Hijri calendar (Umm al-Qura with the user's ±day adjustment),
 * plus the fasting-day number while inside the month.
 */
data class RamadanInfo(
    /** First fasting day (1 Ramadan, current or upcoming). */
    val startDate: LocalDate,
    /** Last fasting day: 29 or 30 Ramadan depending on the Hijri month length. */
    val endDate: LocalDate,
    /** 1-based fasting day number when today is inside Ramadan, else null. */
    val dayNumber: Int?,
    /** Days until Ramadan starts when it has not begun, else null. */
    val daysUntilStart: Long?,
    /** Total fasting days this Ramadan (29 or 30). */
    val totalDays: Int,
)

object RamadanSeason {

    /** Hijri month number of Ramadan. */
    const val RAMADAN_MONTH = 9

    fun info(today: LocalDate = LocalDate.now(), hijriAdjustment: Int = 0): RamadanInfo {
        val todayHijri = HijriDate.from(today, hijriAdjustment)

        // Ramadan (month 9) of the current Hijri year is still ahead while we
        // are in months 1–9; from Shawwal (10) onward the relevant one is next year's.
        val year = if (todayHijri.month >= 10) todayHijri.year + 1 else todayHijri.year
        val span = monthSpan(year, hijriAdjustment)

        return when {
            today < span.startDate -> span.copy(
                daysUntilStart = ChronoUnit.DAYS.between(today, span.startDate),
            )
            today <= span.endDate -> span.copy(
                dayNumber = ChronoUnit.DAYS.between(span.startDate, today).toInt() + 1,
            )
            else -> monthSpan(year + 1, hijriAdjustment).copy(
                daysUntilStart = ChronoUnit.DAYS.between(today, monthSpan(year + 1, hijriAdjustment).startDate),
            )
        }
    }

    private fun monthSpan(hijriYear: Int, adjustment: Int): RamadanInfo {
        val start = HijriDate.of(hijriYear, RAMADAN_MONTH, 1, adjustment).gregorian
        // A 30th day exists only in full Hijri months; HijrahChronology rejects invalid days.
        val length = try {
            HijriDate.of(hijriYear, RAMADAN_MONTH, 30, adjustment)
            30
        } catch (e: java.time.DateTimeException) {
            29
        }
        return RamadanInfo(
            startDate = start,
            endDate = start.plusDays((length - 1).toLong()),
            dayNumber = null,
            daysUntilStart = null,
            totalDays = length,
        )
    }
}
