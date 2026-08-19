package org.muslim.app.core.location

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Reverse geocoder backed by [android.location.Geocoder] (Google's backend on
 * Play-services devices). Best-effort: returns null on any failure so callers
 * fall back to the offline city index or a coordinate label.
 */
class GeocoderRegionNameResolver(
    private val geocoder: Geocoder,
) : RegionNameResolver {

    override suspend fun resolve(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(10_000) {
                if (!Geocoder.isPresent()) return@withTimeoutOrNull null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocodeAsync(latitude, longitude)
                } else {
                    geocodeLegacy(latitude, longitude)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun geocodeAsync(latitude: Double, longitude: Double): String? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (continuation.isActive) {
                        continuation.resume(addresses.firstOrNull()?.toDisplayName())
                    }
                }

                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) continuation.resume(null)
                }
            })
        }

    @Suppress("DEPRECATION")
    private fun geocodeLegacy(latitude: Double, longitude: Double): String? =
        runCatching { geocoder.getFromLocation(latitude, longitude, 1) }
            .getOrNull()
            ?.firstOrNull()
            ?.toDisplayName()
}

/** Builds "City, Country" from the most specific parts the backend returned. */
private fun Address.toDisplayName(): String? {
    val primary = locality ?: subAdminArea ?: adminArea ?: featureName
    if (primary.isNullOrBlank()) return null
    val country = countryName
    return if (country.isNullOrBlank() || country.equals(primary, ignoreCase = true)) primary
    else "$primary, $country"
}
