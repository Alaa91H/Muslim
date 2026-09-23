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
