package org.muslim.app.core.ui.theme

import androidx.annotation.DrawableRes
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity

/**
 * Lightweight ornament descriptor for RemoteViews/Glance surfaces that cannot
 * consume the Compose [LocalIslamicDecoration] composition local.
 */
data class WidgetOrnamentSpec(
    @DrawableRes val drawableRes: Int,
    val tintAlpha: Float,
)

/**
 * Maps the app-wide appearance preference to the exact same primary visual
 * identity used by Compose. Keeping this delegated to [decorationSpec] avoids
 * widgets silently falling back to duplicate legacy motifs.
 */
fun widgetOrnamentSpec(
    style: AppOrnamentStyle,
    intensity: OrnamentIntensity,
): WidgetOrnamentSpec? {
    if (intensity == OrnamentIntensity.Off) return null

    val alpha = when (intensity) {
        OrnamentIntensity.Off -> 0f
        OrnamentIntensity.Subtle -> 0.24f
        OrnamentIntensity.Balanced -> 0.36f
        OrnamentIntensity.Rich -> 0.48f
    }
    return WidgetOrnamentSpec(
        drawableRes = style.decorationSpec().primary.drawableRes,
        tintAlpha = alpha,
    )
}
