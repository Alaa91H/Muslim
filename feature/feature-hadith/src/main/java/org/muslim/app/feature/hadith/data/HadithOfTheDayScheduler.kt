package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Schedules the daily hadith notification at the user's chosen time. */
object HadithOfTheDayScheduler {

    private const val WORK_NAME = "hadith_of_the_day"

    /**
     * Schedules (or re-schedules) the daily notification for [timeMinutes]
     * (minutes from midnight). Uses [ExistingPeriodicWorkPolicy.UPDATE] so a
     * changed time is applied; on every app open the next run is anchored to
     * the next occurrence of the chosen time, which keeps the daily cadence
     * correct without drifting.
     */
    fun schedule(context: Context, timeMinutes: Int) {
        val request = PeriodicWorkRequestBuilder<HadithOfTheDayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(
                nextNotificationDelayMillis(
                    System.currentTimeMillis(),
                    ZoneId.systemDefault(),
                    timeMinutes,
                ),
                TimeUnit.MILLISECONDS,
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Milliseconds from [nowMillis] until the next occurrence of [timeMinutes]
     * (minutes from midnight) in [zone]. Pure and timezone-aware so it can be
     * unit-tested. If that minute has already passed today, it targets tomorrow.
     */
    fun nextNotificationDelayMillis(nowMillis: Long, zone: ZoneId, timeMinutes: Int): Long {
        val clamped = timeMinutes.coerceIn(0, 23 * 60 + 59)
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        fun at(dayOffset: Long): Long =
            now.toLocalDate()
                .plusDays(dayOffset)
                .atTime(clamped / 60, clamped % 60)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        val today = at(0)
        return if (today > nowMillis) today - nowMillis else at(1) - nowMillis
    }
}
