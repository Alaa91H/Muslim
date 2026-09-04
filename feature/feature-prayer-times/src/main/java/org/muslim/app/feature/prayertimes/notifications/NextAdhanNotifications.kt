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
import org.muslim.app.core.notifications.MissedAdhanColors
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builders for the permanent next-Adhan countdown notification.
 *
 * The compact line marks the upcoming prayer's wall-clock time in Islamic green
 * and the remaining duration in red. The expanded elapsed duration follows the
 * same red danger/status treatment; these semantic colours are not user-tinted.
 */
object NextAdhanNotifications {

    /** Fresh identity for the silent countdown card, forcing a current system card after update. */
    const val NEXT_ADHAN_NOTIFICATION_ID = 1015
    /** Most recent retired identity, retained for device-level migration coverage. */
    const val RETIRED_COUNTDOWN_NOTIFICATION_ID = 1013
    private const val OLDER_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1011
    private const val ORIGINAL_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1004
    private const val OLDEST_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1003

    fun cancelRetiredCountdown(context: Context) {
        context.getSystemService(android.app.NotificationManager::class.java).apply {
            cancel(RETIRED_COUNTDOWN_NOTIFICATION_ID)
            cancel(OLDER_RETIRED_COUNTDOWN_NOTIFICATION_ID)
            cancel(ORIGINAL_RETIRED_COUNTDOWN_NOTIFICATION_ID)
            cancel(OLDEST_RETIRED_COUNTDOWN_NOTIFICATION_ID)
        }
    }

    fun build(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean = true,
        use24h: Boolean = false,
    ): Notification {
        val upcomingTimeColor = context.getColor(R.color.adhan_accent)
        val textLines = buildTextLines(
            context = context,
            data = data,
            showMissed = showMissed,
            use24h = use24h,
            upcomingTimeColor = upcomingTimeColor,
        )

        // Tapping the notification opens the prayer-times screen directly.
        val contentIntent = createContentIntent(context)

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            // This is a silent status/countdown card, not the active Adhan alert.
            // It must never attach a large icon that could make the compact card
            // look like a duplicate, retired, or active alarm notification.
            .setContentTitle(textLines.compact)
            .setSubText(data.nextPrayer?.let { context.getString(prayerLabelRes(it)) })
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)

        // BigText is deliberately supplied only for the optional second line.
        // The system keeps [compactLine] as the one-line collapsed presentation.
        if (textLines.expandedMissed.isNotEmpty()) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(textLines.expandedMissed))
        }
        if (data.nextPrayerAt != null) {
            builder.setColor(upcomingTimeColor)
        }
        return builder.build()
    }

    private data class CountdownTextLines(
        val compact: SpannableStringBuilder,
        val expandedMissed: SpannableStringBuilder,
    )

    private fun buildTextLines(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean,
        use24h: Boolean,
        upcomingTimeColor: Int,
    ): CountdownTextLines {
        val compact = SpannableStringBuilder()
        val expandedMissed = SpannableStringBuilder()
        val durationColor = MissedAdhanColors.DEFAULT
        val nextPrayer = data.nextPrayer
        if (!data.hasLocation || nextPrayer == null) {
            compact.append(context.getString(R.string.next_adhan_no_location))
            return CountdownTextLines(compact, expandedMissed)
        }

        val wallClockTime = TimeFormats.timeFormatter(use24h).format(data.nextPrayerAt)
        val title = context.getString(
            R.string.next_adhan_notification_title,
            context.getString(prayerLabelRes(nextPrayer)),
            wallClockTime,
        )
        compact.append(title)
        compact.setSpan(
            ForegroundColorSpan(upcomingTimeColor),
            title.lastIndexOf(wallClockTime).coerceAtLeast(0),
            (title.lastIndexOf(wallClockTime).coerceAtLeast(0) + wallClockTime.length).coerceAtMost(compact.length),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        compact.append(" · ")
        val remainingStart = compact.length
        compact.append(context.getString(R.string.next_adhan_remaining, formatCountdown(data.remainingSeconds)))
        compact.setSpan(
            ForegroundColorSpan(durationColor),
            remainingStart,
            compact.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        data.missedPrayer?.takeIf { showMissed }?.let { missed ->
            expandedMissed.append(
                context.getString(
                    R.string.next_adhan_missed,
                    context.getString(prayerLabelRes(missed)),
                    TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                ),
            )
            expandedMissed.append(" · ")
            val elapsedStart = expandedMissed.length
            expandedMissed.append(context.getString(R.string.next_adhan_elapsed, formatCountdown(data.elapsedSeconds)))
            expandedMissed.setSpan(
                ForegroundColorSpan(durationColor),
                elapsedStart,
                expandedMissed.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        return CountdownTextLines(compact, expandedMissed)
    }

    private fun createContentIntent(context: Context): PendingIntent? = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, "muslim://times".toUri())
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }.getOrNull() ?: context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
        PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
}
