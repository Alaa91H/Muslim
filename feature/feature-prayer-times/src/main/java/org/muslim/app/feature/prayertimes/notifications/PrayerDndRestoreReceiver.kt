package org.muslim.app.feature.prayertimes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors

/** Fired by [PrayerDndManager] when the prayer DND window ends. */
class PrayerDndRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, AdhanEntryPoint::class.java,
        )
        entryPoint.dndManager().restore()
    }
}
