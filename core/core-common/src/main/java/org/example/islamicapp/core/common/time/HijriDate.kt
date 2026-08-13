package org.example.islamicapp.core.common.time

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/**
 * Hijri (Islamic lunar) date.
 *
 * Backed by [java.time.chrono.HijrahDate], which implements the Umm al-Qura
 * calendar — the same calendar the Saudi authorities publish — on both the
 * JDK and Android (API 26+).
 *
 * Per PROJECT_PROMPT.md §6 Phase 1, an optional manual day [adjustment]
 * (±1 day, app-wide) lets the user align the calendar with local moon-sighting
 * announcements when they differ from the astronomical calculation. The
 * adjustment shifts the displayed date for every screen (and later Ramadan /
 * Eid detection).
 */
class HijriDate private constructor(
    val hijrahDate: HijrahDate,
    val adjustment: Int,
) {

    /** Hijri year (AH). */
    val year: Int get() = hijrahDate.get(ChronoField.YEAR)

    /** Hijri month, 1 = Muharram .. 12 = Dhul-Hijjah. */
    val month: Int get() = hijrahDate.get(ChronoField.MONTH_OF_YEAR)

    /** Day of the month, 1..30. */
    val day: Int get() = hijrahDate.get(ChronoField.DAY_OF_MONTH)

    /** Arabic month name. */
    val monthName: String get() = MONTH_NAMES_ARABIC[month - 1]

    /** Equivalent Gregorian date (the date this Hijri date falls on). */
    val gregorian: LocalDate get() = LocalDate.from(hijrahDate)

    /** Formats as "29 محرم 1448". */
    fun formatArabic(): String = "$day $monthName $year"

    /** Formats as "الجمعة 29 محرم 1448هـ". */
    fun formatArabicLong(): String = "${gregorian.dayOfWeek.toArabic()} $day $monthName $year هـ"

    fun plusDays(days: Long): HijriDate =
        HijriDate(hijrahDate.plus(days, ChronoUnit.DAYS), adjustment)

    override fun toString(): String = formatArabic()

    override fun equals(other: Any?): Boolean =
        other is HijriDate && other.hijrahDate == hijrahDate

    override fun hashCode(): Int = hijrahDate.hashCode()

    companion object {

        private val MONTH_NAMES_ARABIC = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
        )

        private val DAY_NAMES_ARABIC = mapOf(
            java.time.DayOfWeek.SATURDAY to "السبت",
            java.time.DayOfWeek.SUNDAY to "الأحد",
            java.time.DayOfWeek.MONDAY to "الاثنين",
            java.time.DayOfWeek.TUESDAY to "الثلاثاء",
            java.time.DayOfWeek.WEDNESDAY to "الأربعاء",
            java.time.DayOfWeek.THURSDAY to "الخميس",
            java.time.DayOfWeek.FRIDAY to "الجمعة",
        )

        /** Today's Hijri date (Umm al-Qura), optionally shifted by [adjustment] days. */
        fun today(adjustment: Int = 0): HijriDate =
            from(LocalDate.now(), adjustment)

        /** Converts a Gregorian date to Hijri, optionally shifted by [adjustment] days. */
        fun from(gregorian: LocalDate, adjustment: Int = 0): HijriDate {
            val base = HijrahDate.from(gregorian)
            return HijriDate(base.plus(adjustment.toLong(), ChronoUnit.DAYS), adjustment)
        }

        /** Builds a Hijri date from its components, optionally shifted by [adjustment] days. */
        fun of(year: Int, month: Int, day: Int, adjustment: Int = 0): HijriDate {
            val base = HijrahChronology.INSTANCE.date(year, month, day)
            return HijriDate(base.plus(adjustment.toLong(), ChronoUnit.DAYS), adjustment)
        }

        private fun java.time.DayOfWeek.toArabic(): String = DAY_NAMES_ARABIC.getValue(this)
    }
}
