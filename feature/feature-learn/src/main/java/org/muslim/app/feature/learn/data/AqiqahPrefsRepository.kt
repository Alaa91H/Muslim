package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.learn.domain.AqiqahCalculator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aqiqahDataStore by preferencesDataStore(name = "family_life_prefs")

/** Local storage for the optional aqiqah reminder. No child data leaves the device. */
@Singleton
class AqiqahPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val birthDate: Flow<LocalDate?> = context.aqiqahDataStore.data.map { preferences ->
        preferences[Keys.BIRTH_DATE]?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
    }

    val reminderEnabled: Flow<Boolean> = context.aqiqahDataStore.data.map { preferences ->
        preferences[Keys.REMINDER_ENABLED] ?: false
    }

    suspend fun setBirthDate(date: LocalDate?) {
        context.aqiqahDataStore.edit { preferences ->
            if (date == null) preferences.remove(Keys.BIRTH_DATE)
            else preferences[Keys.BIRTH_DATE] = date.toString()
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.aqiqahDataStore.edit { preferences ->
            preferences[Keys.REMINDER_ENABLED] = enabled
        }
    }

    /** Only used by tests and migration-safe callers to validate the limit. */
    fun isReminderDateSupported(date: LocalDate, today: LocalDate): Boolean =
        AqiqahCalculator.daysUntilFirst(date, today) >= 0

    private object Keys {
        val BIRTH_DATE = stringPreferencesKey("aqiqah_birth_date")
        val REMINDER_ENABLED = booleanPreferencesKey("aqiqah_reminder_enabled")
    }
}
