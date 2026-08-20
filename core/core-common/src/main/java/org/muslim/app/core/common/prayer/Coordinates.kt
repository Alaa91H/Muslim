package org.muslim.app.core.common.prayer

/**
 * Geographic coordinates of a location.
 *
 * @property latitude  latitude in degrees, -90..90
 * @property longitude longitude in degrees, -180..180
 * @property elevation meters above mean sea level, >= 0 (affects sunrise /
 *   sunset: the visible horizon drops with height, so the sun appears earlier
 *   and sets later in mountainous regions).
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90, was $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180, was $longitude" }
        require(elevation >= 0.0) { "Elevation must be >= 0 meters, was $elevation" }
    }
}
