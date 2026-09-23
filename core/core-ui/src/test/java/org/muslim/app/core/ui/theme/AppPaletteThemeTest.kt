package org.muslim.app.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.muslim.app.core.common.appearance.AppColorPalette

class AppPaletteThemeTest {

    @Test
    fun `all curated palettes provide light and dark schemes`() {
        AppColorPalette.entries.forEach { palette ->
            val light = appPaletteColorScheme(palette, darkTheme = false)
            val dark = appPaletteColorScheme(palette, darkTheme = true)

            assertTrue(light.background != dark.background)
            assertTrue(light.surface != dark.surface)
            assertTrue(light.primary != light.background)
            assertTrue(dark.primary != dark.background)
        }
    }

    @Test
    fun `palette primary and surface content colors remain readable`() {
        AppColorPalette.entries.forEach { palette ->
            listOf(false, true).forEach { darkTheme ->
                val scheme = appPaletteColorScheme(palette, darkTheme)
                assertTrue(
                    palette.name + " primary contrast",
                    contrastRatio(scheme.primary, scheme.onPrimary) >= 4.5f,
                )
                assertTrue(
                    palette.name + " surface contrast",
                    contrastRatio(scheme.surface, scheme.onSurface) >= 4.5f,
                )
                assertTrue(
                    palette.name + " background contrast",
                    contrastRatio(scheme.background, scheme.onBackground) >= 4.5f,
                )
            }
        }
    }

    @Test
    fun `AMOLED mode turns core dark surfaces true black without changing accents`() {
        AppColorPalette.entries.forEach { palette ->
            val base = appPaletteColorScheme(palette, darkTheme = true)
            val amoled = base.withAmoledBlackSurfaces()

            assertEquals(Color.Black, amoled.background)
            assertEquals(Color.Black, amoled.surface)
            assertEquals(Color.Black, amoled.surfaceDim)
            assertEquals(Color.Black, amoled.surfaceContainerLowest)
            assertEquals(Color.Black, amoled.surfaceContainerLow)
            assertEquals(Color.Black, amoled.surfaceContainer)
            assertEquals(base.primary, amoled.primary)
            assertEquals(base.secondary, amoled.secondary)
            assertEquals(base.tertiary, amoled.tertiary)
        }
    }

    @Test
    fun `AMOLED raised surfaces keep a subtle hierarchy above black`() {
        val amoled = appPaletteColorScheme(AppColorPalette.Classic, darkTheme = true)
            .withAmoledBlackSurfaces()

        assertTrue(amoled.surfaceContainerHigh != Color.Black)
        assertTrue(amoled.surfaceContainerHighest != Color.Black)
        assertTrue(amoled.surfaceBright != Color.Black)
    }

    @Test
    fun `preview swatches are sourced from the real palette scheme`() {
        AppColorPalette.entries.forEach { palette ->
            listOf(false, true).forEach { darkTheme ->
                val scheme = appPaletteColorScheme(palette, darkTheme)
                val preview = previewColorsForPalette(palette, darkTheme)

                assertEquals(scheme.primary, preview.primary)
                assertEquals(scheme.secondary, preview.secondary)
                assertEquals(scheme.tertiary, preview.tertiary)
                assertEquals(scheme.surface, preview.surface)
                assertEquals(scheme.background, preview.background)
            }
        }
    }

    private fun contrastRatio(a: Color, b: Color): Float {
        val lighter = maxOf(a.luminance(), b.luminance())
        val darker = minOf(a.luminance(), b.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
