package org.muslim.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.appearance.AppColorPalette
import org.muslim.app.core.common.appearance.CardCornerStyle
import org.muslim.app.core.designsystem.MuslimDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityLightColors
import org.muslim.app.core.ui.accessibility.AccessibilityVisuals
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals
import org.muslim.app.core.designsystem.MuslimLightColors
import org.muslim.app.core.designsystem.MuslimTypography
import org.muslim.app.core.designsystem.IslamicShapes

/**
 * Application theme (Material 3 / Material You).
 *
 * - Dynamic Color from the system wallpaper on Android 12+ (default on).
 * - Falls back to the hand-tuned [MuslimLightColors]/[MuslimDarkColors].
 * - Supports light / dark / follow-system via [darkTheme].
 *
 * Theme mode preference (light/dark/system toggle) will be read from
 * DataStore when the settings module ships (Phase 1/feature-settings).
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    highContrast: Boolean = false,
    accessibilityReadingMode: Boolean = false,
    reduceAnimations: Boolean = false,
    colorPalette: AppColorPalette = AppColorPalette.Classic,
    cardCornerStyle: CardCornerStyle = CardCornerStyle.Soft,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast && darkTheme -> AccessibilityDarkColors
        highContrast -> AccessibilityLightColors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> paletteColorScheme(colorPalette, darkTheme)
    }

    CompositionLocalProvider(
        LocalAccessibilityVisuals provides AccessibilityVisuals(accessibilityReadingMode),
        LocalMuslimMotionPreferences provides MuslimMotionPreferences(reduceAnimations),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MuslimTypography,
            shapes = appShapes(cardCornerStyle),
            content = content,
        )
    }
}

private fun paletteColorScheme(palette: AppColorPalette, darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) MuslimDarkColors else MuslimLightColors
    return when (palette) {
        AppColorPalette.Classic -> base
        AppColorPalette.Emerald -> base.copy(
            primary = if (darkTheme) Color(0xFF89D7AC) else Color(0xFF176B45),
            primaryContainer = if (darkTheme) Color(0xFF174E34) else Color(0xFFC6F2D7),
            tertiary = if (darkTheme) Color(0xFFD6C17B) else Color(0xFF7E650D),
            tertiaryContainer = if (darkTheme) Color(0xFF514819) else Color(0xFFF6E8A8),
        )
        AppColorPalette.Midnight -> base.copy(
            primary = if (darkTheme) Color(0xFFAEC6FF) else Color(0xFF24549B),
            primaryContainer = if (darkTheme) Color(0xFF284778) else Color(0xFFD9E2FF),
            tertiary = if (darkTheme) Color(0xFFB8D5E7) else Color(0xFF2F657E),
            tertiaryContainer = if (darkTheme) Color(0xFF224957) else Color(0xFFC9E8F7),
        )
        AppColorPalette.Sand -> base.copy(
            primary = if (darkTheme) Color(0xFFE0B58A) else Color(0xFF865229),
            primaryContainer = if (darkTheme) Color(0xFF60401F) else Color(0xFFFFDCC1),
            tertiary = if (darkTheme) Color(0xFFE1CA91) else Color(0xFF77622A),
            tertiaryContainer = if (darkTheme) Color(0xFF56491D) else Color(0xFFF8E9B6),
        )
    }
}

private fun appShapes(style: CardCornerStyle): Shapes {
    val card = when (style) {
        CardCornerStyle.Compact -> 10.dp
        CardCornerStyle.Soft -> 18.dp
        CardCornerStyle.Rounded -> 28.dp
    }
    return IslamicShapes.copy(
        medium = RoundedCornerShape(card),
        large = RoundedCornerShape(card + 6.dp),
        extraLarge = RoundedCornerShape(card + 10.dp),
    )
}
