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
        CoroutineScope(Dispatchers.IO).launch {
            val settings = entryPoint.settingsRepository().settings.first()
            if (isReminder) {
                if (settings.reminderMinutes > 0) {
                    AdhanNotifications.showReminder(appContext, prayer, settings.reminderMinutes)
                }
            } else if (settings.adhanEnabled) {
                val soundPath = entryPoint.soundRepository().customSoundFile(prayer)?.absolutePath
                AdhanPlaybackService.start(
                    context = appContext,
                    prayer = prayer,
                    vibrate = settings.vibrateEnabled,
                    soundOption = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                    volumePercent = settings.adhanVolume,
                    soundPath = soundPath,
                )
                // Quiet notifications during the prayer (user-configurable).
                if (settings.dndEnabled) {
                    entryPoint.dndManager().enable(settings.dndDurationMinutes)
                }
            }
            entryPoint.scheduler().schedule(settings)
            // A prayer just started: flip the widget to the next prayer.
            PrayerTimesWidget().updateAll(appContext)
        }
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
