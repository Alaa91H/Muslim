package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import org.muslim.app.core.common.time.HijriDate
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed
import org.muslim.app.feature.learn.domain.HajjCompanion

/**
 * Daily Pilgrim Companion worker (PROJECT_PROMPT.md section Hajj): posts the
 * rite reminder only on Hajj days (Dhul-Hijjah 8-13) and only when the user
 * has the Hajj category enabled in the unified notification manager. Outside
 * the season (or when disabled) it is a no-op.
 */
open class HajjCompanionWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!categoryEnabled()) return Result.success()
        val today = HijriDate.today()
        if (!HajjCompanion.isCompanionDay(today)) return Result.success()
        val mansik = HajjCompanion.mansikFor(today.day) ?: return Result.success()
        notifier().show(mansik)
        return Result.success()
    }

    /** Whether the unified notification manager allows the Hajj category. */
    protected open suspend fun categoryEnabled(): Boolean =
        applicationContext.notificationAllowed(NotificationCategory.Hajj)

    protected open fun notifier(): HajjCompanionNotifier =
        HajjCompanionNotifier(applicationContext)
}
