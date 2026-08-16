package org.example.islamicapp.feature.ramadan.domain

import org.example.islamicapp.core.common.time.HijriDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Upcoming sunnah-fasting opportunities (PROJECT_PROMPT.md §6 Phase 8
 * "الصيام المسنون"): Mondays/Thursdays, the white days (13–15 of each
 * Hijri month), Arafah and Ashura (+Tasu'a).
 */
data class SunnahFast(
    val date: LocalDate,
    val labelAr: String,
    val labelEn: String,
    val daysUntil: Long,
)

object SunnahFasting {

    /**
     * Opportunities within [days] from today (inclusive), sorted by date.
     * Monday/Thursday entries are generated weekly; white days, Arafah and
     * Ashura come from the Hijri calendar.
     */
    fun upcoming(
        today: LocalDate = LocalDate.now(),
        hijriAdjustment: Int = 0,
        days: Long = 30,
    ): List<SunnahFast> {
        val result = mutableListOf<SunnahFast>()

        // Mondays and Thursdays.
        (0..days).forEach { offset ->
            val date = today.plusDays(offset)
            if (date.dayOfWeek == DayOfWeek.MONDAY) {
                result += SunnahFast(date, "صيام الاثنين", "Monday fast", offset)
            } else if (date.dayOfWeek == DayOfWeek.THURSDAY) {
                result += SunnahFast(date, "صيام الخميس", "Thursday fast", offset)
            }
        }

        // White days and special days from the Hijri calendar.
        val todayHijri = HijriDate.from(today, hijriAdjustment)
        var monthCursor = todayHijri
        repeat(2) { // current hijri month and the next one
            val (year, month) = monthCursor.year to monthCursor.month
            listOf(13, 14, 15).forEach { day ->
                val date = runCatching {
                    HijriDate.of(year, month, day, hijriAdjustment).gregorian
                }.getOrNull() ?: return@forEach
                val offset = ChronoUnit.DAYS.between(today, date)
                if (offset in 0..days) {
                    result += SunnahFast(date, "من الأيام البيض", "White days", offset)
                }
            }
            monthCursor = HijriDate.of(year, month, 1, hijriAdjustment).plusDays(30)
        }

        listOf(
            Triple(12, 9, "صيام يوم عرفة" to "Day of Arafah fast"),
            Triple(1, 9, "صيام تاسوعاء" to "Tasu'a fast"),
            Triple(1, 10, "صيام عاشوراء" to "Ashura fast"),
        ).forEach { (month, day, labels) ->
            val thisYear = runCatching {
                HijriDate.of(todayHijri.year, month, day, hijriAdjustment).gregorian
            }.getOrNull()
            val nextYear = runCatching {
                HijriDate.of(todayHijri.year + 1, month, day, hijriAdjustment).gregorian
            }.getOrNull()
            listOfNotNull(thisYear, nextYear).forEach { date ->
                val offset = ChronoUnit.DAYS.between(today, date)
                if (offset in 0..days) {
                    result += SunnahFast(date, labels.first, labels.second, offset)
                }
            }
        }

        return result
            .distinctBy { it.date }
            .sortedBy { it.date }
    }
}
