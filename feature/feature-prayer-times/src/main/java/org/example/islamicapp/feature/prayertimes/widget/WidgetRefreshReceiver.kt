package org.example.islamicapp.feature.prayertimes.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-renders the widget after device reboot and when the clock, timezone or
 * date changes — all exempt implicit broadcasts (see Android docs on
 * "Implicit broadcast exceptions"). `ACTION_TIME_TICK` is *not* exempt, so
 * per-minute updates come from [WidgetRefreshWorker] + the other triggers.
 */
class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PrayerTimesWidget().updateAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
