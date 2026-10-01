package org.muslim.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.muslim.app.feature.quran.domain.CastPrayerLocation
import org.muslim.app.feature.quran.domain.CastPrayerTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** App-level state needed by the navigation shell (location + app preferences). */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: PrayerSettingsRepository,
    appPreferencesRepository: AppPreferencesRepository,
    calculator: PrayerTimesCalculator,
) : ViewModel() {

    val location: StateFlow<SelectedLocation?> = settingsRepository.settings
        .map { it.location }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** User-adjusted Hijri date offset shared with the Ramadan feature and navigation. */
    val hijriAdjustment: StateFlow<Int> = settingsRepository.settings
        .map { it.hijriAdjustment }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Drives the app theme (light/dark/system + dynamic color). */
    val appPreferences: StateFlow<AppPreferences> = appPreferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    val castPrayerSnapshot: StateFlow<CastPrayerSnapshot?> = settingsRepository.settings
        .map { settings ->
            val location = settings.location ?: return@map null
            val zone = ZoneId.of(location.timeZone)
            val result = calculator.compute(
                date = LocalDate.now(zone),
                coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
                profile = settings.toPrayerCalculationProfile(),
                timeZone = zone,
            )
            CastPrayerSnapshot(
                location = CastPrayerLocation(
                    city = location.name,
                    countryCode = "",
                    timeZoneId = location.timeZone,
                ),
                times = result.times
                    .filterKeys { it != Prayer.Sunrise }
                    .map { (prayer, time) ->
                        CastPrayerTime(prayer.name, time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

data class CastPrayerSnapshot(
    val location: CastPrayerLocation,
    val times: List<CastPrayerTime>,
)
