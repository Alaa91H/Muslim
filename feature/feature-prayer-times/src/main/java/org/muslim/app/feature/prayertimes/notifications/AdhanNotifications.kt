package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/** Builders for the prayer notifications (adhan + reminder). */
internal object AdhanNotifications {

    const val ADHAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002
    private const val TEST_ADHAN_NOTIFICATION_ID = 1004

    /** Compact, high-priority card shown while the real adhan is playing. */
    fun adhanNotification(context: Context, prayer: Prayer): Notification =
        NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.adhan_notification_title))
            .setContentText(context.getString(R.string.prayer_name, context.getString(prayerNameRes(prayer))))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.adhan_notification_message, context.getString(prayerNameRes(prayer))),
                ),
            )
            .setColor(context.getColor(R.color.adhan_accent))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Persistent: cannot be swiped away while the adhan is ringing.
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

    /** Sends a genuine system notification alongside the configured test playback. */
    fun showTestAdhan(context: Context, prayer: Prayer) {
        val prayerName = context.getString(prayerNameRes(prayer))
        val notification = NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.adhan_test_notification_title))
            .setContentText(context.getString(R.string.adhan_test_notification_message, prayerName))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.adhan_test_notification_detail, prayerName),
                ),
            )
            .setColor(context.getColor(R.color.adhan_accent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(TEST_ADHAN_NOTIFICATION_ID, notification)
    }

    fun showReminder(context: Context, prayer: Prayer, minutesBefore: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(
                context.getString(
                    R.string.reminder_message,
                    context.getString(prayerNameRes(prayer)),
                    minutesBefore,
                ),
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun prayerNameRes(prayer: Prayer): Int = prayerLabelRes(prayer)
}
