package org.muslim.app.feature.prayertimes.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.content.edit
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Silences notifications during the prayer (Do Not Disturb) right after the
 * adhan, then restores the user's previous interruption filter after the
 * configured duration (default 10 minutes). The previous filter is persisted
 * so a device reboot can restore it too.
 *
 * Requires the user to grant notification-policy access
 * ([android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS]);
 * without it the calls are no-ops (prayer times still work normally).
 */
@Singleton
class PrayerDndManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Whether the user granted notification-policy (DND) access. */
    val isPolicyAccessGranted: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            notificationManager.isNotificationPolicyAccessGranted

    /**
     * Activates DND for [durationMinutes] (calling it again while active just
     * extends the restore window). Never overrides a stricter existing filter.
     */
    fun enable(durationMinutes: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (!isPolicyAccessGranted) return

        val current = runCatching { notificationManager.currentInterruptionFilter }
            .getOrDefault(NotificationManager.INTERRUPTION_FILTER_ALL)
        if (current == NotificationManager.INTERRUPTION_FILTER_NONE ||
            current == NotificationManager.INTERRUPTION_FILTER_ALARMS
        ) {
            // The user is already in a quiet mode — respect it.
            return
        }

        val alreadyActive = prefs.getBoolean(KEY_ACTIVE, false)
        if (!alreadyActive) {
            prefs.edit {
                putInt(KEY_PREVIOUS, current)
                putBoolean(KEY_ACTIVE, true)
            }
        }
        runCatching { notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS) }
        scheduleRestore(durationMinutes.coerceIn(1, 180))
    }

    /** Restores the pre-prayer interruption filter (called by the restore alarm). */
    fun restore() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val previous = prefs.getInt(KEY_PREVIOUS, NotificationManager.INTERRUPTION_FILTER_ALL)
        runCatching { notificationManager.setInterruptionFilter(previous) }
        prefs.edit { remove(KEY_ACTIVE); remove(KEY_PREVIOUS) }
        alarmManager.cancel(restorePendingIntent())
    }

    /** Called on boot: restores DND if it was active when the device rebooted. */
    fun restoreAfterReboot() {
        if (prefs.getBoolean(KEY_ACTIVE, false)) restore()
    }

    private fun scheduleRestore(durationMinutes: Int) {
        alarmManager.cancel(restorePendingIntent())
        val at = System.currentTimeMillis() + durationMinutes * 60_000L
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, restorePendingIntent())
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, restorePendingIntent())
        }
    }

    private fun restorePendingIntent(): PendingIntent {
        val intent = Intent(context, PrayerDndRestoreReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            RESTORE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val PREFS_NAME = "prayer_dnd"
        const val KEY_ACTIVE = "active"
        const val KEY_PREVIOUS = "previous_filter"
        const val RESTORE_REQUEST_CODE = 900
    }
}
