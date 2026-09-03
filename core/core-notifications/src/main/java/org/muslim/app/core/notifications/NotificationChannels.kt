package org.muslim.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
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

    /**
     * High-importance channel for the Adhan itself.
     *
     * This identifier intentionally replaces the retired `adhan_alert_v2`
     * channel. On Android 8+ channel alert behavior is immutable after creation,
     * so a prior low-importance or non-banner channel cannot be raised reliably
     * from app code. A new identity gives the Adhan a fresh, visible
     * high-importance channel without touching its configured Adhan audio.
     */
    const val ADHAN = "adhan_alert_v3"

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

    /** Default-importance channel for finance reminders such as debt due dates. */
    const val FINANCE = "finance_reminders"

    /** Default-importance channel for app-update notifications. */
    const val APP_UPDATE = "app_update"

    /**
     * Ensures every channel exists with the app defaults. Idempotent: channels
     * that already exist are left untouched so a manual override the user made
     * in system settings is never reset by a plain app start.
     */
    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        // Professional grouping: prayer vs Quran/dhikr vs system, so the Settings
        // shade stays organised even as categories grow.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                NotificationChannelGroup("group_prayer", context.getString(R.string.group_prayer)),
                NotificationChannelGroup("group_quran_dhikr", context.getString(R.string.group_quran_dhikr)),
                NotificationChannelGroup("group_other", context.getString(R.string.group_other)),
            ).forEach { group ->
                runCatching { manager.createNotificationChannelGroup(group) }
            }
        }
        NotificationCategory.entries.forEach { category ->
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
        // Professional channel presentation: lock-screen visibility, light, badge,
        // and vibration are aligned with the semantic category so the system
        // shade, lock-screen, heads-up and LED/bubble behaviour feel intentional.
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        if (!prefs.soundEnabled) {
            // null sound = a silent channel when the user muted it.
            setSound(null, null)
        }
        enableVibration(prefs.vibrateEnabled)
        if (!prefs.vibrateEnabled) {
            vibrationPattern = null
        } else {
            // Subtle haptic signature per category: Adhan/Ramadan use a longer
            // double-pulse so the prayer alert feels distinct from routine
            // reminders without overwhelming the user.
            vibrationPattern = when (category) {
                NotificationCategory.Adhan, NotificationCategory.Ramadan ->
                    longArrayOf(0, 400, 200, 400)
                NotificationCategory.PrayerReminder, NotificationCategory.Hajj ->
                    longArrayOf(0, 250, 150, 250)
                else -> longArrayOf(0, 300)
            }
        }
        // LED/edge-light hint for devices that still expose it; colour matches
        // the Islamic green accent for prayer and gold for spiritual content.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enableLights(true)
            lightColor = when (category) {
                NotificationCategory.Adhan, NotificationCategory.PrayerCountdown,
                NotificationCategory.PrayerReminder, NotificationCategory.Ramadan,
                -> 0xFF527A68.toInt()
                NotificationCategory.Recitation, NotificationCategory.QuranDaily,
                NotificationCategory.HadithDaily, NotificationCategory.Adhkar,
                -> 0xFFB49A62.toInt()
                else -> 0xFF527A68.toInt()
            }
        }
        setShowBadge(prefs.badgeEnabled)
        if (category == NotificationCategory.Adhkar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Optional bubble presentation for the periodic reminder (Android 11+).
            setAllowBubbles(true)
        }
        // Group-related channels into spiritual / practical buckets for the
        // system settings screen (cosmetic, no behavioural change).
        group = when (category) {
            NotificationCategory.Adhan, NotificationCategory.PrayerReminder,
            NotificationCategory.PrayerCountdown, NotificationCategory.Ramadan, NotificationCategory.Hajj,
            -> "group_prayer"
            NotificationCategory.Recitation, NotificationCategory.QuranDaily,
            NotificationCategory.HadithDaily, NotificationCategory.Adhkar,
            -> "group_quran_dhikr"
            else -> "group_other"
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
