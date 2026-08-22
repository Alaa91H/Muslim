package org.muslim.app.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import org.muslim.app.core.designsystem.IslamicShapes
import org.muslim.app.core.designsystem.MuslimDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityLightColors
import org.muslim.app.core.ui.accessibility.AccessibilityVisuals
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals
import org.muslim.app.core.designsystem.MuslimLightColors
import org.muslim.app.core.designsystem.MuslimTypography

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
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast && darkTheme -> AccessibilityDarkColors
        highContrast -> AccessibilityLightColors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MuslimDarkColors
        else -> MuslimLightColors
    }

    CompositionLocalProvider(
        LocalAccessibilityVisuals provides AccessibilityVisuals(accessibilityReadingMode),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MuslimTypography,
            shapes = IslamicShapes,
            content = content,
        )
    }
}
