package org.muslim.app.crash

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A single error currently presented by the crash dialog.
 *
 * [detail] is the full technical report (exception class + message + stack)
 * shown in a monospace section; [fatal] distinguishes the two paths:
 *  - fatal (an uncaught exception that already took the process down, shown
 *    after the auto-relaunch) → offers Restart / Close.
 *  - recoverable (an uncaught coroutine exception caught app-wide) → offers a
 *    single "Continue" action and the app keeps running.
 */
data class AppError(
    val detail: String,
    val fatal: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * App-wide in-memory channel between the exception handlers (non-Compose) and
 * the Compose crash dialog. A process-global singleton is appropriate here
 * because exception reporting must work from any thread before any UI exists.
 */
object AppErrorBus {

    private val _current = MutableStateFlow<AppError?>(null)
    val current: StateFlow<AppError?> = _current.asStateFlow()

    fun show(error: AppError) {
        _current.value = error
    }

    fun dismiss() {
        _current.value = null
    }
}
