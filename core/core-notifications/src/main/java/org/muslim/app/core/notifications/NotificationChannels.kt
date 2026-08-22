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

    /** Low-importance channel for the permanent next-adhan countdown. */
    const val PRAYER_COUNTDOWN = "prayer_countdown"

    /** Silent, low-importance channel for the Quran recitation media controls. */
    const val RECITATION = "recitation"

    /** Default-importance channel for the daily Hajj rite reminders ("Pilgrim Companion"). */
    const val HAJJ = "hajj_reminders"

    /** Default-importance channel for family-life reminders such as aqiqah. */
    const val FAMILY = "family_reminders"

    /** Default-importance channel for app-update notifications. */
    const val APP_UPDATE = "app_update"

    /**
     * Ensures every channel exists with the app defaults. Idempotent: channels
     * that already exist are left untouched so a manual override the user made
     * in system settings is never reset by a plain app start.
     */
    fun create(context: Context) {
        NotificationCategory.entries.forEach { category ->
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(category.channelId) == null) {
                manager.createNotificationChannel(
                    buildChannel(context, category, category.defaultPrefs()),
                )
            }
        }
    }

    /**
     * Applies the user's [prefs] to the category's channel.
     *
     * Android treats an existing channel as immutable for raising importance
     * and for sound / vibration / badge updates, so when [forceRecreate] is
     * true (a user changed a setting in-app) and the channel's current state
     * differs from [prefs], the channel is deleted and re-created with the new
     * presentation. When the state already matches, nothing is re-created.
     */
    fun applyCategorySettings(
        context: Context,
        category: NotificationCategory,
        prefs: NotificationCategoryPrefs,
        forceRecreate: Boolean = false,
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(category.channelId)
        if (forceRecreate && existing != null && channelDiffers(existing, prefs)) {
            manager.deleteNotificationChannel(category.channelId)
        }
        manager.createNotificationChannel(buildChannel(context, category, prefs))
    }

    private fun buildChannel(
        context: Context,
        category: NotificationCategory,
        prefs: NotificationCategoryPrefs,
    ): NotificationChannel = NotificationChannel(
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

    /** True when the live channel's presentation differs from the requested [prefs]. */
    private fun channelDiffers(
        existing: NotificationChannel,
        prefs: NotificationCategoryPrefs,
    ): Boolean {
        if (existing.importance != prefs.importance.channelImportance) return true
        val soundMatches = if (prefs.soundEnabled) existing.sound != null else existing.sound == null
        if (!soundMatches) return true
        if (existing.shouldVibrate() != prefs.vibrateEnabled) return true
        if (existing.canShowBadge() != prefs.badgeEnabled) return true
        return false
    }
}
