package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builder for the permanent next-adhan countdown notification
 * ([NotificationCategory.PrayerCountdown]). It deliberately uses the normal,
 * compact notification template: one title for the upcoming prayer, then one
 * colour-coded status line for the upcoming and missed states. This keeps the
 * card always visible without turning it into an expanded notification.
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

        val compactStatus = SpannableStringBuilder()
        if (data.hasLocation && data.nextPrayer != null) {
            val remaining = context.getString(
                R.string.next_adhan_remaining,
                formatCountdown(data.remainingSeconds),
            )
            compactStatus.append(remaining)
            compactStatus.setSpan(
                ForegroundColorSpan(context.getColor(R.color.adhan_accent)),
                0,
                compactStatus.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )

            val missed = data.missedPrayer
            if (missed != null && showMissed) {
                compactStatus.append("  •  ")
                val missedStart = compactStatus.length
                compactStatus.append(
                    context.getString(
                        R.string.next_adhan_missed,
                        context.getString(prayerLabelRes(missed)),
                        TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                    ),
                )
                compactStatus.setSpan(
                    ForegroundColorSpan(missedColor),
                    missedStart,
                    compactStatus.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                compactStatus.setSpan(
                    StyleSpan(Typeface.BOLD),
                    missedStart,
                    compactStatus.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        } else {
            compactStatus.append(context.getString(R.string.next_adhan_no_location))
        }

        // Tapping the notification opens the prayer-times screen directly.
        val contentIntent = runCatching {
            val intent = Intent(Intent.ACTION_VIEW, "muslim://times".toUri())
                .setPackage(context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }.getOrNull() ?: context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(compactStatus)
            .setColor(context.getColor(R.color.adhan_accent))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .build()
    }
}
