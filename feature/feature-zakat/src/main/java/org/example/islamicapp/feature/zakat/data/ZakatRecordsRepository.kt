package org.example.islamicapp.feature.zakat.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.zakatDataStore by preferencesDataStore(name = "zakat_records")

/** A saved zakat calculation (kept across years, PROJECT_PROMPT.md §6 Phase 7). */
@Serializable
data class ZakatRecord(
    /** ISO date of the calculation. */
    val date: String,
    /** "mal" or "fitr". */
    val type: String,
    /** Human-readable summary in the user's currency. */
    val summary: String,
    /** Amount due. */
    val amount: String,
)

/** Persists zakat history as JSON in DataStore — fully offline. */
@Singleton
class ZakatRecordsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val recordsKey = stringPreferencesKey("records")
    private val json = Json { ignoreUnknownKeys = true }

    val records: Flow<List<ZakatRecord>> =
        context.zakatDataStore.data.map { prefs ->
            decode(prefs[recordsKey]).sortedByDescending { it.date }
        }

    suspend fun add(record: ZakatRecord) {
        context.zakatDataStore.edit { prefs ->
            val current = decode(prefs[recordsKey])
            prefs[recordsKey] = json.encodeToString(
                kotlinx.serialization.serializer<List<ZakatRecord>>(),
                current + record,
            )
        }
    }

    suspend fun clear() {
        context.zakatDataStore.edit { it[recordsKey] = "" }
    }

    private fun decode(raw: String?): List<ZakatRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<ZakatRecord>>(raw)
        }.getOrDefault(emptyList())
    }
}
