package org.muslim.app.feature.adhkar.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the customizable periodic adhkar reminder (PROJECT_PROMPT.md §6
 * Phase 4). Uses [AlarmManager.setInexactRepeating] so it needs no exact-alarm
 * permission and stays battery-friendly; the receiver applies the user's time
 * window and category choices.
 */
@Singleton
class PeriodicAdhkarReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(prefs: AdhkarPrefs) {
        cancel()
        if (!prefs.periodicReminderEnabled) return
        val intervalMillis = prefs.periodicReminderIntervalMinutes * 60_000L
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            intervalMillis,
            intervalMillis,
            pendingIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun pendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, PeriodicAdhkarReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val REQUEST_CODE = 201
    }
}
