package org.muslim.app.feature.qibla.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.muslim.app.core.ui.map.MapController
import org.muslim.app.core.ui.map.MapMarker
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addMosqueMarkers
import org.muslim.app.core.ui.map.addPinMarker
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.data.Mosque
import org.muslim.app.feature.qibla.data.MosqueFinderRepository
import org.muslim.app.feature.qibla.data.MosqueResultsCache
import org.muslim.app.feature.qibla.data.MosqueSearchSnapshot
import javax.inject.Inject

@HiltViewModel
class MosqueFinderViewModel @Inject constructor(
    private val repository: MosqueFinderRepository,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val cache = MosqueResultsCache(context)

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
    var showingCachedResults by mutableStateOf(false)
        private set
    var cachedResultsTimestamp by mutableStateOf<Long?>(null)
        private set

    private fun publishFreshResults(latitude: Double, longitude: Double, results: List<Mosque>) {
        mosques = results
        selectedMosque = results.minByOrNull { it.distanceMeters }
        showingCachedResults = false
        cachedResultsTimestamp = null
        if (results.isNotEmpty()) {
            cache.save(
                MosqueSearchSnapshot(
                    latitude = latitude,
                    longitude = longitude,
                    mosques = results,
                    savedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun restoreLastResults(): Boolean {
        val snapshot = cache.load() ?: return false
        if (snapshot.mosques.isEmpty()) return false
        mosques = snapshot.mosques.sortedBy { it.distanceMeters }
        selectedMosque = mosques.minByOrNull { it.distanceMeters }
        showingCachedResults = true
        cachedResultsTimestamp = snapshot.savedAtEpochMillis
        return true
    }

    fun search(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            loading = true
            error = null
            expandingRadiusKm = null
            runCatching { repository.nearby(latitude, longitude) }
                .onSuccess { publishFreshResults(latitude, longitude, it) }
                .onFailure {
                    error = it.message
                    // Keep the last successful results visible instead of
                    // replacing a useful map with an empty error state.
                    restoreLastResults()
                }
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
                .onSuccess { publishFreshResults(latitude, longitude, it) }
                .onFailure {
                    error = it.message
                    restoreLastResults()
                }
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

    val context = LocalContext.current

    // Text filter: searches the already-loaded results by mosque name.
    var nameQuery by rememberSaveable { mutableStateOf("") }
    val filteredMosques = remember(viewModel.mosques, nameQuery) {
        val q = nameQuery.trim()
        if (q.isEmpty()) {
            viewModel.mosques
        } else {
            viewModel.mosques.filter { it.name.contains(q, ignoreCase = true) }
        }
    }

    // The bottom list is scrollable so a tapped marker can bring its card
    // into view and highlight it.
    val listState = rememberLazyListState()

    // Set when a MAP MARKER is tapped (not when the nearest/auto selection
    // fires): drives the auto-scroll to the matching card in the list.
    var pendingScrollMosqueId by remember { mutableStateOf<String?>(null) }

    // Shared map handle (zoom / fly-to / fit-bounds from the search bar).
    val mapController = remember { MapController() }

    // Absolute index of the first mosque card inside the LazyColumn
    // (intro + search + map come before it; loading/error cards shift it).
    val mosqueListOffset =
        (if (latitude != null && longitude != null) 4 else 2) +
            (if (viewModel.loading) 1 else 0) +
            (if (viewModel.error != null) 1 else 0)

    LaunchedEffect(pendingScrollMosqueId, filteredMosques) {
        val id = pendingScrollMosqueId ?: return@LaunchedEffect
        val index = filteredMosques.indexOfFirst { "${it.latitude}_${it.longitude}" == id }
        if (index >= 0) listState.animateScrollToItem(mosqueListOffset + index)
        pendingScrollMosqueId = null
    }

    // Fits the camera so every current result (plus the user pin) is visible.
    fun fitAllResults() {
        val points = buildList {
            if (latitude != null && longitude != null) {
                add(LatLng(latitude, longitude))
            }
            viewModel.mosques.forEach { add(LatLng(it.latitude, it.longitude)) }
        }
        if (points.isEmpty()) return
        val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
        mapController.animateToBounds(bounds)
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
                    // Text search: filters the already-loaded results by name.
                    DigitNormalizedOutlinedTextField(
                        value = nameQuery,
                        onValueChange = { nameQuery = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(stringResource(R.string.mosque_finder_search_hint))
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                    )
                }
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
                        // "Show all" fits the camera to every current result.
                        if (viewModel.mosques.isNotEmpty()) {
                            TextButton(
                                onClick = { fitAllResults() },
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
                                Text(stringResource(R.string.mosque_finder_show_all))
                            }
                        }
                        nearest?.let { mosque ->
                            TextButton(
                                onClick = {
                                    mapController.animateTo(LatLng(mosque.latitude, mosque.longitude), 15.0)
                                    viewModel.selectMosque(mosque)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
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
                                    IconButton(onClick = {
                                        val label = Uri.encode(mosque.name)
                                        val uri = "geo:${mosque.latitude},${mosque.longitude}?q=${mosque.latitude},${mosque.longitude}($label)".toUri()
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = stringResource(R.string.mosque_finder_share),
                                        )
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
                            if (viewModel.showingCachedResults) {
                                Text(
                                    text = stringResource(R.string.mosque_finder_cached_results),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
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
            itemsIndexed(filteredMosques, key = { _, mosque -> mosque.latitude.toString() + mosque.longitude }) { _, mosque ->
                // The card matching the selected mosque is outlined so a
                // tapped marker is unmistakably tied to its list entry.
                val highlighted = viewModel.selectedMosque == mosque
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectMosque(mosque)
                            mapController.animateTo(LatLng(mosque.latitude, mosque.longitude), 16.0)
                        },
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
            if (!viewModel.loading && filteredMosques.isEmpty() && nameQuery.isNotBlank()) {
                item {
                    Text(
                        stringResource(R.string.mosque_finder_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatDistance(meters: Int): String =
    if (meters >= 1000) "%.1f km".format(meters / 1000.0) else "$meters m"
