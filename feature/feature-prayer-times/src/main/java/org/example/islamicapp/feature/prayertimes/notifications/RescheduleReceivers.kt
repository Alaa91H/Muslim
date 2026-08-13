package org.example.islamicapp.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Base receiver that re-computes the alarm schedule. */
abstract class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, AdhanEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            val settings = entryPoint.settingsRepository().settings.first()
            entryPoint.scheduler().schedule(settings)
        }
    }
}

/** Re-schedules alarms after a device reboot. */
class BootReceiver : RescheduleReceiver()

/** Re-schedules alarms when the timezone or clock changes. */
class TimeChangeReceiver : RescheduleReceiver()

/** Intent actions this module's receivers listen to. */
object RescheduleActions {
    val BOOT = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
    val TIME_CHANGE = IntentFilter().apply {
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
    }
}
