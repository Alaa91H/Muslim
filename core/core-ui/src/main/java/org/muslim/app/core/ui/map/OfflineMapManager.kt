package org.muslim.app.core.ui.map

import android.content.Context
import android.content.SharedPreferences
import android.os.StatFs
import androidx.core.content.edit
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition

/**
 * A downloaded offline map region.
 *
 * @param id            MapLibre offline region id.
 * @param name          User-facing label (e.g. "Makkah" or "Egypt").
 * @param kind          "place" / "city" / "country".
 * @param minLat/maxLat/minLng/maxLng Bounding box of the download.
 * @param minZoom/maxZoom Zoom range actually requested.
 * @param downloadedBytes Bytes stored on disk so far (0 until a status arrives).
 * @param requiredCount   Total resources the region needs to be complete.
 * @param completedCount  Resources downloaded so far.
 * @param complete        True once every required resource is on disk.
 */
data class OfflineMapRegion(
    val id: Long,
    val name: String,
    val kind: String,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val minZoom: Double,
    val maxZoom: Double,
    val downloadedBytes: Long,
    val requiredCount: Long,
    val completedCount: Long,
    val complete: Boolean,
) {
    val progress: Float
        get() = if (requiredCount <= 0) 0f
        else (completedCount.toFloat() / requiredCount).coerceIn(0f, 1f)
}

/** Result of creating a download. */
sealed class OfflineMapDownloadResult {
    data class Started(val id: Long) : OfflineMapDownloadResult()
    data class Error(val message: String) : OfflineMapDownloadResult()
}

/**
 * Wraps MapLibre's [OfflineManager] so map tiles (plus the style) can be
 * downloaded once and used with no internet afterwards. MapLibre serves
 * offline-region resources automatically whenever the network is
 * unavailable, so no map-view change is needed after a region is created.
 *
 * Metadata (name/kind/bounds) is kept in [SharedPreferences] keyed by the
 * MapLibre region id, because MapLibre only stores the opaque metadata bytes
 * we pass at creation time.
 */
class OfflineMapManager(context: Context) {

    private val appContext = context.applicationContext
    private val manager: OfflineManager by lazy {
        MapLibre.getInstance(appContext)
        OfflineManager.getInstance(appContext)
    }
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("offline_maps", Context.MODE_PRIVATE)

    /** Callback invoked with progress updates while a region downloads. */
    fun interface ProgressListener {
        fun onProgress(region: OfflineMapRegion)
    }

    /**
     * Starts downloading the map for [bounds] into an offline region.
     * [name] and [kind] are user-facing metadata; [styleUrl] must match the
     * style the map views load ([OsmMapDefaults.STYLE_URI]) so the tiles are
     * reused offline. [onProgress] fires on the main thread as MapLibre
     * reports status changes.
     */
    fun download(
        name: String,
        kind: String,
        bounds: LatLngBounds,
        minZoom: Double = 4.0,
        maxZoom: Double = 14.0,
        styleUrl: String = OsmMapDefaults.STYLE_URI,
        onProgress: ProgressListener = ProgressListener {},
        onResult: (OfflineMapDownloadResult) -> Unit = {},
    ) {
        val definition = OfflineTilePyramidRegionDefinition(
            styleUrl,
            bounds,
            minZoom,
            maxZoom,
            appContext.resources.displayMetrics.density,
        )
        val metadata = encodeMetadata(name, kind, bounds, minZoom, maxZoom)
        manager.createOfflineRegion(
            definition,
            metadata,
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {
                            onProgress.onProgress(toRegion(offlineRegion.id, status))
                        }

                        override fun onError(error: OfflineRegionError) {
                            // MapLibre keeps retrying; surface a status refresh.
                            onProgress.onProgress(toRegion(offlineRegion.id, null))
                        }

                        override fun mapboxTileCountLimitExceeded(limit: Long) = Unit
                    })
                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                    onResult(OfflineMapDownloadResult.Started(offlineRegion.id))
                }

                override fun onError(error: String) {
                    onResult(OfflineMapDownloadResult.Error(error.ifBlank { "offline region creation failed" }))
                }
            },
        )
    }

    /** Deletes an offline region and its tiles from disk. */
    fun delete(id: Long, onDeleted: (Boolean) -> Unit = {}) {
        findRegion(id) { region ->
            if (region == null) {
                prefs.edit { remove(metaKey(id)) }
                onDeleted(false)
                return@findRegion
            }
            region.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                override fun onDelete() {
                    prefs.edit { remove(metaKey(id)) }
                    onDeleted(true)
                }

                override fun onError(error: String) {
                    onDeleted(false)
                }
            })
        }
    }

    /** Deletes every offline region (used by the "delete all" action). */
    fun deleteAll(onDeleted: (Int) -> Unit = {}) {
        listRegions { regions ->
            var remaining = regions.size
            if (remaining == 0) {
                onDeleted(0)
                return@listRegions
            }
            regions.forEach { r ->
                delete(r.id) {
                    remaining--
                    if (remaining == 0) onDeleted(regions.size)
                }
            }
        }
    }

    /** Lists all downloaded regions with their latest status. */
    fun listRegions(onResult: (List<OfflineMapRegion>) -> Unit) {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                if (offlineRegions == null || offlineRegions.isEmpty()) {
                    onResult(emptyList())
                    return
                }
                val result = java.util.Collections.synchronizedList(mutableListOf<OfflineMapRegion>())
                var remaining = offlineRegions.size
                offlineRegions.forEach { region ->
                    region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                        override fun onStatus(status: OfflineRegionStatus?) {
                            result.add(toRegion(region.id, status))
                            remaining--
                            if (remaining == 0) onResult(result.toList())
                        }

                        override fun onError(error: String?) {
                            result.add(toRegion(region.id, null))
                            remaining--
                            if (remaining == 0) onResult(result.toList())
                        }
                    })
                }
            }

            override fun onError(error: String) {
                onResult(emptyList())
            }
        })
    }

    /**
     * Bytes of free storage on the app's data partition. If the OS reports a
     * failure (rare), [Long.MAX_VALUE] is returned so callers never block the
     * UI on an error.
     */
    fun availableBytes(): Long = try {
        StatFs(appContext.filesDir.absolutePath).availableBytes
    } catch (e: Exception) {
        Long.MAX_VALUE
    }

    /** Total capacity of the app's data partition (0 if it cannot be read). */
    fun totalBytes(): Long = try {
        StatFs(appContext.filesDir.absolutePath).totalBytes
    } catch (e: Exception) {
        0L
    }

    /**
     * Low-storage snapshot for the download screen: free space and the
     * largest existing region (so the UI can warn and suggest deleting it).
     * [regions] is the currently listed set, used to pick the largest.
     */
    fun storageSnapshot(regions: List<OfflineMapRegion>): StorageSnapshot {
        val available = availableBytes()
        val total = totalBytes()
        // Warn below 512 MB free, or when free space is under 15% of the
        // partition (whichever is more conservative for the device).
        val low = available < 512L * 1024 * 1024 || (total > 0 && available < total / 100 * 15)
        val largest = regions.maxByOrNull { it.downloadedBytes }
        return StorageSnapshot(
            availableBytes = available,
            totalBytes = total,
            lowOnStorage = low,
            largestRegion = largest?.takeIf { it.downloadedBytes > 0 },
        )
    }

    /** Estimates the on-disk size a download would use, before starting it. */
    fun estimateBytes(
        bounds: LatLngBounds,
        minZoom: Double = 4.0,
        maxZoom: Double = 14.0,
    ): Long {
        // Vector tiles: ~ (tile count) * average compressed vector tile.
        // OpenFreeMap vector tiles average roughly 15-30 KB; use 25 KB.
        val avgTileBytes = 25_000L
        var tiles = 0L
        var zoom = minZoom
        while (zoom <= maxZoom) {
            val n = 1L shl zoom.toInt()
            val tilesX = ((bounds.longitudeSpan / 360.0) * n).toLong().coerceAtLeast(1L)
            val tilesY = ((bounds.latitudeSpan / 180.0) * n).toLong().coerceAtLeast(1L)
            tiles += tilesX * tilesY
            zoom += 1
        }
        // Style + glyphs + sprites overhead (~1.5 MB) plus the tiles.
        return tiles * avgTileBytes + 1_500_000L
    }

    /** Formats a byte count as a human size (KB/MB/GB). */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        return when {
            kb < 1024 -> "%.0f KB".format(kb)
            kb < 1024 * 1024 -> "%.1f MB".format(kb / 1024)
            else -> "%.2f GB".format(kb / (1024 * 1024))
        }
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /** Snapshot of the device's free storage plus the biggest region on disk. */
    data class StorageSnapshot(
        val availableBytes: Long,
        val totalBytes: Long,
        val lowOnStorage: Boolean,
        val largestRegion: OfflineMapRegion?,
    )

    private fun findRegion(id: Long, onFound: (OfflineRegion?) -> Unit) {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                onFound(offlineRegions?.firstOrNull { it.id == id })
            }

            override fun onError(error: String) {
                onFound(null)
            }
        })
    }

    private fun toRegion(id: Long, status: OfflineRegionStatus?): OfflineMapRegion {
        val meta = decodeMetadata(prefs.getString(metaKey(id), null))
        return OfflineMapRegion(
            id = id,
            name = meta.name,
            kind = meta.kind,
            minLat = meta.minLat,
            maxLat = meta.maxLat,
            minLng = meta.minLng,
            maxLng = meta.maxLng,
            minZoom = meta.minZoom,
            maxZoom = meta.maxZoom,
            downloadedBytes = status?.completedResourceSize ?: 0L,
            requiredCount = status?.requiredResourceCount ?: 0L,
            completedCount = status?.completedResourceCount ?: 0L,
            complete = status?.isComplete ?: false,
        )
    }

    private fun metaKey(id: Long) = "region_$id"

    private fun encodeMetadata(
        name: String,
        kind: String,
        bounds: LatLngBounds,
        minZoom: Double,
        maxZoom: Double,
    ): ByteArray = buildString {
        append(name).append('\u0000')
        append(kind).append('\u0000')
        append(bounds.latitudeSouth).append('\u0000')
        append(bounds.latitudeNorth).append('\u0000')
        append(bounds.longitudeWest).append('\u0000')
        append(bounds.longitudeEast).append('\u0000')
        append(minZoom).append('\u0000')
        append(maxZoom)
    }.encodeToByteArray()

    private data class Meta(
        val name: String,
        val kind: String,
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double,
        val minZoom: Double,
        val maxZoom: Double,
    )

    private fun decodeMetadata(raw: String?): Meta {
        if (raw == null) return Meta("", "", 0.0, 0.0, 0.0, 0.0, 4.0, 14.0)
        val parts = raw.split('\u0000')
        fun d(i: Int): Double = parts.getOrNull(i)?.toDoubleOrNull() ?: 0.0
        return Meta(
            name = parts.getOrNull(0) ?: "",
            kind = parts.getOrNull(1) ?: "",
            minLat = d(2),
            maxLat = d(3),
            minLng = d(4),
            maxLng = d(5),
            minZoom = d(6).takeIf { it > 0 } ?: 4.0,
            maxZoom = d(7).takeIf { it > 0 } ?: 14.0,
        )
    }
}
