package org.muslim.app.core.location

/**
 * A geographic location used for prayer-time and qibla calculations.
 *
 * @property altitude meters above mean sea level when the fix provides it
 *   (GPS usually does); null when unknown — callers default to 0.
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
)

/**
 * Provides the user's current location.
 *
 * Implementations may be GPS/network based ([FusedLocationProvider]) or, in
 * future, a mock for tests. Returns null when the permission is denied or no
 * fix is available — the app must stay fully usable via manual entry.
 */
interface LocationProvider {
    /** Returns the current location, or null when unavailable/unpermitted. */
    suspend fun currentLocation(): GeoLocation?
}
