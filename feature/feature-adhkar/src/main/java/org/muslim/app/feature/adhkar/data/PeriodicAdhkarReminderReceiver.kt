package org.muslim.app.feature.adhkar.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationCategoryEnabled
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import org.muslim.app.feature.adhkar.overlay.AdhkarOverlayService
import java.time.LocalTime

/**
 * Fired by [PeriodicAdhkarReminderScheduler] every configured interval.
 * Skips when disabled or outside the chosen time window, then shows a random
 * or category-pinned dhikr: floating overlay when permitted, otherwise a
 * bubble/heads-up notification.
 */
class PeriodicAdhkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, AdhkarEntryPoint::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = entryPoint.prefsRepository().prefs.first()
            if (!prefs.periodicReminderEnabled) return@launch
            if (!appContext.notificationCategoryEnabled(NotificationCategory.Adhkar)) return@launch

            if (prefs.periodicReminderWindowEnabled) {
                val now = LocalTime.now()
                val inWindow = AdhkarReminderWindow.isWithinWindow(
                    AdhkarReminderWindow.minutesOfDay(now.hour, now.minute),
                    AdhkarReminderWindow.minutesOfDay(
                        prefs.periodicReminderWindowStartHour,
                        prefs.periodicReminderWindowStartMinute,
                    ),
                    AdhkarReminderWindow.minutesOfDay(
                        prefs.periodicReminderWindowEndHour,
                        prefs.periodicReminderWindowEndMinute,
                    ),
                )
                if (!inWindow) return@launch
            }

            val category = prefs.periodicReminderCategoryId
                ?.let(DhikrCategory::fromId)
            val dhikr = entryPoint.adhkarRepository()
                .randomDhikr(category, prefs.disabledDhikrIds)
                ?: entryPoint.adhkarRepository().randomDhikr(null, prefs.disabledDhikrIds)
                ?: return@launch

            if (prefs.overlayEnabled && Settings.canDrawOverlays(appContext)) {
                AdhkarOverlayService.start(appContext, dhikr, prefs.overlayDurationSeconds)
            } else {
                AdhkarNotifications.showPeriodicReminder(appContext, dhikr, prefs.overlayDurationSeconds)
            }
        }
    }
}
