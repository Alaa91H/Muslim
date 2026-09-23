package org.muslim.app.feature.qibla.mosques

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.data.MosquePlace
import org.muslim.app.feature.qibla.data.NearbyMosque
import org.muslim.app.feature.qibla.data.NearbyMosqueRadiusOptionsKm

internal enum class MosqueSortMode {
    Distance,
    Name,
}

/** Lightweight list UI only: this tab intentionally embeds no map tiles or map SDK. */
@Composable
internal fun NearbyMosquesTab(
    presentation: NearbyMosquesPresentation,
    onRefresh: () -> Unit,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.nearby_mosques_title)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(MosqueSortMode.Distance) }

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
                    mosqueResultSection(
                        source = state.cachedMosques,
                        searchQuery = searchQuery,
                        sortMode = sortMode,
                        onSearchQueryChange = { searchQuery = it },
                        onClearSearch = { searchQuery = "" },
                        onSortModeChange = { sortMode = it },
                    )
                } else {
                    item { MosqueLoadingMessage(R.string.nearby_mosques_loading_mosques) }
                }
            }

            is NearbyMosquesUiState.Success -> {
                mosqueResultSection(
                    source = state.mosques,
                    searchQuery = searchQuery,
                    sortMode = sortMode,
                    onSearchQueryChange = { searchQuery = it },
                    onClearSearch = { searchQuery = "" },
                    onSortModeChange = { sortMode = it },
                )
            }

            NearbyMosquesUiState.Empty -> item {
                MosqueEmptyState(
                    radiusKm = presentation.radiusKm,
                    onRadiusSelected = onRadiusSelected,
                    onRefresh = onRefresh,
                )
            }

            NearbyMosquesUiState.Error -> item {
                MosqueActionMessage(R.string.nearby_mosques_error, onRefresh)
            }

            is NearbyMosquesUiState.OfflineCache -> {
                item { MosqueMessage(R.string.nearby_mosques_offline_cache) }
                mosqueResultSection(
                    source = state.mosques,
                    searchQuery = searchQuery,
                    sortMode = sortMode,
                    onSearchQueryChange = { searchQuery = it },
                    onClearSearch = { searchQuery = "" },
                    onSortModeChange = { sortMode = it },
                )
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

private fun androidx.compose.foundation.lazy.LazyListScope.mosqueResultSection(
    source: List<NearbyMosque>,
    searchQuery: String,
    sortMode: MosqueSortMode,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSortModeChange: (MosqueSortMode) -> Unit,
) {
    val visible = filterAndSortMosques(source, searchQuery, sortMode)
    val nearestKey = source.minByOrNull(NearbyMosque::distanceMeters)?.place?.stableKey()

    item(key = "mosque-tools") {
        MosqueResultsTools(
            query = searchQuery,
            visibleCount = visible.size,
            totalCount = source.size,
            sortMode = sortMode,
            onQueryChange = onSearchQueryChange,
            onClearQuery = onClearSearch,
            onSortModeChange = onSortModeChange,
        )
    }

    if (visible.isEmpty() && searchQuery.isNotBlank()) {
        item(key = "mosque-search-empty") {
            MosqueSearchEmpty(onClearSearch)
        }
    } else {
        mosqueRows(visible, nearestKey)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.mosqueRows(
    mosques: List<NearbyMosque>,
    nearestKey: String?,
) {
    items(mosques, key = { it.place.stableKey() }) { mosque ->
        NearbyMosqueRow(
            mosque = mosque,
            isNearest = mosque.place.stableKey() == nearestKey,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.cachedMosqueRows(places: List<MosquePlace>) {
    items(places, key = { it.stableKey() }) { place ->
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
    val radiusDescription = stringResource(R.string.nearby_mosques_radius_option_description, radiusKm)
    var radiusMenuExpanded by remember { mutableStateOf(false) }

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

        Spacer(Modifier.size(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.nearby_mosques_radius),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = stringResource(R.string.nearby_mosques_radius_selected, radiusKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                IslamicSecondaryButton(
                    onClick = { radiusMenuExpanded = true },
                    modifier = Modifier.semantics { contentDescription = radiusDescription },
                ) {
                    Text(stringResource(R.string.nearby_mosques_radius_value, radiusKm))
                    Spacer(Modifier.size(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = radiusMenuExpanded,
                    onDismissRequest = { radiusMenuExpanded = false },
                ) {
                    NearbyMosqueRadiusOptionsKm.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nearby_mosques_radius_value, option)) },
                            onClick = {
                                radiusMenuExpanded = false
                                if (option != radiusKm) onRadiusSelected(option)
                            },
                            trailingIcon = {
                                if (option == radiusKm) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueResultsTools(
    query: String,
    visibleCount: Int,
    totalCount: Int,
    sortMode: MosqueSortMode,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSortModeChange: (MosqueSortMode) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.nearby_mosques_search_clear),
                        )
                    }
                }
            },
            placeholder = {
                Text(stringResource(R.string.nearby_mosques_search_hint))
            },
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(Modifier.size(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.nearby_mosques_results_count, visibleCount, totalCount),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box {
                IslamicSecondaryButton(onClick = { sortMenuExpanded = true }) {
                    Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        when (sortMode) {
                            MosqueSortMode.Distance -> stringResource(R.string.nearby_mosques_sort_distance)
                            MosqueSortMode.Name -> stringResource(R.string.nearby_mosques_sort_name)
                        },
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                ) {
                    MosqueSortMode.entries.forEach { option ->
                        val selected = option == sortMode
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (option) {
                                        MosqueSortMode.Distance -> stringResource(R.string.nearby_mosques_sort_distance)
                                        MosqueSortMode.Name -> stringResource(R.string.nearby_mosques_sort_name)
                                    },
                                )
                            },
                            onClick = {
                                sortMenuExpanded = false
                                onSortModeChange(option)
                            },
                            trailingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyMosqueRow(
    mosque: NearbyMosque,
    isNearest: Boolean,
) = MosquePlaceRow(
    place = mosque.place,
    distanceMeters = mosque.distanceMeters,
    isNearest = isNearest,
)

@Composable
private fun CachedMosqueRow(place: MosquePlace) = MosquePlaceRow(
    place = place,
    distanceMeters = null,
    isNearest = false,
)

@Composable
private fun MosquePlaceRow(
    place: MosquePlace,
    distanceMeters: Double?,
    isNearest: Boolean,
) {
    val context = LocalContext.current
    val mosqueName = place.name ?: stringResource(R.string.nearby_mosques_unnamed)
    val distance = if (distanceMeters != null) formatDistance(distanceMeters) else null
    val rowDescription = if (distance != null) {
        stringResource(R.string.nearby_mosques_item_description, mosqueName, distance)
    } else {
        stringResource(R.string.nearby_mosques_cached_item_description, mosqueName)
    }
    val mapDescription = stringResource(R.string.nearby_mosques_map, mosqueName)
    val directionsDescription = stringResource(R.string.nearby_mosques_directions, mosqueName)
    val shareChooserTitle = stringResource(R.string.nearby_mosques_share_chooser)
    val copiedMessage = stringResource(R.string.nearby_mosques_copied)
    var moreMenuExpanded by remember { mutableStateOf(false) }

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
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.size(10.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mosqueName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isNearest) {
                        Spacer(Modifier.size(8.dp))
                        NearestBadge()
                    }
                }

                if (distance != null) {
                    Spacer(Modifier.size(3.dp))
                    Text(
                        text = distance,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
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

            Box {
                IconButton(onClick = { moreMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.nearby_mosques_more_actions),
                    )
                }
                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.nearby_mosques_share)) },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        },
                        onClick = {
                            moreMenuExpanded = false
                            shareMosque(context, place, shareChooserTitle)
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (place.address.isNullOrBlank()) {
                                    stringResource(R.string.nearby_mosques_copy_coordinates)
                                } else {
                                    stringResource(R.string.nearby_mosques_copy_address)
                                },
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        },
                        onClick = {
                            moreMenuExpanded = false
                            copyMosqueLocation(context, place, copiedMessage)
                        },
                    )
                }
            }
        }

        Spacer(Modifier.size(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IslamicSecondaryButton(
                onClick = { openExternalMap(context, place) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = mapDescription },
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.nearby_mosques_map_action))
            }

            IslamicPrimaryButton(
                onClick = { openExternalDirections(context, place) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = directionsDescription },
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.nearby_mosques_directions_action))
            }
        }
    }
}

@Composable
private fun NearestBadge() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = stringResource(R.string.nearby_mosques_nearest),
            modifier = Modifier.then(androidx.compose.foundation.layout.padding(horizontal = 8.dp, vertical = 4.dp)),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MosqueEmptyState(
    radiusKm: Int,
    onRadiusSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val nextRadius = nextMosqueRadius(radiusKm)

    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.nearby_mosques_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = stringResource(R.string.nearby_mosques_empty_detail, radiusKm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (nextRadius != null) {
                IslamicPrimaryButton(
                    onClick = { onRadiusSelected(nextRadius) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.nearby_mosques_expand_radius, nextRadius))
                }
            }
            IslamicSecondaryButton(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.nearby_mosques_retry))
            }
        }
    }
}

@Composable
private fun MosqueSearchEmpty(onClearSearch: () -> Unit) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.nearby_mosques_search_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = stringResource(R.string.nearby_mosques_search_empty_detail),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(12.dp))
        IslamicSecondaryButton(onClick = onClearSearch) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(stringResource(R.string.nearby_mosques_search_clear))
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

internal fun filterAndSortMosques(
    mosques: List<NearbyMosque>,
    query: String,
    sortMode: MosqueSortMode,
): List<NearbyMosque> {
    val trimmedQuery = query.trim()
    val filtered = if (trimmedQuery.isEmpty()) {
        mosques
    } else {
        mosques.filter { mosque ->
            mosque.place.name.orEmpty().contains(trimmedQuery, ignoreCase = true) ||
                mosque.place.address.orEmpty().contains(trimmedQuery, ignoreCase = true)
        }
    }

    return when (sortMode) {
        MosqueSortMode.Distance -> filtered.sortedBy(NearbyMosque::distanceMeters)
        MosqueSortMode.Name -> filtered.sortedWith(
            compareBy<NearbyMosque> { it.place.name.orEmpty().lowercase() }
                .thenBy(NearbyMosque::distanceMeters),
        )
    }
}

internal fun nextMosqueRadius(radiusKm: Int): Int? =
    NearbyMosqueRadiusOptionsKm.firstOrNull { it > radiusKm }

private fun MosquePlace.stableKey(): String = "$osmType/$osmId"

/** Keeps the source coordinates unrounded when constructing a map destination. */
internal fun navigationCoordinates(mosque: MosquePlace): String =
    "${mosque.latitude},${mosque.longitude}"

/** Standard geo URI for showing a mosque marker without starting turn-by-turn navigation. */
internal fun mosqueMapUri(mosque: MosquePlace): String {
    val coordinates = navigationCoordinates(mosque)
    val label = Uri.encode(mosque.name ?: "Mosque")
    return "geo:$coordinates?q=$coordinates($label)"
}

internal fun mosqueOpenStreetMapUrl(mosque: MosquePlace): String =
    "https://www.openstreetmap.org/?mlat=${mosque.latitude}&mlon=${mosque.longitude}#map=18/${mosque.latitude}/${mosque.longitude}"

/**
 * Opens the mosque as a map marker. Google Maps is preferred when installed;
 * otherwise any geo-capable application, then OpenStreetMap in a browser, may handle it.
 */
internal fun openExternalMap(context: Context, mosque: MosquePlace) {
    val geoUri = Uri.parse(mosqueMapUri(mosque))
    val googleMaps = Intent(Intent.ACTION_VIEW, geoUri).setPackage("com.google.android.apps.maps")
    val genericMap = Intent(Intent.ACTION_VIEW, geoUri)
    val openStreetMap = Intent(Intent.ACTION_VIEW, Uri.parse(mosqueOpenStreetMapUrl(mosque)))
    startFirstAvailable(context, googleMaps, genericMap, openStreetMap)
}

/**
 * Opens turn-by-turn navigation. Google Maps navigation is preferred, with a
 * web directions URL and finally the standard geo URI as graceful fallbacks.
 */
internal fun openExternalDirections(context: Context, mosque: NearbyMosque) =
    openExternalDirections(context, mosque.place)

internal fun openExternalDirections(context: Context, mosque: MosquePlace) {
    val coordinates = navigationCoordinates(mosque)
    val googleMaps = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$coordinates"))
        .setPackage("com.google.android.apps.maps")
    val webDirections = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$coordinates"),
    )
    val geoFallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$coordinates"))
    startFirstAvailable(context, googleMaps, webDirections, geoFallback)
}

private fun shareMosque(context: Context, mosque: MosquePlace, chooserTitle: String) {
    val body = buildString {
        append(mosque.name ?: context.getString(R.string.nearby_mosques_unnamed))
        mosque.address?.takeIf(String::isNotBlank)?.let {
            append("\n")
            append(it)
        }
        append("\n")
        append(mosqueOpenStreetMapUrl(mosque))
    }
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, body)
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun copyMosqueLocation(context: Context, mosque: MosquePlace, copiedMessage: String) {
    val value = mosque.address?.takeIf(String::isNotBlank) ?: navigationCoordinates(mosque)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(mosque.name ?: "Mosque", value))
    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
}

private fun startFirstAvailable(context: Context, vararg intents: Intent) {
    for (intent in intents) {
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next compatible maps/browser application.
        }
    }
}
