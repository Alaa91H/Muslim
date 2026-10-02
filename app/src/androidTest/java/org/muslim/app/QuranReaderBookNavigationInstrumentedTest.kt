package org.muslim.app

import android.app.Instrumentation
import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.AppThemeMode

@RunWith(AndroidJUnit4::class)
class QuranReaderBookNavigationInstrumentedTest {
    @Test
    fun arabicTurnsBackAndForwardWithMirroredGestures() = verifyPageTurnRoundTrip(languageCode = "ar")

    @Test
    fun englishTurnsBackAndForwardWithLeftToRightGestures() = verifyPageTurnRoundTrip(languageCode = "en")

    private fun verifyPageTurnRoundTrip(languageCode: String) {
        val isRtl = languageCode == "ar"
        var completed = false
        var sharedSpreadVerified = false
        var phase = WAITING_FOR_PAGE_50
        var lastTargetBounds: Rect? = null
        var stableTargetSamples = 0
        var lastForwardPage49Bounds: Rect? = null
        var stableForwardPage49Samples = 0
        var forwardSwipeRetries = 0
        var lastPreviousPage50Bounds: Rect? = null
        var stablePreviousPage50Samples = 0
        var previousSwipeRetries = 0
        var page49Diagnostics = "not found"
        var page50Diagnostics = "not found"
        var pageHeaders = "none"
        val windowBounds = Rect()

        UiUxV2ScreenshotInstrumentedTest().capturePrayerHomeScreenshot(
            languageCode = languageCode,
            themeMode = AppThemeMode.Light,
            route = "quran/reader/3",
            screenName = "quran-book-navigation",
            afterReady = { instrumentation ->
                val deadline = SystemClock.uptimeMillis() + 45_000
                while (SystemClock.uptimeMillis() < deadline && !completed) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow?.also { it.refresh() }
                    windowBounds.setEmpty()
                    root?.getBoundsInScreen(windowBounds)
                    pageHeaders = pageHeaderDiagnostics(root, languageCode)
                    // Page-header text can disappear from the accessibility tree
                    // during a pager settle even while the tagged page is already
                    // visible. Use the page container identity as the primary
                    // signal so a stale text node cannot trigger a duplicate swipe.
                    val page50Bounds = visibleMushafPageBounds(root, 50, windowBounds)
                        ?: visiblePageHeaderBounds(root, 50, languageCode, windowBounds)
                    val page49Bounds = visibleMushafPageBounds(root, 49, windowBounds)
                        ?: visiblePageHeaderBounds(root, 49, languageCode, windowBounds)
                    page49Diagnostics = page49Bounds?.toShortString() ?: "not visible"
                    page50Diagnostics = page50Bounds?.toShortString() ?: "not visible"

                    if (phase == WAITING_FOR_PREVIOUS_SPREAD) {
                        if (page49Bounds == null && page50Bounds == null) {
                            stableTargetSamples += 1
                        } else {
                            stableTargetSamples = 0
                        }
                        if (stableTargetSamples >= STABLE_PAGE_SAMPLES) {
                            // A two-page layout moves by spreads. Confirm the original
                            // spread left, then return to the Baqarah/Aal Imran spread.
                            swipePage(instrumentation, isRtl = isRtl, towardNext = true)
                            phase = WAITING_FOR_PAGE_50_AGAIN
                            stableTargetSamples = 0
                        }
                    } else if (phase == WAITING_FOR_PAGE_50_AGAIN && sharedSpreadVerified) {
                        completed = page49Bounds != null && page50Bounds != null
                    } else {
                        if (phase == WAITING_FOR_PAGE_49 && page50Bounds != null) {
                            if (page50Bounds == lastPreviousPage50Bounds) {
                                stablePreviousPage50Samples += 1
                            } else {
                                lastPreviousPage50Bounds = Rect(page50Bounds)
                                stablePreviousPage50Samples = 0
                            }
                            if (
                                stablePreviousPage50Samples >= RETRY_STABLE_PAGE_SAMPLES &&
                                previousSwipeRetries < MAX_PAGE_TURN_RETRIES
                            ) {
                                // Retry a dropped page-back swipe only after the
                                // original page has remained settled for 3.6s.
                                swipePage(instrumentation, isRtl = isRtl, towardNext = false)
                                previousSwipeRetries += 1
                                stablePreviousPage50Samples = 0
                                lastPreviousPage50Bounds = null
                            }
                        } else {
                            lastPreviousPage50Bounds = null
                            stablePreviousPage50Samples = 0
                        }

                        if (
                            phase == WAITING_FOR_PAGE_50_AGAIN &&
                            page49Bounds != null &&
                            page50Bounds == null
                        ) {
                            if (page49Bounds == lastForwardPage49Bounds) {
                                stableForwardPage49Samples += 1
                            } else {
                                lastForwardPage49Bounds = Rect(page49Bounds)
                                stableForwardPage49Samples = 0
                            }
                            if (
                                stableForwardPage49Samples >= RETRY_STABLE_PAGE_SAMPLES &&
                                forwardSwipeRetries < MAX_PAGE_TURN_RETRIES
                            ) {
                                // The emulator can occasionally drop a shell-injected
                                // swipe. Retry only after the wrong page has remained
                                // stationary long enough to prove it settled.
                                swipePage(instrumentation, isRtl = isRtl, towardNext = true)
                                forwardSwipeRetries += 1
                                stableForwardPage49Samples = 0
                                lastForwardPage49Bounds = null
                            }
                        } else {
                            lastForwardPage49Bounds = null
                            stableForwardPage49Samples = 0
                        }

                        val targetPage = if (phase == WAITING_FOR_PAGE_49) 49 else 50
                        val targetBounds = if (targetPage == 49) page49Bounds else page50Bounds
                        if (targetBounds == null) {
                            lastTargetBounds = null
                            stableTargetSamples = 0
                        } else if (targetBounds == lastTargetBounds) {
                            stableTargetSamples += 1
                        } else {
                            lastTargetBounds = Rect(targetBounds)
                            stableTargetSamples = 0
                        }

                        if (stableTargetSamples >= STABLE_PAGE_SAMPLES) {
                            when (phase) {
                                WAITING_FOR_PAGE_50 -> {
                                    sharedSpreadVerified = page49Bounds != null && page50Bounds != null
                                    swipePage(instrumentation, isRtl = isRtl, towardNext = false)
                                    phase = if (sharedSpreadVerified) {
                                        WAITING_FOR_PREVIOUS_SPREAD
                                    } else {
                                        WAITING_FOR_PAGE_49
                                    }
                                    lastTargetBounds = null
                                    stableTargetSamples = 0
                                }

                                WAITING_FOR_PAGE_49 -> {
                                    swipePage(instrumentation, isRtl = isRtl, towardNext = true)
                                    phase = WAITING_FOR_PAGE_50_AGAIN
                                    lastTargetBounds = null
                                    stableTargetSamples = 0
                                }

                                WAITING_FOR_PAGE_50_AGAIN -> completed = true
                            }
                        }
                    }
                    SystemClock.sleep(150)
                }
                check(completed) {
                    "Expected $languageCode page turns to move between Baqarah page 49 and Aal Imran page 50 " +
                        "(phase=$phase, sharedSpread=$sharedSpreadVerified, window=$windowBounds, " +
                        "page49=$page49Diagnostics, page50=$page50Diagnostics, " +
                        "previousRetries=$previousSwipeRetries, forwardRetries=$forwardSwipeRetries, headers=$pageHeaders)"
                }
            },
        )
    }

    private fun swipePage(instrumentation: Instrumentation, isRtl: Boolean, towardNext: Boolean) {
        val metrics = instrumentation.targetContext.resources.displayMetrics
        val y = (metrics.heightPixels * 0.4f).toInt()
        // Logical forward is rightward in RTL and leftward in LTR. Stay 20%
        // in from both edges: Android's back-gesture region can consume a
        // rightward page turn that starts too close to the screen edge.
        val movesRight = if (isRtl) towardNext else !towardNext
        val startX = (metrics.widthPixels * if (movesRight) 0.30f else 0.70f).toInt()
        val endX = (metrics.widthPixels * if (movesRight) 0.70f else 0.30f).toInt()
        instrumentationShell(instrumentation, "input swipe $startX $y $endX $y 400")
    }

    private fun instrumentationShell(instrumentation: Instrumentation, command: String) {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    private fun visiblePageHeaderBounds(
        node: AccessibilityNodeInfo?,
        page: Int,
        languageCode: String,
        window: Rect,
    ): Rect? {
        if (node == null) return null
        val label = if (languageCode == "ar") "صفحة $page" else "Page $page"
        if (node.text?.toString()?.contains(label, ignoreCase = true) == true) {
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
            visiblePageHeaderBounds(node.getChild(index), page, languageCode, window)?.let { return it }
        }
        return null
    }

    private fun visibleMushafPageBounds(
        node: AccessibilityNodeInfo?,
        page: Int,
        window: Rect,
    ): Rect? {
        if (node == null) return null
        if (node.viewIdResourceName?.endsWith("mushaf-page-$page") == true) {
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
            visibleMushafPageBounds(node.getChild(index), page, window)?.let { return it }
        }
        return null
    }

    private fun pageHeaderDiagnostics(node: AccessibilityNodeInfo?, languageCode: String): String {
        if (node == null) return "no active window"
        val marker = if (languageCode == "ar") "صفحة " else "Page "
        val headers = mutableListOf<String>()
        fun visit(current: AccessibilityNodeInfo) {
            val text = current.text?.toString().orEmpty()
            if (text.contains(marker, ignoreCase = true)) {
                val bounds = Rect()
                current.getBoundsInScreen(bounds)
                headers += "$text@$bounds"
            }
            for (index in 0 until current.childCount) current.getChild(index)?.let(::visit)
        }
        visit(node)
        return headers.takeLast(6).joinToString(" | ").ifEmpty { "no page labels in accessibility tree" }
    }

    private companion object {
        const val WAITING_FOR_PAGE_50 = 0
        const val WAITING_FOR_PAGE_49 = 1
        const val WAITING_FOR_PAGE_50_AGAIN = 2
        const val WAITING_FOR_PREVIOUS_SPREAD = 3
        const val STABLE_PAGE_SAMPLES = 3
        // Retry only after the wrong page stays unchanged for 3.6s. This
        // gives slow emulator flings time to finish before another gesture.
        const val RETRY_STABLE_PAGE_SAMPLES = 24
        const val MAX_PAGE_TURN_RETRIES = 3
        const val MIN_VISIBLE_FRACTION = 0.35f
    }
}
