package org.muslim.app.core.datastore.prayer

import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.AsrMethod
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerAdjustments

/**
 * Immutable snapshot of all prayer-related user settings, persisted in
 * DataStore (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة").
 */
data class PrayerSettings(
    val method: CalculationMethod = CalculationMethod.MuslimWorldLeague,
    /**
     * True once the user picked a calculation method themselves — the
     * region-based auto-suggestion then never overrides their choice.
     */
    val methodChosenManually: Boolean = false,
    /** Fajr/Isha angles when [method] is [CalculationMethod.Custom]. */
    val customFajrAngle: Double = 18.0,
    val customIshaAngle: Double = 17.0,
    val asrMethod: AsrMethod = AsrMethod.Standard,
    /** null = automatic (recommended for the latitude). */
    val highLatitudeRule: HighLatitudeRule? = null,
    /** Manual per-prayer offsets in minutes. */
    val adjustments: PrayerAdjustments = PrayerAdjustments(),
    val location: SelectedLocation? = null,
    val adhanEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    /** Per-prayer adhan mode; absent keys behave as [AdhanSoundOption.Default]. */
    val adhanSounds: Map<Prayer, AdhanSoundOption> = emptyMap(),
    /**
     * Per-prayer custom audio file path (user-picked or downloaded). When set,
     * it replaces the bundled synthesised tone for that prayer.
     */
    val adhanSoundFiles: Map<Prayer, String> = emptyMap(),
    /** Default adhan playback volume (0..100) used when a prayer has no override. */
    val adhanVolume: Int = 100,
    /**
     * Per-prayer volume override (0..100); absent keys fall back to
     * [adhanVolume], so every prayer is individually tunable.
     */
    val adhanVolumes: Map<Prayer, Int> = emptyMap(),
    /**
     * When true, a single master level ([adhanVolume]) applies to every
     * prayer and the per-prayer sliders are disabled in the UI.
     */
    val useGlobalAdhanVolume: Boolean = false,
    /**
     * Per-prayer bundled recording id (see
     * [org.muslim.app.core.common.prayer.BundledAdhanSound]); always
     * available offline since it ships inside the APK. Absent keys default to
     * Makkah for every prayer.
     */
    val bundledAdhanSounds: Map<Prayer, String> = emptyMap(),
    /**
     * Per-prayer vibration override; absent keys fall back to
     * [vibrateEnabled], so every prayer is individually tunable.
     */
    val vibratePerPrayer: Map<Prayer, Boolean> = emptyMap(),
    /** Minutes before the prayer to remind; 0 disables the reminder. */
    val reminderMinutes: Int = 10,
    /**
     * Silence notifications during the prayer (Do Not Disturb) after the
     * adhan, for [dndDurationMinutes] minutes.
     */
    val dndEnabled: Boolean = false,
    /** How long DND stays active after the adhan (default 10 minutes). */
    val dndDurationMinutes: Int = 10,
    /** App-wide manual Hijri day adjustment (±). */
    val hijriAdjustment: Int = 0,
) {
    /** Resolves the effective adhan volume for [prayer]. */
    fun adhanVolumeFor(prayer: Prayer): Int =
        if (useGlobalAdhanVolume) {
            adhanVolume
        } else {
            adhanVolumes[prayer]?.coerceIn(0, 100) ?: adhanVolume
        }

    /** Resolves whether vibration is enabled for [prayer]. */
    fun vibrateFor(prayer: Prayer): Boolean =
        vibratePerPrayer[prayer] ?: vibrateEnabled
}

/** The user's chosen location (city, GPS fix, or manual coordinates). */
data class SelectedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    val elevation: Double = 0.0,
)
