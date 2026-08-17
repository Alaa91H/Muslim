package org.muslim.app.feature.hadith.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.hadith.R
import org.muslim.app.feature.hadith.domain.Hadith

/**
 * Builds and posts the daily-hadith notification. Separated from
 * [HadithOfTheDayWorker] so the Android framework side-effect can be faked
 * when unit-testing the worker.
 */
open class HadithOfTheDayNotifier(private val context: Context) {

    fun show(hadith: Hadith) {
        NotificationChannels.create(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent().apply {
                setClassName(context, MAIN_ACTIVITY)
                data = Uri.parse("muslim://hadith")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = context.getString(R.string.hadith_of_the_day)
        val body = buildString {
            append(hadith.arabicText)
            if (hadith.translation.isNotBlank()) {
                append("\n\n").append(hadith.translation)
            }
            append("\n\n").append(hadith.source)
        }
        val notification = Notification.Builder(context, NotificationChannels.HADITH_DAILY)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(hadith.arabicText)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 7002
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
