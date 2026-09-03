package org.muslim.app.feature.adhkar.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.overlay.AdhkarBubbleActivity

/** Notification fallback for adhkar reminders (used when overlay is unavailable). */
internal object AdhkarNotifications {

    const val REMINDER_NOTIFICATION_ID = 3002
    const val PERIODIC_NOTIFICATION_ID = 3003

    fun showReminder(context: Context, dhikr: Dhikr) {
        NotificationChannels.create(context)
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val notification = NotificationCompat.Builder(context, NotificationChannels.ADHKAR)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            .setContentTitle(context.getString(R.string.adhkar_reminder_notification_title))
            .setContentText(dhikr.arabic)
            .setSubText(context.getString(R.string.adhkar_reminder_notification_title))
            .setStyle(NotificationCompat.BigTextStyle().bigText(dhikr.arabic).setSummaryText(dhikr.translation))
            .setContentIntent(contentIntent?.let {
                PendingIntent.getActivity(
                    context, 0, it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            })
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(REMINDER_NOTIFICATION_ID, notification)
    }

    /**
     * Periodic reminder (Phase 4): a heads-up notification that also bubbles on
     * Android 11+ when the user has enabled bubbles for the app. Tapping the
     * bubble opens [AdhkarBubbleActivity], which auto-folds after the duration;
     * the notification is cancelled at the same time so the bubble disappears.
     */
    fun showPeriodicReminder(
        context: Context,
        dhikr: Dhikr,
        durationSeconds: Int,
        backgroundColor: Int = 0xE6282830.toInt(),
        cornerRadiusDp: Int = 20,
        fontSizeSp: Int = 22,
    ) {
        NotificationChannels.create(context)
        val bubbleIntent = PendingIntent.getActivity(
            context,
            PERIODIC_BUBBLE_REQUEST_CODE,
            Intent(context, AdhkarBubbleActivity::class.java).apply {
                putExtra(AdhkarBubbleActivity.EXTRA_ARABIC, dhikr.arabic)
                putExtra(AdhkarBubbleActivity.EXTRA_TRANSLATION, dhikr.translation)
                putExtra(AdhkarBubbleActivity.EXTRA_SOURCE, dhikr.source)
                putExtra(AdhkarBubbleActivity.EXTRA_DURATION_SECONDS, durationSeconds)
                putExtra(AdhkarBubbleActivity.EXTRA_BG_COLOR, backgroundColor)
                putExtra(AdhkarBubbleActivity.EXTRA_CORNER_RADIUS_DP, cornerRadiusDp)
                putExtra(AdhkarBubbleActivity.EXTRA_FONT_SIZE_SP, fontSizeSp)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val builder = NotificationCompat.Builder(context, NotificationChannels.ADHKAR)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            .setContentTitle(context.getString(R.string.adhkar_periodic_notification_title))
            .setContentText(dhikr.arabic)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dhikr.arabic))
            .setContentIntent(contentIntent?.let {
                PendingIntent.getActivity(
                    context, PERIODIC_CONTENT_REQUEST_CODE, it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            })
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metadata = NotificationCompat.BubbleMetadata.Builder(
                bubbleIntent,
                IconCompat.createWithResource(context, org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028),
            )
                .setAutoExpandBubble(true)
                .setSuppressNotification(false)
                .build()
            builder.setBubbleMetadata(metadata)
        }

        context.getSystemService(NotificationManager::class.java)
            .notify(PERIODIC_NOTIFICATION_ID, builder.build())

        // Best-effort auto-dismiss so the bubble folds away after the duration.
        CoroutineScope(Dispatchers.Default).launch {
            delay(durationSeconds.coerceIn(1, 600) * 1_000L)
            runCatching {
                context.getSystemService(NotificationManager::class.java)
                    .cancel(PERIODIC_NOTIFICATION_ID)
            }
        }
    }

    private const val PERIODIC_BUBBLE_REQUEST_CODE = 202
    private const val PERIODIC_CONTENT_REQUEST_CODE = 203
}
