package org.muslim.app

import android.app.Instrumentation
import android.graphics.Rect
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
        var sharedSpreadVerified = false
        UiUxV2ScreenshotInstrumentedTest().capturePrayerHomeScreenshot(
            languageCode = "ar",
            themeMode = AppThemeMode.Light,
            route = "quran/reader/3",
            screenName = "quran-book-navigation",
            afterReady = { instrumentation ->
                val deadline = SystemClock.uptimeMillis() + 10_000
                var reachedBaqarahPage = false
                val windowBounds = Rect()
                var page49Diagnostics = "not found"
                var page50Diagnostics = "not found"
                while (SystemClock.uptimeMillis() < deadline && !reachedBaqarahPage) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow?.also { it.refresh() }
                    windowBounds.setEmpty()
                    root?.getBoundsInScreen(windowBounds)
                    val page50Bounds = visiblePageHeaderBounds(root, 50, windowBounds)
                    val page49Bounds = visiblePageHeaderBounds(root, 49, windowBounds)
                    page49Diagnostics = page49Bounds?.toShortString() ?: "not visible"
                    page50Diagnostics = page50Bounds?.toShortString() ?: "not visible"
                    val page50Visible = page50Bounds != null
                    val page49Visible = page49Bounds != null
                    if (page50Visible && page49Visible) {
                        // On a two-page layout, Al Baqarah's last page (49)
                        // and Aal Imran's first page (50) share one printed
                        // spread. Both being substantially on-screen proves
                        // the correct book order without flipping past 49.
                        sharedSpreadVerified = true
                        reachedBaqarahPage = true
                    } else if (page50Visible && !swipedBack) {
                        // On a one-page layout, use a full-width RTL page turn;
                        // a short 200px swipe can be below the pager's fling
                        // threshold on the CI emulator.
                        val metrics = instrumentation.targetContext.resources.displayMetrics
                        val y = (metrics.heightPixels * 0.4f).toInt()
                        val startX = (metrics.widthPixels * 0.1f).toInt()
                        val endX = (metrics.widthPixels * 0.9f).toInt()
                        instrumentationShell(
                            instrumentation,
                            "input swipe $startX $y $endX $y 400",
                        )
                        swipedBack = true
                    } else if (swipedBack && page49Visible) {
                        reachedBaqarahPage = true
                    }
                    SystemClock.sleep(150)
                }
                check(reachedBaqarahPage) {
                    "Al Baqarah page 49 must be visible before/after paging back from Aal Imran page 50 " +
                        "(swiped=$swipedBack, sharedSpread=$sharedSpreadVerified, window=$windowBounds, " +
                        "page49=$page49Diagnostics, page50=$page50Diagnostics)"
                }
            },
        )
    }

    private fun instrumentationShell(instrumentation: Instrumentation, command: String) {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    private fun visiblePageHeaderBounds(
        node: AccessibilityNodeInfo?,
        page: Int,
        window: Rect,
    ): Rect? {
        if (node == null) return null
        if (node.text?.toString()?.contains("صفحة $page") == true) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.width() > 0 && bounds.height() > 0) {
                val visibleBounds = Rect(bounds)
                if (visibleBounds.intersect(window) &&
                    visibleBounds.width() >= bounds.width() * MIN_VISIBLE_FRACTION &&
                    visibleBounds.height() >= bounds.height() * MIN_VISIBLE_FRACTION
                ) return visibleBounds
            }
        }
        for (index in 0 until node.childCount) {
            visiblePageHeaderBounds(node.getChild(index), page, window)?.let { return it }
        }
        return null
    }

    private companion object {
        const val MIN_VISIBLE_FRACTION = 0.35f
    }
}
