package org.muslim.app.feature.qibla.mosques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.location.GeoLocation
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.feature.qibla.data.NearbyMosque
import org.muslim.app.feature.qibla.data.NearbyMosqueRepository
import org.muslim.app.feature.qibla.data.NearbyMosqueRadiusOptionsKm
import org.muslim.app.feature.qibla.data.NearbyMosqueCache
import org.muslim.app.feature.qibla.data.MosquePlace
import javax.inject.Inject

/** Explicit presentation states for the on-demand nearby-mosque tab. */
sealed interface NearbyMosquesUiState {
    data object Idle : NearbyMosquesUiState
    /** Cached place names may be rendered while obtaining a new location; they have no stale distances. */
    data class LoadingLocation(val cachedPlaces: List<MosquePlace> = emptyList()) : NearbyMosquesUiState
    data class LoadingMosques(val cachedMosques: List<NearbyMosque>) : NearbyMosquesUiState
    data class Success(val mosques: List<NearbyMosque>) : NearbyMosquesUiState
    data object Empty : NearbyMosquesUiState
    data object Error : NearbyMosquesUiState
    data class OfflineCache(val mosques: List<NearbyMosque>) : NearbyMosquesUiState
    data object PermissionDenied : NearbyMosquesUiState
    data object LocationUnavailable : NearbyMosquesUiState
}

/** UI model that does not expose exact user coordinates. */
data class NearbyMosquesPresentation(
    val state: NearbyMosquesUiState = NearbyMosquesUiState.Idle,
    val radiusKm: Int = 5,
)

/**
 * Uses the same injected [LocationProvider] as the Qibla compass. It runs only
 * while the Mosques tab is active, never starts permanent tracking, and keeps
 * raw cached places while recalculating all displayed distances from a new fix.
 */
@HiltViewModel
class NearbyMosquesViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val mosqueRepository: NearbyMosqueRepository,
    private val preferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    private val _presentation = MutableStateFlow(NearbyMosquesPresentation())
    val presentation: StateFlow<NearbyMosquesPresentation> = _presentation.asStateFlow()

    private var activeSearch: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { preferences ->
                _presentation.value = _presentation.value.copy(radiusKm = preferences.nearbyMosqueSearchRadiusKm)
            }
        }
    }

    /** Invoked only when the Mosques tab becomes selected. */
    fun activate() {
        if (_presentation.value.state == NearbyMosquesUiState.Idle) refresh()
    }

    /** Cancels in-flight work when the tab is no longer visible but keeps rendered cache data. */
    fun deactivate() {
        activeSearch?.cancel()
        activeSearch = null
    }

    /** Called by the shared permission launcher when the current location permission is declined. */
    fun onPermissionDenied() {
        activeSearch?.cancel()
        _presentation.value = _presentation.value.copy(state = NearbyMosquesUiState.PermissionDenied)
    }

    /** An explicit retry/refresh. The repository decides whether a fresh cache avoids a network request. */
    fun refresh() {
        activeSearch?.cancel()
        activeSearch = viewModelScope.launch {
            val preferences = preferencesRepository.preferences.first()
            val radiusKm = preferences.nearbyMosqueSearchRadiusKm
            val cache = mosqueRepository.cacheFrom(preferences)
            _presentation.value = _presentation.value.copy(
                radiusKm = radiusKm,
                state = NearbyMosquesUiState.LoadingLocation(cache?.places.orEmpty()),
            )
            val location = locationProvider.currentLocation()
            if (location == null) {
                _presentation.value = _presentation.value.copy(state = NearbyMosquesUiState.LocationUnavailable)
                return@launch
            }
            loadForLocation(location, preferences, cache)
        }
    }

    fun selectRadius(radiusKm: Int) {
        if (radiusKm !in NearbyMosqueRadiusOptionsKm) return
        viewModelScope.launch {
            mosqueRepository.setRadius(radiusKm)
            refresh()
        }
    }

    private suspend fun loadForLocation(
        location: GeoLocation,
        preferences: org.muslim.app.core.datastore.AppPreferences,
        cache: NearbyMosqueCache?,
    ) {
        val radiusKm = preferences.nearbyMosqueSearchRadiusKm
        val cachedMosques = cache?.let { mosqueRepository.nearbyFor(location, it.places, radiusKm) }.orEmpty()
        val cacheCoversSearch = cache != null && cache.sourceRadiusKm >= radiusKm
        val currentPositionNearCache = cache != null &&
            mosqueRepository.distanceMeters(location, cache.asGeoLocation()) <= radiusKm * 1_000.0
        val canUseFreshCache = cacheCoversSearch && currentPositionNearCache &&
            mosqueRepository.isFresh(preferences, System.currentTimeMillis())

        if (canUseFreshCache) {
            publishResults(cachedMosques)
            return
        }

        _presentation.value = _presentation.value.copy(
            state = NearbyMosquesUiState.LoadingMosques(cachedMosques),
        )
        val networkPlaces = try {
            mosqueRepository.searchAndCache(location, radiusKm, System.currentTimeMillis())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            if (cachedMosques.isNotEmpty()) {
                _presentation.value = _presentation.value.copy(state = NearbyMosquesUiState.OfflineCache(cachedMosques))
            } else {
                _presentation.value = _presentation.value.copy(state = NearbyMosquesUiState.Error)
            }
            return
        }
        publishResults(mosqueRepository.nearbyFor(location, networkPlaces, radiusKm))
    }

    private fun publishResults(mosques: List<NearbyMosque>) {
        _presentation.value = _presentation.value.copy(
            state = if (mosques.isEmpty()) NearbyMosquesUiState.Empty else NearbyMosquesUiState.Success(mosques),
        )
    }
}

private fun org.muslim.app.feature.qibla.data.NearbyMosqueCache.asGeoLocation(): GeoLocation =
    GeoLocation(sourceLatitude, sourceLongitude)
