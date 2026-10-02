package org.muslim.app

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.feature.settings.locale.withAppLocale

@RunWith(AndroidJUnit4::class)
class AppLocaleConfigurationInstrumentedTest {
    @Test
    fun localeOverride_preservesBaseFontScaleWithoutPinningWindowWidth() {
        val originalLocale = Locale.getDefault()
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val base = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                fontScale = 2f
            })
            val currentWindowWidth = context.resources.configuration.screenWidthDp
            val configuration = base.withAppLocale("ar").resources.configuration
            assertEquals("ar", configuration.locales[0].language)
            assertEquals(2f, configuration.fontScale, 0.01f)
            assertEquals(currentWindowWidth, configuration.screenWidthDp)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun systemLanguage_usesSystemResourcesAfterChangingProcessLocale() {
        val originalLocale = Locale.getDefault()
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val systemContext = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                setLocale(Locale.ENGLISH)
            })
            Locale.setDefault(Locale.forLanguageTag("ar"))
            val configuration = systemContext.withAppLocale(AppPreferences.SYSTEM_LANGUAGE).resources.configuration
            assertEquals("en", configuration.locales[0].language)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
