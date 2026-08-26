package org.muslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.muslim.app.feature.prayertimes.notifications.AdhanNotifications
import org.muslim.app.feature.prayertimes.notifications.NextAdhanNotifications
import org.muslim.app.feature.quran.data.RecitationPlaybackService

/**
 * Clears retained system cards as soon as Android finishes an in-place app
 * update. This intentionally runs before the user opens the app or restarts a
 * foreground service, so retired notification artwork cannot remain visible
 * simply because its normal publisher has not started yet.
 */
class IconIdentityMigrationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        AdhanNotifications.cancelRetiredAdhan(context)
        NextAdhanNotifications.cancelRetiredCountdown(context)
        RecitationPlaybackService.cancelRetiredNotification(context)
    }
}
