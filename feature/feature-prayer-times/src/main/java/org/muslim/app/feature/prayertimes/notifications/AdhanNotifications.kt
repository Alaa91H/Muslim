package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/** Builders for the prayer notifications (adhan + reminder). */
internal object AdhanNotifications {

    const val ADHAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002

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
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_notification)
            .setLargeIcon(Icon.createWithResource(context, context.applicationInfo.icon))
            .setContentTitle(context.getString(R.string.adhan_notification_title))
            .setContentText(context.getString(R.string.prayer_name, context.getString(prayerNameRes(prayer))))
            .setStyle(NotificationCompat.BigTextStyle())
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(
                org.muslim.app.core.notifications.R.drawable.ic_muslim_notification,
                context.getString(R.string.adhan_notification_stop),
                stopIntent,
            )
            .setDeleteIntent(if (dismissible) dismissIntent else null)
            .setOngoing(!dismissible)
            .setAutoCancel(dismissible)
            .build()
    }

    fun showReminder(context: Context, prayer: Prayer, minutesBefore: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_notification)
            .setLargeIcon(Icon.createWithResource(context, context.applicationInfo.icon))
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
