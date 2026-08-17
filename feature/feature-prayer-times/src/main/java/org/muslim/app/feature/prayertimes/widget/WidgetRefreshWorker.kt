package org.muslim.app.feature.prayertimes.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Re-renders every widget instance every 15 minutes (the WorkManager minimum)
 * so the countdown stays reasonably fresh between the event-driven refreshes.
 *
 * Android does not deliver `ACTION_TIME_TICK` to manifest receivers, and
 * per-minute exact alarms would be battery-hostile, so 15 minutes plus the
 * prayer-boundary / boot / settings / app-open refreshes is the right trade.
 */
class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        PrayerTimesWidget().updateAll(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val UNIQUE_NAME = "prayer_widget_periodic_refresh"
        private const val PERIOD_MINUTES = 15L

        /** Enqueues the periodic refresh; keeps the existing schedule if present. */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(PERIOD_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request,
            )
        }
    }
}
