package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Base receiver that re-computes the alarm schedule.
 *
 * Manifest receivers become inactive after [BroadcastReceiver.onReceive]
 * returns. Keeping the pending broadcast result open until DataStore has been
 * read and all alarms have been submitted prevents Android from reclaiming the
 * process halfway through a critical reschedule.
 */
abstract class RescheduleReceiver(private val validActions: Set<String>) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in validActions) return
        rescheduleAsync(context)
    }

    protected fun rescheduleAsync(context: Context) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    AdhanEntryPoint::class.java,
                )
                val settings = entryPoint.settingsRepository().settings.first()
                entryPoint.scheduler().schedule(settings)
                // This optional countdown must never make the critical alarm
                // schedule fail after boot, an app update, or a permission grant.
                runCatching { NextAdhanService.start(appContext) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Re-schedules alarms after a device reboot or in-place package update and
 * restores the prayer DND filter when the device itself was restarted.
 */
class BootReceiver : RescheduleReceiver(RescheduleActions.boot) {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RescheduleActions.boot) return
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                AdhanEntryPoint::class.java,
            )
            entryPoint.dndManager().restoreAfterReboot()
        }
        rescheduleAsync(context)
    }
}

/** Re-schedules alarms when the timezone or wall clock changes. */
class TimeChangeReceiver : RescheduleReceiver(RescheduleActions.timeChange)

/**
 * Rebuilds exact alarms as soon as Android reports that the user granted
 * Alarms & reminders access. Android cancels future exact alarms when this
 * access is revoked, so waiting for a manual app open is not sufficient.
 */
class ExactAlarmPermissionReceiver : RescheduleReceiver(RescheduleActions.exactAlarmAccess)

/**
 * The platform broadcast names that require recalculation. Kept as pure sets so
 * their coverage can be checked in JVM tests without instantiating Android
 * framework classes.
 */
object RescheduleActions {
    const val EXACT_ALARM_PERMISSION_CHANGED =
        "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"

    val boot: Set<String> = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
    )
    val timeChange: Set<String> = setOf(
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
    )
    val exactAlarmAccess: Set<String> = setOf(EXACT_ALARM_PERMISSION_CHANGED)
}
