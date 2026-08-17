package org.muslim.app.core.designsystem

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
val MuslimPrimaryLight = Color(0xFF006B54)
val MuslimOnPrimaryLight = Color(0xFFFFFFFF)
val MuslimPrimaryContainerLight = Color(0xFF9EF2D2)
val MuslimOnPrimaryContainerLight = Color(0xFF002019)

val MuslimSecondaryLight = Color(0xFF4C6358)
val MuslimOnSecondaryLight = Color(0xFFFFFFFF)
val MuslimSecondaryContainerLight = Color(0xFFCEE9DB)
val MuslimOnSecondaryContainerLight = Color(0xFF092017)

val MuslimTertiaryLight = Color(0xFF3E6372)
val MuslimOnTertiaryLight = Color(0xFFFFFFFF)
val MuslimTertiaryContainerLight = Color(0xFFC2E8F9)
val MuslimOnTertiaryContainerLight = Color(0xFF001F29)

val MuslimErrorLight = Color(0xFFBA1A1A)
val MuslimOnErrorLight = Color(0xFFFFFFFF)
val MuslimErrorContainerLight = Color(0xFFFFDAD6)
val MuslimOnErrorContainerLight = Color(0xFF410002)

val MuslimBackgroundLight = Color(0xFFFAFDF9)
val MuslimOnBackgroundLight = Color(0xFF191C1A)
val MuslimSurfaceLight = Color(0xFFFAFDF9)
val MuslimOnSurfaceLight = Color(0xFF191C1A)
val MuslimSurfaceVariantLight = Color(0xFFDBE5DF)
val MuslimOnSurfaceVariantLight = Color(0xFF3F4944)
val MuslimOutlineLight = Color(0xFF6F7974)

// ---- Dark ----
val MuslimPrimaryDark = Color(0xFF83D5B6)
val MuslimOnPrimaryDark = Color(0xFF00382B)
val MuslimPrimaryContainerDark = Color(0xFF00513E)
val MuslimOnPrimaryContainerDark = Color(0xFF9EF2D2)

val MuslimSecondaryDark = Color(0xFFB2CCC0)
val MuslimOnSecondaryDark = Color(0xFF1E352B)
val MuslimSecondaryContainerDark = Color(0xFF344B41)
val MuslimOnSecondaryContainerDark = Color(0xFFCEE9DB)

val MuslimTertiaryDark = Color(0xFFA6CCDD)
val MuslimOnTertiaryDark = Color(0xFF0A3543)
val MuslimTertiaryContainerDark = Color(0xFF254B5A)
val MuslimOnTertiaryContainerDark = Color(0xFFC2E8F9)

val MuslimErrorDark = Color(0xFFFFB4AB)
val MuslimOnErrorDark = Color(0xFF690005)
val MuslimErrorContainerDark = Color(0xFF93000A)
val MuslimOnErrorContainerDark = Color(0xFFFFDAD6)

val MuslimBackgroundDark = Color(0xFF0F1512)
val MuslimOnBackgroundDark = Color(0xFFE0E3DF)
val MuslimSurfaceDark = Color(0xFF0F1512)
val MuslimOnSurfaceDark = Color(0xFFE0E3DF)
val MuslimSurfaceVariantDark = Color(0xFF3F4944)
val MuslimOnSurfaceVariantDark = Color(0xFFBEC9C3)
val MuslimOutlineDark = Color(0xFF89938E)

/** Hand-tuned light color scheme (fallback when Dynamic Color is off). */
val MuslimLightColors = lightColorScheme(
    primary = MuslimPrimaryLight,
    onPrimary = MuslimOnPrimaryLight,
    primaryContainer = MuslimPrimaryContainerLight,
    onPrimaryContainer = MuslimOnPrimaryContainerLight,
    secondary = MuslimSecondaryLight,
    onSecondary = MuslimOnSecondaryLight,
    secondaryContainer = MuslimSecondaryContainerLight,
    onSecondaryContainer = MuslimOnSecondaryContainerLight,
    tertiary = MuslimTertiaryLight,
    onTertiary = MuslimOnTertiaryLight,
    tertiaryContainer = MuslimTertiaryContainerLight,
    onTertiaryContainer = MuslimOnTertiaryContainerLight,
    error = MuslimErrorLight,
    onError = MuslimOnErrorLight,
    errorContainer = MuslimErrorContainerLight,
    onErrorContainer = MuslimOnErrorContainerLight,
    background = MuslimBackgroundLight,
    onBackground = MuslimOnBackgroundLight,
    surface = MuslimSurfaceLight,
    onSurface = MuslimOnSurfaceLight,
    surfaceVariant = MuslimSurfaceVariantLight,
    onSurfaceVariant = MuslimOnSurfaceVariantLight,
    outline = MuslimOutlineLight,
)

/** Hand-tuned dark color scheme (fallback when Dynamic Color is off). */
val MuslimDarkColors = darkColorScheme(
    primary = MuslimPrimaryDark,
    onPrimary = MuslimOnPrimaryDark,
    primaryContainer = MuslimPrimaryContainerDark,
    onPrimaryContainer = MuslimOnPrimaryContainerDark,
    secondary = MuslimSecondaryDark,
    onSecondary = MuslimOnSecondaryDark,
    secondaryContainer = MuslimSecondaryContainerDark,
    onSecondaryContainer = MuslimOnSecondaryContainerDark,
    tertiary = MuslimTertiaryDark,
    onTertiary = MuslimOnTertiaryDark,
    tertiaryContainer = MuslimTertiaryContainerDark,
    onTertiaryContainer = MuslimOnTertiaryContainerDark,
    error = MuslimErrorDark,
    onError = MuslimOnErrorDark,
    errorContainer = MuslimErrorContainerDark,
    onErrorContainer = MuslimOnErrorContainerDark,
    background = MuslimBackgroundDark,
    onBackground = MuslimOnBackgroundDark,
    surface = MuslimSurfaceDark,
    onSurface = MuslimOnSurfaceDark,
    surfaceVariant = MuslimSurfaceVariantDark,
    onSurfaceVariant = MuslimOnSurfaceVariantDark,
    outline = MuslimOutlineDark,
)
