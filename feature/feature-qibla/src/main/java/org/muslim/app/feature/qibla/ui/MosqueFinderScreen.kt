package org.muslim.app.feature.qibla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

    fun search(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            loading = true
            error = null
            runCatching { repository.nearby(latitude, longitude) }
                .onSuccess {
                    mosques = it
                    selectedMosque = null
                }
                .onFailure { error = it.message }
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
                    Box(Modifier.fillMaxWidth().height(240.dp)) {
                        OsmMapView(
                            modifier = Modifier.fillMaxSize(),
                            initialCamera = CameraPosition.Builder()
                                .target(LatLng(latitude, longitude))
                                .zoom(13.0)
                                .build(),
                            key = viewModel.mosques,
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
                                viewModel.selectMosque(
                                    viewModel.mosques.firstOrNull {
                                        "${it.latitude}_${it.longitude}" == markerId
                                    },
                                )
                            },
                            onMapClick = { viewModel.selectMosque(null) },
                        )
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
            items(viewModel.mosques, key = { it.latitude.toString() + it.longitude }) { mosque ->
                Card(Modifier.fillMaxWidth()) {
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
