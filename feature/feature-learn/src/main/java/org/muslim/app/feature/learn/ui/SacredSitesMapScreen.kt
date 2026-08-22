package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.muslim.app.core.ui.map.MapController
import org.muslim.app.core.ui.map.MapMarker
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addMosqueMarkers
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.HajjLocationGuide
import org.muslim.app.feature.learn.domain.SacredSite

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun SacredSitesMapScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = remember { MapController() }
    var selected by remember { mutableStateOf<SacredSite?>(null) }
    val sites = HajjLocationGuide.locations

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hajj_map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                initialCamera = CameraPosition.Builder()
                    .target(LatLng(21.4225, 39.88))
                    .zoom(11.0)
                    .build(),
                controller = controller,
                key = sites,
                onMapReady = { map ->
                    map.addMosqueMarkers(
                        sites.map { site ->
                            MapMarker(
                                id = site.site.name,
                                point = LatLng(site.latitude, site.longitude),
                                name = site.label,
                                distanceMeters = 0,
                            )
                        },
                        layerId = "sacred-sites",
                    )
                },
                symbolLayerIds = listOf("sacred-sites"),
                onSymbolClick = { feature ->
                    selected = feature.getStringProperty("markerId")
                        ?.let { id -> sites.firstOrNull { it.site.name == id }?.site }
                },
                onMapClick = { selected = null },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(onClick = controller::zoomIn) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.hajj_map_zoom_in))
                }
                FilledTonalIconButton(onClick = controller::zoomOut) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.hajj_map_zoom_out))
                }
            }

            selected?.let { site ->
                val info = HajjLocationGuide.guidanceFor(site)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(info.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { selected = null }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.hajj_map_close))
                            }
                        }
                        Spacer(Modifier.size(8.dp))
                        Text(info.supplication, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.size(6.dp))
                        Text(info.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
