package org.muslim.app.feature.family.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import org.muslim.app.feature.family.domain.AqiqahCalculator
import org.muslim.app.feature.family.domain.AqiqahReminderDay

/** Device-local scheduling for the aqiqah reminder selected by the user. */
object AqiqahReminderScheduler {
    private const val WORK_NAME = "aqiqah_reminder"
    private const val BIRTH_DATE_KEY = "birth_date"
    private const val REMINDER_DAY_KEY = "reminder_day"

    /** Returns false when the selected reminder date has already passed. */
    fun schedule(
        context: Context,
        birthDate: LocalDate,
        day: AqiqahReminderDay = AqiqahReminderDay.Seventh,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        val target = AqiqahCalculator.nextReminderMillis(
            birthDate = birthDate,
            nowMillis = nowMillis,
            zone = ZoneId.systemDefault(),
            day = day,
        ) ?: return false
        val input = Data.Builder()
            .putString(BIRTH_DATE_KEY, birthDate.toString())
            .putString(REMINDER_DAY_KEY, day.name)
            .build()
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

    internal fun reminderDayFrom(input: Data): AqiqahReminderDay =
        input.getString(REMINDER_DAY_KEY)
            ?.let { stored -> AqiqahReminderDay.entries.firstOrNull { it.name == stored } }
            ?: AqiqahReminderDay.Seventh
}
