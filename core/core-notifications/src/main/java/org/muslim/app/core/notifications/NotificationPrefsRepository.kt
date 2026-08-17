package org.muslim.app.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationPrefsDataStore by preferencesDataStore(name = "notification_prefs")

/**
 * Single source of truth for the unified notification manager: per-category
 * presentation prefs (enabled / sound / vibration / importance / badge) plus
 * the global quiet-hours window. Persisted in DataStore; every notifier in
 * the app consults it before posting, and any change is mirrored onto the
 * underlying Android channel immediately so system and in-app settings never
 * drift apart.
 */
@Singleton
class NotificationPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Per-category presentation prefs (defaults per [NotificationCategory]). */
    val prefs: Flow<Map<NotificationCategory, NotificationCategoryPrefs>> =
        context.notificationPrefsDataStore.data.map { stored ->
            NotificationCategory.entries.associateWith { category ->
                NotificationCategoryPrefs(
                    enabled = stored[Keys.enabled(category)] ?: category.defaultEnabled,
                    soundEnabled = stored[Keys.sound(category)] ?: category.defaultSound,
                    vibrateEnabled = stored[Keys.vibrate(category)] ?: category.defaultVibrate,
                    importance = enumOr(stored[Keys.importance(category)], category.defaultImportance),
                    badgeEnabled = stored[Keys.badge(category)] ?: category.defaultBadge,
                )
            }
        }

    /** Global quiet-hours window (all non-adhan notifications muted inside). */
    val quietHours: Flow<QuietHours> =
        context.notificationPrefsDataStore.data.map { stored ->
            QuietHours(
                enabled = stored[Keys.QUIET_ENABLED] ?: false,
                startMinutes = stored[Keys.QUIET_START] ?: QuietHours.DEFAULT_QUIET_START,
                endMinutes = stored[Keys.QUIET_END] ?: QuietHours.DEFAULT_QUIET_END,
            )
        }

    /** Current prefs for one category (defaults apply when unset). */
    suspend fun prefsFor(category: NotificationCategory): NotificationCategoryPrefs =
        prefs.first()[category] ?: category.defaultPrefs()

    /** True when [category] is enabled (defaults apply when unset). */
    suspend fun isEnabled(category: NotificationCategory): Boolean =
        prefsFor(category).enabled

    /** Turns [category] on/off; disabling cancels that channel's posted alerts too. */
    suspend fun setEnabled(category: NotificationCategory, enabled: Boolean) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.enabled(category)] = enabled
        }
        if (!enabled) {
            cancelCategoryNotifications(category)
        }
    }

    /** Sets whether [category] plays a sound; the channel is updated live. */
    suspend fun setSoundEnabled(category: NotificationCategory, soundEnabled: Boolean) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.sound(category)] = soundEnabled
        }
        applyToChannel(category)
    }

    /** Sets whether [category] vibrates; the channel is updated live. */
    suspend fun setVibrateEnabled(category: NotificationCategory, vibrateEnabled: Boolean) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.vibrate(category)] = vibrateEnabled
        }
        applyToChannel(category)
    }

    /** Sets the presentation importance of [category]; the channel is updated live. */
    suspend fun setImportance(category: NotificationCategory, importance: NotificationImportance) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.importance(category)] = importance.name
        }
        applyToChannel(category)
    }

    /** Sets whether [category] contributes to the launcher badge; the channel is updated live. */
    suspend fun setBadgeEnabled(category: NotificationCategory, badgeEnabled: Boolean) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.badge(category)] = badgeEnabled
        }
        applyToChannel(category)
    }

    /** Persists the quiet-hours window. */
    suspend fun setQuietHours(hours: QuietHours) {
        context.notificationPrefsDataStore.edit { stored ->
            stored[Keys.QUIET_ENABLED] = hours.enabled
            stored[Keys.QUIET_START] = hours.startMinutes
            stored[Keys.QUIET_END] = hours.endMinutes
        }
    }

    /** True when [atMillis] falls inside the quiet-hours window. */
    suspend fun isQuietHourActive(atMillis: Long = System.currentTimeMillis()): Boolean {
        val hours = quietHours.first()
        if (!hours.enabled) return false
        val calendar = Calendar.getInstance().apply { timeInMillis = atMillis }
        val minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return hours.contains(minutes)
    }

    private suspend fun applyToChannel(category: NotificationCategory) {
        NotificationChannels.applyCategorySettings(context, category, prefsFor(category))
    }

    /** Removes every already-posted notification that uses this category's channel. */
    private fun cancelCategoryNotifications(category: NotificationCategory) {
        runCatching {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.activeNotifications
                .filter { it.notification.channelId == category.channelId }
                .forEach { manager.cancel(it.id) }
        }
    }

    private fun <T : Enum<T>> enumOr(value: String?, default: T): T =
        value?.let { v -> default::class.java.enumConstants?.firstOrNull { it.name == v } } ?: default

    private object Keys {
        fun enabled(category: NotificationCategory) =
            booleanPreferencesKey("notification_${category.name}")
        fun sound(category: NotificationCategory) =
            booleanPreferencesKey("notification_${category.name}_sound")
        fun vibrate(category: NotificationCategory) =
            booleanPreferencesKey("notification_${category.name}_vibrate")
        fun importance(category: NotificationCategory) =
            stringPreferencesKey("notification_${category.name}_importance")
        fun badge(category: NotificationCategory) =
            booleanPreferencesKey("notification_${category.name}_badge")

        val QUIET_ENABLED = booleanPreferencesKey("notification_quiet_enabled")
        val QUIET_START = intPreferencesKey("notification_quiet_start")
        val QUIET_END = intPreferencesKey("notification_quiet_end")
    }
}
