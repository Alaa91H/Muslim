package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import androidx.core.net.toUri
import android.content.Intent
import android.app.PendingIntent
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builders for the permanent "next adhan" countdown notification
 * ([NotificationCategory.PrayerCountdown]): the upcoming prayer with its
 * time and the live countdown, plus — in the user-chosen color (red by
 * default) — the missed adhan with its fixed wall-clock time (stable until
 * the next adhan arrives, unlike a live elapsed counter). The missed line
 * can be hidden via the notification manager ([showMissed]) and its color
 * changed in the settings ([missedColor]). The notification is ongoing and
 * only refreshed in place.
 */
internal object NextAdhanNotifications {

    const val NEXT_ADHAN_NOTIFICATION_ID = 1003

    fun build(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean = true,
        missedColor: Int = org.muslim.app.core.notifications.MissedAdhanColors.DEFAULT,
        use24h: Boolean = false,
    ): Notification {
        val title = if (data.hasLocation && data.nextPrayer != null) {
            context.getString(
                R.string.next_adhan_notification_title,
                context.getString(prayerLabelRes(data.nextPrayer)),
                TimeFormats.timeFormatter(use24h).format(data.nextPrayerAt),
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
                        TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                    ),
                )
                // Live count-up: how long ago the missed adhan sounded.
                if (data.elapsedSeconds > 0) {
                    bigText.append("\n")
                    val elapsedStart = bigText.length
                    bigText.append(
                        context.getString(
                            R.string.next_adhan_elapsed,
                            formatCountdown(data.elapsedSeconds),
                        ),
                    )
                    bigText.setSpan(
                        ForegroundColorSpan(missedColor),
                        elapsedStart,
                        bigText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
                bigText.setSpan(
                    ForegroundColorSpan(missedColor),
                    start,
                    bigText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        } else {
            bigText.append(context.getString(R.string.next_adhan_no_location))
        }

        // Tapping the notification opens the prayer-times screen directly (the
        // home tab now *is* the prayer times page, reached via its deep link).
        val contentIntent = runCatching {
            val intent = Intent(Intent.ACTION_VIEW, "muslim://times".toUri())
                .setPackage(context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }.getOrNull() ?: context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
        // Accent the live countdown with the app's primary colour.
        if (data.nextPrayerAt != null) {
            builder.setColor(context.getColor(org.muslim.app.feature.prayertimes.R.color.adhan_accent))
        }
        return builder.build()
    }
}
