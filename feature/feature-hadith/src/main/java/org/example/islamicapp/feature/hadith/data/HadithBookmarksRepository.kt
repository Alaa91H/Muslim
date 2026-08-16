package org.example.islamicapp.feature.hadith.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.hadithPrefsDataStore by preferencesDataStore(name = "hadith_prefs")

/**
 * Hadith bookmarks and the opt-in "hadith of the day" notification toggle
 * (PROJECT_PROMPT.md §6 Phase 3).
 */
@Singleton
class HadithBookmarksRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bookmarksKey = stringSetPreferencesKey("bookmarks")
    private val dailyNotificationKey = booleanPreferencesKey("daily_notification")

    val bookmarks: Flow<Set<String>> =
        context.hadithPrefsDataStore.data.map { it[bookmarksKey] ?: emptySet() }

    val dailyNotificationEnabled: Flow<Boolean> =
        context.hadithPrefsDataStore.data.map { it[dailyNotificationKey] ?: false }

    suspend fun toggleBookmark(id: String) {
        context.hadithPrefsDataStore.edit { prefs ->
            val current = prefs[bookmarksKey] ?: emptySet()
            prefs[bookmarksKey] =
                if (id in current) current - id else current + id
        }
    }

    suspend fun setDailyNotification(enabled: Boolean) {
        context.hadithPrefsDataStore.edit { it[dailyNotificationKey] = enabled }
    }
}
