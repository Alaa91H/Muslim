package org.muslim.app

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferencesRepository = AppPreferencesRepository(context)
        val prayerRepository = PrayerSettingsRepository(context)
        val originalPreferences = runBlocking { preferencesRepository.preferences.first() }
        val originalPrayerSettings = runBlocking { prayerRepository.settings.first() }
        runBlocking {
            preferencesRepository.setThemeMode(AppThemeMode.Light)
            preferencesRepository.setLanguage("ar")
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
        val activity = instrumentation.startActivitySync(
            android.content.Intent(context, MainActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
        ) as MainActivity

        try {
            instrumentation.waitForIdleSync()
            Thread.sleep(1_000)
            instrumentation.waitForIdleSync()
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            check(bitmap.width > 0 && bitmap.height > 0) { "Screenshot has invalid dimensions" }
            val sampledColors = buildSet {
                for (x in 0 until bitmap.width step 32) {
                    for (y in 0 until bitmap.height step 32) add(bitmap.getPixel(x, y))
                }
            }
            check(sampledColors.size > 1) { "Screenshot appears blank; app content was not rendered" }
            val outputDirectory = File(activity.getExternalFilesDir(null), "uiux-v2")
            check(outputDirectory.mkdirs() || outputDirectory.isDirectory) {
                "Could not create screenshot output directory: ${outputDirectory.absolutePath}"
            }
            File(outputDirectory, "prayer-home-ar-light.png").outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Could not encode screenshot as PNG"
                }
            }
        } finally {
            activity.finish()
            runBlocking {
                preferencesRepository.setThemeMode(originalPreferences.themeMode)
                preferencesRepository.setLanguage(originalPreferences.languageCode)
                prayerRepository.save(originalPrayerSettings)
            }
        }
    }
}
