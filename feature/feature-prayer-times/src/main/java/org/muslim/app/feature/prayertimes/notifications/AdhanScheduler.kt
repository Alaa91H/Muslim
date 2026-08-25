package org.muslim.app.feature.prayertimes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerParameters
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules exact alarms for the upcoming prayer times (Adhan) and the
 * pre-prayer reminders, re-scheduling whenever settings/location change or on
 * boot / timezone change (PROJECT_PROMPT.md §3.5).
 *
 * One alarm per prayer (fixed request code per prayer) for the *next*
 * occurrence found in today or tomorrow; each fired alarm re-schedules the
 * prayer for the following day, so there are at most 5 + 5 pending alarms.
 *
 * Uses [AlarmManager.setExactAndAllowWhileIdle] when permitted (checked via
 * [AlarmManager.canScheduleExactAlarms] on API 31+), degrading to
 * `setAndAllowWhileIdle` otherwise.
 */
@Singleton
class AdhanScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculator: PrayerTimesCalculator,
    private val deliveryJournal: AdhanDeliveryJournal,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Computes and schedules the next occurrence of every prayer; cancels stale alarms. */
    fun schedule(settings: PrayerSettings) {
        val location = settings.location
        if (location == null || !settings.adhanEnabled) {
            cancelAll()
            return
        }

        val zone = ZoneId.of(location.timeZone)
        val params = parametersFor(settings, location)
        val now = System.currentTimeMillis()

        // For each prayer find the next occurrence within today..tomorrow.
        val upcoming = LinkedHashMap<Prayer, Long>()
        for (date in listOf(LocalDate.now(zone), LocalDate.now(zone).plusDays(1))) {
            val result = calculator.compute(
                date = date,
                coordinates = Coordinates(location.latitude, location.longitude, location.elevation),
                parameters = params,
                timeZone = zone,
                asrMethod = settings.asrMethod,
                userAdjustments = settings.adjustments,
            )
            if (!result.isValid) continue
            for ((prayer, at) in result.epochMillis) {
                if (prayer == Prayer.Sunrise) continue
                if (at > now && at !in upcoming.values) upcoming[prayer] = at
            }
        }

        // Cancel any previous alarms, then schedule fresh ones.
        cancelAll()
        for ((prayer, at) in upcoming) {
            scheduleExact(at, prayer, isReminder = false, settings = settings)
            if (settings.reminderMinutes > 0) {
                val reminderAt = at - settings.reminderMinutes * 60_000L
                if (reminderAt > now) scheduleExact(reminderAt, prayer, isReminder = true, settings = settings)
            }
        }
    }

    /**
     * Schedules a short, exact delivery probe through the same receiver and
     * foreground-service path used at prayer time. A direct preview cannot
     * prove that Android will deliver a background alarm, so this method is the
     * authoritative user-triggered verification path.
     */
    fun scheduleDeliveryProbe(settings: PrayerSettings, prayer: Prayer): Boolean {
        if (settings.location == null || !settings.adhanEnabled) return false
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (!canExact) {
            deliveryJournal.failed(prayer, isProbe = true, detail = "Exact alarms unavailable")
            return false
        }
        val pendingIntent = prayerPendingIntent(
            prayer = prayer,
            isReminder = false,
            settings = settings,
            isProbe = true,
        )
        alarmManager.cancel(pendingIntent)
        deliveryJournal.probeScheduled(prayer)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + DELIVERY_PROBE_DELAY_MS,
            pendingIntent,
        )
        return true
    }

    fun cancelAll() {
        // Extras don't affect PendingIntent identity, so defaults are fine here.
        val defaults = PrayerSettings()
        for (prayer in Prayer.entries) {
            alarmManager.cancel(prayerPendingIntent(prayer, isReminder = false, settings = defaults))
            alarmManager.cancel(prayerPendingIntent(prayer, isReminder = true, settings = defaults))
            alarmManager.cancel(
                prayerPendingIntent(
                    prayer = prayer,
                    isReminder = false,
                    settings = defaults,
                    isProbe = true,
                ),
            )
        }
    }

    private fun scheduleExact(at: Long, prayer: Prayer, isReminder: Boolean, settings: PrayerSettings) {
        val pendingIntent = prayerPendingIntent(prayer, isReminder, settings)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
        }
    }

    private fun prayerPendingIntent(
        prayer: Prayer,
        isReminder: Boolean,
        settings: PrayerSettings,
        isProbe: Boolean = false,
    ): PendingIntent {
        val intent = Intent(context, AdhanAlarmReceiver::class.java)
            .putExtra(AdhanAlarmReceiver.EXTRA_PRAYER, prayer.name)
            .putExtra(AdhanAlarmReceiver.EXTRA_IS_REMINDER, isReminder)
            .putExtra(AdhanAlarmReceiver.EXTRA_SOUND_OPTION, (settings.adhanSounds[prayer] ?: AdhanSoundOption.Default).name)
            .putExtra(AdhanAlarmReceiver.EXTRA_VOLUME, settings.adhanVolumeFor(prayer))
            .putExtra(AdhanAlarmReceiver.EXTRA_IS_PROBE, isProbe)
        val requestCode = if (isProbe) PROBE_REQUEST_CODE + prayer.ordinal
        else prayer.ordinal + (if (isReminder) 100 else 0)
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun parametersFor(settings: PrayerSettings, location: SelectedLocation): PrayerParameters {
        return if (settings.method == CalculationMethod.Custom) {
            PrayerParameters(
                method = CalculationMethod.Custom,
                fajrAngle = settings.customFajrAngle,
                ishaAngle = settings.customIshaAngle,
                highLatitudeRule = settings.highLatitudeRule,
            )
        } else {
            PrayerParameters.of(settings.method).copy(highLatitudeRule = settings.highLatitudeRule)
        }
    }

    private companion object {
        const val DELIVERY_PROBE_DELAY_MS = 10_000L
        const val PROBE_REQUEST_CODE = 10_000
    }
}
