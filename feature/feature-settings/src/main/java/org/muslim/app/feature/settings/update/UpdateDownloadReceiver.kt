package org.muslim.app.feature.settings.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed

/**
 * Manifest receiver for DownloadManager completion.
 *
 * Unlike the old ViewModel-local receiver, this continues to work when the
 * update screen is closed or the app process is recreated while downloading.
 */
class UpdateDownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (completedId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val repository = EntryPointAccessors.fromApplication(
                    appContext,
                    UpdateDownloadEntryPoint::class.java,
                ).prefs()
                val prefs = repository.preferences.first()
                if (prefs.updateDownloadId != completedId) return@launch

                val state = UpdateDownloadManager(appContext, repository).currentState()
                if (!appContext.notificationAllowed(NotificationCategory.AppUpdate)) return@launch

                val notifier = UpdateDownloadNotifier(appContext)
                when (state) {
                    is UpdateDownloadState.ReadyToInstall -> notifier.showReady(state.version)
                    is UpdateDownloadState.Failed -> notifier.showFailed()
                    else -> Unit
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateDownloadEntryPoint {
        fun prefs(): AppPreferencesRepository
    }
}
