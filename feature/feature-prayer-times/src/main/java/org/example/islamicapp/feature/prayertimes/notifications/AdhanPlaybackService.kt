package org.example.islamicapp.feature.prayertimes.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import org.example.islamicapp.core.notifications.NotificationChannels
import org.example.islamicapp.feature.prayertimes.domain.Prayer

/**
 * Foreground service that delivers the Adhan reliably in the background.
 *
 * - Plays the bundled adhan from `res/raw/adhan_default.*` when present.
 *   (Adhan audio assets are community-provided/downloadable — see
 *   PROJECT_PROMPT.md §6 Phase 1; no copyrighted recordings ship with the app.)
 * - Falls back to a vibration pattern so the call to prayer is never missed.
 * - Stops itself after [MAX_PLAYBACK_SECONDS].
 */
class AdhanPlaybackService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra(EXTRA_PRAYER)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
            ?: Prayer.Fajr
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true

        NotificationChannels.create(this)
        startForeground(AdhanNotifications.ADHAN_NOTIFICATION_ID, AdhanNotifications.adhanNotification(this, prayer))

        play(prayer, vibrate)

        handler.postDelayed({ stopSelf() }, MAX_PLAYBACK_MS)
        return START_NOT_STICKY
    }

    private fun play(prayer: Prayer, vibrate: Boolean) {
        val rawId = resources.getIdentifier(RAW_ADHAN_NAME, "raw", packageName)
        if (rawId != 0) {
            val player = MediaPlayer.create(this, rawId)
            if (player != null) {
                mediaPlayer = player
                player.setOnCompletionListener { stopSelf() }
                player.setOnErrorListener { _, _, _ -> stopSelf(); true }
                player.start()
                return
            }
        }
        if (vibrate) vibrate()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return
        val effect = VibrationEffect.createWaveform(
            longArrayOf(0, 600, 400, 600, 400, 600),
            -1,
        )
        vibrator.vibrate(effect)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PRAYER = "extra_prayer"
        private const val EXTRA_VIBRATE = "extra_vibrate"
        private const val RAW_ADHAN_NAME = "adhan_default"
        private const val MAX_PLAYBACK_MS = 5 * 60 * 1000L

        fun start(context: Context, prayer: Prayer, vibrate: Boolean) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_VIBRATE, vibrate)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
