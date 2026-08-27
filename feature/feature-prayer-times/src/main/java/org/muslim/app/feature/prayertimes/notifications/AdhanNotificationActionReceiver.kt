package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles the only local termination path exposed by a live Adhan notification. */
class AdhanNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP) {
            AdhanPlaybackService.stop(context)
        }
    }

    companion object {
        const val ACTION_STOP = "org.muslim.app.action.STOP_ADHAN"
    }
}
