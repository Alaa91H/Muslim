package org.muslim.app.feature.qibla.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.muslim.app.core.location.LocationProvider
import javax.inject.Inject

/** Result of a live GPS refresh on the qibla screen. */
sealed interface QiblaGpsState {
    data object Idle : QiblaGpsState
    data object Requesting : QiblaGpsState
    data class Fix(val latitude: Double, val longitude: Double) : QiblaGpsState
    data object Error : QiblaGpsState
}

/**
 * Fetches a fresh GPS fix for the qibla screen (the caller checks the
 * location permission and only invokes [refresh] when granted).
 *
 * Sensors and GPS run only while the screen is open — the fix is requested
 * on demand, never polled in the background (battery principle).
 */
@HiltViewModel
class QiblaGpsViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _gpsState = MutableStateFlow<QiblaGpsState>(QiblaGpsState.Idle)
    val gpsState: StateFlow<QiblaGpsState> = _gpsState.asStateFlow()

    fun refresh() {
        if (_gpsState.value == QiblaGpsState.Requesting) return
        _gpsState.value = QiblaGpsState.Requesting
        viewModelScope.launch {
            val geo = locationProvider.currentLocation()
            _gpsState.value = if (geo == null) {
                QiblaGpsState.Error
            } else {
                QiblaGpsState.Fix(geo.latitude, geo.longitude)
            }
        }
    }

    fun reset() {
        _gpsState.value = QiblaGpsState.Idle
    }
}