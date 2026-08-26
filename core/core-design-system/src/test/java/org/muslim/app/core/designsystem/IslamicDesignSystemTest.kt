package org.muslim.app.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IslamicDesignSystemTest {
    @Test
    fun `dark palette keeps the requested calm reader surfaces`() {
        assertEquals(Color(0xFF0D1110), IslamicPalette.Dark.BackgroundPrimary)
        assertEquals(Color(0xFF121816), IslamicPalette.Dark.BackgroundSecondary)
        assertEquals(Color(0xFF151C19), IslamicPalette.Dark.Surface)
        assertEquals(Color(0xFF1C2923), IslamicPalette.Dark.SurfaceElevated)
        assertEquals(Color(0xFF527A68), IslamicPalette.Dark.Primary)
        assertEquals(Color(0xFFB49A62), IslamicPalette.Gold)
    }

    @Test
    fun `light and mushaf paper palettes preserve their designated backgrounds`() {
        assertEquals(Color(0xFFF5F1E7), IslamicPalette.Light.BackgroundPrimary)
        assertEquals(Color(0xFFFAF8F1), IslamicPalette.Light.Surface)
        assertEquals(Color(0xFFE8DEC7), IslamicPalette.Sepia.BackgroundPrimary)
        assertEquals(Color(0xFFEFE6D3), IslamicPalette.Sepia.Surface)
        assertEquals(IslamicPalette.Sepia.Surface, MuslimSepiaColors.surface)
        assertEquals(IslamicPalette.Sepia.TextPrimary, MuslimSepiaColors.onSurface)
    }

    @Test
    fun `gold remains a restrained tertiary accent rather than bright gold`() {
        val brightGold = Color(0xFFFFD700)
        assertNotEquals(brightGold, IslamicPalette.Gold)
        assertEquals(IslamicPalette.Gold, MuslimDarkColors.tertiary)
        assertEquals(IslamicPalette.Sepia.Gold, MuslimSepiaColors.tertiary)
    }

    @Test
    fun `shape motion and touch tokens stay accessible and restrained`() {
        assertEquals(16.dp, IslamicRadius.AyahMarker)
        assertEquals(20.dp, IslamicRadius.Card)
        assertEquals(24.dp, IslamicRadius.Large)
        assertEquals(48.dp, MuslimTouchTarget.Min)
        assertEquals(150, IslamicMotion.FastMillis)
        assertEquals(200, IslamicMotion.StandardMillis)
        assertEquals(250, IslamicMotion.EmphasisMillis)
        assertEquals(1.dp, IslamicElevation.Resting)
    }
}
