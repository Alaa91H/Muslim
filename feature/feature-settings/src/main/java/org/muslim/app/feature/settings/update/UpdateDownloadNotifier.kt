package org.muslim.app.feature.settings.update

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.settings.R

/**
 * Completion notifications for a durable update download.
 *
 * Installation is intentionally never silent: tapping the ready notification
 * opens the verified update screen, where Android's package installer remains
 * the final authority.
 */
internal class UpdateDownloadNotifier(
    private val context: Context,
) {
    fun showReady(version: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        post(
            title = context.getString(R.string.update_ready_title),
            text = context.getString(R.string.update_ready_text, version),
            actionLabel = context.getString(R.string.update_ready_action),
        )
    }

    fun showFailed() {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        post(
            title = context.getString(R.string.update_download_failed_title),
            text = context.getString(R.string.update_download_failed_text),
            actionLabel = context.getString(R.string.update_notification_action_view),
        )
    }

    private fun post(
        title: String,
        text: String,
        actionLabel: String,
    ) {
        runCatching {
            NotificationChannels.create(context)
            val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                Intent().apply {
                    setClassName(context, MAIN_ACTIVITY)
                    data = UPDATE_DEEP_LINK.toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = Notification.Builder(context, NotificationChannels.APP_UPDATE)
                .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setContentIntent(contentIntent)
                .addAction(
                    Notification.Action.Builder(
                        org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029,
                        actionLabel,
                        contentIntent,
                    ).build(),
                )
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .build()
            context.getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val NOTIFICATION_ID = 9002
        const val REQUEST_CODE = 9002
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
        const val UPDATE_DEEP_LINK = "muslim://settings/update"
    }
}
