package org.muslim.app.core.designsystem

import androidx.compose.ui.unit.dp

/** Central spacing scale used by components and screen layouts. */
object IslamicSpacing {
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 40.dp
}

/** Compatibility aliases for existing feature modules. */
object MuslimSpacing {
    val XSmall = IslamicSpacing.XSmall
    val Small = IslamicSpacing.Small
    val Medium = IslamicSpacing.Medium
    val Large = IslamicSpacing.Large
    val XLarge = IslamicSpacing.XLarge
}

/** Calm surfaces favour generous corners and subtle outlines over heavy shadows. */
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
}

/** Minimum touch target per accessibility guidance. */
object MuslimTouchTarget {
    val Min = 48.dp
}
