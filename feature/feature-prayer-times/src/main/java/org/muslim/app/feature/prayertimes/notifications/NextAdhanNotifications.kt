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
        val title = if (data.hasLocation && data.nextPrayer != null) {
            context.getString(
                R.string.next_adhan_notification_title,
                context.getString(prayerLabelRes(data.nextPrayer)),
                TimeFormats.timeFormatter(use24h).format(data.nextPrayerAt),
            )
        } else {
            context.getString(R.string.next_adhan_no_location)
        }

        // The collapsed notification intentionally carries only the next
        // prayer's name and wall-clock time in [title]. Details belong to the
        // expanded surface so the permanent status notification stays quiet.
        val expandedText = SpannableStringBuilder()
        if (data.hasLocation && data.nextPrayer != null) {
            val remainingStart = expandedText.length
            expandedText.append(
                context.getString(R.string.next_adhan_remaining, formatCountdown(data.remainingSeconds)),
            )
            expandedText.setSpan(
                ForegroundColorSpan(missedColor),
                remainingStart,
                expandedText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            val missed = data.missedPrayer
            if (missed != null && showMissed) {
                expandedText.append("\n")
                expandedText.append(
                    context.getString(
                        R.string.next_adhan_missed,
                        context.getString(prayerLabelRes(missed)),
                        TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                    ),
                )
                val elapsedStart = expandedText.length
                expandedText.append(" · ")
                expandedText.append(
                    context.getString(R.string.next_adhan_elapsed, formatCountdown(data.elapsedSeconds)),
                )
                expandedText.setSpan(
                    ForegroundColorSpan(missedColor),
                    elapsedStart,
                    expandedText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        } else {
            expandedText.append(context.getString(R.string.next_adhan_no_location))
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
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
            // This is a silent status/countdown card, not the active Adhan
            // alert. Do not attach a large branded image that can make it look
            // like a missed or old alarm notification.
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
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
