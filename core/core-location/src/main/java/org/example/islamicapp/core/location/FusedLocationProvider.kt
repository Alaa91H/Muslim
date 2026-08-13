package org.example.islamicapp.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Location provider backed by Google Play services' FusedLocationProvider.
 *
 * - Requests a fresh fix via `getCurrentLocation` (more reliable than the
 *   cached `lastLocation`), with a 15 s timeout.
 * - Returns null when the fine-location permission is missing or a fix can't
 *   be obtained — callers must offer manual/city fallbacks (offline-first).
 */
class FusedLocationProvider(context: Context) : LocationProvider {

    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    override suspend fun currentLocation(): GeoLocation? = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext null
        }
        val location = withTimeoutOrNull(15_000) {
            awaitLocation()
        }
        location?.let { GeoLocation(it.latitude, it.longitude) }
    }

    // Permission is checked in [currentLocation] before this is invoked; the
    // call is split across functions so lint can't see it — suppress here.
    @SuppressLint("MissingPermission")
    private suspend fun awaitLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val task = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
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
}
