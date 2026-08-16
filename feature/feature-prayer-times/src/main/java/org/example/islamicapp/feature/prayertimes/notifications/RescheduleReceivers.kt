package org.example.islamicapp.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Base receiver that re-computes the alarm schedule. */
abstract class RescheduleReceiver : BroadcastReceiver() {
    private companion object {
        const val TAG = "RescheduleReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(appContext, AdhanEntryPoint::class.java)
                val settings = entryPoint.settingsRepository().settings.first()
                entryPoint.scheduler().schedule(settings)
            } catch (error: Exception) {
                Log.e(TAG, "Unable to reschedule prayer alarms", error)
            } finally {
                pendingResult.finish()
            }
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
