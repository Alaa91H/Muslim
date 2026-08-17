package org.muslim.app.feature.adhkar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.adhkarPrefsDataStore by preferencesDataStore(name = "adhkar_prefs")

/**
 * User preferences for the adhkar experience (PROJECT_PROMPT.md §6 Phase 4):
 * the floating overlay message (duration, on/off), which adhkar the user
 * chose to hide, and the optional morning/evening reminders.
 */
data class AdhkarPrefs(
    /** Show adhkar as a floating message above all apps. */
    val overlayEnabled: Boolean = true,
    /** Seconds the floating message stays visible (default 5). */
    val overlayDurationSeconds: Int = 5,
    /** Dhikr ids the user disabled; an absent id means "enabled". */
    val disabledDhikrIds: Set<Long> = emptySet(),
    /** Daily morning adhkar reminder (default 06:00). */
    val morningReminderEnabled: Boolean = false,
    val morningHour: Int = 6,
    val morningMinute: Int = 0,
    /** Daily evening adhkar reminder (default 18:00). */
    val eveningReminderEnabled: Boolean = false,
    val eveningHour: Int = 18,
    val eveningMinute: Int = 0,
) {
    fun isDhikrEnabled(id: Long): Boolean = id !in disabledDhikrIds
}

@Singleton
class AdhkarPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val prefs: Flow<AdhkarPrefs> = context.adhkarPrefsDataStore.data.map { p ->
        AdhkarPrefs(
            overlayEnabled = p[Keys.OVERLAY_ENABLED] ?: true,
            overlayDurationSeconds = (p[Keys.OVERLAY_DURATION] ?: 5).coerceIn(1, 600),
            disabledDhikrIds = (p[Keys.DISABLED_DHIKR_IDS] ?: emptySet())
                .mapNotNull { it.toLongOrNull() }
                .toSet(),
            morningReminderEnabled = p[Keys.MORNING_ENABLED] ?: false,
            morningHour = (p[Keys.MORNING_HOUR] ?: 6).coerceIn(0, 23),
            morningMinute = (p[Keys.MORNING_MINUTE] ?: 0).coerceIn(0, 59),
            eveningReminderEnabled = p[Keys.EVENING_ENABLED] ?: false,
            eveningHour = (p[Keys.EVENING_HOUR] ?: 18).coerceIn(0, 23),
            eveningMinute = (p[Keys.EVENING_MINUTE] ?: 0).coerceIn(0, 59),
        )
    }

    suspend fun setOverlayEnabled(enabled: Boolean) = edit { it[Keys.OVERLAY_ENABLED] = enabled }

    suspend fun setOverlayDurationSeconds(seconds: Int) =
        edit { it[Keys.OVERLAY_DURATION] = seconds.coerceIn(1, 600) }

    suspend fun setDhikrEnabled(id: Long, enabled: Boolean) = edit { prefs ->
        val current = prefs[Keys.DISABLED_DHIKR_IDS] ?: emptySet()
        prefs[Keys.DISABLED_DHIKR_IDS] =
            if (enabled) current - id.toString() else current + id.toString()
    }

    suspend fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) = edit {
        it[Keys.MORNING_ENABLED] = enabled
        it[Keys.MORNING_HOUR] = hour.coerceIn(0, 23)
        it[Keys.MORNING_MINUTE] = minute.coerceIn(0, 59)
    }

    suspend fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) = edit {
        it[Keys.EVENING_ENABLED] = enabled
        it[Keys.EVENING_HOUR] = hour.coerceIn(0, 23)
        it[Keys.EVENING_MINUTE] = minute.coerceIn(0, 59)
    }

    private suspend fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.adhkarPrefsDataStore.edit { transform(it) }
    }

    private object Keys {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val OVERLAY_DURATION = intPreferencesKey("overlay_duration_seconds")
        val DISABLED_DHIKR_IDS = stringSetPreferencesKey("disabled_dhikr_ids")
        val MORNING_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_HOUR = intPreferencesKey("morning_reminder_hour")
        val MORNING_MINUTE = intPreferencesKey("morning_reminder_minute")
        val EVENING_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_HOUR = intPreferencesKey("evening_reminder_hour")
        val EVENING_MINUTE = intPreferencesKey("evening_reminder_minute")
    }
}
