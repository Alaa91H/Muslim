package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles every notification-originated termination path for a live Adhan. */
class AdhanNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP,
            ACTION_DISMISS,
            -> AdhanPlaybackService.stop(context.applicationContext)
        }
    }

    companion object {
        const val ACTION_STOP = "org.muslim.app.action.STOP_ADHAN"
        const val ACTION_DISMISS = "org.muslim.app.action.DISMISS_ADHAN"
    }
}
