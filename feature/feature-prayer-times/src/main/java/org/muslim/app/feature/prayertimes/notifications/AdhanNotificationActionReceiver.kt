package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles the local stop and optional dismiss actions from the active Adhan notification. */
class AdhanNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP -> AdhanPlaybackService.stop(context)
            ACTION_DISMISSED -> {
                if (intent.getBooleanExtra(EXTRA_STOP_ON_DISMISS, false)) {
                    AdhanPlaybackService.stop(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_STOP = "org.muslim.app.action.STOP_ADHAN"
        const val ACTION_DISMISSED = "org.muslim.app.action.DISMISS_ADHAN"
        const val EXTRA_STOP_ON_DISMISS = "extra_stop_on_dismiss"
    }
}
