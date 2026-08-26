package org.muslim.app.core.ui.accessibility

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import org.muslim.app.core.ui.R

/** Visual accessibility settings shared through [LocalAccessibilityVisuals]. */
data class AccessibilityVisuals(
    val readingModeEnabled: Boolean = false,
) {
    /** A locally bundled Arabic sans face with broad glyph coverage and clear forms. */
    val arabicReadingFont: FontFamily?
        get() = if (readingModeEnabled) ArabicReadingFont else null

    /** Extra leading keeps dense Arabic text easier to track line by line. */
    val arabicLineHeightMultiplier: Float
        get() = if (readingModeEnabled) 2.15f else 1.9f
}

val LocalAccessibilityVisuals = staticCompositionLocalOf { AccessibilityVisuals() }

val ArabicReadingFont = FontFamily(Font(R.font.noto_sans_arabic_variable))

/** Deliberately simple high-contrast schemes, independent of wallpaper-derived colours. */
val AccessibilityLightColors = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF111111),
)

val AccessibilityDarkColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF202020),
    onSurfaceVariant = Color(0xFFF2F2F2),
)
