package org.example.islamicapp.feature.ramadan.domain

import org.example.islamicapp.core.common.time.HijriDate
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.temporal.ChronoUnit

/**
 * A full Ramadan window (PROJECT_PROMPT.md §6 Phase 6). [days] holds every
 * Gregorian date of the month, honouring the app-wide Hijri [adjustment].
 */
data class RamadanInfo(
    val hijriYear: Int,
    val start: LocalDate,
    val end: LocalDate,
    val days: List<LocalDate>,
) {
    fun isRamadanDay(today: LocalDate): Boolean = days.any { it == today }

    /** 1..30 on a fasting day, else 0. */
    fun dayOfRamadan(today: LocalDate): Int = days.indexOf(today) + 1

    fun daysUntilStart(today: LocalDate): Int =
        if (today.isBefore(start)) ChronoUnit.DAYS.between(today, start).toInt() else 0

    fun daysRemaining(today: LocalDate): Int =
        if (today.isAfter(end)) 0 else ChronoUnit.DAYS.between(today, end).toInt()
}

/**
 * Computes the Ramadan window from the Umm al-Qura Hijri calendar
 * (same [HijriDate] mechanism the rest of the app uses, including the
 * user's manual day adjustment).
 */
object RamadanDates {

    /** Gregorian date of Ramadan 1 in [hijriYear] AH. */
    fun ramadanStart(hijriYear: Int, adjustment: Int = 0): LocalDate {
        val base = HijrahChronology.INSTANCE.date(hijriYear, 9, 1)
        return LocalDate.from(base.plus(adjustment.toLong(), ChronoUnit.DAYS))
    }

    /** Length of Ramadan in [hijriYear] (29 or 30 days). */
    fun ramadanLength(hijriYear: Int, adjustment: Int = 0): Int {
        val start = HijrahChronology.INSTANCE.date(hijriYear, 9, 1).plus(adjustment.toLong(), ChronoUnit.DAYS)
        val nextMonth = HijrahChronology.INSTANCE.date(hijriYear, 10, 1).plus(adjustment.toLong(), ChronoUnit.DAYS)
        return ChronoUnit.DAYS.between(start, nextMonth).toInt()
    }

    /**
     * The Ramadan window relevant today: the current Ramadan if we are in
     * one, otherwise the next upcoming Ramadan.
     */
    fun upcoming(today: LocalDate, adjustment: Int = 0): RamadanInfo {
        val hijriYear = HijriDate.from(today, adjustment).year
        var year = hijriYear
        var start = ramadanStart(year, adjustment)
        var end = start.plusDays((ramadanLength(year, adjustment) - 1).toLong())
        if (today.isAfter(end)) {
            year += 1
            start = ramadanStart(year, adjustment)
            end = start.plusDays((ramadanLength(year, adjustment) - 1).toLong())
        }
        val days = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
        return RamadanInfo(hijriYear = year, start = start, end = end, days = days)
    }
}
