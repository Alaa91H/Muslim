package org.muslim.app.feature.prayertimes.ui.location

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoordinateTimeZoneResolverTest {
    private val resolver = CoordinateTimeZoneResolver()

    @Test
    fun `resolves IANA zones locally across major world regions`() = runBlocking {
        val cases = listOf(
            CoordinatesCase(52.5200, 13.4050, "Europe/Berlin"),
            CoordinatesCase(24.7136, 46.6753, "Asia/Riyadh"),
            CoordinatesCase(40.7128, -74.0060, "America/New_York"),
            CoordinatesCase(35.6762, 139.6503, "Asia/Tokyo"),
            CoordinatesCase(-33.8688, 151.2093, "Australia/Sydney"),
        )

        cases.forEach { case ->
            assertThat(resolver.resolve(case.latitude, case.longitude))
                .isEqualTo(case.expectedZoneId)
        }
    }

    private data class CoordinatesCase(
        val latitude: Double,
        val longitude: Double,
        val expectedZoneId: String,
    )
}
