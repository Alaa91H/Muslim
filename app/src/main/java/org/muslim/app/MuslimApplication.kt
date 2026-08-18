package org.muslim.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.QuranDownloadManager
import javax.inject.Inject

/**
 * Application entry point.
 */
@HiltAndroidApp
class MuslimApplication : Application() {

    @Inject lateinit var quranDownloadManager: QuranDownloadManager

    override fun onCreate() {
        super.onCreate()
        // Resume any queued recitation downloads that survived a process death
        // (also covered by the boot receiver after a full device reboot).
        CoroutineScope(Dispatchers.IO).launch {
            quranDownloadManager.restore()
        }
    }
}
