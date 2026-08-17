package org.muslim.app.feature.quran.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.notifications.notificationCategoryEnabled
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranRepository
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Optional daily ayah notification (PROJECT_PROMPT.md §6 Phase 2: «آية اليوم»).
 *
 * Picks an ayah deterministically from the day of the year so every user sees
 * the same ayah on the same day. Tap opens the app's quran tab.
 */
class AyahOfTheDayWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Respect the unified notification manager even if the periodic job
        // was scheduled before the user disabled this category.
        if (!applicationContext.notificationCategoryEnabled(NotificationCategory.QuranDaily)) return Result.success()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AyahOfTheDayEntryPoint::class.java,
        )
        val repository = entryPoint.quranRepository()
        val today = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        // Rotate through the full Quran (6236 ayahs) day by day.
        val globalNumber = (today % TOTAL_AYAHS).toInt() + 1
        val ayah = repository.ayahByGlobal(globalNumber) ?: return Result.retry()
        val surahName = repository.observeSurahMetadata(ayah.surahNumber).first()?.arabicName ?: ""

        showNotification(ayah, surahName)
        return Result.success()
    }

    private fun showNotification(ayah: Ayah, surahName: String) {
        NotificationChannels.create(applicationContext)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent().apply {
                setClassName(applicationContext, MAIN_ACTIVITY)
                data = Uri.parse("muslim://quran")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = "$surahName · ${ayah.numberInSurah}"
        val notification = Notification.Builder(applicationContext, NotificationChannels.QURAN_DAILY)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(ayah.text)
            .setStyle(Notification.BigTextStyle().bigText(ayah.text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AyahOfTheDayEntryPoint {
        fun quranRepository(): QuranRepository
    }

    private companion object {
        const val TOTAL_AYAHS = 6236L
        const val NOTIFICATION_ID = 7001
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
