package org.muslim.app.feature.qibla.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs

/**
 * Expected directions are the official test vectors of the Adhan library
 * (QiblaTest.kt), tolerance 0.001°.
 */
class QiblaCalculatorTest {

    private fun assertDirection(lat: Double, lng: Double, expected: Double) {
        val actual = QiblaCalculator.direction(lat, lng)
        assertThat(actual).isWithin(0.001).of(expected)
    }

    @Test
    fun northAmerica() {
        assertDirection(38.9072, -77.0369, 56.560)   // Washington DC
        assertDirection(40.7128, -74.0059, 58.481)   // New York
        assertDirection(37.7749, -122.4194, 18.843)  // San Francisco
        assertDirection(61.2181, -149.9003, 350.883) // Anchorage
    }

    @Test
    fun southPacific() {
        assertDirection(-33.8688, 151.2093, 277.499) // Sydney
        assertDirection(-36.8485, 174.7633, 261.197) // Auckland
    }

    @Test
    fun europe() {
        assertDirection(51.5074, -0.1278, 118.987)   // London
        assertDirection(48.8566, 2.3522, 119.163)    // Paris
        assertDirection(59.9139, 10.7522, 139.027)   // Oslo
    }

    @Test
    fun asia() {
        assertDirection(33.7294, 73.0931, 255.882)   // Islamabad
        assertDirection(35.6895, 139.6917, 293.021)  // Tokyo
    }

    @Test
    fun africa() {
        assertDirection(33.9249, 18.4241, 118.004)   // Cape Town
        assertDirection(30.0444, 31.2357, 136.137)   // Cairo
    }

    @Test
    fun atMeccaBearingIsZero() {
        assertThat(QiblaCalculator.direction(21.4225241, 39.8261818)).isWithin(0.001).of(0.0)
    }

    @Test
    fun distanceSanFranciscoToMecca() {
        // ~13 160 km per haversine; sanity range
        val distance = QiblaCalculator.distanceKm(37.7749, -122.4194)
        assertThat(distance).isAtLeast(13_000.0)
        assertThat(distance).isAtMost(13_400.0)
    }

    @Test
    fun distanceCairoToMecca() {
        // ~1300 km
        val distance = QiblaCalculator.distanceKm(30.0444, 31.2357)
        assertThat(distance).isAtLeast(1_250.0)
        assertThat(distance).isAtMost(1_360.0)
    }

    @Test
    fun routePoints_startsAtUserAndEndsAtKaaba() {
        val route = QiblaCalculator.routePoints(38.9072, -77.0369, steps = 10)
        assertThat(route).hasSize(11)
        assertThat(route.first().latitude).isWithin(1e-9).of(38.9072)
        assertThat(route.first().longitude).isWithin(1e-9).of(-77.0369)
        assertThat(route.last().latitude).isWithin(1e-6).of(QiblaCalculator.KAABA_LATITUDE)
        assertThat(route.last().longitude).isWithin(1e-6).of(QiblaCalculator.KAABA_LONGITUDE)
    }

    @Test
    fun routePoints_staysOnTheGreatCircle() {
        val route = QiblaCalculator.routePoints(38.9072, -77.0369, steps = 20)
        // Every intermediate point is roughly as far from the Kaaba as expected
        // on the great circle: distance shrinks monotonically toward the Kaaba.
        val distances = route.map { QiblaCalculator.distanceKm(it.latitude, it.longitude) }
        for (i in 1 until distances.size) {
            assertThat(distances[i]).isAtMost(distances[i - 1] + 0.01)
        }
    }

    @Test
    fun routePoints_atKaabaIsStable() {
        val route = QiblaCalculator.routePoints(
            QiblaCalculator.KAABA_LATITUDE,
            QiblaCalculator.KAABA_LONGITUDE,
            steps = 4,
        )
        route.forEach { point ->
            assertThat(point.latitude).isWithin(1e-6).of(QiblaCalculator.KAABA_LATITUDE)
            assertThat(point.longitude).isWithin(1e-6).of(QiblaCalculator.KAABA_LONGITUDE)
        }
    }

    @Test
    fun projection_fitsWithinUnitSquare() {
        val route = QiblaCalculator.routePoints(38.9072, -77.0369)
        val projected = QiblaCalculator.projectToUnitSquare(route, 38.9072, -77.0369, 1.0, 1.0)
        assertThat(projected).hasSize(route.size)
        projected.forEach { (x, y) ->
            assertThat(x).isAtLeast(0.0)
            assertThat(x).isAtMost(1.0)
            assertThat(y).isAtLeast(0.0)
            assertThat(y).isAtMost(1.0)
        }
        // North-up: latitude decreases downward. The Kaaba (21.4°N) is south of
        // the user (38.9°N), so it must project below the user's location.
        assertThat(projected.last().second).isGreaterThan(projected.first().second)
    }

    @Test
    fun relativeToTrueNorth_isNormalized() {
        assertThat(QiblaCalculator.relativeToTrueNorth(30.0, 40.0)).isWithin(1e-9).of(350.0)
        assertThat(QiblaCalculator.relativeToTrueNorth(10.0, 20.0)).isWithin(1e-9).of(350.0)
        assertThat(QiblaCalculator.relativeToTrueNorth(350.0, 10.0)).isWithin(1e-9).of(340.0)
    }
}
