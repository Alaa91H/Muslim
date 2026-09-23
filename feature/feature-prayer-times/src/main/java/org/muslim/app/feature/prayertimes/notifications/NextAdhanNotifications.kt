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
 * The collapsed card intentionally uses a two-level hierarchy:
 * prayer + wall-clock time as the title, then the live remaining duration.
 * When expanded, the missed prayer is added as a compact second status row.
 *
 * Keeping each piece of information on its own system-managed row avoids the
 * awkward wrapping and oversized whitespace that BigTextStyle can produce on
 * RTL layouts and OEM notification surfaces.
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
        val upcomingColor = context.getColor(R.color.adhan_accent)
        val textLines = buildTextLines(
            context = context,
            data = data,
            showMissed = showMissed,
            use24h = use24h,
            upcomingColor = upcomingColor,
        )

        // Tapping the notification opens the prayer-times screen directly.
        val contentIntent = createContentIntent(context)

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            // This is a silent status/countdown card, not the active Adhan alert.
            // It must never attach a large icon that could make the compact card
            // look like a duplicate, retired, or active alarm notification.
            .setContentTitle(textLines.title)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)

        if (textLines.remaining.isNotEmpty()) {
            builder.setContentText(textLines.remaining)
        }

        // InboxStyle keeps the expanded layout dense and predictable: the
        // upcoming prayer remains the visual anchor and the optional missed
        // prayer becomes one additional row instead of a separate BigText block.
        if (textLines.remaining.isNotEmpty() || textLines.missed.isNotEmpty()) {
            val expanded = NotificationCompat.InboxStyle()
                .setBigContentTitle(textLines.title)
            if (textLines.remaining.isNotEmpty()) {
                expanded.addLine(textLines.remaining)
            }
            if (textLines.missed.isNotEmpty()) {
                expanded.addLine(textLines.missed)
            }
            builder.setStyle(expanded)
        }

        if (data.nextPrayerAt != null) {
            builder.setColor(upcomingColor)
        }
        return builder.build()
    }

    private data class CountdownTextLines(
        val title: SpannableStringBuilder,
        val remaining: SpannableStringBuilder,
        val missed: SpannableStringBuilder,
    )

    private fun buildTextLines(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean,
        use24h: Boolean,
        upcomingColor: Int,
    ): CountdownTextLines {
        val title = SpannableStringBuilder()
        val remaining = SpannableStringBuilder()
        val missed = SpannableStringBuilder()
        val missedColor = MissedAdhanColors.DEFAULT
        val nextPrayer = data.nextPrayer

        if (!data.hasLocation || nextPrayer == null || data.nextPrayerAt == null) {
            title.append(context.getString(R.string.next_adhan_no_location))
            return CountdownTextLines(title, remaining, missed)
        }

        val prayerLabel = context.getString(prayerLabelRes(nextPrayer))
        val wallClockTime = TimeFormats.timeFormatter(use24h).format(data.nextPrayerAt)
        title.append(prayerLabel)
        title.append("  ·  ")
        val wallClockStart = title.length
        title.append(wallClockTime)
        title.setSpan(
            ForegroundColorSpan(upcomingColor),
            wallClockStart,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        val remainingValue = formatCountdown(data.remainingSeconds)
        val remainingText = context.getString(R.string.next_adhan_remaining, remainingValue)
        remaining.append(remainingText)
        val remainingValueStart = remainingText.lastIndexOf(remainingValue).coerceAtLeast(0)
        remaining.setSpan(
            ForegroundColorSpan(upcomingColor),
            remainingValueStart,
            (remainingValueStart + remainingValue.length).coerceAtMost(remaining.length),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        data.missedPrayer?.takeIf { showMissed }?.let { missedPrayer ->
            missed.append(
                context.getString(
                    R.string.next_adhan_missed,
                    context.getString(prayerLabelRes(missedPrayer)),
                    TimeFormats.timeFormatter(use24h).format(data.missedPrayerAt),
                ),
            )
            missed.append("  ·  ")
            val elapsedValue = formatCountdown(data.elapsedSeconds)
            val elapsedText = context.getString(R.string.next_adhan_elapsed, elapsedValue)
            val elapsedStart = missed.length
            missed.append(elapsedText)
            val elapsedValueOffset = elapsedText.lastIndexOf(elapsedValue).coerceAtLeast(0)
            missed.setSpan(
                ForegroundColorSpan(missedColor),
                elapsedStart + elapsedValueOffset,
                (elapsedStart + elapsedValueOffset + elapsedValue.length).coerceAtMost(missed.length),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        return CountdownTextLines(title, remaining, missed)
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
