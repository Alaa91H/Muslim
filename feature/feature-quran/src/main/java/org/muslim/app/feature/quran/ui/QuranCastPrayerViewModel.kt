package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.muslim.app.feature.quran.domain.CastPrayerLocation
import org.muslim.app.feature.quran.domain.CastPrayerTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class QuranCastPrayerViewModel @Inject constructor(
    settingsRepository: PrayerSettingsRepository,
    calculator: PrayerTimesCalculator,
) : ViewModel() {
    val snapshot: StateFlow<CastPrayerSnapshot?> = settingsRepository.settings
        .map { settings ->
            val location = settings.location ?: return@map null
            val zone = runCatching { ZoneId.of(location.timeZone) }.getOrNull() ?: return@map null
            val result = calculator.compute(
                date = LocalDate.now(zone),
                coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
                profile = settings.toPrayerCalculationProfile(),
                timeZone = zone,
            )
            CastPrayerSnapshot(
                location = CastPrayerLocation(location.name, countryCode = "", timeZoneId = location.timeZone),
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
