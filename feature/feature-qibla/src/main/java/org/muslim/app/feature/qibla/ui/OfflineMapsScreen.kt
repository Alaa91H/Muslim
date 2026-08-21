package org.muslim.app.feature.qibla.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.muslim.app.core.ui.map.MapController
import org.muslim.app.core.ui.map.OfflineMapArea
import org.muslim.app.core.ui.map.OsmMapDefaults
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addBoundsRect
import org.muslim.app.feature.qibla.R

/**
 * Offline maps screen: download map tiles for a city, a country, or a custom
 * area so the maps work with no internet. Shows per-region progress, the
 * real downloaded size, and a size estimate before downloading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Current GPS location, when known — powers the "use my location" picker. */
    latitude: Double? = null,
    longitude: Double? = null,
    viewModel: OfflineMapsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteAll by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.offline_maps_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.offline_maps_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SummaryCard(state, viewModel)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.offline_maps_add))
                    }
                }
                if (state.regions.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.offline_maps_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.offline_maps_downloaded),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { showConfirmDeleteAll = true }) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.offline_maps_delete_all))
                            }
                        }
                    }
                    items(state.regions, key = { it.id }) { region ->
                        RegionCard(region, viewModel)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAreaDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            latitude = latitude,
            longitude = longitude,
        )
    }

    if (showConfirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAll = false },
            title = { Text(stringResource(R.string.offline_maps_delete_all_confirm_title)) },
            text = { Text(stringResource(R.string.offline_maps_delete_all_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAll()
                    showConfirmDeleteAll = false
                }) { Text(stringResource(R.string.offline_maps_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteAll = false }) {
                    Text(stringResource(R.string.offline_maps_cancel))
                }
            },
        )
    }
}

@Composable
private fun SummaryCard(state: OfflineMapsUiState, viewModel: OfflineMapsViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.OfflinePin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.offline_maps_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.offline_maps_summary_body,
                    state.completeCount,
                    state.regions.size,
                    viewModel.formatBytes(state.totalBytes),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            val storage = state.storage
            if (storage != null && storage.lowOnStorage) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.offline_maps_storage_warning,
                        viewModel.formatBytes(storage.availableBytes),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                val largest = storage.largestRegion
                if (largest != null) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { viewModel.deleteLargest() }) {
                        Text(
                            text = stringResource(
                                R.string.offline_maps_storage_delete_largest,
                                largest.name,
                                viewModel.formatBytes(largest.downloadedBytes),
                            ),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            if (state.downloading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.downloadProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.offline_maps_downloading, state.downloadName ?: "", (state.downloadProgress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (state.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun RegionCard(region: org.muslim.app.core.ui.map.OfflineMapRegion, viewModel: OfflineMapsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (region.kind == "country") Icons.Filled.Public else Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.offline_maps_region_meta,
                            region.kind,
                            viewModel.formatBytes(region.downloadedBytes),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { viewModel.delete(region.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.offline_maps_delete))
                }
            }
            if (!region.complete) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { region.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.offline_maps_region_progress, (region.progress * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.offline_maps_region_ready),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AddAreaDialog(
    viewModel: OfflineMapsViewModel,
    onDismiss: () -> Unit,
    latitude: Double? = null,
    longitude: Double? = null,
) {
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    val searching = query.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.offline_maps_add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.offline_maps_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                if (searching) {
                    SearchResults(query = query, viewModel = viewModel, onDismiss = onDismiss)
                } else {
                    Row {
                        TabChip(stringResource(R.string.offline_maps_tab_city), tab == 0, Icons.Filled.Place) { tab = 0 }
                        Spacer(Modifier.width(8.dp))
                        TabChip(stringResource(R.string.offline_maps_tab_country), tab == 1, Icons.Filled.Public) { tab = 1 }
                        Spacer(Modifier.width(8.dp))
                        TabChip(stringResource(R.string.offline_maps_tab_custom), tab == 2, Icons.Filled.Map) { tab = 2 }
                    }
                    Spacer(Modifier.height(12.dp))
                    when (tab) {
                        0 -> AreaList(viewModel.cities, viewModel, onDismiss)
                        1 -> AreaList(viewModel.countries, viewModel, onDismiss)
                        else -> CustomAreaPicker(viewModel, onDismiss, latitude, longitude)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.offline_maps_cancel)) }
        },
    )
}

/**
 * Free-text search across the bundled city + country presets (by English or
 * Arabic name) so the user can jump straight to a specific area to download.
 */
@Composable
private fun SearchResults(
    query: String,
    viewModel: OfflineMapsViewModel,
    onDismiss: () -> Unit,
) {
    val needle = query.trim()
    val results = remember(needle) {
        (viewModel.cities + viewModel.countries).filter { area ->
            area.name.contains(needle, ignoreCase = true) ||
                area.nameArabic.contains(needle)
        }
    }
    if (results.isEmpty()) {
        Text(
            text = stringResource(R.string.offline_maps_search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    } else {
        AreaList(results, viewModel, onDismiss)
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AreaList(
    areas: List<OfflineMapArea>,
    viewModel: OfflineMapsViewModel,
    onDismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val arabic = configuration.locales[0]?.language == "ar"
    LazyColumn(modifier = Modifier.height(320.dp)) {
        items(areas, key = { it.name }) { area ->
            val estimate = viewModel.estimateBytes(area)
            ListItem(
                headlineContent = {
                    Column {
                        Text(if (arabic && area.nameArabic.isNotBlank()) area.nameArabic else area.name)
                        Text(
                            text = stringResource(R.string.offline_maps_size_estimate, viewModel.formatBytes(estimate)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        imageVector = if (area.kind == "country") Icons.Filled.Public else Icons.Filled.Place,
                        contentDescription = null,
                    )
                },
                trailingContent = {
                    TextButton(onClick = {
                        viewModel.download(area)
                        onDismiss()
                    }) { Text(stringResource(R.string.offline_maps_download)) }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAreaPicker(
    viewModel: OfflineMapsViewModel,
    onDismiss: () -> Unit,
    latitude: Double? = null,
    longitude: Double? = null,
) {
    var name by remember { mutableStateOf("") }
    var bounds by remember { mutableStateOf<LatLngBounds?>(null) }
    var halfSpan by remember { mutableDoubleStateOf(0.5) }
    val controller = remember { MapController() }
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }

    // Recomputes the selection box: vertical span tracks the visible viewport
    // (so the box fills the screen height at any zoom), horizontal span is the
    // user-controlled [halfSpan], and the box stays centered on the viewport.
    fun updateSelection(viewport: LatLngBounds) {
        val c = viewport.center
        val halfLat = viewport.latitudeSpan.coerceAtLeast(0.05) / 2
        val box = LatLngBounds.Builder()
            .include(LatLng(c.latitude - halfLat, c.longitude - halfSpan))
            .include(LatLng(c.latitude + halfLat, c.longitude + halfSpan))
            .build()
        bounds = box
        mapRef.value?.addBoundsRect(
            id = "picker",
            bounds = box,
            fillHex = "#1E88E5",
            borderHex = "#0D47A1",
        )
    }

    Column {
        Text(
            text = stringResource(R.string.offline_maps_custom_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.offline_maps_custom_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        ) {
            val home = if (latitude != null && longitude != null) {
                LatLng(latitude, longitude)
            } else {
                null
            }
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                initialCamera = CameraPosition.Builder()
                    .target(home ?: LatLng(24.7136, 46.6753))
                    .zoom(if (home != null) 9.0 else 8.0)
                    .build(),
                styleUri = OsmMapDefaults.STYLE_URI,
                controller = controller,
                onMapReady = { map ->
                    mapRef.value = map
                    controller.visibleBounds()?.let { updateSelection(it) }
                },
                onCameraIdle = { viewport -> updateSelection(viewport) },
            )
            // Zoom controls overlay + "use my location" button, always available.
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FloatingActionButton(
                    onClick = { controller.zoomIn() },
                    modifier = Modifier.size(40.dp),
                ) { Text("+") }
                FloatingActionButton(
                    onClick = { controller.zoomOut() },
                    modifier = Modifier.size(40.dp),
                ) { Text("−") }
                if (home != null) {
                    FloatingActionButton(
                        onClick = { controller.animateTo(home, 9.0) },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = stringResource(R.string.offline_maps_use_my_location),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Width slider (longitude span around the viewport center).
        Text(
            text = stringResource(R.string.offline_maps_custom_width),
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = halfSpan.toFloat(),
            onValueChange = {
                halfSpan = it.toDouble()
                controller.visibleBounds()?.let { viewport -> updateSelection(viewport) }
            },
            valueRange = 0.05f..8f,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = bounds?.let {
                stringResource(
                    R.string.offline_maps_custom_selected,
                    it.center.latitude,
                    it.center.longitude,
                )
            } ?: stringResource(R.string.offline_maps_custom_tap),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = bounds?.let {
                stringResource(
                    R.string.offline_maps_size_estimate,
                    viewModel.formatBytes(viewModel.estimateBoundsBytes(it)),
                )
            } ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        val defaultName = stringResource(R.string.offline_maps_custom_default_name)
        Button(
            onClick = {
                val b = bounds ?: return@Button
                val label = name.ifBlank { defaultName }
                viewModel.downloadCustom(label, b)
                onDismiss()
            },
            enabled = bounds != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.offline_maps_download))
        }
    }
}
