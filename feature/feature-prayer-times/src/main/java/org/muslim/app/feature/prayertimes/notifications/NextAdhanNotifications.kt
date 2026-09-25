package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Builds the permanent prayer-status notification.
 *
 * The collapsed surface stays intentionally concise. Expanding it reveals all
 * five daily prayers in one horizontal strip, with the upcoming prayer
 * highlighted and the most recently elapsed prayer visually distinguished.
 *
 * Countdown/count-up values are Android [android.widget.Chronometer] views, so
 * they remain second-accurate without rebuilding the whole notification once
 * per second.
 */
object NextAdhanNotifications {

    /** Fresh identity for the silent countdown card, forcing a current system card after update. */
    const val NEXT_ADHAN_NOTIFICATION_ID = 1015
    /** Most recent retired identity, retained for device-level migration coverage. */
    const val RETIRED_COUNTDOWN_NOTIFICATION_ID = 1013
    private const val OLDER_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1011
    private const val ORIGINAL_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1004
    private const val OLDEST_RETIRED_COUNTDOWN_NOTIFICATION_ID = 1003

    private val DISPLAY_PRAYERS = listOf(
        Prayer.Fajr,
        Prayer.Dhuhr,
        Prayer.Asr,
        Prayer.Maghrib,
        Prayer.Isha,
    )

    private val PRAYER_VIEW_IDS = mapOf(
        Prayer.Fajr to PrayerViewIds(
            R.id.notification_prayer_fajr_cell,
            R.id.notification_prayer_fajr_name,
            R.id.notification_prayer_fajr_time,
            R.id.notification_prayer_fajr_status_label,
            R.id.notification_prayer_fajr_status_timer,
        ),
        Prayer.Dhuhr to PrayerViewIds(
            R.id.notification_prayer_dhuhr_cell,
            R.id.notification_prayer_dhuhr_name,
            R.id.notification_prayer_dhuhr_time,
            R.id.notification_prayer_dhuhr_status_label,
            R.id.notification_prayer_dhuhr_status_timer,
        ),
        Prayer.Asr to PrayerViewIds(
            R.id.notification_prayer_asr_cell,
            R.id.notification_prayer_asr_name,
            R.id.notification_prayer_asr_time,
            R.id.notification_prayer_asr_status_label,
            R.id.notification_prayer_asr_status_timer,
        ),
        Prayer.Maghrib to PrayerViewIds(
            R.id.notification_prayer_maghrib_cell,
            R.id.notification_prayer_maghrib_name,
            R.id.notification_prayer_maghrib_time,
            R.id.notification_prayer_maghrib_status_label,
            R.id.notification_prayer_maghrib_status_timer,
        ),
        Prayer.Isha to PrayerViewIds(
            R.id.notification_prayer_isha_cell,
            R.id.notification_prayer_isha_name,
            R.id.notification_prayer_isha_time,
            R.id.notification_prayer_isha_status_label,
            R.id.notification_prayer_isha_status_timer,
        ),
    )

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
        val contentIntent = createContentIntent(context)
        val fallback = buildFallbackText(context, data, use24h)

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setContentTitle(fallback.first)
            .setContentText(fallback.second)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(context.getColor(R.color.notification_primary))
            .setColorized(false)
            .setContentIntent(contentIntent)

        if (data.hasLocation && data.nextPrayer != null && data.nextPrayerAt != null) {
            val (compact, expanded) = buildCustomRemoteViews(context, data, showMissed, use24h)
            builder
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(compact)
                .setCustomBigContentView(expanded)
        }

        return builder.build()
    }

    private fun buildFallbackText(
        context: Context,
        data: PrayerCountdownData,
        use24h: Boolean,
    ): Pair<CharSequence, CharSequence> {
        val nextPrayer = data.nextPrayer
        val nextAt = data.nextPrayerAt
        if (!data.hasLocation || nextPrayer == null || nextAt == null) {
            return context.getString(R.string.next_adhan_no_location) to ""
        }

        val wallClockTime = TimeFormats.timeFormatter(use24h).format(nextAt)
        val title = context.getString(
            R.string.next_adhan_notification_title,
            context.getString(prayerLabelRes(nextPrayer)),
            wallClockTime,
        )
        val remaining = context.getString(
            R.string.next_adhan_remaining,
            formatCountdown(data.remainingSeconds),
        )
        return title to remaining
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildCustomRemoteViews(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean = true,
        use24h: Boolean = false,
    ): Pair<RemoteViews, RemoteViews> =
        buildCompactRemoteViews(context, data, use24h) to
            buildExpandedRemoteViews(context, data, showMissed, use24h)

    private fun buildCompactRemoteViews(
        context: Context,
        data: PrayerCountdownData,
        use24h: Boolean,
    ): RemoteViews {
        val nextPrayer = requireNotNull(data.nextPrayer)
        val nextAt = requireNotNull(data.nextPrayerAt)
        return RemoteViews(context.packageName, R.layout.notification_next_adhan_compact).apply {
            setTextViewText(
                R.id.notification_compact_title,
                context.getString(
                    R.string.next_adhan_notification_title,
                    context.getString(prayerLabelRes(nextPrayer)),
                    TimeFormats.timeFormatter(use24h).format(nextAt),
                ),
            )
            setChronometer(
                R.id.notification_compact_remaining,
                remainingChronometerBase(data.remainingSeconds),
                context.getString(R.string.next_adhan_remaining, "%s"),
                true,
            )
            setChronometerCountDown(R.id.notification_compact_remaining, true)
            setTextColor(
                R.id.notification_compact_remaining,
                context.getColor(R.color.notification_primary),
            )
        }
    }

    private fun buildExpandedRemoteViews(
        context: Context,
        data: PrayerCountdownData,
        showMissed: Boolean,
        use24h: Boolean,
    ): RemoteViews {
        val nextPrayer = requireNotNull(data.nextPrayer)
        val nextAt = requireNotNull(data.nextPrayerAt)
        val timeFormatter = TimeFormats.timeFormatter(use24h)

        return RemoteViews(context.packageName, R.layout.notification_next_adhan_expanded).apply {
            setTextViewText(
                R.id.notification_expanded_title,
                context.getString(
                    R.string.next_adhan_notification_title,
                    context.getString(prayerLabelRes(nextPrayer)),
                    timeFormatter.format(nextAt),
                ),
            )

            DISPLAY_PRAYERS.forEach { prayer ->
                val ids = requireNotNull(PRAYER_VIEW_IDS[prayer])
                setTextViewText(ids.name, context.getString(prayerLabelRes(prayer)))
                setTextViewText(
                    ids.time,
                    data.prayerTimes[prayer]?.let(timeFormatter::format) ?: "—",
                )
                setViewVisibility(ids.statusLabel, View.INVISIBLE)
                setViewVisibility(ids.statusTimer, View.INVISIBLE)
                setInt(ids.container, "setBackgroundResource", R.drawable.notification_prayer_cell)
                setTextColor(ids.name, context.getColor(R.color.notification_text_primary))
                setTextColor(ids.time, context.getColor(R.color.notification_text_secondary))
            }

            PRAYER_VIEW_IDS[nextPrayer]?.let { ids ->
                setInt(ids.container, "setBackgroundResource", R.drawable.notification_prayer_cell_next)
                setTextColor(ids.name, context.getColor(R.color.notification_gold))
                setTextColor(ids.time, context.getColor(R.color.notification_text_primary))
                setTextViewText(ids.statusLabel, shortStatusLabel(context, R.string.next_adhan_remaining))
                // Gold remains readable on the primary-container highlight and
                // visually connects the live countdown to the selected prayer.
                setTextColor(ids.statusLabel, context.getColor(R.color.notification_gold))
                setTextColor(ids.statusTimer, context.getColor(R.color.notification_gold))
                setViewVisibility(ids.statusLabel, View.VISIBLE)
                setViewVisibility(ids.statusTimer, View.VISIBLE)
                setChronometer(
                    ids.statusTimer,
                    remainingChronometerBase(data.remainingSeconds),
                    null,
                    true,
                )
                setChronometerCountDown(ids.statusTimer, true)
            }

            data.missedPrayer
                ?.takeIf { showMissed && it != nextPrayer }
                ?.let { missedPrayer ->
                    PRAYER_VIEW_IDS[missedPrayer]?.let { ids ->
                        setInt(ids.container, "setBackgroundResource", R.drawable.notification_prayer_cell_missed)
                        setTextViewText(ids.statusLabel, shortStatusLabel(context, R.string.next_adhan_elapsed))
                        setTextColor(ids.statusLabel, context.getColor(R.color.notification_error))
                        setTextColor(ids.statusTimer, context.getColor(R.color.notification_error))
                        setViewVisibility(ids.statusLabel, View.VISIBLE)
                        setViewVisibility(ids.statusTimer, View.VISIBLE)
                        setChronometer(
                            ids.statusTimer,
                            elapsedChronometerBase(data.elapsedSeconds),
                            null,
                            true,
                        )
                        setChronometerCountDown(ids.statusTimer, false)
                    }
                }
        }
    }

    /**
     * Reuses the existing fully-localized status phrases without adding a new
     * translation key to every locale. The duration placeholder is removed,
     * leaving the locale's own label (e.g. "الوقت المتبقي" / "Remaining").
     */
    private fun shortStatusLabel(context: Context, stringRes: Int): String {
        val marker = "__TIME__"
        return context.getString(stringRes, marker)
            .replace(marker, "")
            .trim()
            .trim(' ', ':', '：', '·', '—', '-')
    }

    private fun remainingChronometerBase(remainingSeconds: Long): Long =
        SystemClock.elapsedRealtime() + remainingSeconds.coerceAtLeast(0) * 1_000L

    private fun elapsedChronometerBase(elapsedSeconds: Long): Long =
        SystemClock.elapsedRealtime() - elapsedSeconds.coerceAtLeast(0) * 1_000L

    private fun createContentIntent(context: Context): PendingIntent? = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, "muslim://times".toUri())
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }.getOrNull() ?: context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
        PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }

    private data class PrayerViewIds(
        @IdRes val container: Int,
        @IdRes val name: Int,
        @IdRes val time: Int,
        @IdRes val statusLabel: Int,
        @IdRes val statusTimer: Int,
    )
}
