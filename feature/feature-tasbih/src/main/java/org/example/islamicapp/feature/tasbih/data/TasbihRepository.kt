package org.example.islamicapp.feature.tasbih.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tasbihDataStore by preferencesDataStore(name = "tasbih_stats")

/** Persisted tasbih statistics: daily totals + user target (Phase 4). */
data class TasbihStats(
    val target: Int = 33,
    val today: Int = 0,
    /** ISO-date → total counted that day (most recent days kept). */
    val history: Map<LocalDate, Int> = emptyMap(),
)

/**
 * Stores the daily totals as a compact JSON map so the weekly chart and the
 * widget can render without any network or account (offline-first).
 */
@Singleton
class TasbihRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val todayKey = stringPreferencesKey("today")
    private val todayTotalKey = intPreferencesKey("today_total")
    private val historyKey = stringPreferencesKey("history")
    private val targetKey = intPreferencesKey("target")

    /** Injected date provider so day-rollover is unit-testable. */
    var todayProvider: () -> LocalDate = LocalDate::now

    private val json = Json { ignoreUnknownKeys = true }

    val stats: Flow<TasbihStats> = context.tasbihDataStore.data.map { prefs ->
        val now = todayProvider()
        val storedDay = prefs[todayKey]?.let(LocalDate::parse)
        // Day rolled over since the last write: today starts at zero.
        val todayCount = if (storedDay == now) prefs[todayTotalKey] ?: 0 else 0
        TasbihStats(
            target = prefs[targetKey] ?: 33,
            today = todayCount,
            history = parseHistory(prefs[historyKey]) + (now to todayCount),
        )
    }

    /** Adds [delta] taps to today's total (delta = 1 per tap normally). */
    suspend fun addToToday(delta: Int) {
        context.tasbihDataStore.edit { prefs ->
            val today = todayProvider()
            val storedDay = prefs[todayKey]?.let(LocalDate::parse)
            val base = if (storedDay == today) prefs[todayTotalKey] ?: 0 else 0
            prefs[todayKey] = today.toString()
            prefs[todayTotalKey] = base + delta
            val history = parseHistory(prefs[historyKey]).toMutableMap()
            history[today] = base + delta
            prefs[historyKey] = encodeHistory(history)
        }
    }

    suspend fun resetToday() {
        context.tasbihDataStore.edit { prefs ->
            val today = todayProvider()
            prefs[todayKey] = today.toString()
            prefs[todayTotalKey] = 0
            val history = parseHistory(prefs[historyKey]).toMutableMap()
            history[today] = 0
            prefs[historyKey] = encodeHistory(history)
        }
    }

    suspend fun setTarget(target: Int) {
        context.tasbihDataStore.edit { it[targetKey] = target }
    }

    private fun parseHistory(raw: String?): Map<LocalDate, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, Int>>(raw)
                .mapKeys { (k, _) -> LocalDate.parse(k) }
        }.getOrDefault(emptyMap())
    }

    private fun encodeHistory(history: Map<LocalDate, Int>): String =
        json.encodeToString(
            kotlinx.serialization.serializer<Map<String, Int>>(),
            history.mapKeys { (k, _) -> k.toString() },
        )

    companion object {
        /** Only the most recent days are retained (bounded storage). */
        const val HISTORY_LIMIT_DAYS = 30
    }
}
