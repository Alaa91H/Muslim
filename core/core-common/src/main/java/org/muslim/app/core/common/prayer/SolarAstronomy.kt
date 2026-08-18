package org.muslim.app.core.common.prayer

import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Converts degrees to radians. */
private fun Double.toRadians(): Double = this * PI / 180.0

/** Converts radians to degrees. */
private fun Double.toDegrees(): Double = this * 180.0 / PI

/**
 * Astronomical equations for the apparent position of the Sun.
 *
 * Faithful port of the equations published in Jean Meeus, "Astronomical
 * Algorithms" (2nd ed.), as implemented by the open-source Adhan library
 * (Batoul Apps, MIT License). All angles are in degrees unless noted.
 */
internal object SolarAstronomy {

    // ---- Angle / time helpers (Adhan DoubleUtil) ----

    fun normalizeWithBound(value: Double, max: Double): Double =
        value - max * floor(value / max)

    fun unwindAngle(value: Double): Double = normalizeWithBound(value, 360.0)

    fun closestAngle(angle: Double): Double =
        if (angle >= -180 && angle <= 180) angle else angle - 360 * (angle / 360).roundToInt()

    // ---- Calendrical helpers ----

    /** Julian Day for a Gregorian date (Astronomical Algorithms, p. 60). */
    fun julianDay(year: Int, month: Int, day: Int, hours: Double = 0.0): Double {
        val y = if (month > 2) year else year - 1
        val m = if (month > 2) month else month + 12
        val d = day + hours / 24.0
        val a = y / 100
        val b = 2 - a + a / 4
        val i0 = (365.25 * (y + 4716)).toInt()
        val i1 = (30.6001 * (m + 1)).toInt()
        return i0 + i1 + d + b - 1524.5
    }

    /** Julian century since J2000 epoch. */
    fun julianCentury(julianDay: Double): Double = (julianDay - 2451545.0) / 36525

    /** Declination, right ascension and apparent sidereal time of the Sun. */
    data class SolarPosition(
        val declination: Double,
        val rightAscension: Double,
        val apparentSiderealTime: Double,
    )

    /** Sun position for a given Julian Day (Astronomical Algorithms pp. 163-165). */
    fun solarPosition(julianDay: Double): SolarPosition {
        val t = julianCentury(julianDay)
        val l0 = meanSolarLongitude(t)
        val lp = meanLunarLongitude(t)
        val omega = ascendingLunarNodeLongitude(t)
        val lambda = apparentSolarLongitude(t, l0).toRadians()
        val theta0 = meanSiderealTime(t)
        val deltaPsi = nutationInLongitude(t, l0, lp, omega)
        val deltaEpsilon = nutationInObliquity(t, l0, lp, omega)
        val epsilon0 = meanObliquityOfTheEcliptic(t)
        val epsilonApparent = apparentObliquityOfTheEcliptic(t, epsilon0).toRadians()

        val declination = asin(sin(epsilonApparent) * sin(lambda)).toDegrees()
        val rightAscension = unwindAngle(atan2(cos(epsilonApparent) * sin(lambda), cos(lambda)).toDegrees())
        val apparentSiderealTime = theta0 + deltaPsi * 3600 * cos((epsilon0 + deltaEpsilon).toRadians()) / 3600
        return SolarPosition(declination, rightAscension, apparentSiderealTime)
    }

    // ---- Sun equations (Astronomical Algorithms) ----

    private fun meanSolarLongitude(t: Double): Double {
        // p. 163
        val l0 = 280.4664567 + 36000.76983 * t + 0.0003032 * t.pow(2.0)
        return unwindAngle(l0)
    }

    private fun meanLunarLongitude(t: Double): Double {
        // p. 144
        return unwindAngle(218.3165 + 481267.8813 * t)
    }

    private fun apparentSolarLongitude(t: Double, meanLongitude: Double): Double {
        // p. 164
        val longitude = meanLongitude + solarEquationOfTheCenter(t, meanSolarAnomaly(t))
        val omega = 125.04 - 1934.136 * t
        val lambda = longitude - 0.00569 - 0.00478 * sin(omega.toRadians())
        return unwindAngle(lambda)
    }

    private fun ascendingLunarNodeLongitude(t: Double): Double {
        // p. 144
        val omega = 125.04452 - 1934.136261 * t + 0.0020708 * t.pow(2.0) + t.pow(3.0) / 450000
        return unwindAngle(omega)
    }

    private fun meanSolarAnomaly(t: Double): Double {
        // p. 163
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t.pow(2.0)
        return unwindAngle(m)
    }

    private fun solarEquationOfTheCenter(t: Double, m: Double): Double {
        // p. 164
        val mRad = m.toRadians()
        val term1 = (1.914602 - 0.004817 * t - 0.000014 * t.pow(2.0)) * sin(mRad)
        val term2 = (0.019993 - 0.000101 * t) * sin(2 * mRad)
        val term3 = 0.000289 * sin(3 * mRad)
        return term1 + term2 + term3
    }

    private fun meanObliquityOfTheEcliptic(t: Double): Double {
        // p. 147
        return 23.439291 - 0.013004167 * t - 0.0000001639 * t.pow(2.0) + 0.0000005036 * t.pow(3.0)
    }

    private fun apparentObliquityOfTheEcliptic(t: Double, meanObliquity: Double): Double {
        // p. 165
        val o = 125.04 - 1934.136 * t
        return meanObliquity + 0.00256 * cos(o.toRadians())
    }

    private fun meanSiderealTime(t: Double): Double {
        // p. 165
        val jd = t * 36525 + 2451545.0
        val theta = 280.46061837 + 360.98564736629 * (jd - 2451545) +
            0.000387933 * t.pow(2.0) - t.pow(3.0) / 38710000
        return unwindAngle(theta)
    }

    private fun nutationInLongitude(t: Double, solarLongitude: Double, lunarLongitude: Double, ascendingNode: Double): Double {
        // p. 144
        val term1 = -17.2 / 3600 * sin(ascendingNode.toRadians())
        val term2 = 1.32 / 3600 * sin(2 * solarLongitude.toRadians())
        val term3 = 0.23 / 3600 * sin(2 * lunarLongitude.toRadians())
        val term4 = 0.21 / 3600 * sin(2 * ascendingNode.toRadians())
        return term1 - term2 - term3 + term4
    }

    private fun nutationInObliquity(t: Double, solarLongitude: Double, lunarLongitude: Double, ascendingNode: Double): Double {
        // p. 144
        val term1 = 9.2 / 3600 * cos(ascendingNode.toRadians())
        val term2 = 0.57 / 3600 * cos(2 * solarLongitude.toRadians())
        val term3 = 0.10 / 3600 * cos(2 * lunarLongitude.toRadians())
        val term4 = 0.09 / 3600 * cos(2 * ascendingNode.toRadians())
        return term1 + term2 + term3 - term4
    }

    fun altitudeOfCelestialBody(latitude: Double, declination: Double, localHourAngle: Double): Double {
        // p. 93
        val term1 = sin(latitude.toRadians()) * sin(declination.toRadians())
        val term2 = cos(latitude.toRadians()) * cos(declination.toRadians()) * cos(localHourAngle.toRadians())
        return asin(term1 + term2).toDegrees()
    }

    // ---- Transit / hour-angle corrections (Astronomical Algorithms p. 102) ----

    fun approximateTransit(longitude: Double, siderealTime: Double, rightAscension: Double): Double {
        val lw = longitude * -1
        var m0 = normalizeWithBound((rightAscension + lw - siderealTime) / 360, 1.0)
        // Near the International Date Line m0 can land on the wrong calendar
        // date; detect and correct a full-cycle error.
        val expectedTransit = normalizeWithBound((12.0 - longitude / 15.0) / 24.0, 1.0)
        return when {
            m0 - expectedTransit > 0.5 -> m0 - 1.0
            expectedTransit - m0 > 0.5 -> m0 + 1.0
            else -> m0
        }
    }

    /** Solar transit in hours (UT). */
    fun correctedTransit(
        approximateTransit: Double,
        longitude: Double,
        siderealTime: Double,
        rightAscension: Double,
        previousRightAscension: Double,
        nextRightAscension: Double,
    ): Double {
        val lw = longitude * -1
        val theta = unwindAngle(siderealTime + 360.985647 * approximateTransit)
        val alpha = unwindAngle(
            interpolateAngles(rightAscension, previousRightAscension, nextRightAscension, approximateTransit)
        )
        val h = closestAngle(theta - lw - alpha)
        val deltaM = h / -360
        return (approximateTransit + deltaM) * 24
    }

    /** Time of a given solar angle in hours (UT). NaN when the angle is unreachable. */
    fun correctedHourAngle(
        approximateTransit: Double,
        angle: Double,
        coordinates: Coordinates,
        afterTransit: Boolean,
        siderealTime: Double,
        rightAscension: Double,
        previousRightAscension: Double,
        nextRightAscension: Double,
        declination: Double,
        previousDeclination: Double,
        nextDeclination: Double,
    ): Double {
        val lw = coordinates.longitude * -1
        val term1 = sin(angle.toRadians()) - sin(coordinates.latitude.toRadians()) * sin(declination.toRadians())
        val term2 = cos(coordinates.latitude.toRadians()) * cos(declination.toRadians())
        val h0 = acos(term1 / term2).toDegrees()
        var m = if (afterTransit) approximateTransit + h0 / 360 else approximateTransit - h0 / 360

        // Iterative correction (p. 103); cap at 10 iterations.
        for (i in 0 until 10) {
            val theta = unwindAngle(siderealTime + 360.985647 * m)
            val alpha = unwindAngle(interpolateAngles(rightAscension, previousRightAscension, nextRightAscension, m))
            val delta = interpolate(declination, previousDeclination, nextDeclination, m)
            val h = theta - lw - alpha
            val altitude = altitudeOfCelestialBody(coordinates.latitude, delta, h)
            val dmDenominator = 360 * cos(delta.toRadians()) * cos(coordinates.latitude.toRadians()) * sin(h.toRadians())
            if (dmDenominator == 0.0) return Double.NaN
            val deltaM = (altitude - angle) / dmDenominator
            m += deltaM
            if (abs(deltaM) <= 0.01) break
        }
        return m * 24
    }

    /** Quadratic interpolation (Astronomical Algorithms p. 24). */
    fun interpolate(value: Double, previousValue: Double, nextValue: Double, factor: Double): Double {
        val a = value - previousValue
        val b = nextValue - value
        val c = b - a
        return value + factor / 2 * (a + b + factor * c)
    }

    /** Quadratic interpolation of angles, accounting for unwinding. */
    fun interpolateAngles(value: Double, previousValue: Double, nextValue: Double, factor: Double): Double {
        val a = unwindAngle(value - previousValue)
        val b = unwindAngle(nextValue - value)
        val c = b - a
        return value + factor / 2 * (a + b + factor * c)
    }
}

/**
 * Solar times for a given date and location (transit, sunrise, sunset) plus
 * helpers for arbitrary angles and Asr. Times are returned in hours since
 * 00:00 UTC of [date].
 */
internal class SolarTime(private val date: LocalDate, private val coordinates: Coordinates) {

    val transit: Double
    val sunrise: Double
    val sunset: Double

    private val solar: SolarAstronomy.SolarPosition
    private val prevSolar: SolarAstronomy.SolarPosition
    private val nextSolar: SolarAstronomy.SolarPosition
    private val approximateTransit: Double

    init {
        val julianDay = SolarAstronomy.julianDay(date.year, date.monthValue, date.dayOfMonth)
        prevSolar = SolarAstronomy.solarPosition(julianDay - 1)
        solar = SolarAstronomy.solarPosition(julianDay)
        nextSolar = SolarAstronomy.solarPosition(julianDay + 1)
        approximateTransit = SolarAstronomy.approximateTransit(
            coordinates.longitude,
            solar.apparentSiderealTime,
            solar.rightAscension,
        )
        // Standard refraction + solar disc radius for sunrise/sunset (-0.833°),
        // plus the horizon dip caused by elevation above sea level — the higher
        // the location, the earlier the sun appears to rise and the later it
        // sets. Formula per praytimes.org (the project's reference):
        //   α = -0.8333 - 0.0347·√h  (h = elevation in meters)
        val solarAltitude = -(50.0 / 60.0) - 0.0347 * sqrt(coordinates.elevation.coerceAtLeast(0.0))
        transit = SolarAstronomy.correctedTransit(
            approximateTransit, coordinates.longitude,
            solar.apparentSiderealTime, solar.rightAscension,
            prevSolar.rightAscension, nextSolar.rightAscension,
        )
        sunrise = SolarAstronomy.correctedHourAngle(
            approximateTransit, solarAltitude, coordinates, false,
            solar.apparentSiderealTime, solar.rightAscension,
            prevSolar.rightAscension, nextSolar.rightAscension,
            solar.declination, prevSolar.declination, nextSolar.declination,
        )
        sunset = SolarAstronomy.correctedHourAngle(
            approximateTransit, solarAltitude, coordinates, true,
            solar.apparentSiderealTime, solar.rightAscension,
            prevSolar.rightAscension, nextSolar.rightAscension,
            solar.declination, prevSolar.declination, nextSolar.declination,
        )
    }

    /** Hours since 00:00 UTC of [date] at which the sun reaches [angle]. */
    fun timeForSolarAngle(angle: Double, afterTransit: Boolean): Double =
        SolarAstronomy.correctedHourAngle(
            approximateTransit, angle, coordinates, afterTransit,
            solar.apparentSiderealTime, solar.rightAscension,
            prevSolar.rightAscension, nextSolar.rightAscension,
            solar.declination, prevSolar.declination, nextSolar.declination,
        )

    /** Hours from transit at which Asr begins for the given shadow factor. */
    fun afternoon(shadowLength: Double): Double {
        val tangent = abs(coordinates.latitude - solar.declination)
        val inverse = shadowLength + tan(tangent.toRadians())
        val angle = atan(1.0 / inverse).toDegrees()

        // The sun's disc must be fully above the horizon (midpoint-based
        // hour angle calculation needs the angular diameter check).
        val solarAngularDiameter = 32.0 / 60.0
        if (angle <= solarAngularDiameter / 2) return Double.NaN

        val maxAltitude = SolarAstronomy.altitudeOfCelestialBody(coordinates.latitude, solar.declination, 0.0)
        if (maxAltitude < angle) return Double.NaN

        val result = timeForSolarAngle(angle, true)
        if (result <= transit) return Double.NaN
        return result
    }
}
