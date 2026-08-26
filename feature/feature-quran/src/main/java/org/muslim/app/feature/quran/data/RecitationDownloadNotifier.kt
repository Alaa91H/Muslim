package org.muslim.app.feature.quran.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.muslim.app.feature.quran.R
import java.util.Locale

/**
 * Temporary, auto-dismissing progress notification for the recitation
 * download that runs right before playback starts. Shows the surah name,
 * percentage, remaining time and transfer speed, then disappears on its own
 * once the download completes (or fails / is cancelled).
 *
 * Reuses the quran-downloads channel so it behaves like the other recitation
 * downloads without adding a new channel to the system settings.
 */
class RecitationDownloadNotifier(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "quran_downloads"
        private const val NOTIFICATION_ID = 4207
        /** Consumed by MainActivity to navigate directly to Quran downloads. */
        const val EXTRA_ROUTE = "org.muslim.app.extra.ROUTE"
        const val DOWNLOADS_ROUTE = "quran/downloads"
    }

    /** Posts/updates the progress notification (replaces the previous one). */
    fun show(surahName: String, percent: Int, remainingSeconds: Long, bytesPerSecond: Long) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.quran_download_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val clamped = percent.coerceIn(0, 100)
        val remaining = if (remainingSeconds > 0) formatDuration(remainingSeconds) else "…"
        val speed = formatBytesPerSecond(bytesPerSecond)
        // Percent comes from the existing quran_download_percent resource and
        // the detail line uses %s-only specifiers, so no literal '%' ever
        // reaches String.format (safe in every translated locale).
        val detail = context.getString(R.string.quran_download_playback_detail, remaining, speed)
        val openDownloadsIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra(EXTRA_ROUTE, DOWNLOADS_ROUTE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        val contentIntent = openDownloadsIntent?.let {
            android.app.PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                it,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2026)
            .setContentTitle(context.getString(R.string.quran_download_playback_title, surahName))
            .setContentIntent(contentIntent)
            .setContentText("${context.getString(R.string.quran_download_percent, clamped)} · $detail")
            .setProgress(100, clamped, false)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    /** Removes the notification (called when the download finishes). */
    fun dismiss() {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    private fun formatDuration(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.ROOT, "%d:%02d", m, s)
    }

    private fun formatBytesPerSecond(bytesPerSecond: Long): String {
        if (bytesPerSecond >= 1_048_576L) {
            return String.format(Locale.ROOT, "%.1f MB", bytesPerSecond / 1_048_576.0)
        }
        if (bytesPerSecond >= 1024L) {
            return String.format(Locale.ROOT, "%.0f KB", bytesPerSecond / 1024.0)
        }
        return "$bytesPerSecond B"
    }
}
