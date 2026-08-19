package org.muslim.app.feature.adhkar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import javax.inject.Inject
import javax.inject.Singleton

private val Context.adhkarDataStore by preferencesDataStore(name = "adhkar_prefs")

@Serializable
private data class AdhkarSeedFile(val note: String = "", val adhkar: List<AdhkarSeedItem>)

@Serializable
private data class AdhkarSeedItem(
    val id: Long,
    val category: String,
    val arabic: String,
    val translation: String,
    val source: String,
    val repetition: Int,
    val virtue: String? = null,
)

/**
 * Adhkar repository (PROJECT_PROMPT.md §6 Phase 4): seeds the curated,
 * source-attributed adhkar from bundled assets and persists a per-dhikr
 * counter so progress survives app restarts.
 */
@Singleton
class AdhkarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val duasRepository: DuasRepository,
) {

    private val json: Json = Json { ignoreUnknownKeys = true }

    private val cached: List<Dhikr> by lazy { load() + duasRepository.allDuas }

    /** All adhkar, ordered by the seed file. */
    fun observeAdhkar(): Flow<List<Dhikr>> = kotlinx.coroutines.flow.flowOf(cached)

    /** Adhkar of one category, in seed order. */
    fun observeCategory(category: DhikrCategory): Flow<List<Dhikr>> =
        kotlinx.coroutines.flow.flowOf(cached.filter { it.category == category })

    /** Persisted count for [dhikrId] (default 0). */
    fun observeCount(dhikrId: Long): Flow<Int> =
        context.adhkarDataStore.data
            .map { prefs -> prefs[countKey(dhikrId)] ?: 0 }
            // Corrupt persisted data must never crash the adhkar library on entry.
            .catch { emit(0) }

    suspend fun increment(dhikrId: Long) {
        context.adhkarDataStore.edit { prefs ->
            val current = prefs[countKey(dhikrId)] ?: 0
            prefs[countKey(dhikrId)] = current + 1
        }
    }

    suspend fun reset(dhikrId: Long) {
        context.adhkarDataStore.edit { prefs -> prefs.remove(countKey(dhikrId)) }
    }

    /** Total count of all adhkar (informational). */
    suspend fun totalCount(): Int = cached.size

    /**
     * Picks a random enabled dhikr, preferring [category] (used by the daily
     * morning/evening reminders); falls back to any enabled dhikr.
     */
    fun randomDhikr(category: DhikrCategory?, disabledIds: Set<Long>): Dhikr? {
        val pool = cached.filter { it.id !in disabledIds && (category == null || it.category == category) }
        return pool.randomOrNull()
    }

    private fun load(): List<Dhikr> {
        val text = context.assets.open(SEED_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val file = json.decodeFromString<AdhkarSeedFile>(text)
        return file.adhkar.map { item ->
            Dhikr(
                id = item.id,
                category = DhikrCategory.fromId(item.category),
                arabic = item.arabic,
                translation = item.translation,
                source = item.source,
                repetition = item.repetition.coerceAtLeast(1),
                virtue = item.virtue,
            )
        }
    }

    private fun countKey(dhikrId: Long) = intPreferencesKey("count_$dhikrId")

    private companion object {
        const val SEED_ASSET = "adhkar.json"
    }
}
