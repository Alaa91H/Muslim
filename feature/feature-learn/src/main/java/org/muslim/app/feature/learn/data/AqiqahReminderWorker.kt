package org.muslim.app.feature.learn.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.notifications.notificationAllowed
import org.muslim.app.feature.learn.R

/** Posts the aqiqah reminder; WorkManager provides reboot and retry resilience. */
open class AqiqahReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!applicationContext.notificationAllowed(NotificationCategory.Family)) return Result.success()
        NotificationChannels.create(applicationContext)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            Intent().apply {
                setClassName(applicationContext, MAIN_ACTIVITY)
                data = "muslim://learn/family-life".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(applicationContext, NotificationChannels.FAMILY)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(applicationContext.getString(R.string.aqiqah_notification_title))
            .setContentText(applicationContext.getString(R.string.aqiqah_notification_body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private companion object {
        const val NOTIFICATION_ID = 7121
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
