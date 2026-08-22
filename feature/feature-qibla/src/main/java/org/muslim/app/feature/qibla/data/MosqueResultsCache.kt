package org.muslim.app.feature.qibla.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A persisted snapshot of the last successful mosque search. */
@Serializable
data class MosqueSearchSnapshot(
    val latitude: Double,
    val longitude: Double,
    val mosques: List<Mosque>,
    val savedAtEpochMillis: Long,
)

/**
 * Small device-local cache for mosque results. It deliberately stores only
 * public OSM result data; no account or location history is uploaded.
 */
class MosqueResultsCache(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): MosqueSearchSnapshot? = preferences.getString(KEY_SNAPSHOT, null)
        ?.let { encoded -> runCatching { json.decodeFromString<MosqueSearchSnapshot>(encoded) }.getOrNull() }

    fun save(snapshot: MosqueSearchSnapshot) {
        preferences.edit()
            .putString(KEY_SNAPSHOT, json.encodeToString(snapshot))
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "mosque_search_cache"
        const val KEY_SNAPSHOT = "last_successful_search"
    }
}
