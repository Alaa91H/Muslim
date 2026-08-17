package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.localTimeFormatter
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builders for the permanent "next adhan" countdown notification
 * ([NotificationCategory.PrayerCountdown]): the upcoming prayer with its
 * time and the live countdown, plus — in red — the missed adhan with its
 * fixed wall-clock time (stable until the next adhan arrives, unlike a live
 * elapsed counter). The missed line can be hidden via the notification
 * manager ([showMissed]). The notification is ongoing and only refreshed in
 * place.
 */
internal object NextAdhanNotifications {

    const val NEXT_ADHAN_NOTIFICATION_ID = 1003

    fun build(context: Context, data: PrayerCountdownData, showMissed: Boolean = true): Notification {
        val title = if (data.hasLocation && data.nextPrayer != null) {
            context.getString(
                R.string.next_adhan_notification_title,
                context.getString(prayerLabelRes(data.nextPrayer)),
                localTimeFormatter.format(data.nextPrayerAt),
            )
        } else {
            context.getString(R.string.next_adhan_no_location)
        }

        val bigText = SpannableStringBuilder()
        if (data.hasLocation && data.nextPrayer != null) {
            bigText.append(
                context.getString(R.string.next_adhan_remaining, formatCountdown(data.remainingSeconds)),
            )
            val missed = data.missedPrayer
            if (missed != null && showMissed) {
                bigText.append("\n")
                val start = bigText.length
                bigText.append(
                    context.getString(
                        R.string.next_adhan_missed,
                        context.getString(prayerLabelRes(missed)),
                        localTimeFormatter.format(data.missedPrayerAt),
                    ),
                )
                bigText.setSpan(
                    ForegroundColorSpan(Color.RED),
                    start,
                    bigText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        } else {
            bigText.append(context.getString(R.string.next_adhan_no_location))
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .build()
    }
}
