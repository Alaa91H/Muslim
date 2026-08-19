package org.muslim.app.crash

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Persists the most recent uncaught exception to a private file so the crash
 * screen can present it after the process is restarted. All I/O is guarded:
 * a failure while reporting a crash must never trigger a second crash.
 */
object CrashLogStore {

    private const val FILE_NAME = "last_crash.txt"

    /** Serializes a throwable into a stable, human-readable report. Pure. */
    fun format(throwable: Throwable, timestamp: Long = System.currentTimeMillis()): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))
        return buildString {
            appendLine("time=$time")
            appendLine("exception=${throwable.javaClass.name}")
            appendLine("message=${throwable.message ?: "<none>"}")
            appendLine()
            append(throwable.stackTraceToString())
        }
    }

    fun save(context: Context, throwable: Throwable) {
        runCatching {
            File(context.filesDir, FILE_NAME).writeText(format(throwable))
        }
    }

    fun readLatest(context: Context): String? =
        runCatching { File(context.filesDir, FILE_NAME).takeIf { it.exists() }?.readText() }
            .getOrNull()

    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE_NAME).delete() }
    }
}
