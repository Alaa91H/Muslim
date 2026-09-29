package org.muslim.app.core.designsystem

import androidx.compose.ui.unit.dp

/** Central spacing scale used by components and screen layouts. */
object IslamicSpacing {
    val XXSmall = 2.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Compact = 12.dp
    val Medium = 16.dp
    val Comfortable = 20.dp
    val Large = 24.dp
    val Section = 32.dp
    val XLarge = 40.dp
    val XXLarge = 48.dp
    val Hero = 64.dp

    /** Default horizontal page gutter for compact phone layouts. */
    val PageHorizontal = Comfortable
    /** Default vertical gap between independent content sections. */
    val SectionVertical = Large
    /** Gap between a screen hero and the first ordinary content section. */
    val HeroToContent = Comfortable
    /** Standard gap between closely related interactive controls. */
    val ControlGap = Small
}

/** Compatibility aliases for existing feature modules. */
object MuslimSpacing {
    val XSmall = IslamicSpacing.XSmall
    val Small = IslamicSpacing.Small
    val Medium = IslamicSpacing.Medium
    val Large = IslamicSpacing.Large
    val XLarge = IslamicSpacing.XLarge
}

/** Icon scale for navigational, supporting and prominent actions. */
object IslamicIconSize {
    val Supporting = 18.dp
    val Standard = 24.dp
    val Prominent = 32.dp
    val Hero = 48.dp
}

/** Calm surfaces favour generous corners and subtle outlines over heavy shadows.
 *  Card 20 dp / Large 24 dp give the app its quiet, premium roundness without
 *  feeling bubbly; Small 12 dp is reserved for chips and markers.
 */
object IslamicRadius {
    val Small = 12.dp
    val AyahMarker = 16.dp
    val Card = 20.dp
    val Large = 24.dp
    val Pill = 100.dp
}

/** Elevation values are intentionally restrained for a reading-first interface. */
object IslamicElevation {
    val None = 0.dp
    val Resting = 1.dp
    val Raised = 3.dp
    val Floating = 6.dp
}

/** Motion durations for small, non-distracting state changes. */
object IslamicMotion {
    const val FastMillis = 150
    const val StandardMillis = 200
    const val EmphasisMillis = 250
    const val EnterExitMillis = 300
}

/** Minimum touch target per accessibility guidance. */
object MuslimTouchTarget {
    val Min = 48.dp
}


/** Semantic window-width classes used by the adaptive UI foundation. */
enum class MuslimWindowWidthClass {
    Compact,
    Medium,
    Expanded,
}

/**
 * Shared layout thresholds and readable-width bounds.
 *
 * Feature screens must not invent their own phone/tablet breakpoints. Specialized
 * drawing geometry (Qibla compass, Quran glyph/highlight metrics, maps, charts)
 * remains feature-owned and is intentionally outside this object.
 */
object IslamicLayout {
    val CompactMaxWidth = 599.dp
    val MediumMinWidth = 600.dp
    val MediumMaxWidth = 839.dp
    val ExpandedMinWidth = 840.dp

    /** Reading/task content should normally remain bounded to this width. */
    val ReadableContentMaxWidth = 760.dp

    /** Multi-pane library/dashboard layouts may use this wider bound. */
    val WideContentMaxWidth = 1200.dp

    val PaneGap = IslamicSpacing.Large

    fun widthClass(width: androidx.compose.ui.unit.Dp): MuslimWindowWidthClass = when {
        width <= CompactMaxWidth -> MuslimWindowWidthClass.Compact
        width <= MediumMaxWidth -> MuslimWindowWidthClass.Medium
        else -> MuslimWindowWidthClass.Expanded
    }
}

/** Shared border/stroke weights for ordinary app surfaces and selection states. */
object IslamicStroke {
    val Standard = 1.dp
    val Selected = 2.dp
    val Emphasis = 3.dp
}

/**
 * Shared content-emphasis alpha values.
 *
 * These are for hierarchy only. Disabled/accessibility semantics still belong to
 * the owning Material control and must not be communicated by opacity alone.
 */
object IslamicContentAlpha {
    const val Full = 1f
    const val Secondary = 0.78f
    const val Muted = 0.60f
    const val Disabled = 0.38f
    const val SubtleDecoration = 0.12f
}
