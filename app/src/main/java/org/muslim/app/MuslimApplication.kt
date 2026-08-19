package org.muslim.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.muslim.app.crash.AppCrashHandler
import org.muslim.app.crash.appCoroutineExceptionHandler
import org.muslim.app.feature.quran.data.QuranDownloadManager
import javax.inject.Inject

/**
 * Application entry point.
 */
@HiltAndroidApp
class MuslimApplication : Application() {

    @Inject lateinit var quranDownloadManager: QuranDownloadManager

    /**
     * Application-wide scope: survives individual failures and routes them to
     * the recoverable error dialog instead of taking the process down.
     */
    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + appCoroutineExceptionHandler(),
    )

    override fun onCreate() {
        super.onCreate()
        // Centralized last-resort catcher for any exception that escapes the
        // main thread (including uncaught coroutine failures outside this
        // scope): persist the crash and auto-relaunch into the crash dialog.
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler(this))
        // Resume any queued recitation downloads that survived a process death
        // (also covered by the boot receiver after a full device reboot).
        applicationScope.launch {
            quranDownloadManager.restore()
        }
    }
}
