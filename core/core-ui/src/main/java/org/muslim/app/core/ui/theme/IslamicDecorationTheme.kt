package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
