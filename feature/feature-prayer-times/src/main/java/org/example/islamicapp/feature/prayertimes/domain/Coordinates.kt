package org.example.islamicapp.feature.prayertimes.domain

/**
 * Geographic coordinates of a location.
 *
 * @property latitude  latitude in degrees, -90..90
 * @property longitude longitude in degrees, -180..180
 */
data class Coordinates(val latitude: Double, val longitude: Double) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90, was $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180, was $longitude" }
    }
}
