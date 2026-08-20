package org.muslim.app.feature.quran.data

import android.app.Notification
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat

/**
 * Process-local bridge between the notification listener and the recitation
 * foreground service. Both live in the same process, so a plain singleton is
 * enough — no IPC or broadcasts needed.
 */
object RecitationPauseController {
    /** Invoked (main thread) when a soundful notification should pause the recitation. */
    @Volatile
    var onPauseRequested: (() -> Unit)? = null

    /** Invoked (main thread) when the recitation may safely resume. */
    @Volatile
    var onResumeRequested: (() -> Unit)? = null

    /** Whether the notification-access permission is granted to our listener. */
    fun isListenerGranted(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
}

/**
 * Pauses Quran recitation while a *soundful* notification is showing and
 * resumes a few seconds after the last one disappears, so the recitation
 * never overlaps a notification tone. Phone calls and other media are already
 * handled by audio focus in [RecitationPlaybackService]; notifications do not
 * request audio focus, which is why they need this listener.
 *
 * The system only delivers callbacks once the user grants notification
 * access (Settings → Special access → Notification access). Until then the
 * service simply does nothing, and everything else keeps working.
 */
class RecitationPauseOnNotifications : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())

    /** Keys of soundful notifications that are currently pausing the recitation. */
    private val pausingKeys = mutableSetOf<String>()

    /** True when we paused the player ourselves (so we may resume it). */
    private var autoPaused = false

    private val resumeRunnable = Runnable {
        pausingKeys.clear()
        if (autoPaused) {
            autoPaused = false
            RecitationPauseController.onResumeRequested?.invoke()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        // Never react to our own notifications (download progress, playback…).
        if (sbn.packageName == packageName) return
        // Only soundful, non-ongoing notifications can overlap the recitation.
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        if (!hasSound(notification)) return

        handler.post {
            val wasIdle = pausingKeys.isEmpty()
            pausingKeys.add(sbn.key)
            if (wasIdle) {
                autoPaused = true
                RecitationPauseController.onPauseRequested?.invoke()
            }
            // Re-arm the resume timer so overlapping notification sounds keep
            // the recitation paused until they all finish.
            handler.removeCallbacks(resumeRunnable)
            handler.postDelayed(resumeRunnable, RESUME_DELAY_MS)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        handler.post {
            pausingKeys.remove(sbn.key)
            if (pausingKeys.isEmpty() && autoPaused) {
                handler.removeCallbacks(resumeRunnable)
                autoPaused = false
                RecitationPauseController.onResumeRequested?.invoke()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun hasSound(notification: Notification): Boolean =
        notification.sound != null ||
            notification.defaults and Notification.DEFAULT_SOUND != 0

    companion object {
        /** How long to stay paused after the last soundful notification. */
        private const val RESUME_DELAY_MS = 4_000L
    }
}
