package org.muslim.app.feature.finance.data

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
import org.muslim.app.feature.finance.R
import java.text.NumberFormat
import java.util.Locale

/** Posts an on-device due-date reminder without uploading debt details. */
open class DebtReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!applicationContext.notificationAllowed(NotificationCategory.Finance)) return Result.success()
        val reminder = DebtReminderScheduler.reminderData(inputData) ?: return Result.failure()
        NotificationChannels.create(applicationContext)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            reminder.id.hashCode(),
            Intent().apply {
                setClassName(applicationContext, MAIN_ACTIVITY)
                data = "muslim://finance".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val amount = NumberFormat.getNumberInstance(Locale.ENGLISH).format(reminder.amount)
        val notification = Notification.Builder(applicationContext, NotificationChannels.FINANCE)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
            .setContentTitle(applicationContext.getString(R.string.finance_debt_notification_title))
            .setContentText(
                applicationContext.getString(
                    R.string.finance_debt_notification_body,
                    reminder.partyName,
                    "$amount ${reminder.currency}".trim(),
                ),
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(reminder.id.hashCode(), notification)
        return Result.success()
    }

    private companion object {
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
