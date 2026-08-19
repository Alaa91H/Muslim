package org.muslim.app.core.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import okhttp3.OkHttpClient
import org.muslim.app.core.common.HttpAgents

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

/** OkHttp client that tags every MapLibre request with the app's user agent. */
private val userAgentHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", HttpAgents.APP_USER_AGENT)
                    .build(),
            )
        }
        .build()
}

/**
 * Handle to drive a [MapLibreMap] from outside the composable (zoom buttons,
 * fly-to, …). The map binds itself on ready; calls are safe before that
 * (no-ops) and after a re-created map re-binds.
 */
class MapController {
    private var map: MapLibreMap? = null

    fun bind(map: MapLibreMap) {
        this.map = map
    }

    fun zoomIn() {
        map?.animateCamera(CameraUpdateFactory.zoomIn(), 300)
    }

    fun zoomOut() {
        map?.animateCamera(CameraUpdateFactory.zoomOut(), 300)
    }

    /** Smoothly moves the camera to [point] at [zoom]. */
    fun animateTo(point: LatLng, zoom: Double = 12.0) {
        map?.animateTo(point, zoom)
    }
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
    /** Optional handle to drive the map from outside (zoom buttons, fly-to). */
    controller: MapController? = null,
    onMapReady: (MapLibreMap) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    /** Fires when a feature in one of [symbolLayerIds] is tapped (e.g. a mosque marker). */
    onSymbolClick: (Feature) -> Unit = {},
    /** Layers whose rendered features should be reported via [onSymbolClick] on tap. */
    symbolLayerIds: List<String> = emptyList(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnReady by rememberUpdatedState(onMapReady)
    val currentOnClick by rememberUpdatedState(onMapClick)
    val currentOnSymbolClick by rememberUpdatedState(onSymbolClick)
    val currentSymbolLayerIds by rememberUpdatedState(symbolLayerIds)

    val mapView = remember(context, key) {
        // Must be initialized once before any MapView is created.
        MapLibre.getInstance(context.applicationContext)
        // Identify the app to OpenFreeMap so tile requests are not blocked (403/406).
        HttpRequestUtil.setOkHttpClient(userAgentHttpClient)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.cameraPosition = initialCamera
                map.setStyle(styleUri) {
                    map.addOnMapClickListener { point ->
                        val layerIds = currentSymbolLayerIds
                        if (layerIds.isNotEmpty()) {
                            val screenPoint = map.projection.toScreenLocation(point)
                            val features = map.queryRenderedFeatures(
                                screenPoint,
                                *layerIds.toTypedArray(),
                            )
                            if (features.isNotEmpty()) {
                                currentOnSymbolClick(features.first())
                                return@addOnMapClickListener true
                            }
                        }
                        currentOnClick(point)
                        true
                    }
                    controller?.bind(map)
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

/** A tappable map marker (mosque) with its label and distance. */
data class MapMarker(
    val id: String,
    val point: LatLng,
    val name: String,
    val distanceMeters: Int,
)

/**
 * Adds mosque markers as one GeoJSON-backed symbol layer. Each feature carries
 * its [MapMarker] id plus "name" and "distance" string properties so callers
 * can rebuild the tapped marker from [org.maplibre.geojson.Feature] in
 * `onSymbolClick` without a lookup table. Replaces the layer in place on
 * subsequent calls (e.g. after a new search).
 */
fun MapLibreMap.addMosqueMarkers(markers: List<MapMarker>, layerId: String = "mosque-markers") {
    if (markers.isEmpty()) return
    getStyle { style ->
        val imageName = "mosque_icon"
        if (style.getImage(imageName) == null) {
            style.addImage(imageName, makeMosqueBitmap())
        }
        val sourceId = "src_$layerId"
        val features = markers.map { marker ->
            Feature.fromGeometry(
                Point.fromLngLat(marker.point.longitude, marker.point.latitude),
                com.google.gson.JsonObject().apply {
                    addProperty("name", marker.name)
                    addProperty("distance", marker.distanceMeters)
                    addProperty("markerId", marker.id)
                },
                marker.id,
            )
        }
        if (style.getSource(sourceId) == null) {
            style.addSource(GeoJsonSource(sourceId, FeatureCollection.fromFeatures(features)))
            style.addLayerAbove(
                SymbolLayer(layerId, sourceId).withProperties(
                    PropertyFactory.iconImage(imageName),
                    PropertyFactory.iconSize(1.0f),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true),
                ),
                "waterway-label",
            )
        } else {
            (style.getSource(sourceId) as GeoJsonSource).setGeoJson(
                FeatureCollection.fromFeatures(features),
            )
        }
    }
}

/** Draws a mosque silhouette (dome + two minarets) into a bitmap. */
private fun makeMosqueBitmap(): Bitmap {
    val size = 64
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#2E7D32".toColorInt()
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    // Central dome.
    canvas.drawArc(RectF(22f, 20f, 42f, 40f), 180f, 180f, true, fill)
    canvas.drawArc(RectF(22f, 20f, 42f, 40f), 180f, 180f, true, border)
    // Base building.
    canvas.drawRoundRect(RectF(16f, 38f, 48f, 54f), 4f, 4f, fill)
    canvas.drawRoundRect(RectF(16f, 38f, 48f, 54f), 4f, 4f, border)
    // Minarets.
    canvas.drawRect(14f, 16f, 19f, 40f, fill)
    canvas.drawRect(14f, 16f, 19f, 40f, border)
    canvas.drawRect(45f, 16f, 50f, 40f, fill)
    canvas.drawRect(45f, 16f, 50f, 40f, border)
    // Minaret caps.
    canvas.drawCircle(16.5f, 14f, 3f, fill)
    canvas.drawCircle(47.5f, 14f, 3f, fill)
    return bitmap
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
