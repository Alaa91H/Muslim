package org.muslim.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Central notification-channel definitions (core-notifications owns the
 * unified notification system per PROJECT_PROMPT.md §3.3). Channel names and
 * descriptions are localized through resources; per-category presentation
 * (importance / sound / vibration / badge) is applied from the user's
 * [NotificationCategoryPrefs] so the in-app manager and the system settings
 * stay in sync.
 */
object NotificationChannels {

    /** High-importance channel for the Adhan itself. */
    const val ADHAN = "adhan"

    /** Default-importance channel for pre-prayer reminders. */
    const val REMINDER = "prayer_reminder"

    /** Low-importance channel for the optional daily ayah notification. */
    const val QURAN_DAILY = "quran_daily"

    /** High-importance channel for Ramadan iftar / suhoor alerts. */
    const val RAMADAN = "ramadan"

    /** Default-importance channel for adhkar reminders and the overlay service. */
    const val ADHKAR = "adhkar"

    /** Low-importance channel for the optional daily hadith notification. */
    const val HADITH_DAILY = "hadith_daily"

    /** Creates/updates every channel with the app defaults; safe to call on each start (idempotent). */
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationCategory.entries.forEach { category ->
            applyCategorySettings(context, category, category.defaultPrefs())
        }
    }

    /**
     * Applies the user's [prefs] to the category's channel. Re-creating a
     * channel with the same id updates its properties (importance, sound,
     * vibration, badge); a manual override the user made in system settings
     * is always respected by Android.
     */
    fun applyCategorySettings(
        context: Context,
        category: NotificationCategory,
        prefs: NotificationCategoryPrefs,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            category.channelId,
            context.getString(category.nameRes),
            prefs.importance.channelImportance,
        ).apply {
            description = context.getString(category.descriptionRes)
            if (!prefs.soundEnabled) {
                // null sound = a silent channel when the user muted it.
                setSound(null, null)
            }
            enableVibration(prefs.vibrateEnabled)
            if (!prefs.vibrateEnabled) {
                vibrationPattern = null
            }
            setShowBadge(prefs.badgeEnabled)
            if (category == NotificationCategory.Adhkar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Optional bubble presentation for the periodic reminder (Android 11+).
                setAllowBubbles(true)
            }
        }
        manager.createNotificationChannel(channel)
    }
}
