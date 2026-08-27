package org.muslim.app.feature.qibla.mosques

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.qibla.data.MosquePlace

class NearbyMosqueDirectionsTest {

    @Test
    fun `navigation destination retains the stored mosque coordinates`() {
        val mosque = MosquePlace(
            osmId = 99,
            osmType = "node",
            name = "Coordinate mosque",
            latitude = 21.422487123,
            longitude = 39.826206789,
        )

        assertThat(navigationCoordinates(mosque)).isEqualTo("21.422487123,39.826206789")
    }
}
