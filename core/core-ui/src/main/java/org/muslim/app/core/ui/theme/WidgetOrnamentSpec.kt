package org.muslim.app.core.ui.theme

import androidx.annotation.DrawableRes
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.ui.R

/**
 * Lightweight ornament descriptor for RemoteViews/Glance surfaces that cannot
 * consume the Compose [LocalIslamicDecoration] composition local.
 */
data class WidgetOrnamentSpec(
    @DrawableRes val drawableRes: Int,
    val tintAlpha: Float,
)

/** Maps the app-wide appearance preference to the same visual family used by Compose. */
fun widgetOrnamentSpec(
    style: AppOrnamentStyle,
    intensity: OrnamentIntensity,
): WidgetOrnamentSpec? {
    if (intensity == OrnamentIntensity.Off) return null

    val drawable = when (style) {
        AppOrnamentStyle.Geometry -> R.drawable.ic_ornament_geometric_12
        AppOrnamentStyle.Arabesque -> R.drawable.ic_ornament_arabesque
        AppOrnamentStyle.Stars -> R.drawable.ic_ornament_star_12
        AppOrnamentStyle.Andalusian -> R.drawable.ic_ornament_geometric_8
        AppOrnamentStyle.Mashrabiya -> R.drawable.ic_ornament_geometric_12
        AppOrnamentStyle.Ottoman -> R.drawable.ic_ornament_arabesque
        AppOrnamentStyle.Mushaf -> R.drawable.ic_ornament_mushaf_divider
        AppOrnamentStyle.Royal -> R.drawable.ic_ornament_star_12
        AppOrnamentStyle.Minimal -> R.drawable.ic_ornament_corner
    }
    val alpha = when (intensity) {
        OrnamentIntensity.Off -> 0f
        OrnamentIntensity.Subtle -> 0.24f
        OrnamentIntensity.Balanced -> 0.36f
        OrnamentIntensity.Rich -> 0.48f
    }
    return WidgetOrnamentSpec(drawableRes = drawable, tintAlpha = alpha)
}
