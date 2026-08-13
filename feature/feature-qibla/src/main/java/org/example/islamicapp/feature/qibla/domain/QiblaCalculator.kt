package org.example.islamicapp.feature.qibla.domain

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Qibla direction and distance toward the Kaaba in Mecca.
 *
 * The bearing formula is the standard great-circle (spherical trigonometry)
 * equation used by the open-source Adhan library (MIT) — see "Spherical
 * Trigonometry for the use of colleges and schools", p. 50.
 */
object QiblaCalculator {

    /** Kaaba coordinates (21.4225°N, 39.8262°E as referenced in PROJECT_PROMPT.md). */
    val KAABA_LATITUDE = 21.4225241
    val KAABA_LONGITUDE = 39.8261818

    /**
     * Initial great-circle bearing from [latitude]/[longitude] toward the Kaaba,
     * in degrees clockwise from true north (0..360).
     */
    fun direction(latitude: Double, longitude: Double): Double {
        // At the Kaaba itself the bearing is degenerate (atan2(0, ±0) is unstable);
        // return 0 rather than a spurious 180°.
        if (kotlin.math.abs(latitude - KAABA_LATITUDE) < 1e-6 &&
            kotlin.math.abs(longitude - KAABA_LONGITUDE) < 1e-6
        ) {
            return 0.0
        }
        val longitudeDelta = (KAABA_LONGITUDE - longitude).toRadians()
        val latitudeRad = latitude.toRadians()
        val term1 = sin(longitudeDelta)
        val term2 = cos(latitudeRad) * tan(KAABA_LATITUDE.toRadians())
        val term3 = sin(latitudeRad) * cos(longitudeDelta)
        val angle = atan2(term1, term2 - term3)
        return ((angle.toDegrees() % 360.0) + 360.0) % 360.0
    }

    /**
     * Great-circle distance from [latitude]/[longitude] to the Kaaba in
     * kilometres (haversine formula).
     */
    fun distanceKm(latitude: Double, longitude: Double): Double {
        val earthRadiusKm = 6371.0088
        val dLat = (KAABA_LATITUDE - latitude).toRadians()
        val dLng = (KAABA_LONGITUDE - longitude).toRadians()
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(latitude.toRadians()) * cos(KAABA_LATITUDE.toRadians()) *
            sin(dLng / 2) * sin(dLng / 2)
        return earthRadiusKm * 2 * asin(sqrt(a))
    }

    /** Compass direction in degrees; `trueNorth - bearing` gives the needle offset. */
    fun relativeToTrueNorth(bearing: Double, deviceHeadingFromNorth: Double): Double =
        ((bearing - deviceHeadingFromNorth) % 360.0 + 360.0) % 360.0

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI
}
