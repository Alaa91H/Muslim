package org.muslim.app.feature.prayertimes.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.AndroidEntryPoint
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.common.prayer.AdhanPlaybackPlan
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import java.io.File
import javax.inject.Inject

/**
 * Foreground service that delivers the Adhan reliably in the background.
 *
 * - Honours the per-prayer [AdhanSoundOption] (sound / vibrate-only / silent)
 *   passed by the alarm.
 * - Plays a custom/downloaded sound file when one is configured for the
 *   prayer, otherwise the bundled synthesised tone ([AdhanSynthesizer]) —
 *   both with a gradual fade-in and the configured master volume.
 * - Falls back to a vibration pattern so the call to prayer is never missed.
 */
@AndroidEntryPoint
class AdhanPlaybackService : Service() {

    @Inject
    lateinit var soundPlayer: AdhanSoundPlayer

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
        val soundPath = intent?.getStringExtra(EXTRA_SOUND_PATH)

        // The synthesised tone always ships with the app, so "Default" plays.
        val plan = AdhanPlaybackPlan.plan(
            option = option,
            hasBundledSound = true,
            vibrationEnabled = vibrateEnabled,
        )
        if (!plan.playSound && !plan.vibrate) {
            // Silent mode — nothing to deliver (reminders still fire).
            stopSelf()
            return START_NOT_STICKY
        }

        NotificationChannels.create(this)
        startForeground(AdhanNotifications.ADHAN_NOTIFICATION_ID, AdhanNotifications.adhanNotification(this, prayer))

        val onFinished = { stopSelf() }
        when {
            plan.playSound && soundPath != null && File(soundPath).exists() ->
                soundPlayer.playFile(File(soundPath), volumePercent, onFinished)
            plan.playSound -> soundPlayer.playSynthesized(volumePercent, onFinished)
            plan.vibrate -> vibrate()
        }

        // Safety net: never let the service run indefinitely.
        android.os.Handler(android.os.Looper.getMainLooper())
            .postDelayed({ stopSelf() }, MAX_PLAYBACK_MS)
        return START_NOT_STICKY
    }

    private fun vibrate() {
        @Suppress("DEPRECATION")
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 600, 400, 600, 400, 600), -1),
        )
    }

    override fun onDestroy() {
        soundPlayer.stop()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PRAYER = "extra_prayer"
        private const val EXTRA_SOUND_OPTION = "extra_sound_option"
        private const val EXTRA_VIBRATE = "extra_vibrate"
        private const val EXTRA_VOLUME = "extra_volume"
        private const val EXTRA_SOUND_PATH = "extra_sound_path"
        private const val MAX_PLAYBACK_MS = 5 * 60 * 1000L

        fun start(
            context: Context,
            prayer: Prayer,
            vibrate: Boolean,
            soundOption: AdhanSoundOption = AdhanSoundOption.Default,
            volumePercent: Int = 100,
            soundPath: String? = null,
        ) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_SOUND_OPTION, soundOption.name)
                .putExtra(EXTRA_VIBRATE, vibrate)
                .putExtra(EXTRA_VOLUME, volumePercent)
                .putExtra(EXTRA_SOUND_PATH, soundPath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
