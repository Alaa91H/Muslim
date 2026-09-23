package org.muslim.app.core.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import org.muslim.app.core.ui.R

/**
 * Small reusable vector ornaments.
 *
 * Every app-level appearance style resolves to its own primary ornament so the
 * selector represents a genuinely different visual identity instead of merely
 * relabelling the same asset.
 */
enum class IslamicOrnament(@DrawableRes val drawableRes: Int) {
    Geometric8(R.drawable.ic_ornament_geometric_8),
    Geometric12(R.drawable.ic_ornament_geometric_12),
    Arabesque(R.drawable.ic_ornament_arabesque),
    Star8(R.drawable.ic_ornament_star_8),
    Star12(R.drawable.ic_ornament_star_12),
    Andalusian(R.drawable.ic_ornament_andalusian),
    Mashrabiya(R.drawable.ic_ornament_mashrabiya),
    Ottoman(R.drawable.ic_ornament_ottoman),
    Royal(R.drawable.ic_ornament_royal),
    Minimal(R.drawable.ic_ornament_minimal),
    MushafDivider(R.drawable.ic_ornament_mushaf_divider),
    SurahHeader(R.drawable.ic_ornament_surah_header),
    Corner(R.drawable.ic_ornament_corner),
}

/** Opacity ranges keep ornamental art subordinate to content and controls. */
object IslamicOrnamentOpacity {
    const val DarkBackground = 0.045f
    const val DarkSection = 0.08f
    const val DarkActive = 0.12f
    const val LightBackground = 0.055f
    const val LightSection = 0.095f
    const val LightActive = 0.12f
}

@Composable
fun IslamicOrnamentImage(
    ornament: IslamicOrnament,
    tint: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(ornament.drawableRes),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint.copy(alpha = alpha.coerceIn(0f, 1f))),
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}
