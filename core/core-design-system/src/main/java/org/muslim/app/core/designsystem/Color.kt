package org.muslim.app.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Central Modern Islamic Minimalism color tokens. Components consume Material
 * roles or [IslamicPalette]; they must not embed hexadecimal values.
 */
object IslamicPalette {
    // Shared accent family: gold is decorative/semantic accent, never a dominant background.
    val Gold = Color(0xFFB49A62)
    val GoldLight = Color(0xFFD0BA82)
    val GoldDark = Color(0xFF927744)

    object Dark {
        val BackgroundPrimary = Color(0xFF0D1110)
        val BackgroundSecondary = Color(0xFF121816)
        val Surface = Color(0xFF151C19)
        val SurfaceElevated = Color(0xFF1C2923)
        val Primary = Color(0xFF527A68)
        val PrimaryDark = Color(0xFF3F6655)
        val PrimaryLight = Color(0xFF718F81)
        val TextPrimary = Color(0xFFE9E5D8)
        val TextSecondary = Color(0xFFA9AAA2)
        val TextMuted = Color(0xFF777D77)
        val QuranPrimary = Color(0xFFE6DDC8)
        val Border = Color(0xFF2A332F)
        val BorderSubtle = Color(0xFF202824)
    }

    object Light {
        val BackgroundPrimary = Color(0xFFF5F1E7)
        val BackgroundSecondary = Color(0xFFF2EEE3)
        val Surface = Color(0xFFFAF8F1)
        val SurfaceElevated = Color(0xFFFFFFFF)
        val Primary = Color(0xFF3F6655)
        val TextPrimary = Color(0xFF252923)
        val TextSecondary = Color(0xFF62685F)
        val TextMuted = Color(0xFF858980)
        val QuranPrimary = Color(0xFF29251D)
        val Border = Color(0xFFD5D0C4)
        val BorderSubtle = Color(0xFFE6E1D6)
    }

    /** Reader-only theme for long Quran sessions; not a replacement app-wide scheme. */
    object Sepia {
        val BackgroundPrimary = Color(0xFFE8DEC7)
        val Surface = Color(0xFFEFE6D3)
        val TextPrimary = Color(0xFF29251D)
        val QuranPrimary = Color(0xFF29251D)
        val Primary = Color(0xFF496653)
        val Gold = Color(0xFF927744)
        val Border = Color(0xFFCFC2A7)
    }
}

// Material 3 aliases retained for existing features.
val MuslimPrimaryLight = IslamicPalette.Light.Primary
val MuslimOnPrimaryLight = Color(0xFFF5F1E7)
val MuslimPrimaryContainerLight = Color(0xFFDCE8DF)
val MuslimOnPrimaryContainerLight = Color(0xFF182A21)
val MuslimSecondaryLight = IslamicPalette.Light.TextSecondary
val MuslimOnSecondaryLight = Color(0xFFFFFFFF)
val MuslimSecondaryContainerLight = IslamicPalette.Light.BackgroundSecondary
val MuslimOnSecondaryContainerLight = IslamicPalette.Light.TextPrimary
val MuslimTertiaryLight = Color(0xFF9A7D45)
val MuslimOnTertiaryLight = Color(0xFFFFFFFF)
val MuslimTertiaryContainerLight = Color(0xFFF1E5C7)
val MuslimOnTertiaryContainerLight = Color(0xFF34260C)
val MuslimErrorLight = Color(0xFFBA1A1A)
val MuslimOnErrorLight = Color(0xFFFFFFFF)
val MuslimErrorContainerLight = Color(0xFFFFDAD6)
val MuslimOnErrorContainerLight = Color(0xFF410002)
val MuslimBackgroundLight = IslamicPalette.Light.BackgroundPrimary
val MuslimOnBackgroundLight = IslamicPalette.Light.TextPrimary
val MuslimSurfaceLight = IslamicPalette.Light.Surface
val MuslimOnSurfaceLight = IslamicPalette.Light.TextPrimary
val MuslimSurfaceVariantLight = IslamicPalette.Light.BackgroundSecondary
val MuslimOnSurfaceVariantLight = IslamicPalette.Light.TextSecondary
val MuslimOutlineLight = IslamicPalette.Light.Border

val MuslimPrimaryDark = IslamicPalette.Dark.Primary
val MuslimOnPrimaryDark = Color(0xFFF2F0E8)
val MuslimPrimaryContainerDark = IslamicPalette.Dark.PrimaryDark
val MuslimOnPrimaryContainerDark = Color(0xFFE1ECE5)
val MuslimSecondaryDark = IslamicPalette.Dark.TextSecondary
val MuslimOnSecondaryDark = IslamicPalette.Dark.BackgroundPrimary
val MuslimSecondaryContainerDark = IslamicPalette.Dark.SurfaceElevated
val MuslimOnSecondaryContainerDark = IslamicPalette.Dark.TextPrimary
val MuslimTertiaryDark = IslamicPalette.Gold
val MuslimOnTertiaryDark = IslamicPalette.Dark.BackgroundPrimary
val MuslimTertiaryContainerDark = Color(0xFF4B3E24)
val MuslimOnTertiaryContainerDark = IslamicPalette.GoldLight
val MuslimErrorDark = Color(0xFFFFB4AB)
val MuslimOnErrorDark = Color(0xFF690005)
val MuslimErrorContainerDark = Color(0xFF93000A)
val MuslimOnErrorContainerDark = Color(0xFFFFDAD6)
val MuslimBackgroundDark = IslamicPalette.Dark.BackgroundPrimary
val MuslimOnBackgroundDark = IslamicPalette.Dark.TextPrimary
val MuslimSurfaceDark = IslamicPalette.Dark.Surface
val MuslimOnSurfaceDark = IslamicPalette.Dark.TextPrimary
val MuslimSurfaceVariantDark = IslamicPalette.Dark.SurfaceElevated
val MuslimOnSurfaceVariantDark = IslamicPalette.Dark.TextSecondary
val MuslimOutlineDark = IslamicPalette.Dark.Border

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
    outlineVariant = IslamicPalette.Light.BorderSubtle,
)

/** Reader-oriented paper scheme for long Quran sessions and design previews. */
val MuslimSepiaColors = lightColorScheme(
    primary = IslamicPalette.Sepia.Primary,
    onPrimary = Color(0xFFF8F4EA),
    primaryContainer = IslamicPalette.Sepia.Surface,
    onPrimaryContainer = IslamicPalette.Sepia.TextPrimary,
    tertiary = IslamicPalette.Sepia.Gold,
    background = IslamicPalette.Sepia.BackgroundPrimary,
    onBackground = IslamicPalette.Sepia.TextPrimary,
    surface = IslamicPalette.Sepia.Surface,
    onSurface = IslamicPalette.Sepia.TextPrimary,
    surfaceVariant = IslamicPalette.Sepia.BackgroundPrimary,
    onSurfaceVariant = IslamicPalette.Sepia.TextPrimary.copy(alpha = 0.72f),
    secondaryContainer = IslamicPalette.Sepia.Surface,
    onSecondaryContainer = IslamicPalette.Sepia.TextPrimary,
    outline = IslamicPalette.Sepia.Border,
    outlineVariant = IslamicPalette.Sepia.Border.copy(alpha = 0.65f),
)

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
    outlineVariant = IslamicPalette.Dark.BorderSubtle,
)
