package org.muslim.app.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the unified notification manager: per-category
 * presentation prefs (enabled / sound / vibration / importance / badge) plus
 * the global quiet-hours window. Persisted in DataStore; every notifier in
 * the app consults it before posting, and any change is mirrored onto the
 * underlying Android channel immediately so system and in-app settings never
 * drift apart.
 */
/**
 * @param store the notification-prefs DataStore, provided by Hilt through
 *   [NotificationDataStoreModule] (and injectable in tests via a temp file).
 */
@Singleton
class NotificationPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: DataStore<Preferences>,
) {

    /** Per-category presentation prefs (defaults per [NotificationCategory]). */
    val prefs: Flow<Map<NotificationCategory, NotificationCategoryPrefs>> =
        store.data.map { stored ->
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
        store.data.map { stored ->
            QuietHours(
                enabled = stored[Keys.QUIET_ENABLED] ?: false,
                startMinutes = stored[Keys.QUIET_START] ?: QuietHours.DEFAULT_QUIET_START,
                endMinutes = stored[Keys.QUIET_END] ?: QuietHours.DEFAULT_QUIET_END,
            )
        }

    /**
     * Whether the permanent "next adhan" notification shows the missed-adhan
     * line (defaults to true); only presentation, no channel side effects.
     */
    val showMissedAdhan: Flow<Boolean> =
        store.data.map { stored -> stored[Keys.SHOW_MISSED_ADHAN] ?: true }

    /**
     * ARGB color of the missed-adhan line in the permanent countdown
     * notification. Defaults to red ([MissedAdhanColors.DEFAULT]); the user
     * may pick any color from the palette in the notification settings.
     */
    val missedAdhanColor: Flow<Int> =
        store.data.map { stored -> stored[Keys.MISSED_ADHAN_COLOR] ?: MissedAdhanColors.DEFAULT }

    /** Current prefs for one category (defaults apply when unset). */
    suspend fun prefsFor(category: NotificationCategory): NotificationCategoryPrefs =
        prefs.first()[category] ?: category.defaultPrefs()

    /** True when [category] is enabled (defaults apply when unset). */
    suspend fun isEnabled(category: NotificationCategory): Boolean =
        prefsFor(category).enabled

    /** Turns [category] on/off; disabling cancels that channel's posted alerts too. */
    suspend fun setEnabled(category: NotificationCategory, enabled: Boolean) {
        store.edit { stored ->
            stored[Keys.enabled(category)] = enabled
        }
        if (!enabled) {
            cancelCategoryNotifications(category)
        }
    }

    /** Sets whether [category] plays a sound; the channel is updated live. */
    suspend fun setSoundEnabled(category: NotificationCategory, soundEnabled: Boolean) {
        store.edit { stored ->
            stored[Keys.sound(category)] = soundEnabled
        }
        applyToChannel(category)
    }

    /** Sets whether [category] vibrates; the channel is updated live. */
    suspend fun setVibrateEnabled(category: NotificationCategory, vibrateEnabled: Boolean) {
        store.edit { stored ->
            stored[Keys.vibrate(category)] = vibrateEnabled
        }
        applyToChannel(category)
    }

    /** Sets the presentation importance of [category]; the channel is updated live. */
    suspend fun setImportance(category: NotificationCategory, importance: NotificationImportance) {
        store.edit { stored ->
            stored[Keys.importance(category)] = importance.name
        }
        applyToChannel(category)
    }

    /** Sets whether [category] contributes to the launcher badge; the channel is updated live. */
    suspend fun setBadgeEnabled(category: NotificationCategory, badgeEnabled: Boolean) {
        store.edit { stored ->
            stored[Keys.badge(category)] = badgeEnabled
        }
        applyToChannel(category)
    }

    /** Persists the quiet-hours window. */
    suspend fun setQuietHours(hours: QuietHours) {
        store.edit { stored ->
            stored[Keys.QUIET_ENABLED] = hours.enabled
            stored[Keys.QUIET_START] = hours.startMinutes
            stored[Keys.QUIET_END] = hours.endMinutes
        }
    }

    /** Persists whether the next-adhan notification shows the missed-adhan line. */
    suspend fun setShowMissedAdhan(show: Boolean) {
        store.edit { stored ->
            stored[Keys.SHOW_MISSED_ADHAN] = show
        }
    }

    /** Persists the color of the missed-adhan line in the countdown notification. */
    suspend fun setMissedAdhanColor(color: Int) {
        store.edit { stored ->
            stored[Keys.MISSED_ADHAN_COLOR] = color
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
        // forceRecreate: the user changed a setting in-app, so the live channel
        // must be deleted + re-created to actually take effect (Android treats
        // existing channels as immutable for raises and sound/vibration/badge).
        NotificationChannels.applyCategorySettings(context, category, prefsFor(category), forceRecreate = true)
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
        val SHOW_MISSED_ADHAN = booleanPreferencesKey("notification_show_missed_adhan")
        val MISSED_ADHAN_COLOR = intPreferencesKey("notification_missed_adhan_color")
        val QUIET_START = intPreferencesKey("notification_quiet_start")
        val QUIET_END = intPreferencesKey("notification_quiet_end")
    }
}
