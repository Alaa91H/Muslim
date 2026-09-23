package org.muslim.app.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity

class IslamicDecorationThemeTest {

    @Test
    fun `every ornament style resolves all feature roles`() {
        AppOrnamentStyle.entries.forEach { style ->
            style.sectionOrnament()
            style.dividerOrnament()
            style.cornerOrnament()
        }
    }

    @Test
    fun `every ornament style has a distinct primary visual identity`() {
        val primaryAssets = AppOrnamentStyle.entries
            .map { it.decorationSpec().primary }

        assertEquals(AppOrnamentStyle.entries.size, primaryAssets.toSet().size)
    }

    @Test
    fun `feature intensity is monotonic and off is invisible`() {
        val off = OrnamentIntensity.Off.featureAlpha(darkTheme = false)
        val subtle = OrnamentIntensity.Subtle.featureAlpha(darkTheme = false)
        val balanced = OrnamentIntensity.Balanced.featureAlpha(darkTheme = false)
        val rich = OrnamentIntensity.Rich.featureAlpha(darkTheme = false)

        assertEquals(0f, off)
        assertTrue(subtle > off)
        assertTrue(balanced > subtle)
        assertTrue(rich > balanced)
    }

    @Test
    fun `background intensity is monotonic and dark mode stays restrained`() {
        listOf(false, true).forEach { darkTheme ->
            val off = OrnamentIntensity.Off.backgroundAlpha(darkTheme)
            val subtle = OrnamentIntensity.Subtle.backgroundAlpha(darkTheme)
            val balanced = OrnamentIntensity.Balanced.backgroundAlpha(darkTheme)
            val rich = OrnamentIntensity.Rich.backgroundAlpha(darkTheme)

            assertEquals(0f, off)
            assertTrue(subtle > off)
            assertTrue(balanced > subtle)
            assertTrue(rich > balanced)
        }

        OrnamentIntensity.entries
            .filterNot { it == OrnamentIntensity.Off }
            .forEach { intensity ->
                assertTrue(
                    intensity.backgroundAlpha(darkTheme = true) <=
                        intensity.backgroundAlpha(darkTheme = false),
                )
            }
    }

    @Test
    fun `appearance previews are clearer than live background decoration`() {
        listOf(false, true).forEach { darkTheme ->
            OrnamentIntensity.entries
                .filterNot { it == OrnamentIntensity.Off }
                .forEach { intensity ->
                    assertTrue(
                        intensity.previewAlpha(darkTheme) >
                            intensity.backgroundAlpha(darkTheme),
                    )
                }
        }
    }

    @Test
    fun `widget ornaments cover every style and honor off`() {
        AppOrnamentStyle.entries.forEach { style ->
            assertEquals(null, widgetOrnamentSpec(style, OrnamentIntensity.Off))
            OrnamentIntensity.entries
                .filterNot { it == OrnamentIntensity.Off }
                .forEach { intensity ->
                    assertTrue(widgetOrnamentSpec(style, intensity) != null)
                }
        }
    }

    @Test
    fun `widget ornament uses the same primary asset as compose`() {
        AppOrnamentStyle.entries.forEach { style ->
            assertEquals(
                style.decorationSpec().primary.drawableRes,
                widgetOrnamentSpec(style, OrnamentIntensity.Balanced)!!.drawableRes,
            )
        }
    }

    @Test
    fun `widget ornament intensity increases monotonically`() {
        val subtle = widgetOrnamentSpec(
            AppOrnamentStyle.Geometry,
            OrnamentIntensity.Subtle,
        )!!.tintAlpha
        val balanced = widgetOrnamentSpec(
            AppOrnamentStyle.Geometry,
            OrnamentIntensity.Balanced,
        )!!.tintAlpha
        val rich = widgetOrnamentSpec(
            AppOrnamentStyle.Geometry,
            OrnamentIntensity.Rich,
        )!!.tintAlpha

        assertTrue(balanced > subtle)
        assertTrue(rich > balanced)
    }

    @Test
    fun `dark mode keeps feature ornaments restrained`() {
        OrnamentIntensity.entries
            .filterNot { it == OrnamentIntensity.Off }
            .forEach { intensity ->
                assertTrue(
                    intensity.featureAlpha(darkTheme = true) <=
                        intensity.featureAlpha(darkTheme = false),
                )
            }
    }
}
