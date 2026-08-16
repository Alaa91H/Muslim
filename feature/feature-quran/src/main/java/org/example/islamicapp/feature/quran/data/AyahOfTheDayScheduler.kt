package org.example.islamicapp.feature.quran.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules the daily ayah notification (idempotent — safe to call often). */
object AyahOfTheDayScheduler {

    private const val WORK_NAME = "ayah_of_the_day"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<AyahOfTheDayWorker>(1, TimeUnit.DAYS)
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
