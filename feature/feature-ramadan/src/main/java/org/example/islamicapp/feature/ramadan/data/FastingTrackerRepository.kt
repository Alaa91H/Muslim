package org.example.islamicapp.feature.ramadan.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fastingDataStore by preferencesDataStore(name = "fasting_tracker")

/**
 * Fasting tracker (PROJECT_PROMPT.md §6 Phase 6): per-day fasting records
 * (Ramadan, make-up fasts and sunnah fasts) plus a qada (make-up) counter,
 * stored fully on-device.
 */
@Singleton
class FastingTrackerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fastedDaysKey = stringSetPreferencesKey("fasted_days")
    private val qadaRemainingKey = intPreferencesKey("qada_remaining")

    /** ISO dates the user marked as fasted. */
    val fastedDays: Flow<Set<String>> =
        context.fastingDataStore.data.map { it[fastedDaysKey] ?: emptySet() }

    /** Number of missed Ramadan days still owed. */
    val qadaRemaining: Flow<Int> =
        context.fastingDataStore.data.map { it[qadaRemainingKey] ?: 0 }

    suspend fun toggleDay(date: LocalDate) {
        context.fastingDataStore.edit { prefs ->
            val current = prefs[fastedDaysKey] ?: emptySet()
            prefs[fastedDaysKey] =
                if (date.toString() in current) current - date.toString()
                else current + date.toString()
        }
    }

    suspend fun setDay(date: LocalDate, fasted: Boolean) {
        context.fastingDataStore.edit { prefs ->
            val current = (prefs[fastedDaysKey] ?: emptySet()).toMutableSet()
            if (fasted) current += date.toString() else current -= date.toString()
            prefs[fastedDaysKey] = current
        }
    }

    suspend fun adjustQada(delta: Int) {
        context.fastingDataStore.edit { prefs ->
            prefs[qadaRemainingKey] = ((prefs[qadaRemainingKey] ?: 0) + delta).coerceAtLeast(0)
        }
    }
}
