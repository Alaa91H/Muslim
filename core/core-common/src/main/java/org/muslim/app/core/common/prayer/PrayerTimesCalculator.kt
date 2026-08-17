package org.muslim.app.core.common.prayer

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Calculates the five daily prayer times with second-level astronomical
 * accuracy.
 *
 * The solar model (apparent Sun position with nutation and sidereal-time
 * corrections, per Meeus' "Astronomical Algorithms") follows the open-source
 * Adhan library by Batoul Apps (MIT License) — the same reference the project
 * prompt recommends for verifying calculations. The algorithm is validated in
 * `PrayerTimesCalculatorTest` against Adhan's official test vectors and
 * against published government prayer tables.
 */
class PrayerTimesCalculator {

    /**
     * Computes prayer times for [date] at [coordinates] with [parameters].
     *
     * @param userAdjustments manual per-prayer offset in minutes (persisted
     *   user preference, added on top of the method's built-in adjustments).
     * @return result with wall-clock times in [timeZone]; `isValid == false`
     *   (empty [PrayerTimesResult.times]) in polar conditions where a
     *   consistent set of times cannot be produced.
     */
    fun compute(
        date: LocalDate,
        coordinates: Coordinates,
        parameters: PrayerParameters,
        timeZone: ZoneId,
        asrMethod: AsrMethod = AsrMethod.Standard,
        userAdjustments: PrayerAdjustments = PrayerAdjustments(),
    ): PrayerTimesResult {
        val result = computeInternal(date, coordinates, parameters, asrMethod.shadowLength)
        if (!result.valid) return PrayerTimesResult.Empty

        val times = LinkedHashMap<Prayer, LocalTime>()
        val epochMillis = LinkedHashMap<Prayer, Long>()
        for (prayer in Prayer.entries) {
            val baseMs = result.millis.getValue(prayer)
            val adjMs = baseMs +
                (parameters.methodAdjustments[prayer] + userAdjustments[prayer]) * 60_000L
            val roundedMs = when {
                parameters.roundUp -> ceil(adjMs / 60_000.0) * 60_000
                else -> round(adjMs / 60_000.0) * 60_000
            }.toLong()
            val instant = Instant.ofEpochMilli(roundedMs)
            times[prayer] = instant.atZone(timeZone).toLocalTime()
            epochMillis[prayer] = roundedMs
        }
        return PrayerTimesResult(date = date, times = times, epochMillis = epochMillis)
    }

    // ---- internal raw computation (Adhan PrayerTimes algorithm) ----

    private class RawResult(val valid: Boolean, val millis: Map<Prayer, Long>)

    private fun computeInternal(
        date: LocalDate,
        coordinates: Coordinates,
        parameters: PrayerParameters,
        asrShadowLength: Double,
    ): RawResult {
        val startOfDayUtc = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        fun ms(hours: Double): Long = startOfDayUtc + floor(hours * 3_600_000).toLong()
        fun msOf(day: LocalDate, hours: Double): Long =
            day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() + floor(hours * 3_600_000).toLong()

        val solarTime = SolarTime(date, coordinates)
        val tomorrow = date.plusDays(1)
        val tomorrowSolarTime = SolarTime(tomorrow, coordinates)

        val transitMs = ms(solarTime.transit)
        val sunriseMs = ms(solarTime.sunrise)
        val sunsetMs = ms(solarTime.sunset)
        val tomorrowSunriseMs = msOf(tomorrow, tomorrowSolarTime.sunrise)

        // dhuhr = solar transit; asr may be NaN when the sun never reaches the shadow angle.
        val dhuhrMs = transitMs
        val asrMs = solarTime.afternoon(asrShadowLength)
            .let { if (it.isNaN()) null else ms(it) }

        // Night length between today's sunset and tomorrow's sunrise.
        val nightMs = tomorrowSunriseMs - sunsetMs

        // Fajr.
        val fajrFromAngle = solarTime.timeForSolarAngle(-parameters.fajrAngle, afterTransit = false)
            .let { if (it.isNaN()) null else ms(it) }
        var fajrMs = fajrFromAngle
        if (parameters.method == CalculationMethod.MoonsightingCommittee && coordinates.latitude >= 55) {
            fajrMs = sunriseMs - (nightMs / 7000).toInt() * 1000L
        }
        val safeFajrMs = safeFajr(date, coordinates, parameters, sunriseMs, nightMs)
        if (fajrMs == null || fajrMs < safeFajrMs || !timesInOrder(fajrMs, sunriseMs)) {
            fajrMs = safeFajrMs
        }

        // Maghrib: sunset, optionally offset by angle or minutes.
        val maghribAngle = parameters.maghribAngle
        val maghribFromSunset = ms(solarTime.sunset + parameters.maghribMinutes / 60.0)
        val maghribMs = if (maghribAngle != null) {
            solarTime.timeForSolarAngle(maghribAngle, afterTransit = true)
                .let { if (it.isNaN()) null else ms(it) }
        } else {
            maghribFromSunset
        } ?: maghribFromSunset

        // Isha.
        val ishaAngle = parameters.ishaAngle
        var ishaMs: Long? = if (ishaAngle == null) {
            maghribMs + parameters.ishaMinutes * 60_000L
        } else {
            solarTime.timeForSolarAngle(-ishaAngle, afterTransit = true)
                .let { if (it.isNaN()) null else ms(it) }
        }
        if (parameters.method == CalculationMethod.MoonsightingCommittee && coordinates.latitude >= 55) {
            ishaMs = sunsetMs + (nightMs / 7000).toInt() * 1000L
        }
        val safeIshaMs = safeIsha(date, coordinates, parameters, sunsetMs, nightMs)
        if (ishaMs == null || !timesInOrder(maghribMs, ishaMs) || ishaMs > safeIshaMs) {
            ishaMs = safeIshaMs
        }

        // Ordering guard: every consecutive pair must be in order with at
        // least a 5-minute gap; otherwise the day is degenerate (polar circle).
        val all = listOf(fajrMs, sunriseMs, dhuhrMs, asrMs, maghribMs, ishaMs)
        val ordered = all.zipWithNext().all { (a, b) -> a != null && b != null && timesInOrder(a, b) }
        if (!ordered) return RawResult(valid = false, millis = emptyMap())

        val millis = LinkedHashMap<Prayer, Long>()
        millis[Prayer.Fajr] = fajrMs!!
        millis[Prayer.Sunrise] = sunriseMs
        millis[Prayer.Dhuhr] = dhuhrMs
        millis[Prayer.Asr] = asrMs ?: return RawResult(valid = false, millis = emptyMap())
        millis[Prayer.Maghrib] = maghribMs
        millis[Prayer.Isha] = ishaMs!!
        return RawResult(valid = true, millis = millis)
    }

    private fun safeFajr(
        date: LocalDate,
        coordinates: Coordinates,
        parameters: PrayerParameters,
        sunriseMs: Long,
        nightMs: Long,
    ): Long {
        if (parameters.method == CalculationMethod.MoonsightingCommittee) {
            val adjustment = seasonAdjustedMorningTwilight(date, coordinates)
            return sunriseMs - adjustment.seconds * 1000L
        }
        val portion = nightPortions(parameters, coordinates).first
        return sunriseMs - (portion * (nightMs / 1000.0)).toLong() * 1000L
    }

    private fun safeIsha(
        date: LocalDate,
        coordinates: Coordinates,
        parameters: PrayerParameters,
        sunsetMs: Long,
        nightMs: Long,
    ): Long {
        if (parameters.method == CalculationMethod.MoonsightingCommittee) {
            val adjustment = seasonAdjustedEveningTwilight(date, coordinates)
            return sunsetMs + adjustment.seconds * 1000L
        }
        val portion = nightPortions(parameters, coordinates).second
        return sunsetMs + (portion * (nightMs / 1000.0)).toLong() * 1000L
    }

    private fun nightPortions(parameters: PrayerParameters, coordinates: Coordinates): Pair<Double, Double> {
        val rule = parameters.highLatitudeRule ?: recommendedFor(coordinates.latitude)
        return when (rule) {
            HighLatitudeRule.MiddleOfTheNight -> 1.0 / 2.0 to 1.0 / 2.0
            HighLatitudeRule.SeventhOfTheNight -> 1.0 / 7.0 to 1.0 / 7.0
            HighLatitudeRule.TwilightAngle ->
                parameters.fajrAngle / 60.0 to (parameters.ishaAngle ?: parameters.fajrAngle) / 60.0
        }
    }

    private fun recommendedFor(latitude: Double): HighLatitudeRule =
        if (latitude > 48.0) HighLatitudeRule.SeventhOfTheNight
        else HighLatitudeRule.MiddleOfTheNight

    private fun timesInOrder(a: Long, b: Long): Boolean = b - a >= MIN_PRAYER_GAP_MS

    private data class TwilightAdjustment(val seconds: Int)

    /** Moonsighting Committee seasonal morning twilight adjustment. */
    private fun seasonAdjustedMorningTwilight(date: LocalDate, coordinates: Coordinates): TwilightAdjustment {
        val latitude = coordinates.latitude
        val a = 75 + 28.65 / 55.0 * abs(latitude)
        val b = 75 + 19.44 / 55.0 * abs(latitude)
        val c = 75 + 32.74 / 55.0 * abs(latitude)
        val d = 75 + 48.10 / 55.0 * abs(latitude)
        val dyy = daysSinceSolstice(date.dayOfYear, date.year, latitude)
        val adjustment = when {
            dyy < 91 -> a + (b - a) / 91.0 * dyy
            dyy < 137 -> b + (c - b) / 46.0 * (dyy - 91)
            dyy < 183 -> c + (d - c) / 46.0 * (dyy - 137)
            dyy < 229 -> d + (c - d) / 46.0 * (dyy - 183)
            dyy < 275 -> c + (b - c) / 46.0 * (dyy - 229)
            else -> b + (a - b) / 91.0 * (dyy - 275)
        }
        return TwilightAdjustment((adjustment * 60.0).roundToInt())
    }

    /** Moonsighting Committee seasonal evening twilight adjustment. */
    private fun seasonAdjustedEveningTwilight(date: LocalDate, coordinates: Coordinates): TwilightAdjustment {
        val latitude = coordinates.latitude
        val a = 75 + 25.60 / 55.0 * abs(latitude)
        val b = 75 + 2.050 / 55.0 * abs(latitude)
        val c = 75 - 9.210 / 55.0 * abs(latitude)
        val d = 75 + 6.140 / 55.0 * abs(latitude)
        val dyy = daysSinceSolstice(date.dayOfYear, date.year, latitude)
        val adjustment = when {
            dyy < 91 -> a + (b - a) / 91.0 * dyy
            dyy < 137 -> b + (c - b) / 46.0 * (dyy - 91)
            dyy < 183 -> c + (d - c) / 46.0 * (dyy - 137)
            dyy < 229 -> d + (c - d) / 46.0 * (dyy - 183)
            dyy < 275 -> c + (b - c) / 46.0 * (dyy - 229)
            else -> b + (a - b) / 91.0 * (dyy - 275)
        }
        return TwilightAdjustment((adjustment * 60.0).roundToInt())
    }

    private fun daysSinceSolstice(dayOfYear: Int, year: Int, latitude: Double): Int {
        val isLeap = year % 4 == 0 && !(year % 100 == 0 && year % 400 != 0)
        val northernOffset = 10
        val southernOffset = if (isLeap) 173 else 172
        val daysInYear = if (isLeap) 366 else 365
        return if (latitude >= 0) {
            var days = dayOfYear + northernOffset
            if (days >= daysInYear) days -= daysInYear
            days
        } else {
            var days = dayOfYear - southernOffset
            if (days < 0) days += daysInYear
            days
        }
    }

    companion object {
        private const val MIN_PRAYER_GAP_MS = 5 * 60 * 1000L
    }
}
