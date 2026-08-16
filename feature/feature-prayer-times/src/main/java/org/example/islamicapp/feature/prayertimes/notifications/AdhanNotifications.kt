package org.example.islamicapp.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import org.example.islamicapp.core.notifications.NotificationChannels
import org.example.islamicapp.feature.prayertimes.R
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.feature.prayertimes.ui.prayerLabelRes

/** Builders for the prayer notifications (adhan + reminder). */
internal object AdhanNotifications {

    const val ADHAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002

    fun adhanNotification(context: Context, prayer: Prayer): Notification =
        NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.adhan_notification_title))
            .setContentText(context.getString(R.string.prayer_name, context.getString(prayerNameRes(prayer))))
            .setStyle(NotificationCompat.BigTextStyle())
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .build()

    fun showReminder(context: Context, prayer: Prayer, minutesBefore: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
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
