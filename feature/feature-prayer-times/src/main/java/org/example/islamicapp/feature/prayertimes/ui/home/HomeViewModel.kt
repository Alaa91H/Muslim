package org.example.islamicapp.feature.prayertimes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.core.common.time.HijriDate
import org.example.islamicapp.feature.prayertimes.data.PrayerSettings
import org.example.islamicapp.feature.prayertimes.data.PrayerSettingsRepository
import org.example.islamicapp.feature.prayertimes.data.toPrayerParameters
import org.example.islamicapp.feature.prayertimes.domain.Coordinates
import org.example.islamicapp.feature.prayertimes.domain.NextPrayer
import org.example.islamicapp.feature.prayertimes.domain.Prayer
import org.example.islamicapp.feature.prayertimes.domain.PrayerTimesCalculator
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerTimesCalculator,
) : ViewModel() {

    data class UiState(
        val hasLocation: Boolean = false,
        val locationName: String = "",
        val hijri: HijriDate? = null,
        val nextPrayer: Prayer? = null,
        val nextPrayerAt: LocalTime? = null,
        val countdownSeconds: Long = 0,
        val times: Map<Prayer, LocalTime> = emptyMap(),
        val isValid: Boolean = false,
    )

    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    val uiState: StateFlow<UiState> =
        combine(settingsRepository.settings, clock) { settings, now -> compute(settings, now) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private fun compute(settings: PrayerSettings, now: Long): UiState {
        val location = settings.location ?: return UiState()
        val zone = ZoneId.of(location.timeZone)
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val coordinates = Coordinates(location.latitude, location.longitude)
        val params = settings.toPrayerParameters()

        val result = calculator.compute(today, coordinates, params, zone, settings.asrMethod, settings.adjustments)

        var next = NextPrayer.nextPrayer(result.epochMillis, now)
        if (next == null) {
            val tomorrowResult = calculator.compute(
                today.plusDays(1), coordinates, params, zone, settings.asrMethod, settings.adjustments,
            )
            if (tomorrowResult.isValid) next = NextPrayer.nextPrayer(tomorrowResult.epochMillis, now)
        }

        return UiState(
            hasLocation = true,
            locationName = location.name,
            hijri = HijriDate.from(today, settings.hijriAdjustment),
            nextPrayer = next?.prayer,
            nextPrayerAt = next?.atEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
            countdownSeconds = next?.let { NextPrayer.countdownSeconds(it.atEpochMillis, now) } ?: 0,
            times = result.times,
            isValid = result.isValid,
        )
    }
}
