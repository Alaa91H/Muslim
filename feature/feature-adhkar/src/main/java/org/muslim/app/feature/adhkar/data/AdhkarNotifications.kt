package org.muslim.app.feature.adhkar.data

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr

/** Notification fallback for adhkar reminders (used when overlay is unavailable). */
internal object AdhkarNotifications {

    const val REMINDER_NOTIFICATION_ID = 3002

    fun showReminder(context: Context, dhikr: Dhikr) {
        NotificationChannels.create(context)
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val notification = NotificationCompat.Builder(context, NotificationChannels.ADHKAR)
            .setSmallIcon(R.drawable.ic_adhkar_notification)
            .setContentTitle(context.getString(R.string.adhkar_reminder_notification_title))
            .setContentText(dhikr.arabic)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dhikr.arabic))
            .setContentIntent(contentIntent?.let {
                android.app.PendingIntent.getActivity(
                    context, 0, it,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
                )
            })
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(REMINDER_NOTIFICATION_ID, notification)
    }
}
