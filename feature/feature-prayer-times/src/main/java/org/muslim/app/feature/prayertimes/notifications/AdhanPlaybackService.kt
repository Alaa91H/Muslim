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

    @Inject
    lateinit var deliveryJournal: AdhanDeliveryJournal

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPlayback(playbackRequest(intent))
        return START_NOT_STICKY
    }

    private fun playbackRequest(intent: Intent?): PlaybackRequest = PlaybackRequest(
        prayer = intent?.getStringExtra(EXTRA_PRAYER)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: Prayer.Fajr,
        option = intent?.getStringExtra(EXTRA_SOUND_OPTION)
            ?.let { runCatching { AdhanSoundOption.valueOf(it) }.getOrNull() } ?: AdhanSoundOption.Default,
        vibrateEnabled = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true,
        volumePercent = intent?.getIntExtra(EXTRA_VOLUME, 100)?.coerceIn(0, 100) ?: 100,
        soundPath = intent?.getStringExtra(EXTRA_SOUND_PATH),
        bundledSound = BundledAdhanSound.fromId(intent?.getStringExtra(EXTRA_BUNDLED_SOUND)),
        isProbe = intent?.getBooleanExtra(EXTRA_IS_PROBE, false) ?: false,
        notificationDismissible = intent?.getBooleanExtra(EXTRA_NOTIFICATION_DISMISSIBLE, false) ?: false,
        stopOnNotificationDismiss = intent?.getBooleanExtra(EXTRA_STOP_ON_NOTIFICATION_DISMISS, false) ?: false,
    )

    private fun startPlayback(request: PlaybackRequest) {
        val plan = AdhanPlaybackPlan.plan(request.option, hasBundledSound = true, request.vibrateEnabled)
        if (!plan.playSound && !plan.vibrate) {
            deliveryJournal.failed(request.prayer, request.isProbe, "Adhan is configured as silent")
            stopSelf()
            return
        }
        val foregroundStarted = startForegroundNotification(
            prayer = request.prayer,
            dismissible = request.notificationDismissible,
            stopOnDismiss = request.stopOnNotificationDismiss,
        )
        if (foregroundStarted.isFailure) {
            startForegroundFailureFallback(request, plan, foregroundStarted.exceptionOrNull())
            return
        }
        startManagedPlayback(request, plan)
    }

    private fun startForegroundFailureFallback(
        request: PlaybackRequest,
        plan: AdhanPlaybackPlan.Plan,
        error: Throwable?,
    ) {
        deliveryJournal.audioFallbackStarted(
            request.prayer,
            request.isProbe,
            "Foreground notification failed: ${error?.javaClass?.simpleName}; synthetic fallback started",
        )
        AdhanPlaybackStatus.isPlaying.value = true
        acquireWakeLock()
        val onFinished = { stopSelf() }
        if (plan.playSound) {
            soundPlayer.playSynthesized(
                request.volumePercent,
                onStarted = { deliveryJournal.audioStarted(request.prayer, request.isProbe) },
                onFinished = onFinished,
            )
        } else if (plan.vibrate) {
            vibrate()
            mainHandler().postDelayed(onFinished, VIBRATION_DURATION_MS)
        }
        scheduleServiceStop()
    }

    private fun startManagedPlayback(request: PlaybackRequest, plan: AdhanPlaybackPlan.Plan) {
        deliveryJournal.serviceStarted(request.prayer, request.isProbe)
        val deliveryStartedAt = System.currentTimeMillis()
        AdhanPlaybackStatus.isPlaying.value = true
        acquireWakeLock()
        val onFinished = { stopSelf() }
        val onAudioStarted = { deliveryJournal.audioStarted(request.prayer, request.isProbe) }
        startRequestedAudio(request, plan, onAudioStarted, onFinished)
        scheduleAudioFallback(request, plan, deliveryStartedAt, onAudioStarted, onFinished)
        scheduleServiceStop()
    }

    private fun startRequestedAudio(
        request: PlaybackRequest,
        plan: AdhanPlaybackPlan.Plan,
        onAudioStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        when {
            plan.playSound && request.soundPath != null && File(request.soundPath).exists() ->
                soundPlayer.playFile(File(request.soundPath), request.volumePercent, onAudioStarted, onFinished)
            plan.playSound -> soundPlayer.playBundled(request.bundledSound, request.volumePercent, onAudioStarted, onFinished)
            plan.vibrate -> {
                if (request.isProbe) deliveryJournal.failed(request.prayer, true, "Adhan is configured for vibration only")
                vibrate()
                mainHandler().postDelayed(onFinished, VIBRATION_DURATION_MS)
            }
        }
    }

    private fun scheduleAudioFallback(
        request: PlaybackRequest,
        plan: AdhanPlaybackPlan.Plan,
        deliveryStartedAt: Long,
        onAudioStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        if (!plan.playSound) return
        mainHandler().postDelayed({
            if (!audioStartedFor(request, deliveryStartedAt)) {
                deliveryJournal.audioFallbackStarted(
                    request.prayer,
                    request.isProbe,
                    "Bundled audio start timed out; synthetic fallback started",
                )
                soundPlayer.playSynthesized(request.volumePercent, onAudioStarted, onFinished)
                mainHandler().postDelayed({
                    if (!audioStartedFor(request, deliveryStartedAt)) {
                        deliveryJournal.failed(request.prayer, request.isProbe, "AudioTrack fallback did not start")
                    }
                }, FALLBACK_AUDIO_START_TIMEOUT_MS)
            }
        }, AUDIO_START_TIMEOUT_MS)
    }

    private fun audioStartedFor(request: PlaybackRequest, deliveryStartedAt: Long): Boolean {
        val latest = if (request.isProbe) deliveryJournal.lastProbe.value else deliveryJournal.lastDelivery.value
        return latest.audioStarted &&
            latest.prayer == request.prayer &&
            latest.isProbe == request.isProbe &&
            latest.atMillis >= deliveryStartedAt
    }

    private fun mainHandler() = android.os.Handler(android.os.Looper.getMainLooper())

    private fun scheduleServiceStop() {
        mainHandler().postDelayed({ stopSelf() }, MAX_PLAYBACK_MS)
    }

    private data class PlaybackRequest(
        val prayer: Prayer,
        val option: AdhanSoundOption,
        val vibrateEnabled: Boolean,
        val volumePercent: Int,
        val soundPath: String?,
        val bundledSound: BundledAdhanSound,
        val isProbe: Boolean,
        val notificationDismissible: Boolean,
        val stopOnNotificationDismiss: Boolean,
    )

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

    private fun startForegroundNotification(
        prayer: Prayer,
        dismissible: Boolean,
        stopOnDismiss: Boolean,
    ) = runCatching {
        NotificationChannels.create(this)
        AdhanNotifications.cancelRetiredAdhan(this)
        startForeground(
            AdhanNotifications.ADHAN_NOTIFICATION_ID,
            AdhanNotifications.adhanNotification(
                context = this,
                prayer = prayer,
                dismissible = dismissible,
                stopOnDismiss = stopOnDismiss,
            ),
        )
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
        private const val EXTRA_IS_PROBE = "extra_is_probe"
        private const val EXTRA_NOTIFICATION_DISMISSIBLE = "extra_notification_dismissible"
        private const val EXTRA_STOP_ON_NOTIFICATION_DISMISS = "extra_stop_on_notification_dismiss"
        private const val AUDIO_START_TIMEOUT_MS = 12_000L
        private const val FALLBACK_AUDIO_START_TIMEOUT_MS = 5_000L
        private const val MAX_PLAYBACK_MS = 5 * 60 * 1000L
        private const val VIBRATION_DURATION_MS = 2_800L

        fun start(
            context: Context,
            prayer: Prayer,
            vibrate: Boolean,
            soundOption: AdhanSoundOption = AdhanSoundOption.Default,
            volumePercent: Int = 100,
            soundPath: String? = null,
            bundledSoundId: String = BundledAdhanSound.DEFAULT_ID,
            isProbe: Boolean = false,
            notificationDismissible: Boolean = false,
            stopOnNotificationDismiss: Boolean = false,
        ) {
            val intent = Intent(context, AdhanPlaybackService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_SOUND_OPTION, soundOption.name)
                .putExtra(EXTRA_VIBRATE, vibrate)
                .putExtra(EXTRA_VOLUME, volumePercent)
                .putExtra(EXTRA_SOUND_PATH, soundPath)
                .putExtra(EXTRA_BUNDLED_SOUND, bundledSoundId)
                .putExtra(EXTRA_IS_PROBE, isProbe)
                .putExtra(EXTRA_NOTIFICATION_DISMISSIBLE, notificationDismissible)
                .putExtra(EXTRA_STOP_ON_NOTIFICATION_DISMISS, stopOnNotificationDismiss)
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
