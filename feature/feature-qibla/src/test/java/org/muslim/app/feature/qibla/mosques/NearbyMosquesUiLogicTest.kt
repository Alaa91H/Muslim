package org.muslim.app.feature.qibla.mosques

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.qibla.data.MosquePlace
import org.muslim.app.feature.qibla.data.NearbyMosque

class NearbyMosquesUiLogicTest {

    private val centralMosque = NearbyMosque(
        place = MosquePlace(
            osmId = 1,
            osmType = "node",
            name = "Central Mosque",
            address = "Main Street 10",
            latitude = 52.0,
            longitude = 13.0,
        ),
        distanceMeters = 2_000.0,
    )

    private val nearbyMasjid = NearbyMosque(
        place = MosquePlace(
            osmId = 2,
            osmType = "node",
            name = "Al Noor Masjid",
            address = "Garden Road 3",
            latitude = 52.01,
            longitude = 13.01,
        ),
        distanceMeters = 500.0,
    )

    @Test
    fun `search matches mosque name or address ignoring case`() {
        val source = listOf(centralMosque, nearbyMasjid)

        assertThat(filterAndSortMosques(source, "NOOR", MosqueSortMode.Distance))
            .containsExactly(nearbyMasjid)
        assertThat(filterAndSortMosques(source, "main street", MosqueSortMode.Distance))
            .containsExactly(centralMosque)
    }

    @Test
    fun `distance sort keeps nearest result first`() {
        val source = listOf(centralMosque, nearbyMasjid)

        assertThat(filterAndSortMosques(source, "", MosqueSortMode.Distance))
            .containsExactly(nearbyMasjid, centralMosque)
            .inOrder()
    }

    @Test
    fun `name sort is alphabetical and keeps distance as a tie breaker`() {
        val source = listOf(centralMosque, nearbyMasjid)

        assertThat(filterAndSortMosques(source, "", MosqueSortMode.Name))
            .containsExactly(nearbyMasjid, centralMosque)
            .inOrder()
    }

    @Test
    fun `next radius progresses through supported choices and stops at maximum`() {
        assertThat(nextMosqueRadius(5)).isEqualTo(10)
        assertThat(nextMosqueRadius(10)).isEqualTo(15)
        assertThat(nextMosqueRadius(25)).isEqualTo(50)
        assertThat(nextMosqueRadius(50)).isNull()
    }

    @Test
    fun `map links retain exact mosque coordinates`() {
        val mosque = nearbyMasjid.place

        assertThat(mosqueMapUri(mosque)).contains("52.01,13.01")
        assertThat(mosqueOpenStreetMapUrl(mosque)).contains("mlat=52.01")
        assertThat(mosqueOpenStreetMapUrl(mosque)).contains("mlon=13.01")
    }
}
