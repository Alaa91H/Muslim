package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed

/**
 * Optional daily hadith notification (PROJECT_PROMPT.md §6 Phase 3: «حديث اليوم»).
 *
 * Picks the same deterministic "hadith of the day" the library shows, so the
 * notification and the in-app card always agree. Tap opens the hadith library.
 *
 * [repository] and [notifier] are the test seams: subclasses can substitute
 * fakes in unit tests without a real application, Hilt graph or Android
 * framework — this is what lets the worker's logic be unit-tested on the JVM
 * with the WorkManager environment mocked.
 */
open class HadithOfTheDayWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Respect the user's toggles even if a periodic job is still queued
        // (e.g. it was scheduled before the toggle was turned off): the
        // hadith-specific switch and the unified notification manager.
        if (!repository().isDailyNotificationEnabled()) return Result.success()
        if (!categoryEnabled()) return Result.success()
        val hadith = repository().hadithOfTheDay() ?: return Result.retry()
        notifier().show(hadith)
        return Result.success()
    }

    /** Whether the unified notification manager allows the hadith category. */
    protected open suspend fun categoryEnabled(): Boolean =
        applicationContext.notificationAllowed(NotificationCategory.HadithDaily)

    protected open fun repository(): HadithOfTheDaySource =
        EntryPointAccessors.fromApplication(
            applicationContext,
            HadithOfTheDayEntryPoint::class.java,
        ).hadithRepository()

    protected open fun notifier(): HadithOfTheDayNotifier =
        HadithOfTheDayNotifier(applicationContext)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HadithOfTheDayEntryPoint {
        fun hadithRepository(): HadithRepository
    }
}
