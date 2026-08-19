package org.muslim.app.feature.qibla.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLngBounds
import org.muslim.app.core.ui.map.OfflineMapArea
import org.muslim.app.core.ui.map.OfflineMapAreas
import org.muslim.app.core.ui.map.OfflineMapManager
import org.muslim.app.core.ui.map.OfflineMapRegion
import javax.inject.Inject

/** UI state for the offline maps screen. */
data class OfflineMapsUiState(
    val regions: List<OfflineMapRegion> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val downloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadName: String? = null,
) {
    val totalBytes: Long get() = regions.sumOf { it.downloadedBytes }
    val completeCount: Int get() = regions.count { it.complete }
}

@HiltViewModel
class OfflineMapsViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val manager = OfflineMapManager(context)

    private val _state = MutableStateFlow(OfflineMapsUiState())
    val state: StateFlow<OfflineMapsUiState> = _state

    val cities: List<OfflineMapArea> = OfflineMapAreas.CITIES
    val countries: List<OfflineMapArea> = OfflineMapAreas.COUNTRIES

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            manager.listRegions { regions ->
                _state.value = _state.value.copy(regions = regions, loading = false)
            }
        }
    }

    /** Estimates the download size for an area, for the pre-download display. */
    fun estimateBytes(area: OfflineMapArea): Long = manager.estimateBytes(area.bounds)

    /** Formats a byte count as a human size (KB/MB/GB). */
    fun formatBytes(bytes: Long): String = manager.formatBytes(bytes)

    /** Starts downloading a preset area. */
    fun download(area: OfflineMapArea) {
        if (_state.value.downloading) return
        _state.value = _state.value.copy(downloading = true, downloadName = area.name, downloadProgress = 0f, error = null)
        manager.download(
            name = area.name,
            kind = area.kind,
            bounds = area.bounds,
            onProgress = { region ->
                _state.value = _state.value.copy(downloadProgress = region.progress)
            },
            onResult = { result ->
                when (result) {
                    is org.muslim.app.core.ui.map.OfflineMapDownloadResult.Started -> Unit
                    is org.muslim.app.core.ui.map.OfflineMapDownloadResult.Error ->
                        _state.value = _state.value.copy(downloading = false, error = result.message)
                }
                refresh()
            },
        )
    }

    /** Starts downloading a custom bounding box (e.g. from the map picker). */
    fun downloadCustom(name: String, bounds: LatLngBounds) {
        if (_state.value.downloading) return
        _state.value = _state.value.copy(downloading = true, downloadName = name, downloadProgress = 0f, error = null)
        manager.download(
            name = name,
            kind = "place",
            bounds = bounds,
            onProgress = { region ->
                _state.value = _state.value.copy(downloadProgress = region.progress)
            },
            onResult = { result ->
                when (result) {
                    is org.muslim.app.core.ui.map.OfflineMapDownloadResult.Started -> Unit
                    is org.muslim.app.core.ui.map.OfflineMapDownloadResult.Error ->
                        _state.value = _state.value.copy(downloading = false, error = result.message)
                }
                refresh()
            },
        )
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            manager.delete(id) { refresh() }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            manager.deleteAll { refresh() }
        }
    }
}
