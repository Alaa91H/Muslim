package org.muslim.app.core.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/**
 * Shared OpenStreetMap wrapper (MapLibre GL Native — free, no API key, no
 * Google Play services). Used by the qibla map, the mosque finder and the
 * prayer-times location picker.
 */
object OsmMapDefaults {
    /**
     * OpenFreeMap "Liberty" style — free vector tiles, no API key, no
     * registration. Tile usage policy: https://openfreemap.org
     */
    const val STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

    /** Reasonable default camera when no location is known yet. */
    fun defaultCamera(): CameraPosition =
        CameraPosition.Builder()
            .target(LatLng(21.4225, 39.8262)) // Kaaba, world center of the app
            .zoom(3.0)
            .build()
}

/**
 * Compose wrapper around [MapView]. The map is created once and its lifecycle
 * is driven by the nearest [androidx.lifecycle.LifecycleOwner]. [onMapReady]
 * fires after the style finishes loading, which is the safe point to add
 * layers/markers; [onMapClick] reports tapped coordinates.
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    initialCamera: CameraPosition = OsmMapDefaults.defaultCamera(),
    styleUri: String = OsmMapDefaults.STYLE_URI,
    /** When this changes the map is recreated — use it to re-apply markers. */
    key: Any? = null,
    onMapReady: (MapLibreMap) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnReady by rememberUpdatedState(onMapReady)
    val currentOnClick by rememberUpdatedState(onMapClick)

    val mapView = remember(context, key) {
        // Must be initialized once before any MapView is created.
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.cameraPosition = initialCamera
                map.setStyle(styleUri) {
                    map.addOnMapClickListener { point ->
                        currentOnClick(point)
                        true
                    }
                    currentOnReady(map)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

/** Moves the camera to [point] at [zoom] with a smooth animation. */
fun MapLibreMap.animateTo(point: LatLng, zoom: Double = 12.0) {
    val camera = CameraUpdateFactory.newCameraPosition(
        CameraPosition.Builder().target(point).zoom(zoom).build(),
    )
    animateCamera(camera, 600)
}

/**
 * Adds a teardrop pin marker at [point]. The pin bitmap is drawn in code, so
 * markers work offline and never depend on style glyphs. [colorHex] is a
 * "#RRGGBB" string; a white border keeps it visible on any basemap. Repeated
 * calls with the same [id] move the existing marker instead of stacking.
 */
fun MapLibreMap.addPinMarker(id: String, point: LatLng, colorHex: String) {
    getStyle { style ->
        val imageName = "pin_$id"
        if (style.getImage(imageName) == null) {
            style.addImage(imageName, makePinBitmap(colorHex))
        }
        val sourceId = "src_$id"
        if (style.getSource(sourceId) == null) {
            style.addSource(
                GeoJsonSource(
                    sourceId,
                    Feature.fromGeometry(Point.fromLngLat(point.longitude, point.latitude)),
                ),
            )
            style.addLayerBelow(
                SymbolLayer(id, sourceId).withProperties(
                    PropertyFactory.iconImage(imageName),
                    PropertyFactory.iconSize(1.0f),
                    PropertyFactory.iconAllowOverlap(true),
                ),
                "waterway-label",
            )
        } else {
            (style.getSource(sourceId) as GeoJsonSource).setGeoJson(
                Feature.fromGeometry(Point.fromLngLat(point.longitude, point.latitude)),
            )
        }
    }
}

/**
 * Adds a polyline (e.g. the qibla great-circle route). [points] must have at
 * least two entries; the layer is replaced in place on subsequent calls.
 */
fun MapLibreMap.addPolyline(id: String, points: List<LatLng>, colorHex: String, width: Float = 4f) {
    if (points.size < 2) return
    getStyle { style ->
        val sourceId = "line_src_$id"
        val coordinates = points.map { Point.fromLngLat(it.longitude, it.latitude) }
        if (style.getSource(sourceId) == null) {
            style.addSource(
                GeoJsonSource(
                    sourceId,
                    Feature.fromGeometry(LineString.fromLngLats(coordinates)),
                ),
            )
            style.addLayer(
                LineLayer(id, sourceId).withProperties(
                    PropertyFactory.lineColor(colorHex),
                    PropertyFactory.lineWidth(width),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                ),
            )
        } else {
            (style.getSource(sourceId) as GeoJsonSource).setGeoJson(
                Feature.fromGeometry(LineString.fromLngLats(coordinates)),
            )
        }
    }
}

/** Draws a teardrop pin (head circle + tail triangle) into a bitmap. */
private fun makePinBitmap(colorHex: String): Bitmap {
    val size = 64
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorHex.toColorInt()
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    // Head circle.
    canvas.drawCircle(size / 2f, size * 0.32f, size * 0.20f, fill)
    canvas.drawCircle(size / 2f, size * 0.32f, size * 0.20f, border)
    // Tail triangle.
    val tail = Path().apply {
        moveTo(size * 0.28f, size * 0.40f)
        lineTo(size * 0.72f, size * 0.40f)
        lineTo(size * 0.5f, size * 0.92f)
        close()
    }
    canvas.drawPath(tail, fill)
    canvas.drawPath(tail, border)
    return bitmap
}
