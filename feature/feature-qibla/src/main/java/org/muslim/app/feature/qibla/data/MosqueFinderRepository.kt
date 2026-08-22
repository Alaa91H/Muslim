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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A nearby mosque from OpenStreetMap. */
@Serializable
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

    /**
     * Public Overpass endpoints tried in order. The main German instance is
     * chronically overloaded (HTTP 504/connection drops under load) and the
     * community mirrors come and go, so the list is ordered by measured
     * reliability and the fetch loop retries all of them. fr.openstreetmap
     * mirrors the full planet and is the most dependable day to day.
     */
    private val OVERPASS_ENDPOINTS = listOf(
        "https://overpass.openstreetmap.fr/api/interpreter",
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.private.coffee/api/interpreter",
        "https://overpass.nchc.org.tw/api/interpreter",
    )

    /**
     * Generous timeouts are essential: OkHttp's 10 s default read timeout
     * is far too short for Overpass — a query for a busy city routinely
     * takes 5–15 s to assemble and return, and the default would abort the
     * call right as the server starts answering.
     */
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
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
        val query = buildQuery(latitude, longitude, radiusMeters)
        var failure: Exception? = null
        var hadSuccessfulResponse = false
        // Multiple full passes over the endpoints: Overpass instances are
        // intermittently overloaded (HTTP 504 gateway timeouts, connection
        // resets) yet answer fine moments later, so a single pass frequently
        // fails even though the network is healthy. A short pause between
        // passes lets a recovering instance come back. A hard deadline keeps
        // even a total outage from blocking the screen for minutes.
        val deadline = System.currentTimeMillis() + 75_000L
        repeat(3) { pass ->
            for (endpoint in OVERPASS_ENDPOINTS) {
                if (System.currentTimeMillis() > deadline) {
                    throw failure ?: IllegalStateException("Overpass search timed out")
                }
                try {
                    val results = fetch(endpoint, query, latitude, longitude)
                    hadSuccessfulResponse = true
                    // A healthy endpoint can still return an empty set when
                    // its local database is lagging. Keep trying the other
                    // mirrors before concluding that the city has no mosques.
                    if (results.isNotEmpty()) return@withContext results
                } catch (e: Exception) {
                    failure = e
                }
            }
            if (pass < 2) Thread.sleep(2_000)
        }
        if (hadSuccessfulResponse) return@withContext emptyList()
        throw failure ?: IllegalStateException("No Overpass endpoint configured")
    }

    /**
     * Finds the nearest mosques by widening the search radius automatically
     * (1 km → 1 000 km) until at least one mosque is found, so a user in a
     * remote area still gets a result without guessing a radius. Returns the
     * list from the first radius that yields matches.
     */
    suspend fun nearbyNearest(
        latitude: Double,
        longitude: Double,
        onRadiusKm: suspend (Int) -> Unit = {},
    ): List<Mosque> = withContext(Dispatchers.IO) {
        nearbyNearestWith(
            fetch = { radiusKm -> nearby(latitude, longitude, radiusKm * 1000) },
            onRadiusKm = onRadiusKm,
        )
    }

    /** Purely injectable search loop used by the production method and JVM tests. */
    internal suspend fun nearbyNearestWith(
        fetch: suspend (radiusKm: Int) -> List<Mosque>,
        onRadiusKm: suspend (Int) -> Unit = {},
        timeBudgetMillis: Long = 90_000L,
    ): List<Mosque> {
        val radiiKm = listOf(1, 3, 5, 10, 25, 50, 100, 250, 500, 1000)
        val deadline = System.currentTimeMillis() + timeBudgetMillis
        for (radiusKm in radiiKm) {
            // When every Overpass instance is unreachable, stop expanding once
            // the budget is spent instead of chaining ten slow timeouts.
            if (System.currentTimeMillis() > deadline) break
            onRadiusKm(radiusKm)
            val found = fetch(radiusKm).sortedBy { it.distanceMeters }
            if (found.isNotEmpty()) return found
        }
        return emptyList()
    }

    internal fun buildQuery(latitude: Double, longitude: Double, radiusMeters: Int): String =
        // `nwr` matches nodes, ways and relations in one statement, and the
        // tags cover every way OSM maps a mosque (place_of_worship+muslim is
        // the canonical one; building=mosque / amenity=mosque are legacy).
        // `out center qt` returns centroids for ways/relations and sorts by
        // quadtile — much faster than the default ordering on large results.
        """
            [out:json][timeout:30];
            (
              nwr["amenity"="mosque"](around:$radiusMeters,$latitude,$longitude);
              nwr["building"="mosque"](around:$radiusMeters,$latitude,$longitude);
              nwr["building"="masjid"](around:$radiusMeters,$latitude,$longitude);
              nwr["place_of_worship"="mosque"](around:$radiusMeters,$latitude,$longitude);
              nwr["place_of_worship"="masjid"](around:$radiusMeters,$latitude,$longitude);
              nwr["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              nwr["amenity"="place_of_worship"]["religion"="islam"](around:$radiusMeters,$latitude,$longitude);
            );
            out center qt;
        """.trimIndent()

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
