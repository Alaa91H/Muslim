package org.muslim.app

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
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

    private fun capturePrayerHomeScreenshot(languageCode: String, themeMode: AppThemeMode) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferencesRepository = AppPreferencesRepository(context)
        val prayerRepository = PrayerSettingsRepository(context)
        val originalPreferences = runBlocking { preferencesRepository.preferences.first() }
        val originalPrayerSettings = runBlocking { prayerRepository.settings.first() }
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
        val scenario = ActivityScenario.launch<MainActivity>(MainActivity::class.java)
        try {
            instrumentation.waitForIdleSync()
            val deadline = SystemClock.uptimeMillis() + 15_000
            var homeVisible = false
            while (SystemClock.uptimeMillis() < deadline) {
                val root = instrumentation.uiAutomation.rootInActiveWindow
                homeVisible = root != null && root.packageName?.toString() == context.packageName &&
                    root.findAccessibilityNodeInfosByText("Makkah").isNotEmpty()
                if (homeVisible) break
                SystemClock.sleep(100)
            }
            check(homeVisible) { "Prayer Home is obscured or has not rendered its location" }
            SystemClock.sleep(500)
            instrumentation.waitForIdleSync()
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
            val screenshotName = "prayer-home-$languageCode-${themeMode.name.lowercase()}.png"
            val pendingScreenshot = File(outputDirectory, "$screenshotName.tmp")
            pendingScreenshot.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Could not encode screenshot as PNG"
                }
            }
            check(pendingScreenshot.renameTo(File(outputDirectory, screenshotName))) {
                "Could not publish completed screenshot"
            }
        } finally {
            scenario.close()
            runBlocking {
                preferencesRepository.setThemeMode(originalPreferences.themeMode)
                preferencesRepository.setDynamicColor(originalPreferences.dynamicColor)
                preferencesRepository.setLanguage(originalPreferences.languageCode)
                prayerRepository.save(originalPrayerSettings)
            }
        }
    }
}
