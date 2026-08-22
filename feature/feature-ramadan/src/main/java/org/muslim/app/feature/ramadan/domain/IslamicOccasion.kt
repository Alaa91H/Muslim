package org.muslim.app.feature.ramadan.domain

import org.muslim.app.core.common.time.HijriDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A calculated Islamic occasion or recommended fasting day. */
data class IslamicOccasion(
    val hijriMonth: Int,
    val hijriDay: Int,
    val title: String,
    val category: OccasionCategory,
    val date: LocalDate,
)

enum class OccasionCategory { Worship, Eid, Season, Friday }

/**
 * Calendar rules that are independent of location and UI. Gregorian dates are
 * derived from the app's Hijri calendar, while the manual adjustment is passed
 * through consistently so local moon-sighting corrections affect every event.
 */
object IslamicOccasionCalculator {
    fun dateOf(year: Int, month: Int, day: Int, adjustment: Int = 0): LocalDate =
        HijriDate.of(year, month, day, adjustment).gregorian

    fun occasionsForYear(hijriYear: Int, adjustment: Int = 0): List<IslamicOccasion> = listOf(
        occasion(hijriYear, 1, 10, "Ashura", OccasionCategory.Worship, adjustment),
        occasion(hijriYear, 9, 1, "Ramadan begins", OccasionCategory.Season, adjustment),
        occasion(hijriYear, 10, 1, "Eid al-Fitr", OccasionCategory.Eid, adjustment),
        occasion(hijriYear, 12, 9, "Day of Arafah", OccasionCategory.Worship, adjustment),
        occasion(hijriYear, 12, 10, "Eid al-Adha", OccasionCategory.Eid, adjustment),
        occasion(hijriYear, 12, 11, "Tashreeq", OccasionCategory.Season, adjustment),
        occasion(hijriYear, 12, 12, "Tashreeq", OccasionCategory.Season, adjustment),
        occasion(hijriYear, 12, 13, "Tashreeq", OccasionCategory.Season, adjustment),
    )

    fun recommendedFastingDates(year: Int, month: Int, adjustment: Int = 0): List<LocalDate> {
        val middle = listOf(13, 14, 15).map { day -> dateOf(year, month, day, adjustment) }
        val ashura = dateOf(year, 1, 10, adjustment)
        return middle + ashura
    }

    fun fridayReminder(date: LocalDate): IslamicOccasion = IslamicOccasion(
        hijriMonth = HijriDate.from(date).month,
        hijriDay = HijriDate.from(date).day,
        title = "Read Surah Al-Kahf and seek the hour of response",
        category = OccasionCategory.Friday,
        date = date,
    )

    fun nextOccasion(from: LocalDate, adjustment: Int = 0): IslamicOccasion? {
        val hijri = HijriDate.from(from, adjustment)
        return (hijri.year..hijri.year + 1)
            .flatMap { occasionsForYear(it, adjustment) }
            .filter { !it.date.isBefore(from) }
            .minByOrNull { ChronoUnit.DAYS.between(from, it.date) }
    }

    private fun occasion(
        year: Int,
        month: Int,
        day: Int,
        title: String,
        category: OccasionCategory,
        adjustment: Int,
    ) = IslamicOccasion(month, day, title, category, dateOf(year, month, day, adjustment))
}
