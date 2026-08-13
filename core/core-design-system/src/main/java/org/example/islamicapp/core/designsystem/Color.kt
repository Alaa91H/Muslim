package org.example.islamicapp.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Fallback Material 3 color schemes, used when Dynamic Color (Material You)
 * is unavailable (Android < 12) or disabled.
 *
 * The palette is a calm teal/green with a warm gold accent — inspired by
 * traditional Islamic ornament while keeping WCAG AA contrast in both modes.
 * TODO: verify all color pairs against WCAG AA (contrast >= 4.5:1 for text).
 */

// ---- Light ----
val ManaraPrimaryLight = Color(0xFF006B54)
val ManaraOnPrimaryLight = Color(0xFFFFFFFF)
val ManaraPrimaryContainerLight = Color(0xFF9EF2D2)
val ManaraOnPrimaryContainerLight = Color(0xFF002019)

val ManaraSecondaryLight = Color(0xFF4C6358)
val ManaraOnSecondaryLight = Color(0xFFFFFFFF)
val ManaraSecondaryContainerLight = Color(0xFFCEE9DB)
val ManaraOnSecondaryContainerLight = Color(0xFF092017)

val ManaraTertiaryLight = Color(0xFF3E6372)
val ManaraOnTertiaryLight = Color(0xFFFFFFFF)
val ManaraTertiaryContainerLight = Color(0xFFC2E8F9)
val ManaraOnTertiaryContainerLight = Color(0xFF001F29)

val ManaraErrorLight = Color(0xFFBA1A1A)
val ManaraOnErrorLight = Color(0xFFFFFFFF)
val ManaraErrorContainerLight = Color(0xFFFFDAD6)
val ManaraOnErrorContainerLight = Color(0xFF410002)

val ManaraBackgroundLight = Color(0xFFFAFDF9)
val ManaraOnBackgroundLight = Color(0xFF191C1A)
val ManaraSurfaceLight = Color(0xFFFAFDF9)
val ManaraOnSurfaceLight = Color(0xFF191C1A)
val ManaraSurfaceVariantLight = Color(0xFFDBE5DF)
val ManaraOnSurfaceVariantLight = Color(0xFF3F4944)
val ManaraOutlineLight = Color(0xFF6F7974)

// ---- Dark ----
val ManaraPrimaryDark = Color(0xFF83D5B6)
val ManaraOnPrimaryDark = Color(0xFF00382B)
val ManaraPrimaryContainerDark = Color(0xFF00513E)
val ManaraOnPrimaryContainerDark = Color(0xFF9EF2D2)

val ManaraSecondaryDark = Color(0xFFB2CCC0)
val ManaraOnSecondaryDark = Color(0xFF1E352B)
val ManaraSecondaryContainerDark = Color(0xFF344B41)
val ManaraOnSecondaryContainerDark = Color(0xFFCEE9DB)

val ManaraTertiaryDark = Color(0xFFA6CCDD)
val ManaraOnTertiaryDark = Color(0xFF0A3543)
val ManaraTertiaryContainerDark = Color(0xFF254B5A)
val ManaraOnTertiaryContainerDark = Color(0xFFC2E8F9)

val ManaraErrorDark = Color(0xFFFFB4AB)
val ManaraOnErrorDark = Color(0xFF690005)
val ManaraErrorContainerDark = Color(0xFF93000A)
val ManaraOnErrorContainerDark = Color(0xFFFFDAD6)

val ManaraBackgroundDark = Color(0xFF0F1512)
val ManaraOnBackgroundDark = Color(0xFFE0E3DF)
val ManaraSurfaceDark = Color(0xFF0F1512)
val ManaraOnSurfaceDark = Color(0xFFE0E3DF)
val ManaraSurfaceVariantDark = Color(0xFF3F4944)
val ManaraOnSurfaceVariantDark = Color(0xFFBEC9C3)
val ManaraOutlineDark = Color(0xFF89938E)

/** Hand-tuned light color scheme (fallback when Dynamic Color is off). */
val ManaraLightColors = lightColorScheme(
    primary = ManaraPrimaryLight,
    onPrimary = ManaraOnPrimaryLight,
    primaryContainer = ManaraPrimaryContainerLight,
    onPrimaryContainer = ManaraOnPrimaryContainerLight,
    secondary = ManaraSecondaryLight,
    onSecondary = ManaraOnSecondaryLight,
    secondaryContainer = ManaraSecondaryContainerLight,
    onSecondaryContainer = ManaraOnSecondaryContainerLight,
    tertiary = ManaraTertiaryLight,
    onTertiary = ManaraOnTertiaryLight,
    tertiaryContainer = ManaraTertiaryContainerLight,
    onTertiaryContainer = ManaraOnTertiaryContainerLight,
    error = ManaraErrorLight,
    onError = ManaraOnErrorLight,
    errorContainer = ManaraErrorContainerLight,
    onErrorContainer = ManaraOnErrorContainerLight,
    background = ManaraBackgroundLight,
    onBackground = ManaraOnBackgroundLight,
    surface = ManaraSurfaceLight,
    onSurface = ManaraOnSurfaceLight,
    surfaceVariant = ManaraSurfaceVariantLight,
    onSurfaceVariant = ManaraOnSurfaceVariantLight,
    outline = ManaraOutlineLight,
)

/** Hand-tuned dark color scheme (fallback when Dynamic Color is off). */
val ManaraDarkColors = darkColorScheme(
    primary = ManaraPrimaryDark,
    onPrimary = ManaraOnPrimaryDark,
    primaryContainer = ManaraPrimaryContainerDark,
    onPrimaryContainer = ManaraOnPrimaryContainerDark,
    secondary = ManaraSecondaryDark,
    onSecondary = ManaraOnSecondaryDark,
    secondaryContainer = ManaraSecondaryContainerDark,
    onSecondaryContainer = ManaraOnSecondaryContainerDark,
    tertiary = ManaraTertiaryDark,
    onTertiary = ManaraOnTertiaryDark,
    tertiaryContainer = ManaraTertiaryContainerDark,
    onTertiaryContainer = ManaraOnTertiaryContainerDark,
    error = ManaraErrorDark,
    onError = ManaraOnErrorDark,
    errorContainer = ManaraErrorContainerDark,
    onErrorContainer = ManaraOnErrorContainerDark,
    background = ManaraBackgroundDark,
    onBackground = ManaraOnBackgroundDark,
    surface = ManaraSurfaceDark,
    onSurface = ManaraOnSurfaceDark,
    surfaceVariant = ManaraSurfaceVariantDark,
    onSurfaceVariant = ManaraOnSurfaceVariantDark,
    outline = ManaraOutlineDark,
)
