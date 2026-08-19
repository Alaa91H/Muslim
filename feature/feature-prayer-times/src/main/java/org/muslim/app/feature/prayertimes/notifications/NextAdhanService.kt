package org.muslim.app.feature.prayertimes.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.notifications.NotificationPrefsRepository
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import java.time.ZoneId
import javax.inject.Inject

/**
 * Foreground service behind the permanent "next adhan" countdown
 * notification. It shows the upcoming prayer with a live countdown and the
 * missed adhan (in red), refreshing the notification every minute.
 *
 * - Owns the [NotificationCategory.PrayerCountdown] notification and honours
 *   the unified notification manager: it stops when the category is disabled
 *   or quiet hours begin, and wakes up again exactly when quiet hours end.
 * - Started from app launch, boot, time/clock changes, every adhan alarm and
 *   whenever prayer settings or the location change, so the status line is
 *   always present while enabled.
 */
@AndroidEntryPoint
class NextAdhanService : Service() {

    @Inject lateinit var settingsRepository: PrayerSettingsRepository
    @Inject lateinit var calculator: PrayerTimesCalculator
    @Inject lateinit var notificationPrefs: NotificationPrefsRepository
    @Inject lateinit var appPreferencesRepository: AppPreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val handler = Handler(Looper.getMainLooper())
    private var lastData: PrayerCountdownData? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationChannels.create(this)
        // Must post quickly after startForegroundService; refreshed by the ticker.
        startForeground(
            NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID,
            NextAdhanNotifications.build(
                this,
                lastData ?: EMPTY,
                use24h = appPreferencesRepository.readTimeFormat24hSync(),
            ),
        )
        cancelRestartAlarm()
        tick()
        return START_STICKY
    }

    /** Recomputes and refreshes the notification, then schedules the next tick in a minute. */
    private fun tick() {
        scope.launch {
            val now = System.currentTimeMillis()
            val enabled = notificationPrefs.isEnabled(NotificationCategory.PrayerCountdown)
            val quietActive = notificationPrefs.isQuietHourActive(now)
            val showMissed = notificationPrefs.showMissedAdhan.first()
            val missedColor = notificationPrefs.missedAdhanColor.first()
            val settings = settingsRepository.settings.first()
            val data = PrayerCountdownData.compute(settings, calculator, now)

            if (!enabled || !data.hasLocation) {
                // Category off or no location: nothing to show; stop and cancel.
                stopSelfAndCancel(enabled, quietActive, settings)
                return@launch
            }
            if (quietActive) {
                // Quiet hours: hide the status line and wake up exactly when they end.
                stopSelfAndCancel(enabled, quietActive, settings)
                return@launch
            }

            lastData = data
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(
                NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID,
                NextAdhanNotifications.build(
                    this@NextAdhanService,
                    data,
                    showMissed,
                    missedColor,
                    use24h = appPreferencesRepository.readTimeFormat24hSync(),
                ),
            )
            handler.postDelayed({ tick() }, TICK_MILLIS)
        }
    }

    private suspend fun stopSelfAndCancel(enabled: Boolean, quietActive: Boolean, settings: PrayerSettings) {
        if (enabled && quietActive) {
            // Wake up again exactly when quiet hours end (only if still wanted).
            scheduleRestartAtQuietEnd(settings)
        }
        handler.removeCallbacksAndMessages(null)
        stopSelf()
    }

    /** Wakes the service up exactly when the quiet-hours window ends. */
    private suspend fun scheduleRestartAtQuietEnd(settings: PrayerSettings) {
        val hours = notificationPrefs.quietHours.first()
        if (!hours.enabled) return
        val zone = runCatching { ZoneId.of(settings.location?.timeZone ?: java.util.TimeZone.getDefault().id) }
            .getOrDefault(java.util.TimeZone.getDefault().toZoneId())
        val wakeAt = hours.nextEndMillis(System.currentTimeMillis(), zone)
        val alarmManager = getSystemService(AlarmManager::class.java)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeAt, restartPendingIntent())
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeAt, restartPendingIntent())
        }
    }

    private fun cancelRestartAlarm() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.cancel(restartPendingIntent())
    }

    private fun restartPendingIntent(): PendingIntent {
        val intent = Intent(this, NextAdhanReceiver::class.java)
        return PendingIntent.getBroadcast(
            this, RESTART_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        runCatching {
            getSystemService(NotificationManager::class.java)
                .cancel(NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID)
        }
        super.onDestroy()
    }

    companion object {
        private val EMPTY = PrayerCountdownData(
            hasLocation = false, isValid = false,
            nextPrayer = null, nextPrayerAt = null, remainingSeconds = 0,
            missedPrayer = null, missedPrayerAt = null, elapsedSeconds = 0,
        )

        /**
         * Refresh cadence. One second keeps the countdown/count-up timers
         * visibly live; the notification is only ever updated in place
         * (same id, [androidx.core.app.NotificationCompat.Builder.setOnlyAlertOnce]),
         * so the cost is a small text rebuild — far cheaper than keeping the
         * CPU awake via exact alarms. Quiet hours and the enabled toggle
         * still stop the service entirely, so no work happens when hidden.
         */
        private const val TICK_MILLIS = 1_000L
        private const val RESTART_REQUEST_CODE = 9001

        fun start(context: Context) {
            val appContext = context.applicationContext
            val intent = Intent(appContext, NextAdhanService::class.java)
            // minSdk is 26 (O), so the foreground-service API is always available.
            appContext.startForegroundService(intent)
        }
    }
}
