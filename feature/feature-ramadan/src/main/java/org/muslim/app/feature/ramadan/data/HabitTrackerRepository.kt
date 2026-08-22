package org.muslim.app.feature.ramadan.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.ramadan.domain.HabitId
import org.muslim.app.feature.ramadan.domain.HabitTrackerState
import org.muslim.app.feature.ramadan.domain.HabitTrackerCalculator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.habitTrackerDataStore by preferencesDataStore(name = "habit_tracker_prefs")

/** Local persistence for daily worship habits and the Ramadan plan. */
@Singleton
class HabitTrackerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val state: Flow<HabitTrackerState> = context.habitTrackerDataStore.data.map { prefs ->
        HabitTrackerState(
            records = decodeRecords(prefs[Keys.RECORDS]),
            khatmaJuz = (prefs[Keys.KHATMA_JUZ] ?: 0).coerceIn(0, HabitTrackerCalculator.RAMADAN_JUZ_COUNT),
            taraweehDates = prefs[Keys.TARAWEEH_DATES].orEmpty().mapNotNull(::parseDate).toSet(),
            itikafEnabled = prefs[Keys.ITIKAF_ENABLED] ?: false,
        )
    }

    suspend fun toggleHabit(date: LocalDate, habit: HabitId) {
        context.habitTrackerDataStore.edit { prefs ->
            val current = decodeRecords(prefs[Keys.RECORDS]).toMutableMap()
            val habits = current[date].orEmpty().toMutableSet()
            if (!habits.add(habit)) habits.remove(habit)
            if (habits.isEmpty()) current.remove(date) else current[date] = habits
            prefs[Keys.RECORDS] = encodeRecords(current)
        }
    }

    suspend fun setKhatmaJuz(juz: Int) {
        context.habitTrackerDataStore.edit { prefs ->
            prefs[Keys.KHATMA_JUZ] = juz.coerceIn(0, HabitTrackerCalculator.RAMADAN_JUZ_COUNT)
        }
    }

    suspend fun toggleTaraweeh(date: LocalDate) {
        context.habitTrackerDataStore.edit { prefs ->
            val dates = prefs[Keys.TARAWEEH_DATES].orEmpty().toMutableSet()
            val encoded = date.toString()
            if (!dates.add(encoded)) dates.remove(encoded)
            prefs[Keys.TARAWEEH_DATES] = dates
        }
    }

    suspend fun setItikafEnabled(enabled: Boolean) {
        context.habitTrackerDataStore.edit { prefs -> prefs[Keys.ITIKAF_ENABLED] = enabled }
    }

    private fun encodeRecords(records: Map<LocalDate, Set<HabitId>>): Set<String> =
        records.map { (date, habits) ->
            "$date|${habits.joinToString(",") { it.name }}"
        }.toSet()

    private fun decodeRecords(raw: Set<String>?): Map<LocalDate, Set<HabitId>> =
        raw.orEmpty().mapNotNull { entry ->
            val separator = entry.indexOf('|')
            if (separator <= 0) return@mapNotNull null
            val date = parseDate(entry.substring(0, separator)) ?: return@mapNotNull null
            val habits = entry.substring(separator + 1)
                .split(',')
                .mapNotNull { value -> runCatching { HabitId.valueOf(value) }.getOrNull() }
                .toSet()
            if (habits.isEmpty()) null else date to habits
        }.toMap()

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()

    private object Keys {
        val RECORDS = stringSetPreferencesKey("daily_records")
        val KHATMA_JUZ = intPreferencesKey("khatma_juz")
        val TARAWEEH_DATES = stringSetPreferencesKey("taraweeh_dates")
        val ITIKAF_ENABLED = booleanPreferencesKey("itikaf_enabled")
    }
}
