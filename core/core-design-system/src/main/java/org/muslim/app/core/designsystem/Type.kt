package org.muslim.app.core.designsystem

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Typography tokens.
 *
 * Arabic-first strategy (per PROJECT_PROMPT.md §4.2):
 *  - UI font: bundle a high-quality Arabic font (Noto Naskh Arabic or IBM
 *    Plex Sans Arabic) as downloadable/res font resources, paired with
 *    Roboto Flex for Latin. FontFamily.Default is used until those fonts are
 *    added so the skeleton has zero font assets.
 *  - Quran text uses a dedicated Uthmani script font (e.g. KFGQPC Hafs),
 *    separate from the UI font — wired in the Quran feature (Phase 2).
 */
object MuslimFonts {
    // TODO(Phase 0/design): bundle Arabic + Latin fonts and reference them here.
    val Arabic = FontFamily.Default
    val Latin = FontFamily.Default
    val Quran = FontFamily.Default // Uthmani script font lands with the Quran feature.
}

val IslamicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(IslamicRadius.Small),
    medium = RoundedCornerShape(IslamicRadius.Card),
    large = RoundedCornerShape(IslamicRadius.Large),
    extraLarge = RoundedCornerShape(IslamicRadius.Large),
)

val MuslimTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MuslimFonts.Arabic,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
