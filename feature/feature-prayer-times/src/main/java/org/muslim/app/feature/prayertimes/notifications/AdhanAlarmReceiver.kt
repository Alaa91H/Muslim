package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, AdhanEntryPoint::class.java)

        val prayer = runCatching { Prayer.valueOf(prayerName) }.getOrNull() ?: return
        // goAsync keeps the receiver alive until all work completes — the
        // process must never be killed mid-handling with the adhan half-delivered.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(appContext, entryPoint, prayer, isReminder, intent)
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
        intent: Intent,
    ) {
        val settings = entryPoint.settingsRepository().settings.first()
        if (isReminder) {
            if (settings.reminderMinutes > 0 &&
                appContext.notificationAllowed(NotificationCategory.PrayerReminder)
            ) {
                AdhanNotifications.showReminder(appContext, prayer, settings.reminderMinutes)
            }
        } else if (settings.adhanEnabled &&
            appContext.notificationAllowed(NotificationCategory.Adhan)
        ) {
                val soundPath = entryPoint.soundRepository().customSoundFile(prayer)?.absolutePath
                val bundledSoundId = settings.bundledAdhanSounds[prayer]
                    ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID
                val option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default
                val vibrate = settings.vibrateFor(prayer)
                val volume = settings.adhanVolumeFor(prayer)
                // Prefer the foreground service (keeps ringing in the
                // background); Android 12+ may block starting it from an
                // alarm, so fall back to in-process playback — the adhan
                // must never be missed.
                val started = runCatching {
                    AdhanPlaybackService.start(
                        context = appContext,
                        prayer = prayer,
                        vibrate = vibrate,
                        soundOption = option,
                        volumePercent = volume,
                        soundPath = soundPath,
                        bundledSoundId = bundledSoundId,
                    )
                }.isSuccess
                if (!started) {
                    val plan = org.muslim.app.core.common.prayer.AdhanPlaybackPlan.plan(
                        option = option,
                        hasBundledSound = true,
                        vibrationEnabled = vibrate,
                    )
                    val player = entryPoint.soundPlayer()
                    when {
                        plan.playSound && soundPath != null &&
                            java.io.File(soundPath).exists() ->
                            player.playFile(java.io.File(soundPath), volume) {}
                        plan.playSound -> player.playBundled(
                            org.muslim.app.core.common.prayer.BundledAdhanSound.fromId(bundledSoundId),
                            volume,
                        ) {}
                    }
                }
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
            entryPoint.scheduler().schedule(settings)
            // Flip the countdown notification to the next prayer immediately.
            NextAdhanService.start(appContext)
            // A prayer just started: flip the widget to the next prayer.
            PrayerTimesWidget().updateAll(appContext)
    }

    companion object {
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_IS_REMINDER = "extra_is_reminder"
        const val EXTRA_SOUND_OPTION = "extra_sound_option"
        const val EXTRA_VOLUME = "extra_volume"

        /** Builds the intent used by [AdhanScheduler] for a given prayer. */
        fun intentFor(context: Context, prayer: Prayer, isReminder: Boolean): Intent =
            Intent(context, AdhanAlarmReceiver::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_IS_REMINDER, isReminder)
    }
}
