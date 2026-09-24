package org.muslim.app.feature.settings.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.muslim.app.core.datastore.AppPreferencesRepository

/**
 * Periodic background update check. Runs only when the user enabled "check for
 * updates" in Settings (off by default); when a newer release exists it posts
 * the update-available notification, which opens the update screen on tap.
 */
open class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // The toggle is the master switch even if a stale job is still queued.
        val dependencies = dependencies()
        val prefsRepository = dependencies.prefs()
        val prefs = prefsRepository.preferences.first()
        if (!prefs.updateCheckEnabled) return Result.success()

        return when (val result = dependencies.checker().checkAndNotify()) {
            is UpdateChecker.Result.UpdateAvailable -> {
                prefsRepository.setLastUpdateCheck(System.currentTimeMillis())
                val currentPreferences = prefsRepository.preferences.first()
                if (currentPreferences.updateCheckEnabled && currentPreferences.autoUpdateEnabled) {
                    dependencies.downloads().enqueue(
                        release = result.release,
                        wifiOnly = currentPreferences.autoUpdateWifiOnly,
                    )
                }
                Result.success()
            }
            UpdateChecker.Result.UpToDate -> {
                prefsRepository.setLastUpdateCheck(System.currentTimeMillis())
                Result.success()
            }
            // Transient failure: WorkManager retries with the scheduler's
            // exponential backoff instead of recording a false successful check.
            UpdateChecker.Result.Unavailable -> Result.retry()
        }
    }

    private fun dependencies(): UpdateEntryPoint =
        EntryPointAccessors.fromApplication(applicationContext, UpdateEntryPoint::class.java)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateEntryPoint {
        fun prefs(): AppPreferencesRepository
        fun checker(): UpdateChecker
        fun downloads(): UpdateDownloadManager
    }
}
