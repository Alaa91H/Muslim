package org.muslim.app.crash

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.core.content.edit

/**
 * Last-resort catcher for any exception that escapes the main thread (including
 * uncaught coroutine failures, which surface through the thread's handler).
 *
 * Instead of dying silently, it persists the crash, schedules a single clean
 * relaunch through [AlarmManager] (which survives the process death), and then
 * hands off to the platform handler so system crash reporting still works. On
 * relaunch [org.muslim.app.MainActivity] reads [CrashLogStore] and shows the
 * elegant crash dialog.
 *
 * A loop guard prevents a crash inside the startup path from relaunching in a
 * tight cycle: the relaunch is skipped if the process already auto-restarted
 * within the guard window.
 */
class AppCrashHandler(private val appContext: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
        CrashLogStore.save(appContext, throwable)
        scheduleRelaunchIfAllowed()

        // The platform handler terminates the process and preserves normal
        // crash reporting; the AlarmManager relaunch above survives that.
        defaultHandler?.uncaughtException(thread, throwable)

        // Defensive: make sure the process actually exits even if no default
        // handler was installed or it returned without killing.
        Process.killProcess(Process.myPid())
        Runtime.getRuntime().exit(EXIT_CODE)
    }

    private fun scheduleRelaunchIfAllowed() {
        if (!isRelaunchAllowed()) return
        markRelaunchScheduled()
        runCatching {
            val intent = appContext.packageManager
                .getLaunchIntentForPackage(appContext.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                ?: return
            val pending = PendingIntent.getActivity(
                appContext,
                RELAUNCH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )
            appContext.getSystemService(AlarmManager::class.java)
                ?.set(AlarmManager.RTC, System.currentTimeMillis() + RELAUNCH_DELAY_MS, pending)
        }
    }

    private fun isRelaunchAllowed(): Boolean {
        val last = prefs().getLong(KEY_LAST_RELAUNCH, 0L)
        return System.currentTimeMillis() - last > RELAUNCH_LOOP_GUARD_MS
    }

    private fun markRelaunchScheduled() {
        prefs().edit { putLong(KEY_LAST_RELAUNCH, System.currentTimeMillis()) }
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private companion object {
        const val TAG = "AppCrashHandler"
        const val PREFS = "app_crash_handler"
        const val KEY_LAST_RELAUNCH = "last_relaunch_at"
        const val RELAUNCH_REQUEST_CODE = 0x5A17
        const val RELAUNCH_DELAY_MS = 700L
        const val RELAUNCH_LOOP_GUARD_MS = 10_000L
        const val EXIT_CODE = 2
    }
}
