package org.muslim.app

import android.graphics.Bitmap
import android.content.Intent
import android.content.res.Configuration
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.view.WindowCompat
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.AppThemeMode
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation

/** Captures the actual Hilt-backed Prayer Home screen on the CI emulator. */
@RunWith(AndroidJUnit4::class)
class UiUxV2ScreenshotInstrumentedTest {
    @Test
    fun capturesPrayerHomeArabicLightScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "ar", themeMode = AppThemeMode.Light)
    }

    @Test
    fun capturesPrayerHomeEnglishDarkScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "en", themeMode = AppThemeMode.Dark)
    }

    @Test
    fun capturesPrayerHomeArabicDarkScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "ar", themeMode = AppThemeMode.Dark)
    }

    @Test
    fun capturesPrayerHomeEnglishLightScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "en", themeMode = AppThemeMode.Light)
    }

    @Test
    fun capturesPrayerHomeArabicLargeFontScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "ar", themeMode = AppThemeMode.Light, fontScale = 2f)
    }

    @Test
    fun capturesPrayerHomeEnglishLargeFontScreenshot() {
        capturePrayerHomeScreenshot(languageCode = "en", themeMode = AppThemeMode.Light, fontScale = 2f)
    }

    internal fun capturePrayerHomeScreenshot(
        languageCode: String,
        themeMode: AppThemeMode,
        fontScale: Float = 1f,
        route: String = "home",
        screenName: String = "prayer-home",
        expanded: Boolean = false,
        afterReady: ((android.app.Instrumentation) -> Unit)? = null,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val originalFontScale = shell("settings get system font_scale").trim().toFloatOrNull() ?: 1f
        val originalAccessibilityFlags = instrumentation.uiAutomation.serviceInfo.flags
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        val preferencesRepository = AppPreferencesRepository(context)
        val prayerRepository = PrayerSettingsRepository(context)
        val originalPreferences = runBlocking { preferencesRepository.preferences.first() }
        val originalPrayerSettings = runBlocking { prayerRepository.settings.first() }
        val fixedClock = InstrumentationRegistry.getArguments().getString("uiux.fixedClock") == "true"
        val originalWallTime = System.currentTimeMillis()
        val originalElapsedTime = SystemClock.elapsedRealtime()
        var expandedDisplayDiagnostics = "not requested"
        runBlocking {
            // Screen QA starts after onboarding; system permission dialogs can
            // otherwise dim or replace the screen while still producing a PNG.
            preferencesRepository.markInitialPermissionSetupHandled()
            preferencesRepository.setDynamicColor(false)
            preferencesRepository.setThemeMode(themeMode)
            preferencesRepository.setLanguage(languageCode)
            prayerRepository.save(
                PrayerSettings(
                    location = SelectedLocation(
                        name = "Makkah",
                        latitude = 21.4225,
                        longitude = 39.8262,
                        timeZone = "Asia/Riyadh",
                    ),
                ),
            )
        }
        var scenario: ActivityScenario<MainActivity>? = null
        try {
            if (expanded) {
                val appliedSize = shell("wm size")
                val appliedDensity = shell("wm density")
                expandedDisplayDiagnostics =
                    "appliedSize=$appliedSize appliedDensity=$appliedDensity"
                val widthDeadline = SystemClock.uptimeMillis() + 10_000
                while (context.resources.configuration.screenWidthDp < 840 && SystemClock.uptimeMillis() < widthDeadline) {
                    SystemClock.sleep(100)
                }
            }
            setSystemFontScale(fontScale)
            if (fixedClock) {
                val result = shell("su 0 date -u 093015002026.00")
                val expectedEpoch = Instant.parse("2026-09-30T15:00:00Z").toEpochMilli()
                check(kotlin.math.abs(System.currentTimeMillis() - expectedEpoch) < 5_000L) {
                    "CI screenshot clock could not be fixed: $result"
                }
            }
            scenario = ActivityScenario.launch<MainActivity>(
                Intent(context, MainActivity::class.java).putExtra("org.muslim.app.extra.ROUTE", route),
            )
            instrumentation.waitForIdleSync()
            val deadline = SystemClock.uptimeMillis() + 15_000
            var homeVisible = false
            var activeWindowDescription = "No active window"
            while (SystemClock.uptimeMillis() < deadline) {
                val root = activeRoot()
                activeWindowDescription = root?.describeTree().orEmpty()
                val expectedContent = when {
                    screenName == "prayer-monthly" -> "uiux-prayer-monthly-content"
                    route == "home" -> "Makkah"
                    else -> "uiux-route:${routePattern(route)}"
                }
                val normalizedContent = activeWindowDescription.replace(Regex("[\\p{M}ـ]"), "").replace('ٱ', 'ا')
                val dataReady = when (route) {
                    "quran" -> "uiux-quran-content-loaded" in activeWindowDescription
                    "quran/reader/1" -> "بسم الله" in normalizedContent
                    "quran/reader/3" -> root?.findAccessibilityNodeInfosByViewId(
                        "${context.packageName}:id/mushaf-page-50",
                    ).orEmpty().isNotEmpty()
                    else -> true
                }
                homeVisible = root != null && root.packageName?.toString() == context.packageName &&
                    expectedContent in activeWindowDescription && dataReady
                if (homeVisible) break
                SystemClock.sleep(100)
            }
            check(homeVisible) {
                "Requested screen $route is not ready for interaction: $activeWindowDescription"
            }
            afterReady?.invoke(instrumentation)
            SystemClock.sleep(500)
            instrumentation.waitForIdleSync()
            checkNotNull(scenario).onActivity { activity ->
                check(activity.resources.configuration.locales[0].language == languageCode) {
                    "Activity locale does not match the requested screenshot variant"
                }
                check(kotlin.math.abs(activity.resources.configuration.fontScale - fontScale) < 0.01f) {
                    "Activity font scale does not match the requested screenshot variant"
                }
                if (expanded) check(activity.resources.configuration.screenWidthDp >= 840) {
                    "Expanded capture did not reach the expanded window breakpoint: " +
                        "${activity.resources.configuration}; $expandedDisplayDiagnostics"
                }
                val bars = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                val lightTheme = themeMode == AppThemeMode.Light
                check(bars.isAppearanceLightStatusBars == lightTheme) {
                    "Status bar icon contrast does not match the app theme"
                }
                check(bars.isAppearanceLightNavigationBars == lightTheme) {
                    "Navigation bar icon contrast does not match the app theme"
                }
            }
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            check(bitmap.width > 0 && bitmap.height > 0) { "Screenshot has invalid dimensions" }
            val sampledColors = buildSet {
                for (x in 0 until bitmap.width step 32) {
                    for (y in 0 until bitmap.height step 32) add(bitmap.getPixel(x, y))
                }
            }
            check(sampledColors.size > 1) { "Screenshot appears blank; app content was not rendered" }
            val externalFilesDirectory = checkNotNull(context.getExternalFilesDir(null)) {
                "App external files directory is unavailable"
            }
            val outputDirectory = File(externalFilesDirectory, "uiux-v2")
            check(outputDirectory.mkdirs() || outputDirectory.isDirectory) {
                "Could not create screenshot output directory: ${outputDirectory.absolutePath}"
            }
            val fontSuffix = when (fontScale) { 2f -> "-200"; 1.5f -> "-150"; else -> "" }
            val widthSuffix = if (expanded) "-expanded" else ""
            val screenshotName = "$screenName-$languageCode-${themeMode.name.lowercase()}$fontSuffix$widthSuffix.png"
            val pendingScreenshot = File(outputDirectory, "$screenshotName.tmp")
            pendingScreenshot.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Could not encode screenshot as PNG"
                }
            }
            bitmap.recycle()
            check(pendingScreenshot.renameTo(File(outputDirectory, screenshotName))) {
                "Could not publish completed screenshot"
            }
            check(homeVisible) {
                "Requested screen $route is obscured or has not rendered: $activeWindowDescription"
            }
        } finally {
            try {
                scenario?.close()
                setSystemFontScale(originalFontScale)
                if (fixedClock) {
                    val restoredTime = originalWallTime + SystemClock.elapsedRealtime() - originalElapsedTime
                    val date = DateTimeFormatter.ofPattern("MMddHHmmyyyy.ss", Locale.US)
                        .withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(restoredTime))
                    shell("su 0 date -u $date")
                }
            } finally { runBlocking {
                preferencesRepository.setThemeMode(originalPreferences.themeMode)
                preferencesRepository.setDynamicColor(originalPreferences.dynamicColor)
                preferencesRepository.setLanguage(originalPreferences.languageCode)
                prayerRepository.save(originalPrayerSettings)
                instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
                    flags = originalAccessibilityFlags
                }
            } }
        }
    }

    private fun setSystemFontScale(scale: Float) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        shell("settings put system font_scale $scale")
        val configuration = Configuration(instrumentation.targetContext.resources.configuration).apply {
            fontScale = scale
        }
        instrumentation.targetContext.resources.updateConfiguration(configuration, null)
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            val contextScale = instrumentation.targetContext.resources.configuration.fontScale
            if (kotlin.math.abs(contextScale - scale) < 0.01f) {
                return
            }
            SystemClock.sleep(100)
        }
        error("System font scale setting did not update to $scale")
    }

    private fun AccessibilityNodeInfo.describeTree(): String = buildString {
        append("package=").append(packageName).append(" text=").append(text)
        append(" id=").append(viewIdResourceName)
        append(" description=").append(contentDescription).append('\n')
        for (index in 0 until childCount) {
            getChild(index)?.let { append(it.describeTree()) }
        }
    }

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes().toString(Charsets.UTF_8) }
    }

    private fun clickAccessibleLabel(label: String) {
        repeat(12) {
            val root = activeRoot()
            val match = root?.findNode { it.text?.toString() == label || it.contentDescription?.toString() == label }
            var clickable = match
            while (clickable != null && !clickable.isClickable) clickable = clickable.parent
            if (clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                SystemClock.sleep(300)
                return
            }
            root?.findNode { it.isScrollable }?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            SystemClock.sleep(300)
        }
        error("Could not activate accessible control: $label")
    }

    private fun AccessibilityNodeInfo.findNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (predicate(this)) return this
        for (index in 0 until childCount) {
            getChild(index)?.findNode(predicate)?.let { return it }
        }
        return null
    }

    private fun activeRoot(): AccessibilityNodeInfo? {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        if (Build.VERSION.SDK_INT >= 33) automation.clearCache()
        return automation.rootInActiveWindow
    }

    private fun routePattern(route: String): String =
        if (route.startsWith("quran/reader/")) "quran/reader/{surahNumber}?ayah={ayah}&autoplay={autoplay}" else route
}
