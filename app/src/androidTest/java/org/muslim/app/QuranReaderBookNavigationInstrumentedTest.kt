package org.muslim.app

import android.app.Instrumentation
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.AppThemeMode

@RunWith(AndroidJUnit4::class)
class QuranReaderBookNavigationInstrumentedTest {
    @Test
    fun aalImranPreviousPageShowsTheBaqarahMushafPage() {
        var swipedBack = false
        UiUxV2ScreenshotInstrumentedTest().capturePrayerHomeScreenshot(
            languageCode = "ar",
            themeMode = AppThemeMode.Light,
            route = "quran/reader/3",
            screenName = "quran-book-navigation",
            afterReady = { instrumentation ->
                val packageName = instrumentation.targetContext.packageName
                val deadline = SystemClock.uptimeMillis() + 10_000
                var reachedBaqarahPage = false
                while (SystemClock.uptimeMillis() < deadline && !reachedBaqarahPage) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow
                    val page50 = root?.findAccessibilityNodeInfosByViewId("$packageName:id/mushaf-page-50").orEmpty()
                    if (page50.isNotEmpty() && !swipedBack) {
                        instrumentationShell(instrumentation, "input swipe 70 320 270 320 400")
                        swipedBack = true
                    } else if (swipedBack) {
                        reachedBaqarahPage = root
                            ?.findAccessibilityNodeInfosByViewId("$packageName:id/mushaf-page-49")
                            .orEmpty().isNotEmpty()
                    }
                    SystemClock.sleep(150)
                }
                check(swipedBack && reachedBaqarahPage) {
                    "Paging back from Aal Imran must reach Al Baqarah's ending page 49"
                }
            },
        )
    }

    private fun instrumentationShell(instrumentation: Instrumentation, command: String) {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }
}
