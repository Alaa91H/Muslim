package org.muslim.app.core.datastore.prayer

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.core.common.prayer.Prayer
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prayerCompletionDataStore by preferencesDataStore(name = "prayer_completion")

/**
 * Stores an optional, local-only checklist for the five daily prayers.
 *
 * This is a personal record rather than a judgement of a user's worship. It
 * deliberately excludes sunrise and does not calculate streaks, scores, or
 * send any completion data off the device.
 */
@Singleton
class PrayerCompletionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun completedPrayers(date: LocalDate): Flow<Set<Prayer>> =
        context.prayerCompletionDataStore.data.map { preferences ->
            (preferences[keyFor(date)] ?: emptySet())
                .mapNotNull { name -> Prayer.entries.firstOrNull { it.name == name } }
                .filterTo(linkedSetOf()) { it.isTrackablePrayer() }
        }

    suspend fun toggle(date: LocalDate, prayer: Prayer) {
        require(prayer.isTrackablePrayer()) { "Sunrise cannot be tracked as a prayer." }
        context.prayerCompletionDataStore.edit { preferences ->
            val key = keyFor(date)
            val completed = (preferences[key] ?: emptySet()).toMutableSet()
            if (!completed.add(prayer.name)) completed.remove(prayer.name)
            if (completed.isEmpty()) preferences.remove(key) else preferences[key] = completed
            cleanupEntriesOlderThan(preferences, date.minusDays(RETAINED_DAYS - 1))
        }
    }

    private fun keyFor(date: LocalDate) = stringSetPreferencesKey("$ENTRY_PREFIX$date")

    private fun cleanupEntriesOlderThan(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        oldestDateToKeep: LocalDate,
    ) {
        preferences.asMap().keys
            .mapNotNull { key ->
                key.name.takeIf { it.startsWith(ENTRY_PREFIX) }
                    ?.removePrefix(ENTRY_PREFIX)
                    ?.let { encodedDate -> runCatching { LocalDate.parse(encodedDate) }.getOrNull() }
                    ?.let { entryDate -> entryDate to key.name }
            }
            .filter { (entryDate, _) -> entryDate.isBefore(oldestDateToKeep) }
            .forEach { (_, keyName) -> preferences.remove(stringSetPreferencesKey(keyName)) }
    }

    private companion object {
        const val ENTRY_PREFIX = "completed_prayers_"
        const val RETAINED_DAYS = 90L
    }
}

/** The canonical five obligatory prayer entries shown in the personal checklist. */
fun Prayer.isTrackablePrayer(): Boolean = this != Prayer.Sunrise

val trackablePrayers: List<Prayer> = Prayer.entries.filter(Prayer::isTrackablePrayer)
