package org.muslim.app.feature.qibla.domain

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** A point on the Earth's surface, in degrees. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

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

    /**
     * Intermediate points along the great circle from the user's location to
     * the Kaaba (spherical linear interpolation), for drawing the qibla path
     * on the offline map (PROJECT_PROMPT.md §6 Phase 1: خريطة القبلة).
     * Includes the start and the Kaaba itself as the first/last points.
     */
    fun routePoints(
        latitude: Double,
        longitude: Double,
        steps: Int = 24,
    ): List<GeoPoint> {
        require(steps >= 2)
        val lat1 = latitude.toRadians()
        val lng1 = longitude.toRadians()
        val lat2 = KAABA_LATITUDE.toRadians()
        val lng2 = KAABA_LONGITUDE.toRadians()

        val dLng = lng2 - lng1
        // Distance angle between the two points (unit sphere).
        val delta = acos(
            sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(dLng),
        )

        return (0..steps).map { i ->
            val f = i.toDouble() / steps
            if (delta < 1e-12) {
                GeoPoint(latitude, longitude)
            } else {
                val sinDelta = sin(delta)
                val a = sin((1 - f) * delta) / sinDelta
                val b = sin(f * delta) / sinDelta
                val x = a * cos(lat1) * cos(lng1) + b * cos(lat2) * cos(lng2)
                val y = a * cos(lat1) * sin(lng1) + b * cos(lat2) * sin(lng2)
                val z = a * sin(lat1) + b * sin(lat2)
                val lat = atan2(z, sqrt(x * x + y * y)).toDegrees()
                val lng = atan2(y, x).toDegrees()
                GeoPoint(lat, lng)
            }
        }
    }

    /**
     * Projects a list of [GeoPoint]s onto a [width]×[height] plane using the
     * equirectangular projection centred on the user's location — North up.
     * Coordinates are 0..1 relative to the drawing area.
     */
    fun projectToUnitSquare(
        points: List<GeoPoint>,
        centerLatitude: Double,
        centerLongitude: Double,
        width: Double,
        height: Double,
    ): List<Pair<Double, Double>> {
        if (points.isEmpty()) return emptyList()
        val cosCenter = cos(centerLatitude.toRadians()).coerceAtLeast(1e-6)
        val lats = points.map { it.latitude }
        val lngs = points.map { it.longitude }
        val minLat = lats.minOrNull() ?: 0.0
        val maxLat = lats.maxOrNull() ?: 0.0
        val minLng = lngs.minOrNull() ?: 0.0
        val maxLng = lngs.maxOrNull() ?: 0.0
        // A tiny span (e.g. the user is right at the Kaaba) must still render.
        val latSpan = (maxLat - minLat).coerceAtLeast(0.5)
        val lngSpan = ((maxLng - minLng) * cosCenter).coerceAtLeast(0.5)
        val scale = min(width / lngSpan, height / latSpan)
        val mapWidth = lngSpan * scale
        val mapHeight = latSpan * scale
        val offsetX = (width - mapWidth) / 2
        val offsetY = (height - mapHeight) / 2
        return points.map { p ->
            val x = offsetX + (p.longitude - minLng) * cosCenter * scale
            val y = offsetY + (maxLat - p.latitude) * scale
            Pair(x / width, y / height)
        }
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI
}
