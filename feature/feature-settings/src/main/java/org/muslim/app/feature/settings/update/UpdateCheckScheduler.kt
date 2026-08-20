package org.muslim.app.feature.settings.update

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.muslim.app.core.datastore.AppPreferences
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic update check with WorkManager at the user-chosen
 * cadence (daily / weekly / monthly). [ExistingPeriodicWorkPolicy.UPDATE]
 * re-anchors the next run whenever the frequency changes; disabling the check
 * cancels the job so nothing runs in the background.
 */
object UpdateCheckScheduler {

    private const val WORK_NAME = "app_update_check"

    fun schedule(context: Context, frequency: String) {
        val hours = when (frequency) {
            AppPreferences.UPDATE_CHECK_WEEKLY -> 7L * 24
            AppPreferences.UPDATE_CHECK_MONTHLY -> 30L * 24
            else -> 24L
        }
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(hours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
