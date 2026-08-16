package org.example.islamicapp.feature.tasbih.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.islamicapp.feature.tasbih.domain.DailyCount
import org.example.islamicapp.feature.tasbih.domain.TasbihCounter
import org.example.islamicapp.feature.tasbih.domain.TasbihPhrase
import org.example.islamicapp.feature.tasbih.domain.TasbihState
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tasbihDataStore by preferencesDataStore(name = "tasbih_prefs")

/**
 * Misbaha state (PROJECT_PROMPT.md §6 Phase 4): current count with automatic
 * daily roll-over, configurable target, selected phrase and a rolling daily
 * history — all persisted on-device in DataStore.
 */
@Singleton
class TasbihRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val state: Flow<TasbihState> = context.tasbihDataStore.data.map { prefs ->
        val today = LocalDate.now()
        val storedCount = prefs[Keys.COUNT] ?: 0
        val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
        TasbihState(
            count = TasbihCounter.effectiveCount(storedCount, storedDate, today),
            target = prefs[Keys.TARGET] ?: DEFAULT_TARGET,
            phrase = TasbihPhrase.entries.getOrElse(prefs[Keys.PHRASE] ?: 0) { TasbihPhrase.SubhanAllah },
            history = decodeHistory(prefs[Keys.HISTORY]),
        )
    }

    suspend fun increment() {
        context.tasbihDataStore.edit { prefs ->
            val today = LocalDate.now()
            val storedCount = prefs[Keys.COUNT] ?: 0
            val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
            val result = TasbihCounter.increment(storedCount, storedDate, today, decodeHistory(prefs[Keys.HISTORY]))
            prefs[Keys.COUNT] = result.count
            prefs[Keys.DATE] = result.date.toString()
            prefs[Keys.HISTORY] = encodeHistory(result.history)
        }
    }

    suspend fun reset() {
        context.tasbihDataStore.edit { prefs ->
            val today = LocalDate.now()
            val storedCount = prefs[Keys.COUNT] ?: 0
            val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
            if (storedCount > 0) {
                val rolled = (listOf(DailyCount(storedDate, storedCount)) + decodeHistory(prefs[Keys.HISTORY])).take(30)
                prefs[Keys.HISTORY] = encodeHistory(rolled)
            }
            prefs[Keys.COUNT] = 0
            prefs[Keys.DATE] = today.toString()
        }
    }

    suspend fun setTarget(target: Int) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.TARGET] = target.coerceIn(1, 100_000) }
    }

    suspend fun setPhrase(phrase: TasbihPhrase) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.PHRASE] = phrase.ordinal }
    }

    private fun encodeHistory(history: List<DailyCount>): String =
        history.joinToString(";") { "${it.date}|${it.count}" }

    private fun decodeHistory(raw: String?): List<DailyCount> =
        raw.orEmpty().split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 2) return@mapNotNull null
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return@mapNotNull null
            DailyCount(date, parts[1].toIntOrNull() ?: return@mapNotNull null)
        }

    private object Keys {
        val COUNT = intPreferencesKey("count")
        val DATE = stringPreferencesKey("date")
        val TARGET = intPreferencesKey("target")
        val PHRASE = intPreferencesKey("phrase")
        val HISTORY = stringPreferencesKey("history")
    }

    companion object {
        const val DEFAULT_TARGET = 33
    }
}
