package org.muslim.app.feature.qibla.data

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.Test
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.location.GeoLocation

class NearbyMosqueRepositoryTest {

    private val source = OverpassMosqueDataSource(mockk())
    private val repository = NearbyMosqueRepository(source, mockk<AppPreferencesRepository>())

    @Test
    fun `query asks only for bounded Muslim worship places with required centers`() {
        val query = source.buildQuery(GeoLocation(21.3891, 39.8579), radiusKm = 5)

        assertThat(query).contains("around:5000,21.389100,39.857900")
        assertThat(query).contains("[\"religion\"=\"muslim\"]")
        assertThat(query).contains("[\"place_of_worship\"=\"mosque\"]")
        assertThat(query).contains("[\"amenity\"=\"mosque\"]")
        assertThat(query).contains("out center 100;")
        assertThat(query).doesNotContain("map")
    }

    @Test
    fun `nearby results are locally distance sorted even when source order is not`() {
        val user = GeoLocation(24.7136, 46.6753)
        val farther = MosquePlace(2, "node", "Far", latitude = 24.7336, longitude = 46.6753)
        val nearest = MosquePlace(1, "node", "Near", latitude = 24.7146, longitude = 46.6753)
        val invalid = MosquePlace(3, "node", "Invalid", latitude = Double.NaN, longitude = 46.0)

        val results = repository.nearbyFor(user, listOf(farther, invalid, nearest), radiusKm = 5)

        assertThat(results.map { it.place.name }).containsExactly("Near", "Far").inOrder()
        assertThat(results.first().distanceMeters).isLessThan(results.last().distanceMeters)
    }

    @Test
    fun `distance uses the local great circle calculation`() {
        val meters = haversineMeters(0.0, 0.0, 1.0, 0.0)

        assertThat(meters).isWithin(250.0).of(111_195.0)
    }

    @Test
    fun `cache is decoded as places not precomputed distances`() {
        val cache = NearbyMosqueCache(
            places = listOf(MosquePlace(9, "way", "Cached", latitude = 24.7137, longitude = 46.6754)),
            sourceLatitude = 24.7136,
            sourceLongitude = 46.6753,
            sourceRadiusKm = 5,
        )
        val preferences = AppPreferences(
            nearbyMosqueCacheJson = Json.encodeToString(cache),
            nearbyMosqueCacheSavedAtEpochMillis = 1_000L,
        )

        val decoded = repository.cacheFrom(preferences)

        assertThat(decoded).isEqualTo(cache)
        assertThat(decoded!!.places.single().name).isEqualTo("Cached")
    }

    @Test
    fun `freshness expires after ten minutes and rejects future timestamps`() {
        assertThat(repository.isFresh(AppPreferences(nearbyMosqueCacheSavedAtEpochMillis = 1_000L), nowMillis = 600_999L)).isTrue()
        assertThat(repository.isFresh(AppPreferences(nearbyMosqueCacheSavedAtEpochMillis = 1_000L), nowMillis = 601_001L)).isFalse()
        assertThat(repository.isFresh(AppPreferences(nearbyMosqueCacheSavedAtEpochMillis = 2_000L), nowMillis = 1_000L)).isFalse()
    }
}
