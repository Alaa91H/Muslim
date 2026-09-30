package org.muslim.app

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.muslim.app.core.datastore.AppThemeMode

/** Every required screen/locale/theme/font/window combination is a separate result. */
@RunWith(Parameterized::class)
class UiUxV2MatrixInstrumentedTest(
    private val screenName: String,
    private val route: String,
    private val language: String,
    private val theme: AppThemeMode,
    private val fontScale: Float,
    private val expanded: Boolean,
) {
    @Test
    fun capturesRequestedScreenVariant() {
        UiUxV2ScreenshotInstrumentedTest().capturePrayerHomeScreenshot(
            languageCode = language,
            themeMode = theme,
            fontScale = fontScale,
            route = route,
            screenName = screenName,
            expanded = expanded,
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}-{2}-{3}-font{4}-expanded{5}")
        fun variants(): List<Array<Any>> = buildList {
            val screens = listOf(
                "prayer-home" to "home",
                "prayer-monthly" to "home",
                "quran-home" to "quran",
                "quran-reader" to "quran/reader/1",
                "qibla" to "qibla",
                "more" to "more",
                "hadith" to "hadith",
                "settings" to "settings",
            )
            for ((name, route) in screens) {
                for (language in listOf("ar", "en")) {
                    for (theme in listOf(AppThemeMode.Light, AppThemeMode.Dark)) {
                        for (fontScale in listOf(1f, 1.5f, 2f)) {
                            for (expanded in listOf(false, true)) {
                                add(arrayOf(name, route, language, theme, fontScale, expanded))
                            }
                        }
                    }
                }
            }
        }
    }
}
