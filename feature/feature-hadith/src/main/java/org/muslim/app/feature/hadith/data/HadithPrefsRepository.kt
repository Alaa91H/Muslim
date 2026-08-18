package org.muslim.app.feature.hadith.data

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

private val Context.hadithPrefsDataStore by preferencesDataStore(name = "hadith_prefs")

/** Device-local hadith bookmarks (ids of saved hadiths). */
@Singleton
class HadithPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val bookmarkedIds: Flow<Set<Long>> =
        context.hadithPrefsDataStore.data.map { prefs ->
            prefs[Keys.BOOKMARKS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    /** Version of the bundled corpus already seeded (0 = never seeded). */
    val seedVersion: Flow<Int> =
        context.hadithPrefsDataStore.data.map { it[Keys.SEED_VERSION] ?: 0 }

    /** Whether the daily hadith notification is enabled (default: on). */
    val dailyNotificationEnabled: Flow<Boolean> =
        context.hadithPrefsDataStore.data.map { it[Keys.DAILY_NOTIFICATION] ?: true }

    suspend fun setDailyNotificationEnabled(enabled: Boolean) {
        context.hadithPrefsDataStore.edit { prefs ->
            prefs[Keys.DAILY_NOTIFICATION] = enabled
        }
    }

    /** The daily notification time, in minutes from midnight (default 08:00). */
    val dailyNotificationTimeMinutes: Flow<Int> =
        context.hadithPrefsDataStore.data.map { prefs ->
            prefs[Keys.DAILY_NOTIFICATION_TIME] ?: DEFAULT_NOTIFICATION_TIME_MINUTES
        }

    suspend fun setDailyNotificationTimeMinutes(minutes: Int) {
        context.hadithPrefsDataStore.edit { prefs ->
            prefs[Keys.DAILY_NOTIFICATION_TIME] = minutes.coerceIn(0, 23 * 60 + 59)
        }
    }

    suspend fun setSeedVersion(version: Int) {
        context.hadithPrefsDataStore.edit { prefs ->
            prefs[Keys.SEED_VERSION] = version
        }
    }

    suspend fun addBookmark(id: Long) {
        context.hadithPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKS]?.toMutableSet() ?: mutableSetOf()
            current.add(id.toString())
            prefs[Keys.BOOKMARKS] = current
        }
    }

    suspend fun removeBookmark(id: Long) {
        context.hadithPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKS]?.toMutableSet() ?: mutableSetOf()
            current.remove(id.toString())
            prefs[Keys.BOOKMARKS] = current
        }
    }

    private object Keys {
        val BOOKMARKS = stringSetPreferencesKey("hadith_bookmarks")
        val SEED_VERSION = intPreferencesKey("hadith_seed_version")
        val DAILY_NOTIFICATION = booleanPreferencesKey("hadith_daily_notification")
        val DAILY_NOTIFICATION_TIME = intPreferencesKey("hadith_daily_notification_time")
    }

    companion object {
        /** Default daily-hadith notification time: 08:00 (minutes from midnight). */
        const val DEFAULT_NOTIFICATION_TIME_MINUTES = 8 * 60
    }
}
