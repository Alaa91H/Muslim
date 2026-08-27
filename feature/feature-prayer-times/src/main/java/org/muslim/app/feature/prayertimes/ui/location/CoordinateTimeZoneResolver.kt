package org.muslim.app.feature.prayertimes.ui.location

import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import us.dustinj.timezonemap.TimeZoneMap

/**
 * Resolves a coordinate to an IANA time-zone identifier without sending the
 * user's location to a network service.
 *
 * The boundary index is scoped to a small area around the requested point to
 * avoid retaining a global geometry map in memory. Points close to the
 * International Date Line use the library's global fallback because its
 * regional constructor cannot span the longitude discontinuity.
 */
@Singleton
class CoordinateTimeZoneResolver @Inject constructor() {

    suspend fun resolve(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.Default) {
            runCatching {
                val timeZoneMap = if (isNearInternationalDateLine(longitude)) {
                    TimeZoneMap.forEverywhere()
                } else {
                    TimeZoneMap.forRegion(
                        minDegreesLatitude = (latitude - REGION_HALF_SPAN_DEGREES).coerceAtLeast(MIN_LATITUDE),
                        minDegreesLongitude = longitude - REGION_HALF_SPAN_DEGREES,
                        maxDegreesLatitude = (latitude + REGION_HALF_SPAN_DEGREES).coerceAtMost(MAX_LATITUDE),
                        maxDegreesLongitude = longitude + REGION_HALF_SPAN_DEGREES,
                    )
                }
                timeZoneMap.getOverlappingTimeZone(latitude, longitude)?.zoneId
                    ?.takeIf { zoneId -> runCatching { ZoneId.of(zoneId) }.isSuccess }
            }.getOrNull()
        }

    private fun isNearInternationalDateLine(longitude: Double): Boolean =
        longitude - REGION_HALF_SPAN_DEGREES <= MIN_LONGITUDE ||
            longitude + REGION_HALF_SPAN_DEGREES >= MAX_LONGITUDE

    private companion object {
        const val REGION_HALF_SPAN_DEGREES = 0.25
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}
