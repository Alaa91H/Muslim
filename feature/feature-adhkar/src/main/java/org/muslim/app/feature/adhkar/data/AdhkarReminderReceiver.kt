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
import org.muslim.app.core.notifications.notificationAllowed
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import org.muslim.app.feature.adhkar.overlay.AdhkarOverlayService

/**
 * Fired by [AdhkarReminderScheduler] at the configured morning/evening time.
 * Shows the floating overlay when the user granted overlay permission,
 * otherwise falls back to a notification, then re-schedules the next day.
 */
class AdhkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, AdhkarEntryPoint::class.java)
        val slot = intent.getStringExtra(EXTRA_SLOT)
            ?.let { runCatching { AdhkarReminderSlot.valueOf(it) }.getOrNull() }
            ?: AdhkarReminderSlot.Morning

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = entryPoint.prefsRepository().prefs.first()
            val slotEnabled = when (slot) {
                AdhkarReminderSlot.Morning -> prefs.morningReminderEnabled
                AdhkarReminderSlot.Evening -> prefs.eveningReminderEnabled
            }
            if (slotEnabled && appContext.notificationAllowed(NotificationCategory.Adhkar)) {
                val category = when (slot) {
                    AdhkarReminderSlot.Morning -> DhikrCategory.Morning
                    AdhkarReminderSlot.Evening -> DhikrCategory.Evening
                }
                val dhikr = entryPoint.adhkarRepository()
                    .randomDhikr(category, prefs.disabledDhikrIds, prefs.shortDhikrOnly)
                    ?: entryPoint.adhkarRepository().randomDhikr(null, prefs.disabledDhikrIds, prefs.shortDhikrOnly)
                if (dhikr != null) {
                    val overlayGranted = Settings.canDrawOverlays(appContext)
                    if (prefs.overlayEnabled && overlayGranted) {
                        AdhkarOverlayService.start(
                            appContext,
                            dhikr,
                            prefs.overlayDurationSeconds,
                            prefs.overlayBackgroundColor,
                            prefs.overlayCornerRadiusDp,
                            prefs.overlayFontSizeSp,
                        )
                    } else {
                        AdhkarNotifications.showReminder(appContext, dhikr)
                    }
                }
            }
            // Always re-arm the next occurrence.
            entryPoint.reminderScheduler().schedule(prefs)
        }
    }

    companion object {
        const val EXTRA_SLOT = "extra_slot"
    }
}
