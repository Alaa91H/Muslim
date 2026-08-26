package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builders for the permanent next-Adhan countdown notification.
 *
 * The collapsed system surface is deliberately one line only: next prayer,
 * wall-clock time, and a red remaining duration. Expanding the card adds one
 * additional line for the last missed Adhan and its red elapsed duration.
 */
object NextAdhanNotifications {

    /** New identity for the silent countdown card. Legacy ongoing cards must be cancelled explicitly. */
    const val NEXT_ADHAN_NOTIFICATION_ID = 1004
    const val RETIRED_COUNTDOWN_NOTIFICATION_ID = 1003

    fun cancelRetiredCountdown(context: Context) {
        context.getSystemService(android.app.NotificationManager::class.java)
            .cancel(RETIRED_COUNTDOWN_NOTIFICATION_ID)
    }

    fun build(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean = true,
        missedColor: Int = org.muslim.app.core.notifications.MissedAdhanColors.DEFAULT,
        use24h: Boolean = false,
    ): Notification {
        val compactLine = SpannableStringBuilder()
        val expandedMissedLine = SpannableStringBuilder()

        if (data.hasLocation && data.nextPrayer != null) {
            compactLine.append(
                context.getString(
                    R.string.next_adhan_notification_title,
                    context.getString(prayerLabelRes(data.nextPrayer)),
                    TimeFormats.timeFormatter(use24h).format(data.nextPrayerAt),
                ),
            )
            compactLine.append(" · ")
            val remainingStart = compactLine.length
            compactLine.append(
                context.getString(R.string.next_adhan_remaining, formatCountdown(data.remainingSeconds)),
            )
            compactLine.setSpan(
                ForegroundColorSpan(missedColor),
                remainingStart,
                compactLine.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )

            val missed = data.missedPrayer
            if (missed != null && showMissed) {
                expandedMissedLine.append(
                    context.getString(
                        R.string.next_adhan_missed,
                        context.getString(prayerLabelRes(missed)),
                        TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                    ),
                )
                expandedMissedLine.append(" · ")
                val elapsedStart = expandedMissedLine.length
                expandedMissedLine.append(
                    context.getString(R.string.next_adhan_elapsed, formatCountdown(data.elapsedSeconds)),
                )
                expandedMissedLine.setSpan(
                    ForegroundColorSpan(missedColor),
                    elapsedStart,
                    expandedMissedLine.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        } else {
            compactLine.append(context.getString(R.string.next_adhan_no_location))
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

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1251)
            // This is a silent status/countdown card, not the active Adhan alert.
            // It must never attach a large icon that could make the compact card
            // look like a duplicate, retired, or active alarm notification.
            .setContentTitle(compactLine)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)

        // BigText is deliberately supplied only for the optional second line.
        // The system keeps [compactLine] as the one-line collapsed presentation.
        if (expandedMissedLine.isNotEmpty()) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(expandedMissedLine))
        }
        if (data.nextPrayerAt != null) {
            builder.setColor(context.getColor(org.muslim.app.feature.prayertimes.R.color.adhan_accent))
        }
        return builder.build()
    }
}
