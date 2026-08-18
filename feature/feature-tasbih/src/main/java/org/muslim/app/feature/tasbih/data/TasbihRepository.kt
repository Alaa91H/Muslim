package org.muslim.app.feature.tasbih.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.tasbih.domain.DailyCount
import org.muslim.app.feature.tasbih.domain.TasbihCounter
import org.muslim.app.feature.tasbih.domain.TargetSoundSettings
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihState
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tasbihDataStore by preferencesDataStore(name = "tasbih_prefs")

/**
 * Misbaha state (PROJECT_PROMPT.md §6 Phase 4): an independent daily counter
 * per dhikr phrase (so switching phrases never loses progress), a configurable
 * target, undo, per-phrase reset, and a rolling daily history of totals.
 */
@Singleton
class TasbihRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Sound-on-target preferences (toggle + chosen system tone). */
    val targetSoundSettings: Flow<TargetSoundSettings> = context.tasbihDataStore.data.map { prefs ->
        TargetSoundSettings(
            enabled = prefs[Keys.SOUND_ENABLED] ?: false,
            tone = prefs[Keys.SOUND_TONE] ?: TargetSoundSettings.TONE_NOTIFICATION,
        )
    }

    suspend fun setTargetSoundEnabled(enabled: Boolean) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setTargetSoundTone(tone: String) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.SOUND_TONE] = tone }
    }

    val state: Flow<TasbihState> = context.tasbihDataStore.data.map { prefs ->
        val today = LocalDate.now()
        val storedCounts = decodeCounts(prefs[Keys.COUNTS])
        val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
        TasbihState(
            counts = TasbihCounter.effectiveCounts(storedCounts, storedDate, today),
            target = prefs[Keys.TARGET] ?: DEFAULT_TARGET,
            phrase = TasbihPhrase.entries.getOrElse(prefs[Keys.PHRASE] ?: 0) { TasbihPhrase.SubhanAllah },
            history = decodeHistory(prefs[Keys.HISTORY]),
        )
    }

    suspend fun increment(phrase: TasbihPhrase) {
        context.tasbihDataStore.edit { prefs ->
            val today = LocalDate.now()
            val storedCounts = decodeCounts(prefs[Keys.COUNTS])
            val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
            val result = TasbihCounter.increment(
                storedCounts, storedDate, today, decodeHistory(prefs[Keys.HISTORY]), phrase,
            )
            prefs[Keys.COUNTS] = encodeCounts(result.counts)
            prefs[Keys.DATE] = result.date.toString()
            prefs[Keys.HISTORY] = encodeHistory(result.history)
        }
    }

    suspend fun decrement(phrase: TasbihPhrase) {
        context.tasbihDataStore.edit { prefs ->
            val today = LocalDate.now()
            val effective = effectiveCounts(prefs, today)
            prefs[Keys.COUNTS] = encodeCounts(TasbihCounter.decrement(effective, phrase))
            prefs[Keys.DATE] = today.toString()
        }
    }

    suspend fun reset(phrase: TasbihPhrase) {
        context.tasbihDataStore.edit { prefs ->
            val today = LocalDate.now()
            val effective = effectiveCounts(prefs, today)
            prefs[Keys.COUNTS] = encodeCounts(TasbihCounter.resetPhrase(effective, phrase))
            prefs[Keys.DATE] = today.toString()
        }
    }

    /** Zeroes every phrase's counter (the active phrase and all others). */
    suspend fun resetAll() {
        context.tasbihDataStore.edit { prefs ->
            prefs[Keys.COUNTS] = ""
            prefs[Keys.DATE] = LocalDate.now().toString()
        }
    }

    suspend fun setTarget(target: Int) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.TARGET] = target.coerceIn(1, 100_000) }
    }

    suspend fun setPhrase(phrase: TasbihPhrase) {
        context.tasbihDataStore.edit { prefs -> prefs[Keys.PHRASE] = phrase.ordinal }
    }

    /** The active daily counters, normalized for [today] (empty on a new day). */
    private fun effectiveCounts(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        today: LocalDate,
    ): Map<TasbihPhrase, Int> {
        val stored = decodeCounts(prefs[Keys.COUNTS])
        val storedDate = prefs[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
        return TasbihCounter.effectiveCounts(stored, storedDate, today)
    }

    private fun encodeCounts(counts: Map<TasbihPhrase, Int>): String =
        counts.entries.joinToString(";") { "${it.key.ordinal}:${it.value}" }

    private fun decodeCounts(raw: String?): Map<TasbihPhrase, Int> =
        raw.orEmpty().split(";").mapNotNull { entry ->
            if (entry.isEmpty()) return@mapNotNull null
            val parts = entry.split(":")
            if (parts.size != 2) return@mapNotNull null
            val phrase = TasbihPhrase.entries.getOrNull(parts[0].toIntOrNull() ?: return@mapNotNull null)
                ?: return@mapNotNull null
            val count = parts[1].toIntOrNull() ?: return@mapNotNull null
            phrase to count
        }.toMap()

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
        val COUNTS = stringPreferencesKey("counts")
        val DATE = stringPreferencesKey("date")
        val TARGET = intPreferencesKey("target")
        val PHRASE = intPreferencesKey("phrase")
        val HISTORY = stringPreferencesKey("history")
        val SOUND_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("sound_on_target_enabled")
        val SOUND_TONE = stringPreferencesKey("sound_on_target_tone")
    }

    companion object {
        const val DEFAULT_TARGET = 33
    }
}
