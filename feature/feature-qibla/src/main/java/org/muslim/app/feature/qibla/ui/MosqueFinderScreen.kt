package org.muslim.app.feature.qibla.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.muslim.app.core.ui.map.MapController
import org.muslim.app.core.ui.map.MapMarker
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addMosqueMarkers
import org.muslim.app.core.ui.map.addPinMarker
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.data.Mosque
import org.muslim.app.feature.qibla.data.MosqueFinderRepository
import javax.inject.Inject

@HiltViewModel
class MosqueFinderViewModel @Inject constructor(
    private val repository: MosqueFinderRepository,
) : ViewModel() {

    var mosques by mutableStateOf<List<Mosque>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedMosque by mutableStateOf<Mosque?>(null)
        private set
    var expandingRadiusKm by mutableStateOf<Int?>(null)
        private set

    fun search(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            loading = true
            error = null
            expandingRadiusKm = null
            runCatching { repository.nearby(latitude, longitude) }
                .onSuccess {
                    mosques = it
                    // Surface the nearest mosque immediately so the user never
                    // has to hunt for it in the list.
                    selectedMosque = it.minByOrNull { mosque -> mosque.distanceMeters }
                }
                .onFailure { error = it.message }
            loading = false
        }
    }

    /** Finds the nearest mosque by widening the radius until one is found. */
    fun searchNearest(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            loading = true
            error = null
            expandingRadiusKm = 1
            runCatching {
                repository.nearbyNearest(latitude, longitude) { radiusKm ->
                    expandingRadiusKm = radiusKm
                }
            }
                .onSuccess {
                    mosques = it
                    selectedMosque = it.minByOrNull { mosque -> mosque.distanceMeters }
                }
                .onFailure { error = it.message }
            expandingRadiusKm = null
            loading = false
        }
    }

    fun selectMosque(mosque: Mosque?) {
        selectedMosque = mosque
    }
}

/**
 * Nearby mosques from OpenStreetMap (Overpass, no API key — §12). Needs
 * connectivity for the search itself; results are plain local data after.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueFinderScreen(
    latitude: Double?,
    longitude: Double?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MosqueFinderViewModel = hiltViewModel(),
) {
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            viewModel.search(latitude, longitude)
        }
    }

    // The bottom list is scrollable so a tapped marker can bring its card
    // into view and highlight it.
    val listState = rememberLazyListState()

    // Set when a MAP MARKER is tapped (not when the nearest/auto selection
    // fires): drives the auto-scroll to the matching card in the list.
    var pendingScrollMosqueId by remember { mutableStateOf<String?>(null) }

    // Absolute index of the first mosque card inside the LazyColumn
    // (intro + map come before it; loading/error cards shift it).
    val mosqueListOffset =
        2 + (if (viewModel.loading) 1 else 0) + (if (viewModel.error != null) 1 else 0)

    LaunchedEffect(pendingScrollMosqueId, viewModel.mosques) {
        val id = pendingScrollMosqueId ?: return@LaunchedEffect
        val index = viewModel.mosques.indexOfFirst { "${it.latitude}_${it.longitude}" == id }
        if (index >= 0) listState.animateScrollToItem(mosqueListOffset + index)
        pendingScrollMosqueId = null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mosque_finder_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.mosque_finder_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (latitude != null && longitude != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.searchNearest(latitude, longitude) },
                            enabled = !viewModel.loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = viewModel.expandingRadiusKm?.let {
                                    stringResource(
                                        R.string.mosque_finder_expanding,
                                        formatDistance(it * 1000),
                                    )
                                } ?: stringResource(R.string.mosque_finder_find_nearest),
                            )
                        }
                    }
                }
                item {
                    val mapController = remember { MapController() }
                    val nearest = viewModel.mosques.minByOrNull { it.distanceMeters }
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        OsmMapView(
                            modifier = Modifier.fillMaxSize(),
                            initialCamera = CameraPosition.Builder()
                                .target(LatLng(latitude, longitude))
                                .zoom(13.0)
                                .build(),
                            key = viewModel.mosques,
                            controller = mapController,
                            onMapReady = { map ->
                                map.addPinMarker("user", LatLng(latitude, longitude), "#1E88E5")
                                map.addMosqueMarkers(
                                    viewModel.mosques.map { mosque ->
                                        MapMarker(
                                            id = "${mosque.latitude}_${mosque.longitude}",
                                            point = LatLng(mosque.latitude, mosque.longitude),
                                            name = mosque.name,
                                            distanceMeters = mosque.distanceMeters,
                                        )
                                    },
                                )
                            },
                            symbolLayerIds = listOf("mosque-markers"),
                            onSymbolClick = { feature ->
                                val markerId = feature.getStringProperty("markerId")
                                val mosque = viewModel.mosques.firstOrNull {
                                    "${it.latitude}_${it.longitude}" == markerId
                                }
                                viewModel.selectMosque(mosque)
                                // Marker tap → highlight + auto-scroll to the
                                // matching card in the bottom list.
                                pendingScrollMosqueId = markerId
                            },
                            onMapClick = { viewModel.selectMosque(null) },
                        )
                        // Zoom controls + refresh (map stays fully usable in
                        // scrollable screens via explicit buttons).
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalIconButton(onClick = { mapController.zoomIn() }) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.mosque_finder_zoom_in),
                                )
                            }
                            FilledTonalIconButton(onClick = { mapController.zoomOut() }) {
                                Icon(
                                    Icons.Filled.Remove,
                                    contentDescription = stringResource(R.string.mosque_finder_zoom_out),
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { viewModel.search(latitude, longitude) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = stringResource(R.string.mosque_finder_retry),
                            )
                        }
                        nearest?.let { mosque ->
                            TextButton(
                                onClick = {
                                    mapController.animateTo(LatLng(mosque.latitude, mosque.longitude), 15.0)
                                    viewModel.selectMosque(mosque)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.mosque_finder_nearest))
                            }
                        }
                        viewModel.selectedMosque?.let { mosque ->
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                    ) {
                                        Text(
                                            mosque.name,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            stringResource(
                                                R.string.mosque_finder_distance,
                                                formatDistance(mosque.distanceMeters),
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    TextButton(onClick = {
                                        mapController.animateTo(
                                            LatLng(mosque.latitude, mosque.longitude),
                                            16.0,
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.mosque_finder_go_to))
                                    }
                                    IconButton(onClick = { viewModel.selectMosque(null) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.mosque_finder_close),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (latitude == null || longitude == null) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.mosque_finder_no_location),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            if (viewModel.loading) {
                item { Text(stringResource(R.string.mosque_finder_searching)) }
            }
            viewModel.error?.let { message ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.mosque_finder_error),
                                color = MaterialTheme.colorScheme.error,
                            )
                            TextButton(onClick = {
                                if (latitude != null && longitude != null) {
                                    viewModel.search(latitude, longitude)
                                }
                            }) {
                                Text(stringResource(R.string.mosque_finder_retry))
                            }
                            Text(
                                message,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            itemsIndexed(viewModel.mosques, key = { _, mosque -> mosque.latitude.toString() + mosque.longitude }) { _, mosque ->
                // The card matching the selected mosque is outlined so a
                // tapped marker is unmistakably tied to its list entry.
                val highlighted = viewModel.selectedMosque == mosque
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = if (highlighted) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        ) {
                            Text(mosque.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(
                                    R.string.mosque_finder_distance,
                                    formatDistance(mosque.distanceMeters),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "${mosque.bearingFromUser.toInt()}°",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (!viewModel.loading && viewModel.mosques.isEmpty() && viewModel.error == null && latitude != null) {
                item { Text(stringResource(R.string.mosque_finder_empty)) }
            }
        }
    }
}

private fun formatDistance(meters: Int): String =
    if (meters >= 1000) "%.1f km".format(meters / 1000.0) else "$meters m"
