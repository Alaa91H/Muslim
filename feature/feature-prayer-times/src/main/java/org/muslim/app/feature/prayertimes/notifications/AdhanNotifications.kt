package org.muslim.app.feature.prayertimes.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/** Builders and verified posting for the active Adhan alert and pre-prayer reminder. */
object AdhanNotifications {

    /** A fresh identity forces Android to render this release's notification artwork. */
    const val ADHAN_NOTIFICATION_ID = 1014
    /** Most recent retired identity, retained for device-level migration coverage. */
    const val RETIRED_ADHAN_NOTIFICATION_ID = 1012
    private const val OLDER_RETIRED_ADHAN_NOTIFICATION_ID = 1010
    private const val ORIGINAL_RETIRED_ADHAN_NOTIFICATION_ID = 1005
    private const val OLDEST_RETIRED_ADHAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002

    /**
     * Android may retain an ongoing foreground notification across an in-place
     * update. Cancel the old active-Adhan identity before publishing the fresh
     * one so stale artwork cannot coexist with the current card.
     */
    fun cancelRetiredAdhan(context: Context) {
        context.getSystemService(NotificationManager::class.java).apply {
            cancel(RETIRED_ADHAN_NOTIFICATION_ID)
            cancel(OLDER_RETIRED_ADHAN_NOTIFICATION_ID)
            cancel(ORIGINAL_RETIRED_ADHAN_NOTIFICATION_ID)
            cancel(OLDEST_RETIRED_ADHAN_NOTIFICATION_ID)
        }
    }

    /**
     * The result of attempting to publish the active Adhan alert.
     *
     * `NotificationManager.notify()` is void and can return normally even while
     * Android suppresses an alert. This result verifies the prerequisites and
     * confirms that the app's active-notification list contains the exact
     * Adhan id/channel before the receiver records it as visible delivery.
     */
    data class PostResult(
        val posted: Boolean,
        val detail: String? = null,
    )

    fun adhanNotification(context: Context, prayer: Prayer): Notification {
        val stopIntent = PendingIntent.getBroadcast(
            context,
            ADHAN_NOTIFICATION_ID,
            Intent(context, AdhanNotificationActionReceiver::class.java)
                .setAction(AdhanNotificationActionReceiver.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val prayerLabel = context.getString(prayerNameRes(prayer))
        return NotificationCompat.Builder(context, NotificationChannels.ADHAN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            // Do not attach a large image. On some OEM notification templates it
            // is rendered alongside the launcher/app identity and looks like a
            // duplicated or stale second icon. The new monochrome status glyph
            // is the only app-supplied visual on this active alert.
            .setContentTitle(context.getString(R.string.adhan_notification_title))
            .setContentText(context.getString(R.string.prayer_name, prayerLabel))
            .setSubText(prayerLabel)
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.adhan_notification_big_text, prayerLabel)))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // Android uses the high-importance channel for the heads-up surface.
            // These compatibility flags cover pre-channel devices and declare that
            // the content is safe for the device lock screen.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(context.getColor(R.color.adhan_accent))
            .setColorized(false)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setShowWhen(true)
            .setUsesChronometer(false)
            .setTicker(context.getString(R.string.adhan_notification_ticker, prayerLabel))
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setTimeoutAfter(10 * 60 * 1000L) // auto-dismiss guard if service dies unexpectedly
            .addAction(
                org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028,
                context.getString(R.string.adhan_notification_stop),
                stopIntent,
            )
            // A live Adhan remains visible and cannot be swiped away. The sole
            // explicit in-notification termination path is the Stop Adhan action.
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .build()
    }

    /**
     * Checks every Android prerequisite for a visible Adhan alert without
     * posting it. The settings screen and receiver use this same check so the
     * displayed readiness cannot disagree with real delivery.
     */
    fun notificationPreflight(context: Context): PostResult {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return PostResult(posted = false, detail = "Android notification permission is not granted")
        }
        if (!manager.areNotificationsEnabled()) {
            return PostResult(posted = false, detail = "Android app notifications are disabled")
        }

        // A channel can be deleted in Android settings while an alarm remains
        // scheduled. Re-create a missing channel with defaults, but never alter
        // an existing user-owned channel.
        NotificationChannels.create(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(NotificationChannels.ADHAN)
                ?: return PostResult(posted = false, detail = "Adhan notification channel is unavailable")
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return PostResult(posted = false, detail = "Adhan notification channel is disabled in Android settings")
            }
        }
        return PostResult(posted = true)
    }

    /**
     * Publishes and confirms the active Adhan alert. Audio delivery is never
     * blocked by a failed visual alert, but diagnostics must report the real
     * Android condition rather than treating a non-throwing `notify()` call as
     * proof that the user could see it.
     */
    fun showAdhan(context: Context, prayer: Prayer): PostResult {
        cancelReminder(context)
        cancelRetiredAdhan(context)
        val preflight = notificationPreflight(context)
        if (!preflight.posted) return preflight
        val manager = context.getSystemService(NotificationManager::class.java)
        val posted = runCatching {
            manager.notify(ADHAN_NOTIFICATION_ID, adhanNotification(context, prayer))
            // NotificationManager.notify is asynchronous on some Android
            // builds. A same-stack activeNotifications query can therefore be
            // empty even though the system accepts and shows the alert moments
            // later. Wait only a small bounded window on the receiver's worker
            // path before recording the true Android result.
            repeat(ACTIVE_NOTIFICATION_CONFIRMATION_ATTEMPTS) {
                if (manager.activeNotifications.any { notification ->
                        notification.id == ADHAN_NOTIFICATION_ID &&
                            notification.notification.channelId == NotificationChannels.ADHAN
                    }
                ) return@runCatching true
                SystemClock.sleep(ACTIVE_NOTIFICATION_CONFIRMATION_INTERVAL_MS)
            }
            false
        }
        return posted.fold(
            onSuccess = { active ->
                if (active) {
                    PostResult(posted = true)
                } else {
                    PostResult(posted = false, detail = "Android did not retain the active Adhan alert")
                }
            },
            onFailure = { error ->
                PostResult(
                    posted = false,
                    detail = "Adhan notification posting failed: ${error.javaClass.simpleName}",
                )
            },
        )
    }

    /** Removes the single active card only after playback ends or the user stops it. */
    fun cancelActiveAdhan(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(ADHAN_NOTIFICATION_ID)
    }

    /** Removes the single pre-prayer card as soon as the real Adhan begins. */
    fun cancelReminder(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(REMINDER_NOTIFICATION_ID)
    }

    fun showReminder(context: Context, prayer: Prayer, minutesBefore: Int) {
        val label = context.getString(prayerNameRes(prayer))
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(
                context.getString(
                    R.string.reminder_message,
                    label,
                    minutesBefore,
                ),
            )
            .setSubText(label)
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_message, label, minutesBefore)))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setTimeoutAfter(15 * 60 * 1000L)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun prayerNameRes(prayer: Prayer): Int = prayerLabelRes(prayer)

    private const val ACTIVE_NOTIFICATION_CONFIRMATION_ATTEMPTS = 10
    private const val ACTIVE_NOTIFICATION_CONFIRMATION_INTERVAL_MS = 75L
}
