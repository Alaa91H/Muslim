package org.example.islamicapp.feature.qibla.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
}
