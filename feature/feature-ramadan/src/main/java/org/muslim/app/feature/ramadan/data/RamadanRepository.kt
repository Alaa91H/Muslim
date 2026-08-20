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
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.ramadanDataStore by preferencesDataStore(name = "ramadan_prefs")

/** Ramadan-mode user settings (PROJECT_PROMPT.md §6 Phase 6). */
data class RamadanSettings(
    /** Gregorian dates marked as fasted (ISO strings in storage). */
    val fastingDays: Set<LocalDate> = emptySet(),
    val iftarNotificationEnabled: Boolean = true,
    val suhoorReminderEnabled: Boolean = true,
    /** Minutes before Fajr to remind the user to have suhoor. */
    val suhoorMinutesBefore: Int = 30,
    /**
     * When false (default), iftar/suhoor alarms only fire during Ramadan;
     * the user can opt into year-round reminders by enabling this.
     */
    val notifyOutsideRamadan: Boolean = false,
)

/**
 * Persists Ramadan settings: the fasting tracker and the suhoor/iftar
 * notification toggles.
 */
@Singleton
class RamadanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val settings: Flow<RamadanSettings> = context.ramadanDataStore.data.map { prefs ->
        RamadanSettings(
            fastingDays = prefs[Keys.FASTING_DAYS].orEmpty().mapNotNull { iso ->
                runCatching { LocalDate.parse(iso) }.getOrNull()
            }.toSet(),
            iftarNotificationEnabled = prefs[Keys.IFTAR_NOTIFICATION] ?: true,
            suhoorReminderEnabled = prefs[Keys.SUHOOR_REMINDER] ?: true,
            suhoorMinutesBefore = prefs[Keys.SUHOOR_MINUTES] ?: 30,
            notifyOutsideRamadan = prefs[Keys.NOTIFY_OUTSIDE_RAMADAN] ?: false,
        )
    }

    suspend fun toggleFastingDay(date: LocalDate) {
        context.ramadanDataStore.edit { prefs ->
            val current = prefs[Keys.FASTING_DAYS].orEmpty().toMutableSet()
            val iso = date.toString()
            if (!current.add(iso)) current.remove(iso)
            prefs[Keys.FASTING_DAYS] = current
        }
    }

    suspend fun setIftarNotificationEnabled(enabled: Boolean) {
        context.ramadanDataStore.edit { prefs -> prefs[Keys.IFTAR_NOTIFICATION] = enabled }
    }

    suspend fun setSuhoorReminderEnabled(enabled: Boolean) {
        context.ramadanDataStore.edit { prefs -> prefs[Keys.SUHOOR_REMINDER] = enabled }
    }

    suspend fun setSuhoorMinutesBefore(minutes: Int) {
        context.ramadanDataStore.edit { prefs -> prefs[Keys.SUHOOR_MINUTES] = minutes.coerceIn(5, 120) }
    }

    suspend fun setNotifyOutsideRamadan(enabled: Boolean) {
        context.ramadanDataStore.edit { prefs -> prefs[Keys.NOTIFY_OUTSIDE_RAMADAN] = enabled }
    }

    private object Keys {
        val FASTING_DAYS = stringSetPreferencesKey("fasting_days")
        val IFTAR_NOTIFICATION = booleanPreferencesKey("iftar_notification")
        val SUHOOR_REMINDER = booleanPreferencesKey("suhoor_reminder")
        val SUHOOR_MINUTES = intPreferencesKey("suhoor_minutes")
        val NOTIFY_OUTSIDE_RAMADAN = booleanPreferencesKey("notify_outside_ramadan")
    }
}
