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
        val prefs = prefs().preferences.first()
        if (!prefs.updateCheckEnabled) return Result.success()
        val checker = UpdateChecker(applicationContext)
        if (!checker.categoryAllowed()) return Result.success()
        return when (checker.checkAndNotify()) {
            is UpdateChecker.Result.UpdateAvailable -> Result.success()
            UpdateChecker.Result.UpToDate -> Result.success()
            // Transient failure: try again on the next period.
            UpdateChecker.Result.Unavailable -> Result.retry()
        }
    }

    protected open fun prefs(): AppPreferencesRepository =
        EntryPointAccessors.fromApplication(applicationContext, UpdateEntryPoint::class.java)
            .prefs()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateEntryPoint {
        fun prefs(): AppPreferencesRepository
    }
}
