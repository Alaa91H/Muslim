package org.example.islamicapp.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.example.islamicapp.core.designsystem.ManaraDarkColors
import org.example.islamicapp.core.designsystem.ManaraLightColors
import org.example.islamicapp.core.designsystem.ManaraTypography

/**
 * Application theme (Material 3 / Material You).
 *
 * - Dynamic Color from the system wallpaper on Android 12+ (default on).
 * - Falls back to the hand-tuned [ManaraLightColors]/[ManaraDarkColors].
 * - Supports light / dark / follow-system via [darkTheme].
 *
 * Theme mode preference (light/dark/system toggle) will be read from
 * DataStore when the settings module ships (Phase 1/feature-settings).
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ManaraDarkColors
        else -> ManaraLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ManaraTypography,
        content = content,
    )
}
