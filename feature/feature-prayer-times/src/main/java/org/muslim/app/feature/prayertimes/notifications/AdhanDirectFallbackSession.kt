package org.muslim.app.feature.prayertimes.notifications

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.EntryPointAccessors

/**
 * Owns the rare receiver-level AudioTrack recovery session.
 *
 * The normal Adhan path is owned by [AdhanPlaybackService], but when Android
 * fails to start that foreground service the alarm receiver plays a direct
 * synthetic fallback. Keeping that fallback in a process-wide session makes
 * the notification Stop/Dismiss actions capable of terminating it too.
 */
internal object AdhanDirectFallbackSession {

    private val lock = Any()
    private var generation = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    fun begin(newWakeLock: PowerManager.WakeLock): Long = synchronized(lock) {
        generation += 1L
        releaseWakeLockLocked()
        wakeLock = newWakeLock
        generation
    }

    fun finish(context: Context, token: Long) {
        val ownsSession = synchronized(lock) {
            if (token != generation) {
                false
            } else {
                generation += 1L
                releaseWakeLockLocked()
                true
            }
        }
        if (ownsSession) {
            AdhanNotifications.cancelActiveAdhan(context)
        }
    }

    /**
     * Stops every out-of-service playback resource immediately.
     *
     * [AdhanSoundPlayer] is a singleton, so this also closes the small timing
     * window where a foreground service is stopping but its onDestroy callback
     * has not reached the player yet.
     */
    fun stop(context: Context) {
        synchronized(lock) {
            generation += 1L
            releaseWakeLockLocked()
        }
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AdhanEntryPoint::class.java,
        )
        entryPoint.soundPlayer().stop()
        AdhanNotifications.cancelActiveAdhan(context)
    }

    private fun releaseWakeLockLocked() {
        wakeLock?.let { heldLock ->
            runCatching {
                if (heldLock.isHeld) heldLock.release()
            }
        }
        wakeLock = null
    }
}
