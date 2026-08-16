package org.example.islamicapp.feature.adhkar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dhikrReminderDataStore by preferencesDataStore(name = "dhikr_reminder")

/**
 * Persists the periodic dhikr-reminder interval (PROJECT_PROMPT.md §6
 * Phase 4 — user-controlled, can be turned off completely).
 *
 * intervalMinutes == 0 means the reminder is disabled.
 */
@Singleton
class DhikrReminderRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val intervalKey = intPreferencesKey("interval_minutes")

    val intervalMinutes: Flow<Int> =
        context.dhikrReminderDataStore.data.map { it[intervalKey] ?: DEFAULT_INTERVAL_MINUTES }

    suspend fun setInterval(minutes: Int) {
        context.dhikrReminderDataStore.edit { it[intervalKey] = minutes }
    }

    companion object {
        const val DISABLED = 0
        const val DEFAULT_INTERVAL_MINUTES = 60

        /** Choices offered in the UI (WorkManager's minimum period is 15 min). */
        val CHOICES_MINUTES = listOf(DISABLED, 30, 60, 180, 360)
    }
}
