package org.muslim.app.feature.qibla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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

    fun search(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            loading = true
            error = null
            runCatching { repository.nearby(latitude, longitude) }
                .onSuccess { mosques = it }
                .onFailure { error = it.message }
            loading = false
        }
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
