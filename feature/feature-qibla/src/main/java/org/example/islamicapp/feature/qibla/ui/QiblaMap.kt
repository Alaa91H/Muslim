package org.example.islamicapp.feature.qibla.ui

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.example.islamicapp.feature.qibla.R
import org.example.islamicapp.feature.qibla.domain.QiblaCalculator
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/**
 * OpenStreetMap alternative for the Qibla (PROJECT_PROMPT.md §6 Phase 1:
 * "عرض بديل على خريطة — خط مستقيم من موقع المستخدم إلى مكة").
 *
 * Uses osmdroid with free OSM raster tiles (no API key), tiles cached under
 * the app's cache dir. Draws a straight line from the user's location to the
 * Kaaba, with markers at both ends. Interactive: pinch-zoom + controls.
 */
@Composable
fun QiblaMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Point osmdroid at app-private storage (no storage permission needed)
    // and set a proper tile-server user agent (tile.openstreetmap.org policy).
    remember {
        Configuration.getInstance().apply {
            load(context, PreferenceManager.getDefaultSharedPreferences(context))
            userAgentValue = "Manara/0.1 (org.example.islamicapp)"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            minZoomLevel = 2.0
            maxZoomLevel = 18.0
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    ) {
        it.overlays.clear()
        setupQiblaOverlays(it, latitude, longitude)
        it.invalidate()
    }
}

/** Draws the user/Kaaba markers and the straight line, then fits the view. */
private fun setupQiblaOverlays(mapView: MapView, latitude: Double, longitude: Double) {
    val user = GeoPoint(latitude, longitude)
    val kaaba = GeoPoint(QiblaCalculator.KAABA_LATITUDE, QiblaCalculator.KAABA_LONGITUDE)

    val line = Polyline(mapView).apply {
        setPoints(listOf(user, kaaba))
        outlinePaint.color = Color.rgb(212, 160, 23) // gold, matching the compass
        outlinePaint.strokeWidth = 5f
    }
    mapView.overlays.add(line)

    val userMarker = Marker(mapView).apply {
        position = user
        title = mapView.context.getString(R.string.qibla_map_user)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        setIcon(mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default))
    }
    mapView.overlays.add(userMarker)

    val kaabaMarker = Marker(mapView).apply {
        position = kaaba
        title = mapView.context.getString(R.string.qibla_map_kaaba)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        setIcon(mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default))
    }
    mapView.overlays.add(kaabaMarker)

    // Fit both points with a little padding; clamp the zoom so the whole
    // route stays visible (esp. when the user is near Makkah).
    val box = BoundingBox(
        maxOf(user.latitude, kaaba.latitude),
        maxOf(user.longitude, kaaba.longitude),
        minOf(user.latitude, kaaba.latitude),
        minOf(user.longitude, kaaba.longitude),
    )
    mapView.zoomToBoundingBox(box, true, 96)
    mapView.controller.setZoom(mapView.zoomLevelDouble.coerceIn(3.0, 6.5).toInt())
    mapView.controller.setCenter(
        GeoPoint((user.latitude + kaaba.latitude) / 2, (user.longitude + kaaba.longitude) / 2)
    )
}
