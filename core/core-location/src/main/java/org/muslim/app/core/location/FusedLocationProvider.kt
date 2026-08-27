package org.muslim.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Location provider backed by Google Play services' FusedLocationProvider.
 *
 * It accepts Android's approximate or precise foreground location permission.
 * A precise grant requests a high-accuracy live fix; an approximate grant still
 * supplies a valid coordinate for prayer-time calculation. If a live fix is
 * unavailable, the most recent fused location is used as an explicit local
 * fallback. No location is sent to the network and no device timezone is used.
 */
class FusedLocationProvider(context: Context) : LocationProvider {

    private val appContext = context.applicationContext

    /**
     * Some Google Play Services/OEM stacks can fail while constructing the fused
     * client itself. Keep that platform work inside a recoverable lazy result so
     * choosing GPS cannot terminate the process before [currentLocation] runs.
     */
    private val client: FusedLocationProviderClient? by lazy {
        runCatching { LocationServices.getFusedLocationProviderClient(appContext) }.getOrNull()
    }

    private val platformLocationManager: LocationManager?
        get() = runCatching { appContext.getSystemService(LocationManager::class.java) }.getOrNull()

    override suspend fun currentLocation(): GeoLocation? = try {
        withContext(Dispatchers.IO) {
        val hasFine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return@withContext null

        val priority = if (hasFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val location = withTimeoutOrNull(20_000) { awaitCurrentLocation(priority) }
            ?: withTimeoutOrNull(5_000) { awaitLastKnownLocation() }
            ?: mostRecentPlatformLocation()
            location?.takeIf { it.latitude.isFinite() && it.longitude.isFinite() }?.let { geo ->
                GeoLocation(
                    latitude = geo.latitude,
                    longitude = geo.longitude,
                    altitude = if (geo.hasAltitude()) geo.altitude else null,
                )
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (_: Throwable) {
        // Play Services and OEM location stacks can fail before any task callback.
        null
    }

    // Permission is checked in [currentLocation] before these methods are invoked.
    @SuppressLint("MissingPermission")
    private suspend fun awaitCurrentLocation(priority: Int): Location? = suspendCancellableCoroutine { continuation ->
        val fusedClient = client ?: run {
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val task = runCatching { fusedClient.getCurrentLocation(priority, null) }
            .getOrElse {
                if (continuation.isActive) continuation.resume(null)
                return@suspendCancellableCoroutine
            }
        task.addOnSuccessListener { location ->
            if (continuation.isActive) continuation.resume(location)
        }
        task.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
        task.addOnCanceledListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val fusedClient = client ?: run {
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val task = runCatching { fusedClient.lastLocation }
            .getOrElse {
                if (continuation.isActive) continuation.resume(null)
                return@suspendCancellableCoroutine
            }
        task.addOnSuccessListener { location ->
            if (continuation.isActive) continuation.resume(location)
        }
        task.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
        task.addOnCanceledListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    /** Local Android GPS/network cache fallback for devices without a current fused fix. */
    @SuppressLint("MissingPermission")
    private fun mostRecentPlatformLocation(): Location? = runCatching {
        val locationManager = platformLocationManager ?: return@runCatching null
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
            .maxByOrNull { location -> location.time }
    }.getOrNull()
}
