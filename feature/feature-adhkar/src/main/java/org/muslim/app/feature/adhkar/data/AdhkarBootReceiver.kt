package org.muslim.app.feature.adhkar.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Re-arms the daily and periodic adhkar reminders after a device reboot. */
class AdhkarBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, AdhkarEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = entryPoint.prefsRepository().prefs.first()
            entryPoint.reminderScheduler().schedule(prefs)
            entryPoint.periodicReminderScheduler().schedule(prefs)
        }
    }
}
