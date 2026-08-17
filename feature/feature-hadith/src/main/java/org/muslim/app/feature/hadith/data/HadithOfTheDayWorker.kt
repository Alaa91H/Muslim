package org.muslim.app.feature.hadith.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.hadith.R
import org.muslim.app.feature.hadith.domain.Hadith

/**
 * Optional daily hadith notification (PROJECT_PROMPT.md §6 Phase 3: «حديث اليوم»).
 *
 * Picks the same deterministic "hadith of the day" the library shows, so the
 * notification and the in-app card always agree. Tap opens the hadith library.
 */
class HadithOfTheDayWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            HadithOfTheDayEntryPoint::class.java,
        )
        val hadith = entryPoint.hadithRepository().hadithOfTheDay() ?: return Result.retry()
        showNotification(hadith)
        return Result.success()
    }

    private fun showNotification(hadith: Hadith) {
        NotificationChannels.create(applicationContext)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent().apply {
                setClassName(applicationContext, MAIN_ACTIVITY)
                data = Uri.parse("muslim://hadith")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = applicationContext.getString(R.string.hadith_of_the_day)
        val body = buildString {
            append(hadith.arabicText)
            if (hadith.translation.isNotBlank()) {
                append("\n\n").append(hadith.translation)
            }
            append("\n\n").append(hadith.source)
        }
        val notification = Notification.Builder(applicationContext, NotificationChannels.HADITH_DAILY)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(hadith.arabicText)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HadithOfTheDayEntryPoint {
        fun hadithRepository(): HadithRepository
    }

    private companion object {
        const val NOTIFICATION_ID = 7002
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
