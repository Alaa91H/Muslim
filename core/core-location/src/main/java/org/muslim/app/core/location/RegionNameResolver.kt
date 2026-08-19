package org.muslim.app.core.location

/**
 * Resolves a human-readable place name (area / city / region) for coordinates.
 *
 * Implementations may query an online reverse-geocoder or an offline index.
 * Returns null when no name can be determined so callers can fall back to a
 * generic label — the app must stay fully usable offline.
 */
interface RegionNameResolver {
    /** Returns a display name for the coordinates, or null when unavailable. */
    suspend fun resolve(latitude: Double, longitude: Double): String?
}
