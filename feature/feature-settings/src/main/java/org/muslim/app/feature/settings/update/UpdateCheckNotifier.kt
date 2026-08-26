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
 * Posts the "new update available" notification on the [NotificationChannels.APP_UPDATE]
 * channel. Tapping it opens the in-app update screen ([UPDATE_ROUTE]) where the
 * user sees the changelog, the new version's size and the download button.
 */
class UpdateCheckNotifier(private val context: Context) {

    /**
     * Posts the update-available notification. On Android 13+ posting without
     * the POST_NOTIFICATIONS runtime permission throws SecurityException, so
     * we skip silently when notifications are disabled app-wide instead of
     * crashing the caller (the permissions manager guides the user there).
     */
    fun show(release: ReleaseInfo) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        runCatching {
            NotificationChannels.create(context)
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent().apply {
                    setClassName(context, MAIN_ACTIVITY)
                    data = "muslim://settings/update".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = Notification.Builder(context, NotificationChannels.APP_UPDATE)
                .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2027)
                .setContentTitle(context.getString(R.string.update_available_title))
                .setContentText(context.getString(R.string.update_available_text, release.version))
                .setStyle(
                    Notification.BigTextStyle().bigText(
                        context.getString(R.string.update_available_text, release.version),
                    ),
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
            context.getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val NOTIFICATION_ID = 9001
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
        const val UPDATE_ROUTE = "settings/update"
    }
}
