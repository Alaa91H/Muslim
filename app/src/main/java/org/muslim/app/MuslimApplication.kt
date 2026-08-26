package org.muslim.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.muslim.app.crash.AppCrashHandler
import org.muslim.app.crash.appCoroutineExceptionHandler
import org.muslim.app.feature.prayertimes.notifications.AdhanNotifications
import org.muslim.app.feature.prayertimes.notifications.NextAdhanNotifications
import org.muslim.app.feature.quran.data.QuranDownloadManager
import org.muslim.app.wear.WearCompanionPublisher
import javax.inject.Inject

/**
 * Application entry point.
 */
@HiltAndroidApp
class MuslimApplication : Application() {

    @Inject lateinit var quranDownloadManager: QuranDownloadManager
    @Inject lateinit var wearCompanionPublisher: WearCompanionPublisher

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
        // Android can retain ongoing notifications across an in-place APK
        // update. Remove both retired identities immediately on first launch,
        // rather than waiting for the next scheduled prayer or countdown tick.
        AdhanNotifications.cancelRetiredAdhan(this)
        NextAdhanNotifications.cancelRetiredCountdown(this)
        // Resume any queued recitation downloads that survived a process death
        // (also covered by the boot receiver after a full device reboot).
        applicationScope.launch {
            quranDownloadManager.restore()
        }
        // The publisher observes the opt-in toggle and performs no Data Layer
        // communication until the user explicitly enables their paired watch.
        wearCompanionPublisher.start()
    }
}
