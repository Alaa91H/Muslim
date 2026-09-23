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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.appearance.AppColorPalette
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.CardCornerStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.designsystem.IslamicShapes
import org.muslim.app.core.designsystem.MuslimDarkColors
import org.muslim.app.core.designsystem.MuslimLightColors
import org.muslim.app.core.designsystem.MuslimTypography
import org.muslim.app.core.ui.accessibility.AccessibilityDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityLightColors
import org.muslim.app.core.ui.accessibility.AccessibilityVisuals
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals

/**
 * Application theme (Material 3 / Material You).
 *
 * Dynamic wallpaper colour remains optional. When it is disabled, every
 * curated palette provides a complete set of primary/secondary/tertiary,
 * container, background, surface and outline roles in both light and dark
 * modes instead of recolouring only one or two accents.
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
    ornamentStyle: AppOrnamentStyle = AppOrnamentStyle.Geometry,
    ornamentIntensity: OrnamentIntensity = OrnamentIntensity.Balanced,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast && darkTheme -> AccessibilityDarkColors
        highContrast -> AccessibilityLightColors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> appPaletteColorScheme(colorPalette, darkTheme)
    }

    CompositionLocalProvider(
        LocalAccessibilityVisuals provides AccessibilityVisuals(accessibilityReadingMode),
        LocalMuslimMotionPreferences provides MuslimMotionPreferences(reduceAnimations),
        LocalIslamicDecoration provides IslamicDecorationPreferences(
            style = ornamentStyle,
            intensity = ornamentIntensity,
            darkTheme = darkTheme,
        ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MuslimTypography,
            shapes = appShapes(cardCornerStyle),
            content = content,
        )
    }
}

/** Compact swatches used by the Appearance screen without duplicating theme hex values there. */
data class PalettePreviewColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color,
    val background: Color,
)

fun previewColorsForPalette(
    palette: AppColorPalette,
    darkTheme: Boolean,
): PalettePreviewColors {
    val scheme = appPaletteColorScheme(palette, darkTheme)
    return PalettePreviewColors(
        primary = scheme.primary,
        secondary = scheme.secondary,
        tertiary = scheme.tertiary,
        surface = scheme.surface,
        background = scheme.background,
    )
}

private data class PaletteTokens(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
)

private val LightInk = Color(0xFFF8F8F4)
private val DarkInk = Color(0xFF111411)

private fun readableOn(color: Color): Color =
    if (color.luminance() > 0.44f) DarkInk else LightInk

internal fun appPaletteColorScheme(
    palette: AppColorPalette,
    darkTheme: Boolean,
): ColorScheme {
    val base = if (darkTheme) MuslimDarkColors else MuslimLightColors
    if (palette == AppColorPalette.Classic) return base

    val t = paletteTokens(palette, darkTheme)
    return base.copy(
        primary = t.primary,
        onPrimary = readableOn(t.primary),
        primaryContainer = t.primaryContainer,
        onPrimaryContainer = readableOn(t.primaryContainer),
        secondary = t.secondary,
        onSecondary = readableOn(t.secondary),
        secondaryContainer = t.secondaryContainer,
        onSecondaryContainer = readableOn(t.secondaryContainer),
        tertiary = t.tertiary,
        onTertiary = readableOn(t.tertiary),
        tertiaryContainer = t.tertiaryContainer,
        onTertiaryContainer = readableOn(t.tertiaryContainer),
        background = t.background,
        onBackground = readableOn(t.background),
        surface = t.surface,
        onSurface = readableOn(t.surface),
        surfaceVariant = t.surfaceVariant,
        onSurfaceVariant = readableOn(t.surfaceVariant),
        outline = t.outline,
        outlineVariant = t.outlineVariant,
    )
}

@Suppress("LongMethod")
private fun paletteTokens(
    palette: AppColorPalette,
    darkTheme: Boolean,
): PaletteTokens = when (palette) {
    AppColorPalette.Classic -> error("Classic uses the core design-system scheme")

    AppColorPalette.Emerald -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFF89D7AC),
            primaryContainer = Color(0xFF174E34),
            secondary = Color(0xFFB4D3C2),
            secondaryContainer = Color(0xFF28463A),
            tertiary = Color(0xFFD6C17B),
            tertiaryContainer = Color(0xFF514819),
            background = Color(0xFF0B1611),
            surface = Color(0xFF112018),
            surfaceVariant = Color(0xFF1A2A21),
            outline = Color(0xFF789486),
            outlineVariant = Color(0xFF34483E),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF176B45),
            primaryContainer = Color(0xFFC6F2D7),
            secondary = Color(0xFF436857),
            secondaryContainer = Color(0xFFD2E8DA),
            tertiary = Color(0xFF7E650D),
            tertiaryContainer = Color(0xFFF6E8A8),
            background = Color(0xFFEFF8F1),
            surface = Color(0xFFF8FCF9),
            surfaceVariant = Color(0xFFDDE9E1),
            outline = Color(0xFF6A8577),
            outlineVariant = Color(0xFFBFCFC5),
        )
    }

    AppColorPalette.Midnight -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFAEC6FF),
            primaryContainer = Color(0xFF284778),
            secondary = Color(0xFFB8D5E7),
            secondaryContainer = Color(0xFF224957),
            tertiary = Color(0xFFD6C586),
            tertiaryContainer = Color(0xFF50461E),
            background = Color(0xFF09111D),
            surface = Color(0xFF111A28),
            surfaceVariant = Color(0xFF1A2637),
            outline = Color(0xFF718096),
            outlineVariant = Color(0xFF344156),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF24549B),
            primaryContainer = Color(0xFFD9E2FF),
            secondary = Color(0xFF2F657E),
            secondaryContainer = Color(0xFFC9E8F7),
            tertiary = Color(0xFF7C650F),
            tertiaryContainer = Color(0xFFF4E9B2),
            background = Color(0xFFEFF4FF),
            surface = Color(0xFFF8FAFF),
            surfaceVariant = Color(0xFFDCE3F2),
            outline = Color(0xFF6A7889),
            outlineVariant = Color(0xFFC2C9D5),
        )
    }

    AppColorPalette.Sand -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFE0B58A),
            primaryContainer = Color(0xFF60401F),
            secondary = Color(0xFFD5C59D),
            secondaryContainer = Color(0xFF56491D),
            tertiary = Color(0xFFE1CA91),
            tertiaryContainer = Color(0xFF58481D),
            background = Color(0xFF17120E),
            surface = Color(0xFF231B15),
            surfaceVariant = Color(0xFF31261E),
            outline = Color(0xFF9B846F),
            outlineVariant = Color(0xFF4C3D31),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF865229),
            primaryContainer = Color(0xFFFFDCC1),
            secondary = Color(0xFF77622A),
            secondaryContainer = Color(0xFFF8E9B6),
            tertiary = Color(0xFF8A652A),
            tertiaryContainer = Color(0xFFF9E6B8),
            background = Color(0xFFFCF4EA),
            surface = Color(0xFFFFFAF4),
            surfaceVariant = Color(0xFFE7D9CB),
            outline = Color(0xFF7D6855),
            outlineVariant = Color(0xFFCDBEAF),
        )
    }

    AppColorPalette.RoyalBlue -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFF9CB8FF),
            primaryContainer = Color(0xFF2A4586),
            secondary = Color(0xFFB7C8F4),
            secondaryContainer = Color(0xFF33416B),
            tertiary = Color(0xFFD6C17B),
            tertiaryContainer = Color(0xFF524515),
            background = Color(0xFF0D111A),
            surface = Color(0xFF141A28),
            surfaceVariant = Color(0xFF1E2534),
            outline = Color(0xFF7C88A7),
            outlineVariant = Color(0xFF3C465D),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF214AA6),
            primaryContainer = Color(0xFFD9E2FF),
            secondary = Color(0xFF4A5E96),
            secondaryContainer = Color(0xFFDDE3FF),
            tertiary = Color(0xFF8A6A1F),
            tertiaryContainer = Color(0xFFF6E7AE),
            background = Color(0xFFEDF2FF),
            surface = Color(0xFFF8FAFF),
            surfaceVariant = Color(0xFFDCE2F2),
            outline = Color(0xFF6D7A99),
            outlineVariant = Color(0xFFC1C9DB),
        )
    }

    AppColorPalette.Turquoise -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFF73D7D1),
            primaryContainer = Color(0xFF00504E),
            secondary = Color(0xFFA7D9D6),
            secondaryContainer = Color(0xFF244C4A),
            tertiary = Color(0xFFC7CB6D),
            tertiaryContainer = Color(0xFF4E5200),
            background = Color(0xFF091413),
            surface = Color(0xFF0F1E1D),
            surfaceVariant = Color(0xFF20302F),
            outline = Color(0xFF73908E),
            outlineVariant = Color(0xFF344C4A),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF006A68),
            primaryContainer = Color(0xFF9EF2EC),
            secondary = Color(0xFF3D6664),
            secondaryContainer = Color(0xFFC2ECE8),
            tertiary = Color(0xFF6A6F00),
            tertiaryContainer = Color(0xFFECEF9A),
            background = Color(0xFFE8F8F7),
            surface = Color(0xFFF6FCFB),
            surfaceVariant = Color(0xFFD5E8E6),
            outline = Color(0xFF647D7B),
            outlineVariant = Color(0xFFB9CFCC),
        )
    }

    AppColorPalette.Olive -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFA8C686),
            primaryContainer = Color(0xFF3F5221),
            secondary = Color(0xFFC2D3A8),
            secondaryContainer = Color(0xFF4A5830),
            tertiary = Color(0xFFE1C87A),
            tertiaryContainer = Color(0xFF554613),
            background = Color(0xFF0F130D),
            surface = Color(0xFF171C13),
            surfaceVariant = Color(0xFF252B20),
            outline = Color(0xFF818C74),
            outlineVariant = Color(0xFF3C4535),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF556B2F),
            primaryContainer = Color(0xFFD9E8BD),
            secondary = Color(0xFF6C7B4B),
            secondaryContainer = Color(0xFFE1ECCF),
            tertiary = Color(0xFF886A19),
            tertiaryContainer = Color(0xFFF8E7B1),
            background = Color(0xFFF0F6E8),
            surface = Color(0xFFFAFCF7),
            surfaceVariant = Color(0xFFDEE5D4),
            outline = Color(0xFF77816E),
            outlineVariant = Color(0xFFC4CCBA),
        )
    }

    AppColorPalette.Burgundy -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFE0A7B3),
            primaryContainer = Color(0xFF5E2231),
            secondary = Color(0xFFE8C2C9),
            secondaryContainer = Color(0xFF643943),
            tertiary = Color(0xFFE4C27D),
            tertiaryContainer = Color(0xFF584714),
            background = Color(0xFF160C0F),
            surface = Color(0xFF241317),
            surfaceVariant = Color(0xFF352126),
            outline = Color(0xFF98747F),
            outlineVariant = Color(0xFF51343C),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF7A2238),
            primaryContainer = Color(0xFFFFD9DF),
            secondary = Color(0xFF81515D),
            secondaryContainer = Color(0xFFF8DDE2),
            tertiary = Color(0xFF8B6820),
            tertiaryContainer = Color(0xFFF9E7AF),
            background = Color(0xFFFCEEEF),
            surface = Color(0xFFFFF9F9),
            surfaceVariant = Color(0xFFEFDADF),
            outline = Color(0xFF8D6B75),
            outlineVariant = Color(0xFFD4BDC3),
        )
    }

    AppColorPalette.Amethyst -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFD2BCFF),
            primaryContainer = Color(0xFF51317F),
            secondary = Color(0xFFE0D1FF),
            secondaryContainer = Color(0xFF5A4678),
            tertiary = Color(0xFFE1C77A),
            tertiaryContainer = Color(0xFF554714),
            background = Color(0xFF120F17),
            surface = Color(0xFF1D1727),
            surfaceVariant = Color(0xFF2D2538),
            outline = Color(0xFF8E819E),
            outlineVariant = Color(0xFF493E55),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF6D43B3),
            primaryContainer = Color(0xFFE9DDFF),
            secondary = Color(0xFF745C99),
            secondaryContainer = Color(0xFFF0E8FF),
            tertiary = Color(0xFF896A1A),
            tertiaryContainer = Color(0xFFF8E8AF),
            background = Color(0xFFF4EEFF),
            surface = Color(0xFFFCF9FF),
            surfaceVariant = Color(0xFFE4DCEF),
            outline = Color(0xFF7F748D),
            outlineVariant = Color(0xFFC8BED1),
        )
    }

    AppColorPalette.Copper -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFFFB59D),
            primaryContainer = Color(0xFF7A381D),
            secondary = Color(0xFFECC3B4),
            secondaryContainer = Color(0xFF6B493D),
            tertiary = Color(0xFFF0C97E),
            tertiaryContainer = Color(0xFF5A4712),
            background = Color(0xFF170F0C),
            surface = Color(0xFF241813),
            surfaceVariant = Color(0xFF35241E),
            outline = Color(0xFF9E786A),
            outlineVariant = Color(0xFF51392F),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF9A4D2E),
            primaryContainer = Color(0xFFFFDBD0),
            secondary = Color(0xFF8A6254),
            secondaryContainer = Color(0xFFF8E0D8),
            tertiary = Color(0xFF8E681B),
            tertiaryContainer = Color(0xFFFCE8AF),
            background = Color(0xFFFDF0EA),
            surface = Color(0xFFFFFAF8),
            surfaceVariant = Color(0xFFF0DDD6),
            outline = Color(0xFF8B6C60),
            outlineVariant = Color(0xFFD3BFB7),
        )
    }

    AppColorPalette.Slate -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFB0C4CC),
            primaryContainer = Color(0xFF30464F),
            secondary = Color(0xFFC7D6DB),
            secondaryContainer = Color(0xFF465A62),
            tertiary = Color(0xFFD5C37B),
            tertiaryContainer = Color(0xFF514515),
            background = Color(0xFF0D1112),
            surface = Color(0xFF151A1C),
            surfaceVariant = Color(0xFF23292C),
            outline = Color(0xFF7B898E),
            outlineVariant = Color(0xFF3A4549),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF48636D),
            primaryContainer = Color(0xFFD8E7EC),
            secondary = Color(0xFF5F747C),
            secondaryContainer = Color(0xFFE0EBEF),
            tertiary = Color(0xFF816713),
            tertiaryContainer = Color(0xFFF6E7AE),
            background = Color(0xFFEEF4F6),
            surface = Color(0xFFFAFCFD),
            surfaceVariant = Color(0xFFDCE4E8),
            outline = Color(0xFF728186),
            outlineVariant = Color(0xFFBEC9CD),
        )
    }

    AppColorPalette.Sepia -> if (darkTheme) {
        PaletteTokens(
            primary = Color(0xFFD6B98B),
            primaryContainer = Color(0xFF5D4020),
            secondary = Color(0xFFE2CDA7),
            secondaryContainer = Color(0xFF6B563F),
            tertiary = Color(0xFFE2C176),
            tertiaryContainer = Color(0xFF594510),
            background = Color(0xFF16120E),
            surface = Color(0xFF221C16),
            surfaceVariant = Color(0xFF30271F),
            outline = Color(0xFF958272),
            outlineVariant = Color(0xFF4A3B31),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF7D5A33),
            primaryContainer = Color(0xFFF0DFC8),
            secondary = Color(0xFF8D7458),
            secondaryContainer = Color(0xFFF4E6D3),
            tertiary = Color(0xFF8B6718),
            tertiaryContainer = Color(0xFFF9E6A9),
            background = Color(0xFFF4EBDD),
            surface = Color(0xFFFCF8F1),
            surfaceVariant = Color(0xFFE6DCCE),
            outline = Color(0xFF847261),
            outlineVariant = Color(0xFFCBBEAE),
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
