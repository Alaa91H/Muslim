package org.muslim.app.feature.prayertimes.ui.location

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.feature.prayertimes.data.CitiesRepository
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.feature.prayertimes.domain.City
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler
import org.muslim.app.feature.prayertimes.notifications.NextAdhanService
import org.muslim.app.feature.prayertimes.widget.PrayerTimesWidget
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerSettingsRepository,
    private val locationProvider: LocationProvider,
    private val scheduler: AdhanScheduler,
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

    /** Saves a city from the offline database. */
    fun selectCity(city: City) {
        save(
            SelectedLocation(name = city.displayName, latitude = city.latitude, longitude = city.longitude, timeZone = city.timeZone),
            regionHint = city.country,
        )
    }

    /** Validates and saves manually entered coordinates. Returns true on success. */
    fun saveManual(latitudeText: String, longitudeText: String): Boolean {
        val latitude = latitudeText.trim().toDoubleOrNull()
        val longitude = longitudeText.trim().toDoubleOrNull()
        if (latitude == null || longitude == null || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            messages.value = Message.Error("invalid")
            return false
        }
        save(
            SelectedLocation(
                name = "$latitude, $longitude",
                latitude = latitude,
                longitude = longitude,
                timeZone = TimeZone.getDefault().id,
            )
        )
        return true
    }

    /** Fetches the current GPS location (caller must hold the permission). */
    fun useGps() {
        viewModelScope.launch {
            val geo = locationProvider.currentLocation()
            if (geo == null) {
                messages.value = Message.Error("gps_failed")
            } else {
                save(
                    SelectedLocation(
                        name = "gps",
                        latitude = geo.latitude,
                        longitude = geo.longitude,
                        timeZone = TimeZone.getDefault().id,
                    )
                )
            }
        }
    }

    fun gpsDenied() {
        messages.value = Message.Error("gps_denied")
    }

    fun consumeMessage() {
        messages.value = null
    }

    private fun save(location: SelectedLocation, regionHint: String? = null) {
        viewModelScope.launch {
            val current = repository.settings.first()
            // First-time region default: adopt the officially used method of
            // the chosen country until the user customizes it themselves.
            val settings = if (regionHint != null && !current.methodChosenManually) {
                current.copy(
                    location = location,
                    method = CalculationMethod.suggestedFor(regionHint),
                )
            } else {
                current.copy(location = location)
            }
            repository.save(settings)
            scheduler.schedule(settings)
            // The countdown notification must reflect the new location.
            NextAdhanService.start(context)
            // The home-screen widget shows the next prayer for this location.
            PrayerTimesWidget().updateAll(context)
            messages.value = Message.Saved
        }
    }
}
