package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity

/**
 * App-wide Islamic decoration preference exposed to every Compose surface.
 *
 * Religious text is never used as decoration. The engine is intentionally
 * vector-only and draws only a handful of static motifs to keep scrolling and
 * recomposition cheap on low-end devices.
 */
@Immutable
data class IslamicDecorationPreferences(
    val style: AppOrnamentStyle = AppOrnamentStyle.Geometry,
    val intensity: OrnamentIntensity = OrnamentIntensity.Balanced,
    val darkTheme: Boolean = false,
)

val LocalIslamicDecoration = staticCompositionLocalOf { IslamicDecorationPreferences() }

private data class IslamicDecorationSpec(
    val primary: IslamicOrnament,
    val secondary: IslamicOrnament,
    val primarySize: Dp,
    val secondarySize: Dp,
)

private fun AppOrnamentStyle.spec(): IslamicDecorationSpec = when (this) {
    AppOrnamentStyle.Geometry -> IslamicDecorationSpec(
        IslamicOrnament.Geometric12,
        IslamicOrnament.Geometric8,
        260.dp,
        190.dp,
    )
    AppOrnamentStyle.Arabesque -> IslamicDecorationSpec(
        IslamicOrnament.Arabesque,
        IslamicOrnament.Corner,
        280.dp,
        180.dp,
    )
    AppOrnamentStyle.Stars -> IslamicDecorationSpec(
        IslamicOrnament.Star12,
        IslamicOrnament.Star8,
        250.dp,
        180.dp,
    )
    AppOrnamentStyle.Andalusian -> IslamicDecorationSpec(
        IslamicOrnament.Geometric8,
        IslamicOrnament.Corner,
        275.dp,
        190.dp,
    )
    AppOrnamentStyle.Mashrabiya -> IslamicDecorationSpec(
        IslamicOrnament.Geometric12,
        IslamicOrnament.Geometric8,
        300.dp,
        210.dp,
    )
    AppOrnamentStyle.Ottoman -> IslamicDecorationSpec(
        IslamicOrnament.Arabesque,
        IslamicOrnament.Star8,
        285.dp,
        185.dp,
    )
    AppOrnamentStyle.Mushaf -> IslamicDecorationSpec(
        IslamicOrnament.SurahHeader,
        IslamicOrnament.MushafDivider,
        300.dp,
        240.dp,
    )
    AppOrnamentStyle.Royal -> IslamicDecorationSpec(
        IslamicOrnament.Star12,
        IslamicOrnament.Corner,
        290.dp,
        200.dp,
    )
    AppOrnamentStyle.Minimal -> IslamicDecorationSpec(
        IslamicOrnament.Corner,
        IslamicOrnament.MushafDivider,
        180.dp,
        200.dp,
    )
}

private fun OrnamentIntensity.alpha(darkTheme: Boolean): Float = when (this) {
    OrnamentIntensity.Off -> 0f
    OrnamentIntensity.Subtle -> if (darkTheme) 0.038f else 0.044f
    OrnamentIntensity.Balanced -> if (darkTheme) 0.060f else 0.070f
    OrnamentIntensity.Rich -> if (darkTheme) 0.088f else 0.100f
}

/**
 * Lightweight decorative layer intended to sit behind ordinary app content.
 * It deliberately renders at most three vector motifs.
 */
@Composable
fun IslamicDecorationLayer(
    modifier: Modifier = Modifier,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.spec()
    val alpha = preferences.intensity.alpha(preferences.darkTheme)
    val tint = when (preferences.style) {
        AppOrnamentStyle.Mushaf,
        AppOrnamentStyle.Royal,
        AppOrnamentStyle.Ottoman,
        -> MaterialTheme.colorScheme.tertiary

        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        IslamicOrnamentImage(
            ornament = spec.primary,
            tint = tint,
            alpha = alpha,
            modifier = Modifier
                .size(spec.primarySize)
                .align(Alignment.TopEnd),
        )

        if (preferences.style != AppOrnamentStyle.Minimal) {
            IslamicOrnamentImage(
                ornament = spec.secondary,
                tint = tint,
                alpha = alpha * 0.80f,
                modifier = Modifier
                    .size(spec.secondarySize)
                    .align(Alignment.BottomStart),
            )
        }

        if (preferences.intensity == OrnamentIntensity.Rich) {
            IslamicOrnamentImage(
                ornament = spec.secondary,
                tint = tint,
                alpha = alpha * 0.55f,
                modifier = Modifier
                    .size(spec.secondarySize * 0.72f)
                    .align(Alignment.CenterEnd),
            )
        }
    }
}

/**
 * Compact live preview used by Appearance settings. It reuses the exact same
 * renderer as the app background, so the chooser never lies about the result.
 */
@Composable
fun IslamicDecorationPreview(
    style: AppOrnamentStyle,
    intensity: OrnamentIntensity,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val current = LocalIslamicDecoration.current
    val border = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = border,
    ) {
        CompositionLocalProvider(
            LocalIslamicDecoration provides current.copy(
                style = style,
                intensity = intensity,
            ),
        ) {
            IslamicDecorationLayer()
        }
    }
}


internal fun OrnamentIntensity.featureAlpha(darkTheme: Boolean): Float = when (this) {
    OrnamentIntensity.Off -> 0f
    OrnamentIntensity.Subtle -> if (darkTheme) 0.060f else 0.070f
    OrnamentIntensity.Balanced -> if (darkTheme) 0.090f else 0.105f
    OrnamentIntensity.Rich -> if (darkTheme) 0.125f else 0.145f
}

internal fun AppOrnamentStyle.sectionOrnament(): IslamicOrnament = when (this) {
    AppOrnamentStyle.Geometry -> IslamicOrnament.Geometric12
    AppOrnamentStyle.Arabesque -> IslamicOrnament.Arabesque
    AppOrnamentStyle.Stars -> IslamicOrnament.Star12
    AppOrnamentStyle.Andalusian -> IslamicOrnament.Geometric8
    AppOrnamentStyle.Mashrabiya -> IslamicOrnament.Geometric12
    AppOrnamentStyle.Ottoman -> IslamicOrnament.Arabesque
    AppOrnamentStyle.Mushaf -> IslamicOrnament.SurahHeader
    AppOrnamentStyle.Royal -> IslamicOrnament.Star12
    AppOrnamentStyle.Minimal -> IslamicOrnament.Corner
}

internal fun AppOrnamentStyle.dividerOrnament(): IslamicOrnament = when (this) {
    AppOrnamentStyle.Mushaf,
    AppOrnamentStyle.Minimal,
    -> IslamicOrnament.MushafDivider

    AppOrnamentStyle.Arabesque,
    AppOrnamentStyle.Ottoman,
    -> IslamicOrnament.Arabesque

    AppOrnamentStyle.Stars,
    AppOrnamentStyle.Royal,
    -> IslamicOrnament.Star8

    AppOrnamentStyle.Geometry,
    AppOrnamentStyle.Andalusian,
    AppOrnamentStyle.Mashrabiya,
    -> IslamicOrnament.Geometric8
}

internal fun AppOrnamentStyle.cornerOrnament(): IslamicOrnament = when (this) {
    AppOrnamentStyle.Geometry -> IslamicOrnament.Geometric8
    AppOrnamentStyle.Stars -> IslamicOrnament.Star8
    AppOrnamentStyle.Mashrabiya -> IslamicOrnament.Geometric12
    AppOrnamentStyle.Ottoman -> IslamicOrnament.Arabesque
    AppOrnamentStyle.Arabesque,
    AppOrnamentStyle.Andalusian,
    AppOrnamentStyle.Mushaf,
    AppOrnamentStyle.Royal,
    AppOrnamentStyle.Minimal,
    -> IslamicOrnament.Corner
}

/**
 * A short decorative band for feature headers. It follows the globally
 * selected ornament and intensity and disappears completely when decoration
 * is disabled.
 */
@Composable
fun IslamicDecorationBand(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    compact: Boolean = false,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    IslamicOrnamentImage(
        ornament = preferences.style.sectionOrnament(),
        tint = tint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme),
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 56.dp else 84.dp),
    )
}

/** A low-profile ornamental divider for section transitions. */
@Composable
fun IslamicDecorationDivider(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.tertiary,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    IslamicOrnamentImage(
        ornament = preferences.style.dividerOrnament(),
        tint = tint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme) * 0.82f,
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp),
    )
}

/**
 * Mirrored decorative anchors for hero cards and major grouped surfaces.
 * The motifs remain non-semantic and are intentionally bounded to two vectors.
 */
@Composable
fun IslamicDecorationCorners(
    tint: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val ornament = preferences.style.cornerOrnament()
    val alpha = preferences.intensity.featureAlpha(preferences.darkTheme)
    val ornamentSize = if (compact) 52.dp else 68.dp

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        IslamicOrnamentImage(
            ornament = ornament,
            tint = tint,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(ornamentSize),
        )
        IslamicOrnamentImage(
            ornament = ornament,
            tint = tint,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(ornamentSize)
                .graphicsLayer(rotationZ = 180f),
        )
    }
}
