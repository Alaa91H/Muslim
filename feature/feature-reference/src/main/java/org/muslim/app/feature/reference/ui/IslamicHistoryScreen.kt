package org.muslim.app.feature.reference.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.muslim.app.core.ui.map.MapController
import org.muslim.app.core.ui.map.MapMarker
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addPinMarkers
import org.muslim.app.core.ui.map.addPolygonOverlay
import org.muslim.app.core.ui.map.addPolyline
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.HistoryCoordinate
import org.muslim.app.feature.reference.domain.HistoryEra
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryPerson
import org.muslim.app.feature.reference.domain.HistoricalMapLayer
import org.muslim.app.feature.reference.domain.HistoricalPlace
import org.muslim.app.feature.reference.domain.IslamicHistoryContent

/** A standalone, bilingual history destination with source-aware map boundaries. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var language by remember { mutableStateOf(HistoryLanguage.Arabic) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.reference_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        language = if (language == HistoryLanguage.Arabic) {
                            HistoryLanguage.English
                        } else {
                            HistoryLanguage.Arabic
                        }
                    }) {
                        Text(
                            if (language == HistoryLanguage.Arabic) {
                                stringResource(R.string.history_language_english)
                            } else {
                                stringResource(R.string.history_language_arabic)
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            HistoryTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            when (selectedTab) {
                0 -> TimelineTab(language = language)
                1 -> AtlasTab(language = language)
                else -> PeopleTab(language = language)
            }
        }
    }
}

@Composable
private fun HistoryTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.history_timeline_tab),
        stringResource(R.string.history_atlas_tab),
        stringResource(R.string.history_people_tab),
    )
    TabRow(selectedTabIndex = selectedTab) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelect(index) },
                text = { Text(label, maxLines = 2, textAlign = TextAlign.Center) },
            )
        }
    }
}

@Composable
private fun TimelineTab(language: HistoryLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HistoryNotice(stringResource(R.string.history_timeline_intro)) }
        items(IslamicHistoryContent.timeline, key = { it.id }) { era ->
            TimelineEraCard(era = era, language = language)
        }
        item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
    }
}

@Composable
private fun TimelineEraCard(era: HistoryEra, language: HistoryLanguage) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = era.title.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = eraRange(era),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = era.summary.resolve(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = stringResource(R.string.history_era_highlights),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
            era.highlights.forEach { highlight ->
                Text(
                    text = "• ${highlight.resolve(language)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun eraRange(era: HistoryEra): String = stringResource(
    R.string.history_era_range,
    era.startCe,
    era.endCe?.toString() ?: stringResource(R.string.history_present),
)

@Composable
private fun AtlasTab(language: HistoryLanguage) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedPlace by remember { mutableStateOf<HistoricalPlace?>(null) }
    val layers = IslamicHistoryContent.atlasLayers
    val layer = layers[selectedIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        AtlasSelector(
            layers = layers,
            selectedIndex = selectedIndex,
            language = language,
            onSelect = {
                selectedIndex = it
                selectedPlace = null
            },
        )
        AtlasMap(
            layer = layer,
            language = language,
            selectedPlace = selectedPlace,
            onPlaceSelected = { selectedPlace = it },
            onDismissPlace = { selectedPlace = null },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AtlasSelector(
    layers: List<HistoricalMapLayer>,
    selectedIndex: Int,
    language: HistoryLanguage,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = stringResource(R.string.history_atlas_layer),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            layers.forEachIndexed { index, layer ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    label = { Text(layer.title.resolve(language)) },
                )
            }
        }
    }
}

@Composable
private fun AtlasMap(
    layer: HistoricalMapLayer,
    language: HistoryLanguage,
    selectedPlace: HistoricalPlace?,
    onPlaceSelected: (HistoricalPlace) -> Unit,
    onDismissPlace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = remember { MapController() }
    val placeLayerId = "history_places_${layer.id}"
    Box(modifier = modifier.fillMaxWidth()) {
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            initialCamera = CameraPosition.Builder()
                .target(layer.initialCenter.toLatLng())
                .zoom(layer.zoom)
                .build(),
            controller = controller,
            key = layer.id,
            onMapReady = { map -> addAtlasLayers(map, layer, placeLayerId) },
            symbolLayerIds = listOf(placeLayerId),
            onSymbolClick = { feature ->
                feature.getStringProperty("markerId")?.let { id ->
                    layer.places.firstOrNull { it.id == id }?.let(onPlaceSelected)
                }
            },
            onMapClick = { onDismissPlace() },
        )
        AtlasZoomButtons(controller = controller)
        AtlasLegend(layer = layer, language = language)
        selectedPlace?.let { place ->
            AtlasPlaceCard(place = place, language = language, onDismiss = onDismissPlace)
        }
    }
}

private fun addAtlasLayers(
    map: org.maplibre.android.maps.MapLibreMap,
    layer: HistoricalMapLayer,
    placeLayerId: String,
) {
    map.addPolygonOverlay(
        id = "history_area_${layer.id}",
        points = layer.schematicArea.map { it.toLatLng() },
        fillHex = "#B87333",
        borderHex = "#704214",
    )
    layer.routes.forEach { route ->
        map.addPolyline(
            id = "history_route_${layer.id}_${route.id}",
            points = route.coordinates.map { it.toLatLng() },
            colorHex = "#1E6B52",
            width = 4f,
        )
    }
    map.addPinMarkers(
        markers = layer.places.map { place ->
            MapMarker(
                id = place.id,
                point = place.coordinate.toLatLng(),
                name = place.title.english,
                distanceMeters = 0,
            )
        },
        layerId = placeLayerId,
    )
}

@Composable
private fun AtlasZoomButtons(controller: MapController) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilledTonalIconButton(onClick = controller::zoomIn) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.history_atlas_zoom_in))
        }
        FilledTonalIconButton(onClick = controller::zoomOut) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.history_atlas_zoom_out))
        }
    }
}

@Composable
private fun BoxScope.AtlasLegend(layer: HistoricalMapLayer, language: HistoryLanguage) {
    Card(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .width(220.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.history_atlas_schematic),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(layer.summary.resolve(language), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            if (layer.routes.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.history_atlas_routes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                layer.routes.forEach { route ->
                    Text("• ${route.title.resolve(language)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
                }
            }
            Text(
                text = stringResource(R.string.history_atlas_places),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun BoxScope.AtlasPlaceCard(
    place: HistoricalPlace,
    language: HistoryLanguage,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = place.title.resolve(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.history_atlas_close))
                }
            }
            Text(place.note.resolve(language), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun PeopleTab(language: HistoryLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HistoryNotice(stringResource(R.string.history_people_intro)) }
        items(IslamicHistoryContent.personalities, key = { it.id }) { person ->
            PersonCard(person = person, language = language)
        }
        item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
    }
}

@Composable
private fun PersonCard(person: HistoryPerson, language: HistoryLanguage) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = person.name.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = person.years,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = stringResource(R.string.history_people_field, person.field.resolve(language)),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
            Text(person.summary.resolve(language), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 10.dp))
            Text(
                text = stringResource(R.string.history_people_contribution),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(person.contribution.resolve(language), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun HistoryNotice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun HistoryCoordinate.toLatLng(): LatLng = LatLng(latitude, longitude)
