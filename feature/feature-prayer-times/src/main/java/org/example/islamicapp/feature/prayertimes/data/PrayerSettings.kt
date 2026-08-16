package org.example.islamicapp.feature.prayertimes.data

import org.example.islamicapp.feature.prayertimes.domain.AdhanSoundOption
import org.example.islamicapp.core.prayer.AsrMethod
import org.example.islamicapp.core.prayer.CalculationMethod
import org.example.islamicapp.core.prayer.HighLatitudeRule
import org.example.islamicapp.core.prayer.Prayer
import org.example.islamicapp.core.prayer.PrayerAdjustments

/**
 * Immutable snapshot of all prayer-related user settings, persisted in
 * DataStore (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة").
 */
data class PrayerSettings(
    val method: CalculationMethod = CalculationMethod.MuslimWorldLeague,
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
    /** Master adhan playback volume, 0..100. */
    val adhanVolume: Int = 100,
    /** Minutes before the prayer to remind; 0 disables the reminder. */
    val reminderMinutes: Int = 10,
    /** App-wide manual Hijri day adjustment (±). */
    val hijriAdjustment: Int = 0,
)

/** The user's chosen location (city, GPS fix, or manual coordinates). */
data class SelectedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
)
