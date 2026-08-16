package org.example.islamicapp.core.common.prayer

/**
 * Per-prayer adhan behaviour (PROJECT_PROMPT.md §6 Phase 1: اختيار صوت أذان
 * مختلف — أو صامت/اهتزاز فقط — لكل صلاة على حدة).
 *
 * The model is data-driven: a future downloadable sound library plugs into
 * the same field (e.g. a `soundId`), while these three modes cover the
 * currently-available behaviour.
 */
enum class AdhanSoundOption {
    /** Play the selected adhan sound; fall back to vibration when none ships. */
    Default,

    /** Vibrate only — no sound. */
    VibrateOnly,

    /** Completely silent (the reminder notification still fires). */
    Silent,
}

/**
 * Pure decision of what an alarm should do, kept free of Android types so it
 * is unit-testable (PROJECT_PROMPT.md §3.7).
 */
object AdhanPlaybackPlan {

    data class Plan(
        val playSound: Boolean,
        val vibrate: Boolean,
    )

    fun plan(
        option: AdhanSoundOption,
        hasBundledSound: Boolean,
        vibrationEnabled: Boolean,
    ): Plan = when (option) {
        AdhanSoundOption.Silent -> Plan(playSound = false, vibrate = false)
        AdhanSoundOption.VibrateOnly -> Plan(playSound = false, vibrate = vibrationEnabled)
        AdhanSoundOption.Default ->
            if (hasBundledSound) Plan(playSound = true, vibrate = false)
            else Plan(playSound = false, vibrate = vibrationEnabled)
    }
}
