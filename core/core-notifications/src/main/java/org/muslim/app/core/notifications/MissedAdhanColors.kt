package org.muslim.app.core.notifications

/**
 * Palette for the missed-adhan line in the permanent countdown notification.
 * Colors are chosen (Material 600/700 shades) to stay readable on both the
 * light and the dark notification backgrounds. The user picks one in the
 * notification settings; the value is stored as an ARGB int in DataStore.
 */
data class MissedAdhanColorOption(
    val id: String,
    val argb: Int,
)

object MissedAdhanColors {

    /** Default line color: red ([OPTIONS] first entry). */
    const val DEFAULT = 0xFFE53935.toInt()

    val OPTIONS: List<MissedAdhanColorOption> = listOf(
        MissedAdhanColorOption("red", 0xFFE53935.toInt()),
        MissedAdhanColorOption("orange", 0xFFFB8C00.toInt()),
        MissedAdhanColorOption("amber", 0xFFF9A825.toInt()),
        MissedAdhanColorOption("green", 0xFF43A047.toInt()),
        MissedAdhanColorOption("blue", 0xFF1E88E5.toInt()),
        MissedAdhanColorOption("purple", 0xFF8E24AA.toInt()),
        MissedAdhanColorOption("pink", 0xFFD81B60.toInt()),
        MissedAdhanColorOption("cyan", 0xFF00ACC1.toInt()),
    )

    fun byArgb(argb: Int): MissedAdhanColorOption? =
        OPTIONS.firstOrNull { it.argb == argb }
}
