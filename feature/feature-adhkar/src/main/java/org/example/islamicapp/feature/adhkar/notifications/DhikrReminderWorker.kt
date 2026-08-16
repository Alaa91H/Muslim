package org.example.islamicapp.feature.adhkar.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.example.islamicapp.core.notifications.NotificationChannels
import org.example.islamicapp.feature.adhkar.R
import org.example.islamicapp.feature.adhkar.data.AdhkarRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Posts a lightweight heads-up reminder with a short dhikr. The full Bubble
 * treatment (PROJECT_PROMPT.md §6 Phase 4) builds on Android 11+ shortcut
 * bubbling; until a dedicated activity exists, a standard notification keeps
 * the reminder non-intrusive and entirely optional.
 */
class DhikrReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val repository = AdhkarRepository()
        val dhikr = repository.shortReminders.random(Random(System.currentTimeMillis()))
        val context = applicationContext

        val notification = NotificationCompat.Builder(context, NotificationChannels.DHIKR)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle(context.getString(R.string.dhikr_reminder_title))
            .setContentText(dhikr.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(dhikr.text))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
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
        private const val UNIQUE_NAME = "dhikr_periodic_reminder"
        private const val NOTIFICATION_ID = 4201

        /**
         * Applies [intervalMinutes] as the periodic reminder schedule.
         * A value of 0 cancels the reminder entirely.
         */
        fun schedule(context: Context, intervalMinutes: Long) {
            val workManager = WorkManager.getInstance(context)
            if (intervalMinutes <= 0) {
                workManager.cancelUniqueWork(UNIQUE_NAME)
                return
            }
            val request =
                PeriodicWorkRequestBuilder<DhikrReminderWorker>(intervalMinutes, TimeUnit.MINUTES)
                    .build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // re-schedule with the new interval
                request,
            )
        }
    }
}

/** Scheduling helper injected where the settings UI lives. */
@Singleton
class DhikrReminderScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    fun schedule(intervalMinutes: Int) {
        DhikrReminderWorker.schedule(context, intervalMinutes.toLong())
    }
}
