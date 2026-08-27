package org.muslim.app.feature.prayertimes.ui.location

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.location.RegionNameResolver
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.feature.prayertimes.data.CitiesRepository
import org.muslim.app.feature.prayertimes.domain.City
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler
import org.muslim.app.feature.prayertimes.notifications.NextAdhanService
import org.muslim.app.feature.prayertimes.widget.PrayerTimesWidget

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerSettingsRepository,
    private val locationProvider: LocationProvider,
    private val scheduler: AdhanScheduler,
    private val regionNameResolver: RegionNameResolver,
    private val coordinateTimeZoneResolver: CoordinateTimeZoneResolver,
) : ViewModel() {

    sealed interface Message {
        data class Error(val text: String) : Message
        data object Saved : Message
    }

    val searchQuery = MutableStateFlow("")

    val results: StateFlow<List<City>> = searchQuery
        .map { CitiesRepository.search(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val messages = MutableStateFlow<Message?>(null)

    /** Saves a city from the offline database with its explicit IANA zone. */
    fun selectCity(city: City) {
        persist(
            SelectedLocation(
                name = city.displayName,
                latitude = city.latitude,
                longitude = city.longitude,
                timeZone = city.timeZone,
                elevation = city.elevation,
            ),
        )
    }

    /**
     * Validates and saves manually entered coordinates. The local IANA lookup
     * happens before persistence so a manually entered place is never silently
     * interpreted in the device's unrelated civil timezone.
     */
    fun saveManual(latitudeText: String, longitudeText: String): Boolean {
        // Normalize first so Arabic-Indic/Persian digits parse correctly.
        val latitude = org.muslim.app.core.common.text.Digits.toWesternDigits(latitudeText).trim().toDoubleOrNull()
        val longitude = org.muslim.app.core.common.text.Digits.toWesternDigits(longitudeText).trim().toDoubleOrNull()
        if (latitude == null || longitude == null || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            messages.value = Message.Error("invalid")
            return false
        }
        viewModelScope.launch {
            val timeZone = coordinateTimeZoneResolver.resolve(latitude, longitude)
            if (timeZone == null) {
                messages.value = Message.Error("gps_failed")
                return@launch
            }
            persistNow(
                SelectedLocation(
                    name = "$latitude, $longitude",
                    latitude = latitude,
                    longitude = longitude,
                    timeZone = timeZone,
                ),
            )
        }
        return true
    }

    /** Fetches the current GPS location (caller must hold the permission). */
    fun useGps() {
        viewModelScope.launch {
            try {
                val geo = locationProvider.currentLocation()
                if (geo == null) {
                    messages.value = Message.Error("gps_failed")
                    return@launch
                }
                val timeZone = coordinateTimeZoneResolver.resolve(geo.latitude, geo.longitude)
                if (timeZone == null) {
                    messages.value = Message.Error("gps_failed")
                    return@launch
                }
                persistNow(
                    SelectedLocation(
                        name = resolveRegionName(geo.latitude, geo.longitude),
                        latitude = geo.latitude,
                        longitude = geo.longitude,
                        timeZone = timeZone,
                        elevation = geo.altitude ?: 0.0,
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                // GPS, reverse geocoding, persistence and rescheduling are all
                // best-effort app operations. A platform or provider failure
                // must become the existing recoverable GPS message, never an FC.
                messages.value = Message.Error("gps_failed")
            }
        }
    }

    /**
     * Resolves a display name for GPS coordinates so the home screen shows
     * the area instead of "gps": online reverse-geocode first, then the
     * nearest offline city, then a coordinate label.
     */
    private suspend fun resolveRegionName(latitude: Double, longitude: Double): String {
        regionNameResolver.resolve(latitude, longitude)?.let { return it }
        nearestCity(latitude, longitude)?.let { return it }
        return "$latitude, $longitude"
    }

    /** Returns the nearest offline city's display name within ~150 km, else null. */
    private fun nearestCity(latitude: Double, longitude: Double): String? {
        var best: City? = null
        var bestDistanceSq = Double.MAX_VALUE
        for (city in CitiesRepository.all) {
            val dLat = city.latitude - latitude
            val dLon = city.longitude - longitude
            val d = dLat * dLat + dLon * dLon
            if (d < bestDistanceSq) {
                bestDistanceSq = d
                best = city
            }
        }
        // ~1.35 degrees ~= 150 km; beyond that the match would be misleading.
        val thresholdSq = 1.35 * 1.35
        return best?.takeIf { bestDistanceSq <= thresholdSq }?.displayName
    }

    fun gpsDenied() {
        messages.value = Message.Error("gps_denied")
    }

    fun consumeMessage() {
        messages.value = null
    }

    private fun persist(location: SelectedLocation) {
        viewModelScope.launch { persistNow(location) }
    }

    /**
     * Persists location without mutating the calculation method. MWL remains
     * the global baseline until the user deliberately chooses another method.
     */
    private suspend fun persistNow(location: SelectedLocation) {
        val settings = repository.settings.first().copy(location = location)
        repository.save(settings)
        scheduler.schedule(settings)
        // The countdown notification must reflect the new location.
        NextAdhanService.start(context)
        // The home-screen widget shows the next prayer for this location.
        PrayerTimesWidget().updateAll(context)
        messages.value = Message.Saved
    }
}
