package org.muslim.app

import android.graphics.Bitmap
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.view.WindowCompat
import java.io.File
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

    private fun capturePrayerHomeScreenshot(
        languageCode: String,
        themeMode: AppThemeMode,
        fontScale: Float = 1f,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferencesRepository = AppPreferencesRepository(context)
        val prayerRepository = PrayerSettingsRepository(context)
        val originalPreferences = runBlocking { preferencesRepository.preferences.first() }
        val originalPrayerSettings = runBlocking { prayerRepository.settings.first() }
        val originalFontScale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
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
            setSystemFontScale(fontScale)
            scenario = ActivityScenario.launch<MainActivity>(MainActivity::class.java)
            instrumentation.waitForIdleSync()
            val deadline = SystemClock.uptimeMillis() + 15_000
            var homeVisible = false
            var activeWindowDescription = "No active window"
            while (SystemClock.uptimeMillis() < deadline) {
                val root = instrumentation.uiAutomation.rootInActiveWindow
                activeWindowDescription = root?.describeTree().orEmpty()
                homeVisible = root != null && root.packageName?.toString() == context.packageName &&
                    "Makkah" in activeWindowDescription
                if (homeVisible) break
                SystemClock.sleep(100)
            }
            SystemClock.sleep(500)
            instrumentation.waitForIdleSync()
            checkNotNull(scenario).onActivity { activity ->
                check(activity.resources.configuration.locales[0].language == languageCode) {
                    "Activity locale does not match the requested screenshot variant"
                }
                check(kotlin.math.abs(activity.resources.configuration.fontScale - fontScale) < 0.01f) {
                    "Activity font scale does not match the requested screenshot variant"
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
            val fontSuffix = if (fontScale == 2f) "-200" else ""
            val screenshotName = "prayer-home-$languageCode-${themeMode.name.lowercase()}$fontSuffix.png"
            val pendingScreenshot = File(outputDirectory, "$screenshotName.tmp")
            pendingScreenshot.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Could not encode screenshot as PNG"
                }
            }
            check(pendingScreenshot.renameTo(File(outputDirectory, screenshotName))) {
                "Could not publish completed screenshot"
            }
            check(homeVisible) {
                "Prayer Home is obscured or has not rendered its location: $activeWindowDescription"
            }
        } finally {
            scenario?.close()
            setSystemFontScale(originalFontScale)
            runBlocking {
                preferencesRepository.setThemeMode(originalPreferences.themeMode)
                preferencesRepository.setDynamicColor(originalPreferences.dynamicColor)
                preferencesRepository.setLanguage(originalPreferences.languageCode)
                prayerRepository.save(originalPrayerSettings)
            }
        }
    }

    private fun setSystemFontScale(scale: Float) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val descriptor = instrumentation.uiAutomation.executeShellCommand("settings put system font_scale $scale")
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (kotlin.math.abs(instrumentation.targetContext.resources.configuration.fontScale - scale) < 0.01f) {
                return
            }
            SystemClock.sleep(100)
        }
        error("System font scale did not update to $scale")
    }

    private fun AccessibilityNodeInfo.describeTree(): String = buildString {
        append("package=").append(packageName).append(" text=").append(text)
        append(" description=").append(contentDescription).append('\n')
        for (index in 0 until childCount) {
            getChild(index)?.let { append(it.describeTree()) }
        }
    }
}
