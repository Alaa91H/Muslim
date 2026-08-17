package org.muslim.app.core.notifications

import android.app.NotificationManager

/**
 * How prominent a notification is. Mapped 1:1 onto the Android channel
 * importance, which decides heads-up display, sound and placement.
 */
enum class NotificationImportance(val channelImportance: Int) {
    Low(NotificationManager.IMPORTANCE_LOW),
    Default(NotificationManager.IMPORTANCE_DEFAULT),
    High(NotificationManager.IMPORTANCE_HIGH),
}

/**
 * Everything the unified notification manager remembers about one category:
 * the master switch plus fine-grained presentation choices that are applied
 * to the underlying Android channel in real time.
 */
data class NotificationCategoryPrefs(
    val enabled: Boolean,
    val soundEnabled: Boolean,
    val vibrateEnabled: Boolean,
    val importance: NotificationImportance,
    val badgeEnabled: Boolean,
)

/**
 * Global "do not disturb" window. All non-adhan notifications are suppressed
 * inside it — the call to prayer is deliberately exempt so it is never
 * silenced. Times are minutes from midnight and support overnight ranges.
 */
data class QuietHours(
    val enabled: Boolean = false,
    val startMinutes: Int = DEFAULT_QUIET_START,
    val endMinutes: Int = DEFAULT_QUIET_END,
) {
    /** True when [minutesOfDay] falls inside the window (handles overnight ranges). */
    fun contains(minutesOfDay: Int): Boolean {
        if (!enabled || startMinutes == endMinutes) return false
        return if (startMinutes < endMinutes) {
            minutesOfDay in startMinutes until endMinutes
        } else {
            minutesOfDay >= startMinutes || minutesOfDay < endMinutes
        }
    }

    companion object {
        const val DEFAULT_QUIET_START = 22 * 60 // 22:00
        const val DEFAULT_QUIET_END = 6 * 60 // 06:00
    }
}

/**
 * Every notification category the app can post. Each maps to exactly one
 * [NotificationChannels] channel, so the system-level channel settings and
 * the in-app master switches stay in sync (PROJECT_PROMPT.md §3.3 — unified
 * notification system owned by core-notifications).
 */
enum class NotificationCategory(
    val channelId: String,
    val defaultEnabled: Boolean,
    val defaultImportance: NotificationImportance,
    val defaultSound: Boolean,
    val defaultVibrate: Boolean,
    val defaultBadge: Boolean,
    val nameRes: Int,
    val descriptionRes: Int,
) {
    /** The Adhan call to prayer (loud, high importance). */
    Adhan(
        NotificationChannels.ADHAN, defaultEnabled = true,
        defaultImportance = NotificationImportance.High, defaultSound = true, defaultVibrate = true, defaultBadge = true,
        nameRes = R.string.channel_adhan, descriptionRes = R.string.channel_adhan_desc,
    ),

    /** Pre-prayer reminder minutes before the adhan. */
    PrayerReminder(
        NotificationChannels.REMINDER, defaultEnabled = true,
        defaultImportance = NotificationImportance.Default, defaultSound = true, defaultVibrate = false, defaultBadge = true,
        nameRes = R.string.channel_reminder, descriptionRes = R.string.channel_reminder_desc,
    ),

    /** Daily ayah-of-the-day. */
    QuranDaily(
        NotificationChannels.QURAN_DAILY, defaultEnabled = true,
        defaultImportance = NotificationImportance.Low, defaultSound = true, defaultVibrate = false, defaultBadge = false,
        nameRes = R.string.channel_quran_daily, descriptionRes = R.string.channel_quran_daily_desc,
    ),

    /** Ramadan iftar / suhoor alerts. */
    Ramadan(
        NotificationChannels.RAMADAN, defaultEnabled = true,
        defaultImportance = NotificationImportance.High, defaultSound = true, defaultVibrate = true, defaultBadge = true,
        nameRes = R.string.channel_ramadan, descriptionRes = R.string.channel_ramadan_desc,
    ),

    /** Adhkar reminders + the floating overlay service. */
    Adhkar(
        NotificationChannels.ADHKAR, defaultEnabled = true,
        defaultImportance = NotificationImportance.Default, defaultSound = true, defaultVibrate = false, defaultBadge = false,
        nameRes = R.string.channel_adhkar, descriptionRes = R.string.channel_adhkar_desc,
    ),

    /** Daily hadith-of-the-day. */
    HadithDaily(
        NotificationChannels.HADITH_DAILY, defaultEnabled = true,
        defaultImportance = NotificationImportance.Low, defaultSound = true, defaultVibrate = false, defaultBadge = false,
        nameRes = R.string.channel_hadith_daily, descriptionRes = R.string.channel_hadith_daily_desc,
    );

    /** The defaults the app ships with for this category. */
    fun defaultPrefs() = NotificationCategoryPrefs(
        enabled = defaultEnabled,
        soundEnabled = defaultSound,
        vibrateEnabled = defaultVibrate,
        importance = defaultImportance,
        badgeEnabled = defaultBadge,
    )
}
