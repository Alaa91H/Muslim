package org.muslim.app.feature.qibla.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maplibre.android.geometry.LatLng
import org.muslim.app.core.ui.map.OsmMapView
import org.muslim.app.core.ui.map.addPinMarker
import org.muslim.app.core.ui.map.addPolyline
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.domain.QiblaCalculator

private const val KAABA_PIN_COLOR = "#D4A017"
private const val YOU_PIN_COLOR = "#1E88E5"
private const val ROUTE_COLOR = "#2E7D32"

/**
 * Qibla map (PROJECT_PROMPT.md §6 Phase 1: خريطة القبلة) — now rendered on a
 * real OpenStreetMap basemap via MapLibre GL Native (free, no API key, no
 * Google Play services). Shows the great-circle route from the user's location
 * to the Kaaba, a gold pin for the Kaaba and a blue pin for the user. Tiles
 * are streamed from OpenFreeMap; if the device is offline the route and pins
 * still render on the cached basemap.
 */
@Composable
fun QiblaMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val route = remember(latitude, longitude) {
        QiblaCalculator.routePoints(latitude, longitude)
            .map { LatLng(it.latitude, it.longitude) }
    }
    val userPoint = remember(latitude, longitude) { LatLng(latitude, longitude) }
    val kaaba = remember { LatLng(QiblaCalculator.KAABA_LATITUDE, QiblaCalculator.KAABA_LONGITUDE) }
    val routeColor = ROUTE_COLOR

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
    ) {
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            initialCamera = org.maplibre.android.camera.CameraPosition.Builder()
                .target(userPoint)
                .zoom(4.5)
                .build(),
            onMapReady = { map ->
                map.addPinMarker("you", userPoint, YOU_PIN_COLOR)
                map.addPinMarker("kaaba", kaaba, KAABA_PIN_COLOR)
                map.addPolyline("route", route, routeColor, width = 4f)
            },
        )
    }
}

/** Small legend row shown above the map. */
@Composable
fun QiblaMapLegend(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.qibla_map_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
