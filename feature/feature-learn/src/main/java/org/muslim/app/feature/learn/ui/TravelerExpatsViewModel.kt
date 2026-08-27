package org.muslim.app.feature.learn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.feature.learn.data.TravelOriginRepository
import org.muslim.app.feature.learn.domain.TravelContent
import org.muslim.app.feature.learn.domain.TravelDistanceAssessment
import org.muslim.app.feature.learn.domain.TravelDistanceThreshold
import org.muslim.app.feature.learn.domain.TravelPoint
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

sealed interface TravelerGpsState {
    data object Idle : TravelerGpsState
    data object Requesting : TravelerGpsState
    data class Fix(val point: TravelPoint) : TravelerGpsState
    data object Error : TravelerGpsState
}

data class HighLatitudePreview(
    val latitude: Double,
    val rule: HighLatitudeRule,
    val fajr: String?,
    val isha: String?,
    val calculationAvailable: Boolean,
)

data class TravelerUiState(
    val origin: TravelPoint? = null,
    val gpsState: TravelerGpsState = TravelerGpsState.Idle,
    val threshold: TravelDistanceThreshold = TravelDistanceThreshold.EIGHTY,
    val distanceAssessment: TravelDistanceAssessment? = null,
    val highLatitudePreview: HighLatitudePreview? = null,
)

/** Coordinates one user-requested GPS fix; it never runs tracking in background. */
@HiltViewModel
class TravelerExpatsViewModel @Inject constructor(
    private val originRepository: TravelOriginRepository,
    private val locationProvider: LocationProvider,
    private val prayerSettingsRepository: PrayerSettingsRepository,
    private val prayerTimesCalculator: PrayerTimesCalculator,
) : ViewModel() {
    private val gpsState = MutableStateFlow<TravelerGpsState>(TravelerGpsState.Idle)
    private val threshold = MutableStateFlow(TravelDistanceThreshold.EIGHTY)

    val uiState: StateFlow<TravelerUiState> = combine(
        originRepository.origin,
        gpsState,
        threshold,
        prayerSettingsRepository.settings,
    ) { origin, gps, selectedThreshold, settings ->
        TravelerUiState(
            origin = origin,
            gpsState = gps,
            threshold = selectedThreshold,
            distanceAssessment = assessmentFor(origin, gps, selectedThreshold),
            highLatitudePreview = highLatitudePreview(settings),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TravelerUiState())

    fun refreshGps() {
        if (gpsState.value == TravelerGpsState.Requesting) return
        gpsState.value = TravelerGpsState.Requesting
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            gpsState.value = if (location == null) {
                TravelerGpsState.Error
            } else {
                TravelerGpsState.Fix(TravelPoint(location.latitude, location.longitude))
            }
        }
    }

    fun setCurrentAsOrigin() {
        val current = (gpsState.value as? TravelerGpsState.Fix)?.point ?: return
        viewModelScope.launch { originRepository.save(current) }
    }

    fun clearOrigin() {
        viewModelScope.launch { originRepository.clear() }
    }

    fun selectThreshold(value: TravelDistanceThreshold) {
        threshold.value = value
    }

    private fun assessmentFor(
        origin: TravelPoint?,
        gps: TravelerGpsState,
        selectedThreshold: TravelDistanceThreshold,
    ): TravelDistanceAssessment? {
        val current = (gps as? TravelerGpsState.Fix)?.point ?: return null
        return origin?.let { TravelContent.assessDistance(it, current, selectedThreshold) }
    }

    private fun highLatitudePreview(settings: PrayerSettings): HighLatitudePreview? {
        val location = settings.location ?: return null
        val zone = runCatching { ZoneId.of(location.timeZone) }.getOrElse { return null }
        val result = prayerTimesCalculator.compute(
            date = LocalDate.now(zone),
            coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
            profile = settings.toPrayerCalculationProfile(),
            timeZone = zone,
        )
        val rule = settings.highLatitudeRule
        return HighLatitudePreview(
            latitude = location.latitude,
            rule = rule,
            fajr = result.timeFor(Prayer.Fajr)?.toString(),
            isha = result.timeFor(Prayer.Isha)?.toString(),
            calculationAvailable = result.isValid,
        )
    }

}
