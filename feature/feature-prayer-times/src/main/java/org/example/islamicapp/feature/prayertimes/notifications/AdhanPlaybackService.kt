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
import org.example.islamicapp.feature.prayertimes.domain.AdhanPlaybackPlan
import org.example.islamicapp.feature.prayertimes.domain.AdhanSoundOption
import org.example.islamicapp.core.prayer.Prayer

/**
 * Foreground service that delivers the Adhan reliably in the background.
 *
 * - Honours the per-prayer [AdhanSoundOption] (sound / vibrate-only / silent)
 *   passed by the alarm.
 * - Applies the configured master volume with a gradual fade-in
 *   (PROJECT_PROMPT.md §6: تحكم بمستوى الصوت + تأثير Fade-in تدريجي).
 * - Plays the bundled adhan from `res/raw/adhan_default.*` when present
 *   (community-provided; no copyrighted recordings ship with the app).
 * - Falls back to a vibration pattern so the call to prayer is never missed.
 */
class AdhanPlaybackService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var fadeRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra(EXTRA_PRAYER)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
            ?: Prayer.Fajr
        val option = intent?.getStringExtra(EXTRA_SOUND_OPTION)
            ?.let { runCatching { AdhanSoundOption.valueOf(it) }.getOrNull() }
            ?: AdhanSoundOption.Default
        val vibrateEnabled = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true
        val volumePercent = intent?.getIntExtra(EXTRA_VOLUME, 100)?.coerceIn(0, 100) ?: 100

        val plan = AdhanPlaybackPlan.plan(
            option = option,
            hasBundledSound = hasBundledSound(),
            vibrationEnabled = vibrateEnabled,
        )
        if (!plan.playSound && !plan.vibrate) {
            // Silent mode — nothing to deliver (reminders still fire).
            stopSelf()
            return START_NOT_STICKY
        }

        NotificationChannels.create(this)
        startForeground(AdhanNotifications.ADHAN_NOTIFICATION_ID, AdhanNotifications.adhanNotification(this, prayer))

        if (plan.playSound) {
            playWithFadeIn(volumePercent)
        } else if (plan.vibrate) {
            vibrate()
        }

        handler.postDelayed({ stopSelf() }, MAX_PLAYBACK_MS)
        return START_NOT_STICKY
    }

    private fun hasBundledSound(): Boolean =
        resources.getIdentifier(RAW_ADHAN_NAME, "raw", packageName) != 0

    private fun playWithFadeIn(volumePercent: Int) {
        val rawId = resources.getIdentifier(RAW_ADHAN_NAME, "raw", packageName)
        val player = MediaPlayer.create(this, rawId) ?: return
        mediaPlayer = player
        player.setOnCompletionListener { stopSelf() }
        player.setOnErrorListener { _, _, _ -> stopSelf(); true }

        // Fade-in: ramp volume from 0 to the target over FADE_IN_MS.
        val target = volumePercent / 100f
        player.setVolume(0f, 0f)
        player.start()
        if (target <= 0f) return
        val start = System.currentTimeMillis()
        val runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer !== player) return
                val elapsed = System.currentTimeMillis() - start
                val progress = (elapsed.toFloat() / FADE_IN_MS).coerceAtMost(1f)
                val volume = target * progress
                player.setVolume(volume, volume)
                if (progress < 1f) handler.postDelayed(this, FADE_STEP_MS)
            }
        }
        fadeRunnable = runnable
        handler.post(runnable)
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
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PRAYER = "extra_prayer"
        private const val EXTRA_SOUND_OPTION = "extra_sound_option"
        private const val EXTRA_VIBRATE = "extra_vibrate"
        private const val EXTRA_VOLUME = "extra_volume"
        private const val RAW_ADHAN_NAME = "adhan_default"
        private const val MAX_PLAYBACK_MS = 5 * 60 * 1000L
        private const val FADE_IN_MS = 4_000L
        private const val FADE_STEP_MS = 50L

        fun start(
            context: Context,
            prayer: Prayer,
            vibrate: Boolean,
            soundOption: AdhanSoundOption = AdhanSoundOption.Default,
            volumePercent: Int = 100,
        ) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_SOUND_OPTION, soundOption.name)
                .putExtra(EXTRA_VIBRATE, vibrate)
                .putExtra(EXTRA_VOLUME, volumePercent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
