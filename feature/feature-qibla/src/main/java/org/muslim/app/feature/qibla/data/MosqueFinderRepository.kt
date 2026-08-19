package org.muslim.app.feature.qibla.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.muslim.app.core.common.HttpAgents
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A nearby mosque from OpenStreetMap. */
data class Mosque(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    /** Metres from the user. */
    val distanceMeters: Int,
    /** Degrees from the user towards the mosque (true bearing). */
    val bearingFromUser: Double,
)

/** Wire format of the Overpass JSON response (only what we need). */
@Serializable
private data class OverpassResponse(val elements: List<OverpassElement> = emptyList())

@Serializable
private data class OverpassElement(
    val type: String = "",
    val id: Long = 0,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap(),
)

/** Ways/relations report their geometry center instead of lat/lon. */
@Serializable
private data class OverpassCenter(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
)

/**
 * Mosque finder (PROJECT_PROMPT.md §6 Phase 8 "باحث المساجد القريبة")
 * powered by the OpenStreetMap Overpass API — no API key, no tracking
 * (the query sends only the rough coordinates, as required by §12).
 */
@Singleton
class MosqueFinderRepository @Inject constructor() {

    /** Public Overpass endpoints tried in order (the main one is often overloaded). */
    private val OVERPASS_ENDPOINTS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.private.coffee/api/interpreter",
    )

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", HttpAgents.APP_USER_AGENT)
                    .build(),
            )
        }
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches mosques within [radiusMeters] of the location, sorted by
     * distance. Requires connectivity; throws on network failure.
     */
    suspend fun nearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 5000,
    ): List<Mosque> = withContext(Dispatchers.IO) {
        // Queries both point mosques (nodes) and building outlines (ways) so
        // the finder works in every corner of the world, not just where
        // mosques happen to be mapped as single nodes. `out center` returns
        // the centroid for ways.
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              node["building"="mosque"](around:$radiusMeters,$latitude,$longitude);
              way["building"="mosque"](around:$radiusMeters,$latitude,$longitude);
              node["amenity"="mosque"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="mosque"](around:$radiusMeters,$latitude,$longitude);
            );
            out center 120;
        """.trimIndent()

        var failure: Exception? = null
        for (endpoint in OVERPASS_ENDPOINTS) {
            try {
                return@withContext fetch(endpoint, query, latitude, longitude)
            } catch (e: Exception) {
                failure = e
            }
        }
        throw failure ?: IllegalStateException("No Overpass endpoint configured")
    }

    private fun fetch(
        endpoint: String,
        query: String,
        latitude: Double,
        longitude: Double,
    ): List<Mosque> {
        val request = Request.Builder()
            .url(endpoint)
            .post(query.toRequestBody("text/plain".toMediaType()))
            .build()

        return client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            val body = response.body?.string().orEmpty()
            val parsed = json.decodeFromString<OverpassResponse>(body)
            parsed.elements
                .mapNotNull { element ->
                    val lat = element.lat
                    val lon = element.lon
                    val (effLat, effLon) = when {
                        lat != 0.0 && lon != 0.0 -> lat to lon
                        element.center != null && element.center.lat != 0.0 -> element.center.lat to element.center.lon
                        else -> return@mapNotNull null
                    }
                    val d = haversineMeters(latitude, longitude, effLat, effLon)
                    Mosque(
                        name = element.tags["name"]
                            ?: element.tags["name:ar"]
                            ?: element.tags["name:en"]
                            ?: element.tags["name:fr"]
                            ?: "مسجد",
                        latitude = effLat,
                        longitude = effLon,
                        distanceMeters = d.toInt(),
                        bearingFromUser = initialBearing(latitude, longitude, effLat, effLon),
                    )
                }
                .distinctBy { "%.5f,%.5f".format(it.latitude, it.longitude) }
                .sortedBy { it.distanceMeters }
        }
    }

    /** Great-circle distance in metres. */
    internal fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    /** Initial true bearing (degrees clockwise from north) from 1 → 2. */
    internal fun initialBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dl = Math.toRadians(lon2 - lon1)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }
}
