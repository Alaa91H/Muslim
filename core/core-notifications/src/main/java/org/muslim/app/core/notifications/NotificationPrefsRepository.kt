package org.muslim.app.core.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationPrefsDataStore by preferencesDataStore(name = "notification_prefs")

/**
 * Every notification category the app can post. Each maps to exactly one
 * [NotificationChannels] channel, so the system-level channel settings and
 * the in-app master switches stay in sync (PROJECT_PROMPT.md §3.3 — unified
 * notification system owned by core-notifications).
 */
enum class NotificationCategory(
    val channelId: String,
    val defaultEnabled: Boolean,
) {
    /** The Adhan call to prayer (loud, high importance). */
    Adhan(NotificationChannels.ADHAN, defaultEnabled = true),

    /** Pre-prayer reminder minutes before the adhan. */
    PrayerReminder(NotificationChannels.REMINDER, defaultEnabled = true),

    /** Daily ayah-of-the-day. */
    QuranDaily(NotificationChannels.QURAN_DAILY, defaultEnabled = true),

    /** Ramadan iftar / suhoor alerts. */
    Ramadan(NotificationChannels.RAMADAN, defaultEnabled = true),

    /** Adhkar reminders + the floating overlay service. */
    Adhkar(NotificationChannels.ADHKAR, defaultEnabled = true),

    /** Daily hadith-of-the-day. */
    HadithDaily(NotificationChannels.HADITH_DAILY, defaultEnabled = true),
}

/**
 * Single source of truth for which notification categories the user has
 * enabled. Persisted in DataStore; every notifier in the app consults this
 * before posting, so one switch here turns a whole category on/off across
 * features (prayers, adhkar, Quran, Ramadan, hadith).
 */
@Singleton
class NotificationPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Per-category enabled state (defaults per [NotificationCategory]). */
    val prefs: Flow<Map<NotificationCategory, Boolean>> =
        context.notificationPrefsDataStore.data.map { stored ->
            NotificationCategory.entries.associateWith { category ->
                stored[Keys.enabled(category)] ?: category.defaultEnabled
            }
        }

    /** True when [category] is enabled (defaults apply when unset). */
    suspend fun isEnabled(category: NotificationCategory): Boolean =
        prefs.first()[category] ?: category.defaultEnabled

    /** Turns [category] on/off; false cancels that channel's posted alerts too. */
    suspend fun setEnabled(category: NotificationCategory, enabled: Boolean) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.enabled(category)] = enabled
        }
    }

    private object Keys {
        fun enabled(category: NotificationCategory) =
            booleanPreferencesKey("notification_${category.name}")
    }
}
