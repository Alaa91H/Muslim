package org.example.islamicapp.feature.hadith.notifications

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.example.islamicapp.core.notifications.NotificationChannels
import org.example.islamicapp.feature.hadith.R
import org.example.islamicapp.feature.hadith.data.HadithRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional "hadith of the day" notification (PROJECT_PROMPT.md §6 Phase 3).
 * Scheduled once per day at ~09:00 local time when the user opts in.
 */
class HadithOfDayWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val repository = HadithRepository()
        val hadith = repository.hadithOfTheDay(LocalDate.now().dayOfYear)
        val context = applicationContext

        val notification = NotificationCompat.Builder(context, NotificationChannels.OCCASION)
            .setSmallIcon(android.R.drawable.ic_menu_day)
            .setContentTitle(hadith.titleAr)
            .setContentText(hadith.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(hadith.text))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    1,
                    context.packageManager.getLaunchIntentForPackage(context.packageName),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val UNIQUE_NAME = "hadith_of_day"
        private const val NOTIFICATION_ID = 4301

        /** Schedules the daily notification at ~09:00 local; cancels when disabled. */
        fun schedule(context: Context, enabled: Boolean) {
            val workManager = WorkManager.getInstance(context)
            if (!enabled) {
                workManager.cancelUniqueWork(UNIQUE_NAME)
                return
            }
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(9, 0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val initialDelay = Duration.between(now, next)
            val request = PeriodicWorkRequestBuilder<HadithOfDayWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
                .build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request,
            )
        }
    }
}

/** Scheduling helper for the settings UI. */
@Singleton
class HadithOfDayScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    fun schedule(enabled: Boolean) = HadithOfDayWorker.schedule(context, enabled)
}
