package org.muslim.app.feature.qibla.data

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.muslim.app.core.common.HttpAgents
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.location.GeoLocation
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A persisted mosque place; user-specific distances are deliberately not cached. */
@Serializable
data class MosquePlace(
    val osmId: Long,
    val osmType: String,
    val name: String? = null,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
)

/** A mosque rendered for a particular current user location. */
data class NearbyMosque(
    val place: MosquePlace,
    val distanceMeters: Double,
)

/** Cached OSM places plus the source location/radius used to obtain the data. */
@Serializable
data class NearbyMosqueCache(
    val places: List<MosquePlace>,
    val sourceLatitude: Double,
    val sourceLongitude: Double,
    val sourceRadiusKm: Int,
)

/** Values shown in the radius selector and accepted by the network query. */
val NearbyMosqueRadiusOptionsKm = listOf(1, 3, 5, 10)

private const val CACHE_FRESH_FOR_MILLIS = 10 * 60 * 1_000L
private const val MAX_RESULTS = 100
private const val EARTH_RADIUS_METERS = 6_371_008.8
private const val OVERPASS_ENDPOINT = "https://overpass-api.de/api/interpreter"

/**
 * Fetches the minimum OSM data necessary for a lightweight nearby-mosque list.
 * It uses the app-wide OkHttp client and has no map rendering or map SDK dependency.
 */
@Singleton
class OverpassMosqueDataSource @Inject constructor(
    private val client: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(location: GeoLocation, radiusKm: Int): List<MosquePlace> {
        require(radiusKm in NearbyMosqueRadiusOptionsKm) { "Unsupported mosque radius: $radiusKm" }
        require(location.isValid()) { "Invalid location" }
        val query = buildQuery(location, radiusKm)
        val url = OVERPASS_ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("data", query)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", HttpAgents.APP_USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()
        val responseBody = client.newCall(request).awaitBody()
        val parsed = runCatching { json.decodeFromString<OverpassResponse>(responseBody) }
            .getOrElse { throw MosqueDataException("Could not read nearby-mosque data", it) }

        return parsed.elements.asSequence()
            .mapNotNull(OverpassElement::toMosquePlaceOrNull)
            .distinctBy { "${it.osmType}/${it.osmId}" }
            .take(MAX_RESULTS)
            .toList()
    }

    internal fun buildQuery(location: GeoLocation, radiusKm: Int): String {
        val radiusMeters = radiusKm * 1_000
        val latitude = "%.6f".format(java.util.Locale.US, location.latitude)
        val longitude = "%.6f".format(java.util.Locale.US, location.longitude)
        return """
            [out:json][timeout:8];
            (
              nwr["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              nwr["amenity"="place_of_worship"]["place_of_worship"="mosque"](around:$radiusMeters,$latitude,$longitude);
              nwr["amenity"="mosque"](around:$radiusMeters,$latitude,$longitude);
            );
            out center $MAX_RESULTS;
        """.trimIndent()
    }
}

/**
 * Single repository for mosque fetching and caching. The cache contains only OSM
 * places; every presentation recalculates distances from the latest usable fix.
 */
@Singleton
class NearbyMosqueRepository @Inject constructor(
    private val source: OverpassMosqueDataSource,
    private val preferencesRepository: AppPreferencesRepository,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun searchAndCache(location: GeoLocation, radiusKm: Int, nowMillis: Long): List<MosquePlace> {
        val places = source.search(location, radiusKm)
        val cache = NearbyMosqueCache(
            places = places,
            sourceLatitude = location.latitude,
            sourceLongitude = location.longitude,
            sourceRadiusKm = radiusKm,
        )
        preferencesRepository.setNearbyMosqueCache(json.encodeToString(cache), nowMillis)
        return places
    }

    suspend fun setRadius(radiusKm: Int) = preferencesRepository.setNearbyMosqueSearchRadiusKm(radiusKm)

    fun cacheFrom(preferences: AppPreferences): NearbyMosqueCache? =
        preferences.nearbyMosqueCacheJson.takeIf(String::isNotBlank)
            ?.let { raw -> runCatching { json.decodeFromString<NearbyMosqueCache>(raw) }.getOrNull() }
            ?.takeIf { cache -> cache.places.all { it.isValid() } }

    fun isFresh(preferences: AppPreferences, nowMillis: Long): Boolean =
        preferences.nearbyMosqueCacheSavedAtEpochMillis > 0L &&
            nowMillis >= preferences.nearbyMosqueCacheSavedAtEpochMillis &&
            nowMillis - preferences.nearbyMosqueCacheSavedAtEpochMillis <= CACHE_FRESH_FOR_MILLIS

    /** Filters and orders cache/network places with a new location; never trusts source ordering. */
    fun nearbyFor(location: GeoLocation, places: List<MosquePlace>, radiusKm: Int): List<NearbyMosque> {
        val maxDistanceMeters = radiusKm * 1_000.0
        return places.asSequence()
            .filter(MosquePlace::isValid)
            .map { place -> NearbyMosque(place, distanceMeters(location, place)) }
            .filter { it.distanceMeters <= maxDistanceMeters }
            .sortedBy(NearbyMosque::distanceMeters)
            .toList()
    }

    fun distanceMeters(location: GeoLocation, place: MosquePlace): Double =
        haversineMeters(location.latitude, location.longitude, place.latitude, place.longitude)

    fun distanceMeters(from: GeoLocation, to: GeoLocation): Double =
        haversineMeters(from.latitude, from.longitude, to.latitude, to.longitude)
}

/** Great-circle distance calculated locally, independent of Overpass response ordering. */
internal fun haversineMeters(fromLatitude: Double, fromLongitude: Double, toLatitude: Double, toLongitude: Double): Double {
    val latDelta = Math.toRadians(toLatitude - fromLatitude)
    val lonDelta = Math.toRadians(toLongitude - fromLongitude)
    val a = sin(latDelta / 2).let { it * it } +
        cos(Math.toRadians(fromLatitude)) * cos(Math.toRadians(toLatitude)) *
            sin(lonDelta / 2).let { it * it }
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

private fun GeoLocation.isValid(): Boolean =
    latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0

private fun MosquePlace.isValid(): Boolean =
    latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0

private class MosqueDataException(message: String, cause: Throwable? = null) : IOException(message, cause)

@Serializable
private data class OverpassResponse(val elements: List<OverpassElement> = emptyList())

@Serializable
private data class OverpassElement(
    val id: Long,
    val type: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap(),
) {
    fun toMosquePlaceOrNull(): MosquePlace? {
        val latitude = lat ?: center?.lat ?: return null
        val longitude = lon ?: center?.lon ?: return null
        val place = MosquePlace(
            osmId = id,
            osmType = type,
            name = tags["name"]?.trim()?.takeIf(String::isNotBlank),
            address = formatAddress(tags),
            latitude = latitude,
            longitude = longitude,
        )
        return place.takeIf(MosquePlace::isValid)
    }
}

@Serializable
private data class OverpassCenter(val lat: Double, val lon: Double)

private fun formatAddress(tags: Map<String, String>): String? {
    tags["addr:full"]?.trim()?.takeIf(String::isNotBlank)?.let { return it }
    return listOfNotNull(
        listOfNotNull(tags["addr:housenumber"], tags["addr:street"])
            .joinToString(" ").trim().takeIf(String::isNotBlank),
        tags["addr:city"]?.trim()?.takeIf(String::isNotBlank),
    ).joinToString(", ").takeIf(String::isNotBlank)
}

private suspend fun Call.awaitBody(): String = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(MosqueDataException("Nearby-mosque service returned HTTP ${it.code}"))
                    }
                    return
                }
                val body = it.body.string()
                if (continuation.isActive) continuation.resume(body)
            }
        }
    })
    continuation.invokeOnCancellation { cancel() }
}
