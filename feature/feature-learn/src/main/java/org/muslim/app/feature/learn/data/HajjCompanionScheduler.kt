package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Schedules the daily Pilgrim Companion reminder at the configured morning time. */
object HajjCompanionScheduler {

    private const val WORK_NAME = "hajj_companion"

    /** Default reminder time: 05:30 (minutes from midnight). */
    const val DEFAULT_TIME_MINUTES = 5 * 60 + 30

    fun schedule(context: Context, timeMinutes: Int = DEFAULT_TIME_MINUTES) {
        val request = PeriodicWorkRequestBuilder<HajjCompanionWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(
                nextReminderDelayMillis(System.currentTimeMillis(), ZoneId.systemDefault(), timeMinutes),
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
     * unit-tested; when that minute already passed today it targets tomorrow.
     */
    fun nextReminderDelayMillis(nowMillis: Long, zone: ZoneId, timeMinutes: Int): Long {
        val clamped = timeMinutes.coerceIn(0, 23 * 60 + 59)
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        fun at(dayOffset: Long): Long =
            now.toLocalDate().plusDays(dayOffset)
                .atTime(clamped / 60, clamped % 60)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        val today = at(0)
        return if (today > nowMillis) today - nowMillis else at(1) - nowMillis
    }
}
