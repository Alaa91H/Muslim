package org.muslim.app.feature.adhkar.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Which daily adhkar reminder slot fired. */
enum class AdhkarReminderSlot { Morning, Evening }

/**
 * Schedules the daily morning/evening adhkar reminders as exact alarms
 * (PROJECT_PROMPT.md §6 Phase 4). Each fired reminder re-schedules its own
 * next occurrence; alarms are re-created whenever the prefs change and after
 * boot (see [AdhkarBootReceiver]).
 */
@Singleton
class AdhkarReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** (Re)creates the alarms for the currently-enabled reminder slots. */
    fun schedule(prefs: AdhkarPrefs) {
        cancelAll()
        if (prefs.morningReminderEnabled) scheduleSlot(prefs, AdhkarReminderSlot.Morning)
        if (prefs.eveningReminderEnabled) scheduleSlot(prefs, AdhkarReminderSlot.Evening)
    }

    fun cancelAll() {
        AdhkarReminderSlot.entries.forEach { slot ->
            alarmManager.cancel(slotPendingIntent(slot))
        }
    }

    private fun scheduleSlot(prefs: AdhkarPrefs, slot: AdhkarReminderSlot) {
        val zone = ZoneId.systemDefault()
        val hour = if (slot == AdhkarReminderSlot.Morning) prefs.morningHour else prefs.eveningHour
        val minute = if (slot == AdhkarReminderSlot.Morning) prefs.morningMinute else prefs.eveningMinute

        val now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val at = next.toInstant().toEpochMilli()

        val pendingIntent = slotPendingIntent(slot)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent)
        }
    }

    private fun slotPendingIntent(slot: AdhkarReminderSlot): PendingIntent {
        val intent = Intent(context, AdhkarReminderReceiver::class.java)
            .putExtra(AdhkarReminderReceiver.EXTRA_SLOT, slot.name)
        return PendingIntent.getBroadcast(
            context,
            slot.ordinal + 200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
