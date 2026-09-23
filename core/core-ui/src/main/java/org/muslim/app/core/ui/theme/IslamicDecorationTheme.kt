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

internal enum class IslamicDecorationTintRole {
    Primary,
    Tertiary,
}

/**
 * One authoritative visual identity for every appearance ornament style.
 *
 * Keeping all roles in a single spec prevents the settings chooser, screen
 * backgrounds, section bands, dividers, corners and widgets from drifting
 * into unrelated motifs.
 */
@Immutable
internal data class IslamicDecorationSpec(
    val primary: IslamicOrnament,
    val secondary: IslamicOrnament,
    val section: IslamicOrnament,
    val divider: IslamicOrnament,
    val corner: IslamicOrnament,
    val primarySize: Dp,
    val secondarySize: Dp,
    val tintRole: IslamicDecorationTintRole,
    val secondaryRotation: Float = 0f,
)

internal fun AppOrnamentStyle.decorationSpec(): IslamicDecorationSpec = when (this) {
    AppOrnamentStyle.Geometry -> IslamicDecorationSpec(
        primary = IslamicOrnament.Geometric12,
        secondary = IslamicOrnament.Geometric8,
        section = IslamicOrnament.Geometric12,
        divider = IslamicOrnament.Geometric8,
        corner = IslamicOrnament.Geometric8,
        primarySize = 260.dp,
        secondarySize = 190.dp,
        tintRole = IslamicDecorationTintRole.Primary,
    )
    AppOrnamentStyle.Arabesque -> IslamicDecorationSpec(
        primary = IslamicOrnament.Arabesque,
        secondary = IslamicOrnament.Corner,
        section = IslamicOrnament.Arabesque,
        divider = IslamicOrnament.Arabesque,
        corner = IslamicOrnament.Corner,
        primarySize = 280.dp,
        secondarySize = 180.dp,
        tintRole = IslamicDecorationTintRole.Primary,
        secondaryRotation = 180f,
    )
    AppOrnamentStyle.Stars -> IslamicDecorationSpec(
        primary = IslamicOrnament.Star12,
        secondary = IslamicOrnament.Star8,
        section = IslamicOrnament.Star12,
        divider = IslamicOrnament.Star8,
        corner = IslamicOrnament.Star8,
        primarySize = 250.dp,
        secondarySize = 180.dp,
        tintRole = IslamicDecorationTintRole.Primary,
    )
    AppOrnamentStyle.Andalusian -> IslamicDecorationSpec(
        primary = IslamicOrnament.Andalusian,
        secondary = IslamicOrnament.Andalusian,
        section = IslamicOrnament.Andalusian,
        divider = IslamicOrnament.Andalusian,
        corner = IslamicOrnament.Andalusian,
        primarySize = 275.dp,
        secondarySize = 184.dp,
        tintRole = IslamicDecorationTintRole.Primary,
        secondaryRotation = 180f,
    )
    AppOrnamentStyle.Mashrabiya -> IslamicDecorationSpec(
        primary = IslamicOrnament.Mashrabiya,
        secondary = IslamicOrnament.Mashrabiya,
        section = IslamicOrnament.Mashrabiya,
        divider = IslamicOrnament.Mashrabiya,
        corner = IslamicOrnament.Mashrabiya,
        primarySize = 292.dp,
        secondarySize = 204.dp,
        tintRole = IslamicDecorationTintRole.Primary,
        secondaryRotation = 45f,
    )
    AppOrnamentStyle.Ottoman -> IslamicDecorationSpec(
        primary = IslamicOrnament.Ottoman,
        secondary = IslamicOrnament.Arabesque,
        section = IslamicOrnament.Ottoman,
        divider = IslamicOrnament.Arabesque,
        corner = IslamicOrnament.Ottoman,
        primarySize = 276.dp,
        secondarySize = 176.dp,
        tintRole = IslamicDecorationTintRole.Tertiary,
        secondaryRotation = 180f,
    )
    AppOrnamentStyle.Mushaf -> IslamicDecorationSpec(
        primary = IslamicOrnament.SurahHeader,
        secondary = IslamicOrnament.MushafDivider,
        section = IslamicOrnament.SurahHeader,
        divider = IslamicOrnament.MushafDivider,
        corner = IslamicOrnament.Corner,
        primarySize = 300.dp,
        secondarySize = 240.dp,
        tintRole = IslamicDecorationTintRole.Tertiary,
    )
    AppOrnamentStyle.Royal -> IslamicDecorationSpec(
        primary = IslamicOrnament.Royal,
        secondary = IslamicOrnament.Star8,
        section = IslamicOrnament.Royal,
        divider = IslamicOrnament.Royal,
        corner = IslamicOrnament.Royal,
        primarySize = 286.dp,
        secondarySize = 190.dp,
        tintRole = IslamicDecorationTintRole.Tertiary,
        secondaryRotation = 22.5f,
    )
    AppOrnamentStyle.Minimal -> IslamicDecorationSpec(
        primary = IslamicOrnament.Minimal,
        secondary = IslamicOrnament.Minimal,
        section = IslamicOrnament.Minimal,
        divider = IslamicOrnament.Minimal,
        corner = IslamicOrnament.Minimal,
        primarySize = 190.dp,
        secondarySize = 150.dp,
        tintRole = IslamicDecorationTintRole.Primary,
        secondaryRotation = 180f,
    )
}

internal fun OrnamentIntensity.backgroundAlpha(darkTheme: Boolean): Float = when (this) {
    OrnamentIntensity.Off -> 0f
    OrnamentIntensity.Subtle -> if (darkTheme) 0.038f else 0.044f
    OrnamentIntensity.Balanced -> if (darkTheme) 0.060f else 0.070f
    OrnamentIntensity.Rich -> if (darkTheme) 0.088f else 0.100f
}

/**
 * Preview opacity is intentionally stronger than the real background opacity:
 * thumbnails must communicate the motif clearly without making live content
 * visually noisy.
 */
internal fun OrnamentIntensity.previewAlpha(darkTheme: Boolean): Float = when (this) {
    OrnamentIntensity.Off -> 0f
    OrnamentIntensity.Subtle -> if (darkTheme) 0.18f else 0.20f
    OrnamentIntensity.Balanced -> if (darkTheme) 0.24f else 0.27f
    OrnamentIntensity.Rich -> if (darkTheme) 0.31f else 0.35f
}

/**
 * Lightweight decorative layer intended to sit behind ordinary app content.
 * It deliberately renders at most three vector motifs.
 */
@Composable
fun IslamicDecorationLayer(
    modifier: Modifier = Modifier,
    previewMode: Boolean = false,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.decorationSpec()
    val alpha = if (previewMode) {
        preferences.intensity.previewAlpha(preferences.darkTheme)
    } else {
        preferences.intensity.backgroundAlpha(preferences.darkTheme)
    }
    val tint = when (spec.tintRole) {
        IslamicDecorationTintRole.Primary -> MaterialTheme.colorScheme.primary
        IslamicDecorationTintRole.Tertiary -> MaterialTheme.colorScheme.tertiary
    }
    val sizeFactor = if (previewMode) 0.42f else 1f
    val primarySize = spec.primarySize * sizeFactor
    val secondarySize = spec.secondarySize * sizeFactor

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
                .size(primarySize)
                .align(Alignment.TopEnd),
        )

        if (preferences.style != AppOrnamentStyle.Minimal || previewMode) {
            IslamicOrnamentImage(
                ornament = spec.secondary,
                tint = tint,
                alpha = alpha * 0.80f,
                modifier = Modifier
                    .size(secondarySize)
                    .align(Alignment.BottomStart)
                    .graphicsLayer(rotationZ = spec.secondaryRotation),
            )
        }

        if (preferences.intensity == OrnamentIntensity.Rich && !previewMode) {
            IslamicOrnamentImage(
                ornament = spec.secondary,
                tint = tint,
                alpha = alpha * 0.55f,
                modifier = Modifier
                    .size(secondarySize * 0.72f)
                    .align(Alignment.CenterEnd)
                    .graphicsLayer(rotationZ = -spec.secondaryRotation),
            )
        }
    }
}

/**
 * Reader-specific decoration keeps a recognisable Mushaf identity while still
 * respecting the app-wide ornament intensity. Sacred text remains the focus:
 * these vectors are short header/divider accents and never form a text
 * background.
 */
@Composable
fun IslamicReadingHeaderDecoration(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.tertiary,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    IslamicOrnamentImage(
        ornament = IslamicOrnament.SurahHeader,
        tint = tint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme) * 0.72f,
        modifier = modifier.fillMaxWidth().height(18.dp),
    )
}

@Composable
fun IslamicReadingDivider(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.tertiary,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    IslamicOrnamentImage(
        ornament = IslamicOrnament.MushafDivider,
        tint = tint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme) * 0.78f,
        modifier = modifier.fillMaxWidth().height(14.dp),
    )
}

@Composable
fun IslamicReadingBasmalaAccent(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.tertiary,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    IslamicOrnamentImage(
        ornament = IslamicOrnament.Star8,
        tint = tint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme) * 0.86f,
        modifier = modifier.fillMaxWidth().height(18.dp),
    )
}

/**
 * Compact high-clarity preview used by Appearance settings. The same
 * decoration spec is reused, but preview opacity and scale are tuned so the
 * user can actually distinguish similar motifs before selecting them.
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
        modifier = modifier.height(96.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = border,
    ) {
        CompositionLocalProvider(
            LocalIslamicDecoration provides current.copy(
                style = style,
                intensity = intensity,
            ),
        ) {
            IslamicDecorationLayer(previewMode = true)
        }
    }
}

internal fun OrnamentIntensity.featureAlpha(darkTheme: Boolean): Float = when (this) {
    OrnamentIntensity.Off -> 0f
    OrnamentIntensity.Subtle -> if (darkTheme) 0.060f else 0.070f
    OrnamentIntensity.Balanced -> if (darkTheme) 0.090f else 0.105f
    OrnamentIntensity.Rich -> if (darkTheme) 0.125f else 0.145f
}

internal fun AppOrnamentStyle.sectionOrnament(): IslamicOrnament =
    decorationSpec().section

internal fun AppOrnamentStyle.dividerOrnament(): IslamicOrnament =
    decorationSpec().divider

internal fun AppOrnamentStyle.cornerOrnament(): IslamicOrnament =
    decorationSpec().corner

/**
 * Centered motif for circular worship controls such as the Qibla compass and
 * digital misbaha. It is intentionally a single vector so it remains cheap
 * while the surrounding sensor/counter UI updates frequently.
 */
@Composable
fun IslamicDecorationMedallion(
    modifier: Modifier = Modifier,
    tint: Color? = null,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.decorationSpec()
    val resolvedTint = tint ?: when (spec.tintRole) {
        IslamicDecorationTintRole.Primary -> MaterialTheme.colorScheme.primary
        IslamicDecorationTintRole.Tertiary -> MaterialTheme.colorScheme.tertiary
    }

    IslamicOrnamentImage(
        ornament = spec.section,
        tint = resolvedTint,
        alpha = preferences.intensity.featureAlpha(preferences.darkTheme) * 0.56f,
        modifier = modifier.fillMaxSize(),
    )
}

/**
 * A short decorative band for feature headers. It follows the globally
 * selected ornament and intensity and disappears completely when decoration
 * is disabled.
 */
@Composable
fun IslamicDecorationBand(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    compact: Boolean = false,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.decorationSpec()
    val resolvedTint = tint ?: when (spec.tintRole) {
        IslamicDecorationTintRole.Primary -> MaterialTheme.colorScheme.primary
        IslamicDecorationTintRole.Tertiary -> MaterialTheme.colorScheme.tertiary
    }

    IslamicOrnamentImage(
        ornament = spec.section,
        tint = resolvedTint,
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
    tint: Color? = null,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.decorationSpec()
    val resolvedTint = tint ?: when (spec.tintRole) {
        IslamicDecorationTintRole.Primary -> MaterialTheme.colorScheme.primary
        IslamicDecorationTintRole.Tertiary -> MaterialTheme.colorScheme.tertiary
    }

    IslamicOrnamentImage(
        ornament = spec.divider,
        tint = resolvedTint,
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
    tint: Color? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val preferences = LocalIslamicDecoration.current
    if (preferences.intensity == OrnamentIntensity.Off) return

    val spec = preferences.style.decorationSpec()
    val resolvedTint = tint ?: when (spec.tintRole) {
        IslamicDecorationTintRole.Primary -> MaterialTheme.colorScheme.primary
        IslamicDecorationTintRole.Tertiary -> MaterialTheme.colorScheme.tertiary
    }
    val alpha = preferences.intensity.featureAlpha(preferences.darkTheme)
    val ornamentSize = if (compact) 52.dp else 68.dp

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        IslamicOrnamentImage(
            ornament = spec.corner,
            tint = resolvedTint,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(ornamentSize),
        )
        IslamicOrnamentImage(
            ornament = spec.corner,
            tint = resolvedTint,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(ornamentSize)
                .graphicsLayer(rotationZ = 180f),
        )
    }
}
