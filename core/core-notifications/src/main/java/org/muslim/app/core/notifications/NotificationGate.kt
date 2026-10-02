package org.muslim.app.core.notifications

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * Suspending gate every notifier/receiver consults before posting: returns
 * true only when the user has [NotificationCategory] enabled in the unified
 * notification manager. Single entry point for the whole app — one switch in
 * settings turns a category off everywhere.
 */
suspend fun Context.notificationCategoryEnabled(category: NotificationCategory): Boolean {
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext, NotificationEntryPoint::class.java,
    )
    return entryPoint.notificationPrefs().isEnabled(category)
}

/**
 * The full gate used by every notifier: the category must be enabled AND
 * (for non-adhan categories) the current time must fall outside the user's
 * quiet-hours window. Adhan is exempt from quiet hours, but all app alerts and
 * Adhan playback are suppressed while Android reports an active call mode.
 */
suspend fun Context.notificationAllowed(
    category: NotificationCategory,
    atMillis: Long = System.currentTimeMillis(),
): Boolean {
    if (CallAudioMode.isActive(applicationContext)) return false
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext, NotificationEntryPoint::class.java,
    )
    val prefs = entryPoint.notificationPrefs()
    if (!prefs.isEnabled(category)) return false
    if (category == NotificationCategory.Adhan) return true
    return !prefs.isQuietHourActive(atMillis)
}
