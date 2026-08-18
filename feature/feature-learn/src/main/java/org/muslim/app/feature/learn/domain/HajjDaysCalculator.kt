package org.muslim.app.feature.learn.domain

import org.muslim.app.core.common.time.HijriDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** The five key days of the Hajj season (9-13 Dhul-Hijjah). */
enum class HajjKeyDayKind { ARAFAH, NAHR, TASHREEQ_FIRST, TASHREEQ_SECOND, TASHREEQ_THIRD }

/** One key day of the Hajj season with its Hijri and Gregorian dates. */
data class HajjKeyDay(
    val kind: HajjKeyDayKind,
    val hijri: HijriDate,
) {
    /** Equivalent Gregorian date of this key day. */
    val gregorian: LocalDate get() = hijri.gregorian

    /** Whole days between [entered] and this day (0 = the same day). */
    fun daysFrom(entered: HijriDate): Long =
        ChronoUnit.DAYS.between(entered.hijrahDate, hijri.hijrahDate)
}

/**
 * Pure calculator for the Hajj season (PROJECT_PROMPT.md section Hajj): from
 * any entered Hijri date it returns the season of that year — the Day of
 * Arafah (9 Dhul-Hijjah), the Day of An-Nahr (10 Dhul-Hijjah) and the three
 * Days of Tashreeq (11-13 Dhul-Hijjah) — each with its Gregorian equivalent.
 */
object HajjDaysCalculator {

    const val DHUL_HIJJAH = 12
    const val ARAFAH_DAY = 9
    const val NAHR_DAY = 10
    const val TASHREEQ_FIRST_DAY = 11
    const val TASHREEQ_LAST_DAY = 13

    /** The full Hajj season for a given Hijri year. */
    fun daysForYear(year: Int): List<HajjKeyDay> = listOf(
        HajjKeyDay(HajjKeyDayKind.ARAFAH, HijriDate.of(year, DHUL_HIJJAH, ARAFAH_DAY)),
        HajjKeyDay(HajjKeyDayKind.NAHR, HijriDate.of(year, DHUL_HIJJAH, NAHR_DAY)),
        HajjKeyDay(HajjKeyDayKind.TASHREEQ_FIRST, HijriDate.of(year, DHUL_HIJJAH, TASHREEQ_FIRST_DAY)),
        HajjKeyDay(HajjKeyDayKind.TASHREEQ_SECOND, HijriDate.of(year, DHUL_HIJJAH, TASHREEQ_FIRST_DAY + 1)),
        HajjKeyDay(HajjKeyDayKind.TASHREEQ_THIRD, HijriDate.of(year, DHUL_HIJJAH, TASHREEQ_LAST_DAY)),
    )

    /** The season of the year containing [entered]. */
    fun seasonFor(entered: HijriDate): List<HajjKeyDay> = daysForYear(entered.year)

    /**
     * Validates and builds the entered Hijri date. Returns null for
     * out-of-range values or calendar-invalid days (e.g. 30 in a 29-day month).
     */
    fun parse(year: Int, month: Int, day: Int): HijriDate? {
        if (year < 1300 || year > 1600 || month !in 1..12 || day !in 1..30) return null
        return runCatching { HijriDate.of(year, month, day) }.getOrNull()
    }
}
