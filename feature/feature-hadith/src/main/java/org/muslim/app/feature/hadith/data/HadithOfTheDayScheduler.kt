package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules the daily hadith notification (idempotent — safe to call often). */
object HadithOfTheDayScheduler {

    private const val WORK_NAME = "hadith_of_the_day"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<HadithOfTheDayWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
