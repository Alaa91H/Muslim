package org.muslim.app.feature.prayertimes.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.common.prayer.AdhanPlaybackPlan
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.BundledAdhanSound
import org.muslim.app.core.common.prayer.Prayer
import java.io.File
import javax.inject.Inject

/**
 * Process-wide adhan playback status. The settings screen mirrors it so its
 * "معاينة / إيقاف" button knows whether a preview is currently ringing (the
 * service cannot be observed directly, but both live in the same process).
 */
object AdhanPlaybackStatus {
    val isPlaying = MutableStateFlow(false)
}

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

    private var wakeLock: PowerManager.WakeLock? = null

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
        val bundled = BundledAdhanSound.fromId(intent?.getStringExtra(EXTRA_BUNDLED_SOUND))

        // A bundled real recording always ships with the app, so "Default"
        // plays offline with no download.
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
        AdhanPlaybackStatus.isPlaying.value = true
        acquireWakeLock()

        val onFinished = { stopSelf() }
        when {
            plan.playSound && soundPath != null && File(soundPath).exists() ->
                soundPlayer.playFile(File(soundPath), volumePercent, onFinished)
            plan.playSound -> soundPlayer.playBundled(bundled, volumePercent, onFinished)
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

    private fun acquireWakeLock() {
        // Keep the CPU awake while the adhan rings so a sleeping device (the
        // typical dawn-prayer case) cannot cut the audio off right after the
        // notification appears. Released in onDestroy.
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Muslim:AdhanPlayback",
        ).apply {
            setReferenceCounted(false)
            acquire(MAX_PLAYBACK_MS)
        }
    }

    override fun onDestroy() {
        wakeLock?.let { runCatching { if (it.isHeld) it.release() } }
        wakeLock = null
        AdhanPlaybackStatus.isPlaying.value = false
        soundPlayer.stop()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PRAYER = "extra_prayer"
        private const val EXTRA_SOUND_OPTION = "extra_sound_option"
        private const val EXTRA_VIBRATE = "extra_vibrate"
        private const val EXTRA_VOLUME = "extra_volume"
        private const val EXTRA_SOUND_PATH = "extra_sound_path"
        private const val EXTRA_BUNDLED_SOUND = "extra_bundled_sound"
        private const val MAX_PLAYBACK_MS = 5 * 60 * 1000L

        fun start(
            context: Context,
            prayer: Prayer,
            vibrate: Boolean,
            soundOption: AdhanSoundOption = AdhanSoundOption.Default,
            volumePercent: Int = 100,
            soundPath: String? = null,
            bundledSoundId: String = BundledAdhanSound.DEFAULT_ID,
        ) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_SOUND_OPTION, soundOption.name)
                .putExtra(EXTRA_VIBRATE, vibrate)
                .putExtra(EXTRA_VOLUME, volumePercent)
                .putExtra(EXTRA_SOUND_PATH, soundPath)
                .putExtra(EXTRA_BUNDLED_SOUND, bundledSoundId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Stops any in-progress adhan playback (e.g. a preview). */
        fun stop(context: Context) {
            context.stopService(Intent(context, AdhanPlaybackService::class.java))
        }
    }
}
