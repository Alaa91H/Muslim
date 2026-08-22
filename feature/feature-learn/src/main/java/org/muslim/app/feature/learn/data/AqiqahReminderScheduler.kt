package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import org.muslim.app.feature.learn.domain.AqiqahCalculator

/** Device-local scheduling for the aqiqah reminder selected by the user. */
object AqiqahReminderScheduler {
    private const val WORK_NAME = "aqiqah_reminder"
    private const val BIRTH_DATE_KEY = "birth_date"

    /** Returns false when the seventh-day reminder has already passed. */
    fun schedule(context: Context, birthDate: LocalDate, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val target = AqiqahCalculator.nextReminderMillis(birthDate, nowMillis, ZoneId.systemDefault())
            ?: return false
        val input = Data.Builder().putString(BIRTH_DATE_KEY, birthDate.toString()).build()
        val request = OneTimeWorkRequestBuilder<AqiqahReminderWorker>()
            .setInputData(input)
            .setInitialDelay(target - nowMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return true
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    internal fun birthDateFrom(input: Data): LocalDate? =
        input.getString(BIRTH_DATE_KEY)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}
