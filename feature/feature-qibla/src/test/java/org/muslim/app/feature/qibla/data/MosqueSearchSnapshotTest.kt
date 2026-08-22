package org.muslim.app.feature.qibla.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class MosqueSearchSnapshotTest {
    private val json = Json

    @Test
    fun `snapshot round trip preserves last successful results`() {
        val original = MosqueSearchSnapshot(
            latitude = 52.52,
            longitude = 13.405,
            mosques = listOf(
                Mosque(
                    name = "Berlin Mosque",
                    latitude = 52.489,
                    longitude = 13.331,
                    distanceMeters = 6_200,
                    bearingFromUser = 247.5,
                ),
            ),
            savedAtEpochMillis = 1_700_000_000_000,
        )

        val restored = json.decodeFromString<MosqueSearchSnapshot>(json.encodeToString(original))

        assertThat(restored).isEqualTo(original)
    }
}
