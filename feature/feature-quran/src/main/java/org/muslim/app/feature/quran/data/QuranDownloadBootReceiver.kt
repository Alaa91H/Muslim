package org.muslim.app.feature.quran.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-arms queued/paused/waiting recitation downloads after a device reboot so
 * in-progress transfers survive and resume automatically. Restored tasks are
 * re-delivered to [QuranDownloadService] via a short exact alarm (the same
 * background-start-safe path used for night-only downloads).
 */
class QuranDownloadBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuranDownloadEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                entryPoint.downloadManager().restore()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
