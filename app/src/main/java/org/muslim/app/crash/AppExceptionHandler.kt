package org.muslim.app.crash

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * Catches uncaught exceptions inside the application's own coroutine scope so
 * a failure in background work (e.g. restoring queued downloads) surfaces the
 * recoverable error dialog instead of taking the whole process down.
 *
 * Coroutines running in `viewModelScope` / `lifecycleScope` use their own
 * handlers and still propagate to [AppCrashHandler], which remains the global
 * last resort.
 */
fun appCoroutineExceptionHandler(): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled coroutine exception", throwable)
        AppErrorBus.show(
            AppError(
                detail = throwable.stackTraceToString(),
                fatal = false,
            ),
        )
    }

private const val TAG = "AppExceptionHandler"
