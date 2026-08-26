package org.muslim.app.feature.prayertimes.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/** Builders and verified posting for the prayer notifications (Adhan + reminder). */
internal object AdhanNotifications {

    const val ADHAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002

    /**
     * The result of attempting to publish the active Adhan alert.
     *
     * `NotificationManager.notify()` is void and can return normally even while
     * Android suppresses an alert. This result verifies the prerequisites and
     * confirms that the app's active-notification list contains the exact
     * Adhan id/channel before the receiver records it as visible delivery.
     */
    data class PostResult(
        val posted: Boolean,
        val detail: String? = null,
    )

    fun adhanNotification(
        context: Context,
        prayer: Prayer,
        dismissible: Boolean = false,
        stopOnDismiss: Boolean = false,
    ): Notification {
        val stopIntent = PendingIntent.getBroadcast(
            context,
            ADHAN_NOTIFICATION_ID,
            Intent(context, AdhanNotificationActionReceiver::class.java)
                .setAction(AdhanNotificationActionReceiver.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val dismissIntent = PendingIntent.getBroadcast(
            context,
            ADHAN_NOTIFICATION_ID + 1,
            Intent(context, AdhanNotificationActionReceiver::class.java)
                .setAction(AdhanNotificationActionReceiver.ACTION_DISMISSED)
                .putExtra(AdhanNotificationActionReceiver.EXTRA_STOP_ON_DISMISS, stopOnDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
            .setLargeIcon(Icon.createWithResource(context, org.muslim.app.core.notifications.R.drawable.ic_muslim_adhan_large_v1250))
            .setContentTitle(context.getString(R.string.adhan_notification_title))
            .setContentText(context.getString(R.string.prayer_name, context.getString(prayerNameRes(prayer))))
            .setStyle(NotificationCompat.BigTextStyle())
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(
                org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250,
                context.getString(R.string.adhan_notification_stop),
                stopIntent,
            )
            .setDeleteIntent(if (dismissible) dismissIntent else null)
            .setOngoing(!dismissible)
            .setAutoCancel(dismissible)
            .build()
    }

    /**
     * Checks every Android prerequisite for a visible Adhan alert without
     * posting it. The settings screen and receiver use this same check so the
     * displayed readiness cannot disagree with real delivery.
     */
    fun notificationPreflight(context: Context): PostResult {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return PostResult(posted = false, detail = "Android notification permission is not granted")
        }
        if (!manager.areNotificationsEnabled()) {
            return PostResult(posted = false, detail = "Android app notifications are disabled")
        }

        // A channel can be deleted in Android settings while an alarm remains
        // scheduled. Re-create a missing channel with defaults, but never alter
        // an existing user-owned channel.
        NotificationChannels.create(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(NotificationChannels.ADHAN)
                ?: return PostResult(posted = false, detail = "Adhan notification channel is unavailable")
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return PostResult(posted = false, detail = "Adhan notification channel is disabled in Android settings")
            }
        }
        return PostResult(posted = true)
    }

    /**
     * Publishes and confirms the active Adhan alert. Audio delivery is never
     * blocked by a failed visual alert, but diagnostics must report the real
     * Android condition rather than treating a non-throwing `notify()` call as
     * proof that the user could see it.
     */
    fun showAdhan(context: Context, prayer: Prayer): PostResult {
        val preflight = notificationPreflight(context)
        if (!preflight.posted) return preflight
        val manager = context.getSystemService(NotificationManager::class.java)
        val posted = runCatching {
            manager.notify(ADHAN_NOTIFICATION_ID, adhanNotification(context, prayer))
            manager.activeNotifications.any { notification ->
                notification.id == ADHAN_NOTIFICATION_ID &&
                    notification.notification.channelId == NotificationChannels.ADHAN
            }
        }
        return posted.fold(
            onSuccess = { active ->
                if (active) {
                    PostResult(posted = true)
                } else {
                    PostResult(posted = false, detail = "Android did not retain the active Adhan alert")
                }
            },
            onFailure = { error ->
                PostResult(
                    posted = false,
                    detail = "Adhan notification posting failed: ${error.javaClass.simpleName}",
                )
            },
        )
    }

    fun showReminder(context: Context, prayer: Prayer, minutesBefore: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
            .setLargeIcon(Icon.createWithResource(context, org.muslim.app.core.notifications.R.drawable.ic_muslim_adhan_large_v1250))
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(
                context.getString(
                    R.string.reminder_message,
                    context.getString(prayerNameRes(prayer)),
                    minutesBefore,
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun prayerNameRes(prayer: Prayer): Int = prayerLabelRes(prayer)
}
