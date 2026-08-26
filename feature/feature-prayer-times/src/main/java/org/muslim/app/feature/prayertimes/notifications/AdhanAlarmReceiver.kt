package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed
import org.muslim.app.feature.prayertimes.widget.PrayerTimesWidget

/**
 * Fired by the exact alarms scheduled via [AdhanScheduler]. Shows the Adhan
 * (or the pre-prayer reminder) and re-schedules the next window.
 */
class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER) ?: return
        val isReminder = intent.getBooleanExtra(EXTRA_IS_REMINDER, false)
        val isProbe = intent.getBooleanExtra(EXTRA_IS_PROBE, false)
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, AdhanEntryPoint::class.java)

        val prayer = runCatching { Prayer.valueOf(prayerName) }.getOrNull() ?: return
        // goAsync keeps the receiver alive until all work completes — the
        // process must never be killed mid-handling with the adhan half-delivered.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(appContext, entryPoint, prayer, isReminder, isProbe, intent)
            } catch (throwable: Throwable) {
                entryPoint.deliveryJournal().failed(
                    prayer = prayer,
                    isProbe = isProbe,
                    detail = "Receiver failed: ${throwable.javaClass.simpleName}",
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(
        appContext: Context,
        entryPoint: AdhanEntryPoint,
        prayer: Prayer,
        isReminder: Boolean,
        isProbe: Boolean,
        intent: Intent,
    ) {
        val settings = entryPoint.settingsRepository().settings.first()
        val deliveryJournal = entryPoint.deliveryJournal()
        deliveryJournal.receiverReached(prayer, isProbe)
        if (isReminder) {
            if (settings.reminderMinutes > 0 &&
                appContext.notificationAllowed(NotificationCategory.PrayerReminder)
            ) {
                AdhanNotifications.showReminder(appContext, prayer, settings.reminderMinutes)
            }
        } else {
            val deliveryPolicy = AdhanAlarmDeliveryPolicy.resolve(
                adhanEnabled = settings.adhanEnabled,
                presentationAllowed = appContext.notificationAllowed(NotificationCategory.Adhan),
            )
            // Post the alarm first when presentation is permitted. The service
            // replaces the same id on success; if it fails, the prayer alert
            // still remains visible instead of disappearing silently.
            if (deliveryPolicy.postVisibleNotification) {
                val notificationResult = AdhanNotifications.showAdhan(appContext, prayer)
                if (notificationResult.posted) {
                    deliveryJournal.visibleNotificationPosted(prayer, isProbe)
                } else {
                    deliveryJournal.visibleNotificationBlocked(
                        prayer = prayer,
                        isProbe = isProbe,
                        detail = notificationResult.detail ?: "Android rejected Adhan notification posting",
                    )
                }
            } else {
                deliveryJournal.visibleNotificationBlocked(
                    prayer = prayer,
                    isProbe = isProbe,
                    detail = "Adhan notification disabled by app settings or quiet hours",
                )
            }
            // Audible Adhan is a separate user choice from notification
            // presentation. A muted/disabled Android channel must never turn a
            // scheduled prayer into no delivery at all; the playback service
            // still owns the mandatory foreground notification and direct
            // fallback remains available if that service cannot start.
            if (deliveryPolicy.startAudio) {
                val soundPath = entryPoint.soundRepository().customSoundFile(prayer)?.absolutePath
                val bundledSoundId = settings.bundledAdhanSounds[prayer]
                    ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID
                val option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default
                deliverPrayerAudio(
                    appContext = appContext,
                    entryPoint = entryPoint,
                    request = AdhanDeliveryRequest(
                        prayer = prayer,
                        isProbe = isProbe,
                        option = option,
                        vibrate = settings.vibrateFor(prayer),
                        volume = settings.adhanVolumeFor(prayer),
                        soundPath = soundPath,
                        bundledSoundId = bundledSoundId,
                        notificationDismissible = settings.adhanNotificationDismissible,
                        stopOnNotificationDismiss = settings.stopAdhanOnNotificationDismiss,
                    ),
                )
                // Supplementary automation is intentionally restricted to an
                // audible adhan choice; its best-effort HTTPS request never
                // delays or changes local playback.
                if (option == AdhanSoundOption.Default) {
                    entryPoint.smartHomeBridgeDispatcher().dispatchAdhanStarted(prayer)
                }
                // Quiet notifications during the prayer (user-configurable).
                if (settings.dndEnabled) {
                    entryPoint.dndManager().enable(settings.dndDurationMinutes)
                }
            }
        }
        entryPoint.scheduler().schedule(settings)
        // Flip the countdown notification to the next prayer immediately.
        NextAdhanService.start(appContext)
        // A prayer just started: flip the widget to the next prayer.
        PrayerTimesWidget().updateAll(appContext)
    }

    private data class AdhanDeliveryRequest(
        val prayer: Prayer,
        val isProbe: Boolean,
        val option: AdhanSoundOption,
        val vibrate: Boolean,
        val volume: Int,
        val soundPath: String?,
        val bundledSoundId: String,
        val notificationDismissible: Boolean,
        val stopOnNotificationDismiss: Boolean,
    )

    /** Starts foreground audio then verifies actual playback before a local fallback. */
    private suspend fun deliverPrayerAudio(
        appContext: Context,
        entryPoint: AdhanEntryPoint,
        request: AdhanDeliveryRequest,
    ) {
        val plan = org.muslim.app.core.common.prayer.AdhanPlaybackPlan.plan(
            option = request.option,
            hasBundledSound = true,
            vibrationEnabled = request.vibrate,
        )
        val journal = entryPoint.deliveryJournal()
        journal.serviceStartRequested(request.prayer, request.isProbe)
        val serviceStart = runCatching {
            AdhanPlaybackService.start(
                context = appContext,
                prayer = request.prayer,
                vibrate = request.vibrate,
                soundOption = request.option,
                volumePercent = request.volume,
                soundPath = request.soundPath,
                bundledSoundId = request.bundledSoundId,
                isProbe = request.isProbe,
                notificationDismissible = request.notificationDismissible,
                stopOnNotificationDismiss = request.stopOnNotificationDismiss,
            )
        }
        if (serviceStart.isFailure) {
            journal.audioFallbackStarted(
                request.prayer,
                request.isProbe,
                "Foreground service start failed: ${serviceStart.exceptionOrNull()?.javaClass?.simpleName}; direct synthetic fallback started",
            )
            playDirectFallback(appContext, entryPoint, request, plan)
            return
        }
        // startForegroundService may return before Android creates the service.
        // If the service never reaches onStartCommand, no service-owned timeout
        // can recover audio. Wait only for its journal checkpoint, then claim a
        // direct AudioTrack fallback; a service that did start is left entirely
        // in charge of MediaPlayer and its own fallback.
        awaitServiceStartOrFallback(appContext, entryPoint, request, plan)
    }

    private suspend fun awaitServiceStartOrFallback(
        appContext: Context,
        entryPoint: AdhanEntryPoint,
        request: AdhanDeliveryRequest,
        plan: org.muslim.app.core.common.prayer.AdhanPlaybackPlan.Plan,
    ) {
        if (!plan.playSound) return
        delay(SERVICE_START_CONFIRMATION_TIMEOUT_MS)
        val latest = if (request.isProbe) entryPoint.deliveryJournal().lastProbe.value
        else entryPoint.deliveryJournal().lastDelivery.value
        val serviceReached = latest.prayer == request.prayer &&
            latest.isProbe == request.isProbe &&
            latest.stage in setOf(
            AdhanDeliveryStage.ServiceStarted,
            AdhanDeliveryStage.AudioFallbackStarted,
            AdhanDeliveryStage.AudioStarted,
            AdhanDeliveryStage.Failed,
        )
        if (serviceReached) return

        // Prevent a delayed service start from taking over the singleton player
        // after the receiver has claimed the direct recovery path.
        AdhanPlaybackService.stop(appContext)
        entryPoint.deliveryJournal().audioFallbackStarted(
            request.prayer,
            request.isProbe,
            "Foreground service did not enter playback; direct synthetic fallback started",
        )
        playDirectFallback(appContext, entryPoint, request, plan)
    }

    private fun playDirectFallback(
        appContext: Context,
        entryPoint: AdhanEntryPoint,
        request: AdhanDeliveryRequest,
        plan: org.muslim.app.core.common.prayer.AdhanPlaybackPlan.Plan,
    ) {
        if (!plan.playSound) return
        val player = entryPoint.soundPlayer()
        val fallbackWakeLock = appContext.getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$WAKE_LOCK_TAG:direct-fallback")
            .apply {
                setReferenceCounted(false)
                acquire(DIRECT_FALLBACK_WAKELOCK_TIMEOUT_MS)
            }
        val onStarted = { entryPoint.deliveryJournal().audioStarted(request.prayer, request.isProbe) }
        val onFinished = {
            if (fallbackWakeLock.isHeld) fallbackWakeLock.release()
        }
        // Use an offline AudioTrack here rather than retrying the same
        // MediaPlayer source whose service path never became reachable. This is
        // a one-time delivery fallback only; it never changes the stored sound
        // selection or volume.
        player.playSynthesized(
            request.volume,
            onStarted = onStarted,
            onFinished = onFinished,
        )
    }

    companion object {
        private const val DIRECT_FALLBACK_WAKELOCK_TIMEOUT_MS = 4 * 60_000L
        private const val SERVICE_START_CONFIRMATION_TIMEOUT_MS = 3_500L
        private const val WAKE_LOCK_TAG = "Muslim:Adhan"
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_IS_REMINDER = "extra_is_reminder"
        const val EXTRA_SOUND_OPTION = "extra_sound_option"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_IS_PROBE = "extra_is_probe"

        /** Builds the intent used by [AdhanScheduler] for a given prayer. */
        fun intentFor(context: Context, prayer: Prayer, isReminder: Boolean): Intent =
            Intent(context, AdhanAlarmReceiver::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_IS_REMINDER, isReminder)
    }
}
