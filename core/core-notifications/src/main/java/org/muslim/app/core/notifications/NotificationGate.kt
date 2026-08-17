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
