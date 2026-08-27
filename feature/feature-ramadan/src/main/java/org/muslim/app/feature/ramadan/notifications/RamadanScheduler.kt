package org.muslim.app.feature.ramadan.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.PrayerSettings
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
     *
     * By default the alarms only fire inside Ramadan (see
     * [org.muslim.app.feature.ramadan.data.RamadanSettings.notifyOutsideRamadan]);
     * enabling that flag keeps them running all year.
     */
    fun schedule(prayerSettings: PrayerSettings, ramadanSettings: org.muslim.app.feature.ramadan.data.RamadanSettings) {
        val location = prayerSettings.location ?: run {
            cancelAll()
            return
        }
        val zone = ZoneId.of(location.timeZone)
        val profile = prayerSettings.toPrayerCalculationProfile()
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zone)
        val tomorrow = today.plusDays(1)
        val info = org.muslim.app.feature.ramadan.domain.RamadanDates.upcoming(today, prayerSettings.hijriAdjustment)
        val todayInRamadan = info.isRamadanDay(today)
        val tomorrowInRamadan = info.isRamadanDay(tomorrow)
        val outsideEnabled = ramadanSettings.notifyOutsideRamadan
        val suhoorMinutesBefore = ramadanSettings.suhoorMinutesBefore

        val todayResult = calculator.compute(
            date = today,
            coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
            profile = profile,
            timeZone = zone,
        )
        val tomorrowResult = calculator.compute(
            date = tomorrow,
            coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
            profile = profile,
            timeZone = zone,
        )

        cancelAll()

        val plan = RamadanSchedulePlanner.plan(
            now = now,
            todayInRamadan = todayInRamadan,
            tomorrowInRamadan = tomorrowInRamadan,
            outsideEnabled = outsideEnabled,
            suhoorMinutesBefore = suhoorMinutesBefore,
            todayMaghrib = todayResult.epochMillis[Prayer.Maghrib],
            tomorrowMaghrib = tomorrowResult.epochMillis[Prayer.Maghrib],
            tomorrowFajr = tomorrowResult.epochMillis[Prayer.Fajr],
        )
        plan.iftarAtMillis?.let { scheduleAlarm(it, RamadanAlarmReceiver.TYPE_IFTAR) }
        plan.suhoorAtMillis?.let { scheduleAlarm(it, RamadanAlarmReceiver.TYPE_SUHOOR) }
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


    private companion object {
        const val REQUEST_IFTAR = 2001
        const val REQUEST_SUHOOR = 2002
    }
}
