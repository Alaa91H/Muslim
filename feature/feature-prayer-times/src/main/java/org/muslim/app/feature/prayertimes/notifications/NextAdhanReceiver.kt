package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restarts the permanent next-adhan countdown. Fired by the exact alarm set
 * when quiet hours begin (so the status line returns exactly when they end);
 * the boot and time-change receivers start it through the same helper.
 */
class NextAdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NextAdhanService.start(context.applicationContext)
    }
}
