package org.example.islamicapp.feature.ramadan.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.example.islamicapp.core.common.prayer.CalculationMethod
import org.example.islamicapp.core.common.prayer.Coordinates
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.core.common.prayer.PrayerParameters
import org.example.islamicapp.core.common.prayer.PrayerTimesCalculator
import org.example.islamicapp.core.datastore.prayer.PrayerSettings
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily iftar (Maghrib) and suhoor-reminder (Fajr minus the
 * user-configured lead time) notifications for the current Ramadan
 * (PROJECT_PROMPT.md §6 Phase 6: "عدّاد تنازلي للسحور والإفطار، مع تنبيه
 * سحور قابل للتخصيص"). Exact alarms when permitted, same reliability model
 * as the Adhan scheduler.
 */
@Singleton
class RamadanScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculator: PrayerTimesCalculator,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * (Re)schedules the next iftar and suhoor alarms. Safe to call whenever
     * settings, location or Ramadan preferences change.
     */
    fun schedule(prayerSettings: PrayerSettings, suhoorMinutesBefore: Int) {
        val location = prayerSettings.location ?: run {
            cancelAll()
            return
        }
        val zone = ZoneId.of(location.timeZone)
        val params = parametersFor(prayerSettings)
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zone)
        val tomorrow = today.plusDays(1)

        val todayResult = calculator.compute(
            date = today,
            coordinates = Coordinates(location.latitude, location.longitude),
            parameters = params,
            timeZone = zone,
            asrMethod = prayerSettings.asrMethod,
            userAdjustments = prayerSettings.adjustments,
        )
        val tomorrowResult = calculator.compute(
            date = tomorrow,
            coordinates = Coordinates(location.latitude, location.longitude),
            parameters = params,
            timeZone = zone,
            asrMethod = prayerSettings.asrMethod,
            userAdjustments = prayerSettings.adjustments,
        )

        cancelAll()

        // Iftar = today's Maghrib (or tomorrow's if already passed).
        val iftarToday = todayResult.epochMillis[Prayer.Maghrib]
        if (iftarToday != null && iftarToday > now) {
            scheduleAlarm(iftarToday, RamadanAlarmReceiver.TYPE_IFTAR)
        } else {
            val iftarTomorrow = tomorrowResult.epochMillis[Prayer.Maghrib]
            if (iftarTomorrow != null) scheduleAlarm(iftarTomorrow, RamadanAlarmReceiver.TYPE_IFTAR)
        }

        // Suhoor reminder = tomorrow's Fajr minus the lead time.
        val fajrTomorrow = tomorrowResult.epochMillis[Prayer.Fajr]
        if (fajrTomorrow != null) {
            val reminderAt = fajrTomorrow - suhoorMinutesBefore.coerceAtLeast(0) * 60_000L
            if (reminderAt > now) scheduleAlarm(reminderAt, RamadanAlarmReceiver.TYPE_SUHOOR)
        }
    }

    fun cancelAll() {
        alarmManager.cancel(pendingIntent(RamadanAlarmReceiver.TYPE_IFTAR))
        alarmManager.cancel(pendingIntent(RamadanAlarmReceiver.TYPE_SUHOOR))
    }

    private fun scheduleAlarm(at: Long, type: String) {
        val intent = pendingIntent(type)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    private fun pendingIntent(type: String): PendingIntent {
        val intent = Intent(context, RamadanAlarmReceiver::class.java)
            .putExtra(RamadanAlarmReceiver.EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(
            context,
            if (type == RamadanAlarmReceiver.TYPE_IFTAR) REQUEST_IFTAR else REQUEST_SUHOOR,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun parametersFor(settings: PrayerSettings): PrayerParameters =
        if (settings.method == CalculationMethod.Custom) {
            PrayerParameters(
                method = CalculationMethod.Custom,
                fajrAngle = settings.customFajrAngle,
                ishaAngle = settings.customIshaAngle,
                highLatitudeRule = settings.highLatitudeRule,
            )
        } else {
            PrayerParameters.of(settings.method).copy(highLatitudeRule = settings.highLatitudeRule)
        }

    private companion object {
        const val REQUEST_IFTAR = 2001
        const val REQUEST_SUHOOR = 2002
    }
}
