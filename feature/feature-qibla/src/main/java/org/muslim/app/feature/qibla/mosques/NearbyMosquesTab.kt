package org.muslim.app.feature.qibla.mosques

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.data.MosquePlace
import org.muslim.app.feature.qibla.data.NearbyMosque
import org.muslim.app.feature.qibla.data.NearbyMosqueRadiusOptionsKm

/** Lightweight list UI only: this tab intentionally embeds no map, tiles, or map SDK. */
@Composable
internal fun NearbyMosquesTab(
    presentation: NearbyMosquesPresentation,
    onRefresh: () -> Unit,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.nearby_mosques_title)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = title },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            NearbyMosquesHeader(
                radiusKm = presentation.radiusKm,
                onRefresh = onRefresh,
                onRadiusSelected = onRadiusSelected,
            )
        }
        when (val state = presentation.state) {
            NearbyMosquesUiState.Idle -> item { MosqueMessage(R.string.nearby_mosques_ready) }
            is NearbyMosquesUiState.LoadingLocation -> {
                item { MosqueLoadingMessage(R.string.nearby_mosques_loading_location) }
                if (state.cachedPlaces.isNotEmpty()) {
                    item { MosqueMessage(R.string.nearby_mosques_cached_locations_loading) }
                    cachedMosqueRows(state.cachedPlaces)
                }
            }
            is NearbyMosquesUiState.LoadingMosques -> {
                if (state.cachedMosques.isNotEmpty()) {
                    item { MosqueMessage(R.string.nearby_mosques_refreshing_cache) }
                    mosqueRows(state.cachedMosques)
                } else {
                    item { MosqueLoadingMessage(R.string.nearby_mosques_loading_mosques) }
                }
            }
            is NearbyMosquesUiState.Success -> mosqueRows(state.mosques)
            NearbyMosquesUiState.Empty -> item {
                MosqueActionMessage(R.string.nearby_mosques_empty, onRefresh)
            }
            NearbyMosquesUiState.Error -> item {
                MosqueActionMessage(R.string.nearby_mosques_error, onRefresh)
            }
            is NearbyMosquesUiState.OfflineCache -> {
                item { MosqueMessage(R.string.nearby_mosques_offline_cache) }
                mosqueRows(state.mosques)
            }
            NearbyMosquesUiState.PermissionDenied -> item {
                MosqueActionMessage(R.string.nearby_mosques_permission_denied, onRefresh)
            }
            NearbyMosquesUiState.LocationUnavailable -> item {
                MosqueActionMessage(R.string.nearby_mosques_location_unavailable, onRefresh)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.mosqueRows(mosques: List<NearbyMosque>) {
    items(mosques, key = { "${it.place.osmType}/${it.place.osmId}" }) { mosque ->
        NearbyMosqueRow(mosque)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.cachedMosqueRows(places: List<MosquePlace>) {
    items(places, key = { "${it.osmType}/${it.osmId}" }) { place ->
        CachedMosqueRow(place)
    }
}

@Composable
private fun NearbyMosquesHeader(
    radiusKm: Int,
    onRefresh: () -> Unit,
    onRadiusSelected: (Int) -> Unit,
) {
    val refreshDescription = stringResource(R.string.nearby_mosques_refresh)
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nearby_mosques_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.nearby_mosques_location_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IslamicSecondaryButton(
                onClick = onRefresh,
                modifier = Modifier.semantics { contentDescription = refreshDescription },
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.nearby_mosques_refresh))
            }
        }
        Spacer(Modifier.size(14.dp))
        Text(
            text = stringResource(R.string.nearby_mosques_radius),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NearbyMosqueRadiusOptionsKm.forEach { option ->
                val radiusDescription = stringResource(R.string.nearby_mosques_radius_option_description, option)
                IslamicSecondaryButton(
                    onClick = { onRadiusSelected(option) },
                    enabled = option != radiusKm,
                    modifier = Modifier.weight(1f).semantics {
                        contentDescription = radiusDescription
                    },
                ) {
                    Text(stringResource(R.string.nearby_mosques_radius_value, option))
                }
            }
        }
    }
}

@Composable
private fun NearbyMosqueRow(mosque: NearbyMosque) = MosquePlaceRow(
    place = mosque.place,
    distanceMeters = mosque.distanceMeters,
)

@Composable
private fun CachedMosqueRow(place: MosquePlace) = MosquePlaceRow(
    place = place,
    distanceMeters = null,
)

@Composable
private fun MosquePlaceRow(place: MosquePlace, distanceMeters: Double?) {
    val context = LocalContext.current
    val mosqueName = place.name ?: stringResource(R.string.nearby_mosques_unnamed)
    val distance = if (distanceMeters != null) formatDistance(distanceMeters) else null
    val rowDescription = if (distance != null) {
        stringResource(R.string.nearby_mosques_item_description, mosqueName, distance)
    } else {
        stringResource(R.string.nearby_mosques_cached_item_description, mosqueName)
    }
    val directionsDescription = stringResource(R.string.nearby_mosques_directions, mosqueName)
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = rowDescription },
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Mosque,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = mosqueName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (distance != null) {
                    Text(
                        text = distance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                place.address?.let { address ->
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.size(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IslamicSecondaryButton(
                onClick = { openExternalDirections(context, place) },
                modifier = Modifier.semantics { contentDescription = directionsDescription },
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.nearby_mosques_directions_action))
            }
        }
    }
}

@Composable
private fun formatDistance(distanceMeters: Double): String = when {
    distanceMeters < 1_000.0 -> stringResource(R.string.nearby_mosques_distance_meters, distanceMeters.toInt())
    else -> stringResource(R.string.nearby_mosques_distance_kilometers, distanceMeters / 1_000.0)
}

@Composable
private fun MosqueLoadingMessage(message: Int) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(12.dp))
            Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun MosqueMessage(message: Int) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MosqueActionMessage(message: Int, onRefresh: () -> Unit) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(12.dp))
        IslamicSecondaryButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.nearby_mosques_retry))
        }
    }
}

/** Keeps the source coordinates unrounded when constructing a navigation destination. */
internal fun navigationCoordinates(mosque: MosquePlace): String =
    "${mosque.latitude},${mosque.longitude}"

/**
 * Opens an external navigation application. Google Maps is preferred when it is
 * installed; otherwise any application that resolves the standard geo URI can handle it.
 */
internal fun openExternalDirections(context: Context, mosque: NearbyMosque) =
    openExternalDirections(context, mosque.place)

internal fun openExternalDirections(context: Context, mosque: MosquePlace) {
    val coordinates = navigationCoordinates(mosque)
    val googleMaps = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$coordinates"))
        .setPackage("com.google.android.apps.maps")
    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$coordinates"))
    try {
        context.startActivity(googleMaps)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(fallback)
        } catch (_: ActivityNotFoundException) {
            // A maps application is optional; failure to resolve it must not crash the Qibla screen.
        }
    }
}
