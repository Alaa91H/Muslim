package org.muslim.app.core.datastore.prayer

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.AsrMethod
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerAdjustments
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prayerSettingsDataStore by preferencesDataStore(name = "prayer_settings")

/**
 * Persists [PrayerSettings] in a DataStore on the device. No data ever leaves
 * the device (privacy-first principle).
 */
@Singleton
class PrayerSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val settings: Flow<PrayerSettings> = context.prayerSettingsDataStore.data.map { prefs ->
        PrayerSettings(
            method = enumOr(prefs[Keys.METHOD], CalculationMethod.MuslimWorldLeague),
            customFajrAngle = prefs[Keys.CUSTOM_FAJR]?.toDouble() ?: 18.0,
            customIshaAngle = prefs[Keys.CUSTOM_ISHA]?.toDouble() ?: 17.0,
            asrMethod = enumOr(prefs[Keys.ASR], AsrMethod.Standard),
            highLatitudeRule = prefs[Keys.HIGH_LAT]?.let { runCatching { HighLatitudeRule.valueOf(it) }.getOrNull() },
            adjustments = PrayerAdjustments(
                fajr = prefs[Keys.ADJ_FAJR] ?: 0,
                sunrise = prefs[Keys.ADJ_SUNRISE] ?: 0,
                dhuhr = prefs[Keys.ADJ_DHUHR] ?: 0,
                asr = prefs[Keys.ADJ_ASR] ?: 0,
                maghrib = prefs[Keys.ADJ_MAGHRIB] ?: 0,
                isha = prefs[Keys.ADJ_ISHA] ?: 0,
            ),
            location = SelectedLocation(
                name = prefs[Keys.LOCATION_NAME] ?: "",
                latitude = prefs[Keys.LOCATION_LAT] ?: 0.0,
                longitude = prefs[Keys.LOCATION_LNG] ?: 0.0,
                timeZone = prefs[Keys.LOCATION_ZONE] ?: java.util.TimeZone.getDefault().id,
            ).takeIf { prefs[Keys.LOCATION_NAME] != null },
            adhanEnabled = prefs[Keys.ADHAN_ENABLED] ?: true,
            vibrateEnabled = prefs[Keys.VIBRATE_ENABLED] ?: true,
            adhanSounds = Prayer.entries
                .mapNotNull { prayer ->
                    prefs[Keys.soundFor(prayer)]?.let { name ->
                        runCatching { AdhanSoundOption.valueOf(name) }.getOrNull()?.let { prayer to it }
                    }
                }
                .toMap(),
            adhanSoundFiles = Prayer.entries
                .mapNotNull { prayer ->
                    prefs[Keys.soundFileFor(prayer)]?.let { prayer to it }
                }
                .toMap(),
            adhanVolume = prefs[Keys.ADHAN_VOLUME] ?: 100,
            reminderMinutes = prefs[Keys.REMINDER_MINUTES] ?: 10,
            dndEnabled = prefs[Keys.DND_ENABLED] ?: false,
            dndDurationMinutes = (prefs[Keys.DND_DURATION] ?: 10).coerceIn(1, 180),
            hijriAdjustment = prefs[Keys.HIJRI_ADJUSTMENT] ?: 0,
        )
    }

    /** Persists a full settings snapshot. */
    suspend fun save(newSettings: PrayerSettings) {
        context.prayerSettingsDataStore.edit { prefs ->
            prefs[Keys.METHOD] = newSettings.method.name
            prefs[Keys.CUSTOM_FAJR] = newSettings.customFajrAngle.toFloat()
            prefs[Keys.CUSTOM_ISHA] = newSettings.customIshaAngle.toFloat()
            prefs[Keys.ASR] = newSettings.asrMethod.name
            if (newSettings.highLatitudeRule != null) {
                prefs[Keys.HIGH_LAT] = newSettings.highLatitudeRule.name
            } else {
                prefs.remove(Keys.HIGH_LAT)
            }
            prefs[Keys.ADJ_FAJR] = newSettings.adjustments[Prayer.Fajr]
            prefs[Keys.ADJ_SUNRISE] = newSettings.adjustments[Prayer.Sunrise]
            prefs[Keys.ADJ_DHUHR] = newSettings.adjustments[Prayer.Dhuhr]
            prefs[Keys.ADJ_ASR] = newSettings.adjustments[Prayer.Asr]
            prefs[Keys.ADJ_MAGHRIB] = newSettings.adjustments[Prayer.Maghrib]
            prefs[Keys.ADJ_ISHA] = newSettings.adjustments[Prayer.Isha]
            val location = newSettings.location
            if (location != null) {
                prefs[Keys.LOCATION_NAME] = location.name
                prefs[Keys.LOCATION_LAT] = location.latitude
                prefs[Keys.LOCATION_LNG] = location.longitude
                prefs[Keys.LOCATION_ZONE] = location.timeZone
            } else {
                prefs.remove(Keys.LOCATION_NAME)
                prefs.remove(Keys.LOCATION_LAT)
                prefs.remove(Keys.LOCATION_LNG)
                prefs.remove(Keys.LOCATION_ZONE)
            }
            prefs[Keys.ADHAN_ENABLED] = newSettings.adhanEnabled
            prefs[Keys.VIBRATE_ENABLED] = newSettings.vibrateEnabled
            Prayer.entries.forEach { prayer ->
                prefs[Keys.soundFor(prayer)] = newSettings.adhanSounds[prayer]?.name ?: AdhanSoundOption.Default.name
                val file = newSettings.adhanSoundFiles[prayer]
                if (file != null) {
                    prefs[Keys.soundFileFor(prayer)] = file
                } else {
                    prefs.remove(Keys.soundFileFor(prayer))
                }
            }
            prefs[Keys.ADHAN_VOLUME] = newSettings.adhanVolume
            prefs[Keys.REMINDER_MINUTES] = newSettings.reminderMinutes
            prefs[Keys.DND_ENABLED] = newSettings.dndEnabled
            prefs[Keys.DND_DURATION] = newSettings.dndDurationMinutes
            prefs[Keys.HIJRI_ADJUSTMENT] = newSettings.hijriAdjustment
        }
    }

    /** Applies a small mutation to the current settings and persists it. */
    suspend fun update(transform: (PrayerSettings) -> PrayerSettings) {
        save(transform(settings.first()))
    }

    private fun <T : Enum<T>> enumOr(value: String?, default: T): T =
        value?.let { v -> default::class.java.enumConstants?.firstOrNull { it.name == v } } ?: default

    private object Keys {
        val METHOD = stringPreferencesKey("calculation_method")
        val CUSTOM_FAJR = floatPreferencesKey("custom_fajr_angle")
        val CUSTOM_ISHA = floatPreferencesKey("custom_isha_angle")
        val ASR = stringPreferencesKey("asr_method")
        val HIGH_LAT = stringPreferencesKey("high_latitude_rule")
        val ADJ_FAJR = intPreferencesKey("adjustment_fajr")
        val ADJ_SUNRISE = intPreferencesKey("adjustment_sunrise")
        val ADJ_DHUHR = intPreferencesKey("adjustment_dhuhr")
        val ADJ_ASR = intPreferencesKey("adjustment_asr")
        val ADJ_MAGHRIB = intPreferencesKey("adjustment_maghrib")
        val ADJ_ISHA = intPreferencesKey("adjustment_isha")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val LOCATION_LAT = doublePreferencesKey("location_latitude")
        val LOCATION_LNG = doublePreferencesKey("location_longitude")
        val LOCATION_ZONE = stringPreferencesKey("location_timezone")
        val ADHAN_ENABLED = booleanPreferencesKey("adhan_enabled")
        val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        val ADHAN_VOLUME = intPreferencesKey("adhan_volume")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val DND_DURATION = intPreferencesKey("dnd_duration_minutes")
        val HIJRI_ADJUSTMENT = intPreferencesKey("hijri_adjustment")

        fun soundFor(prayer: Prayer): Preferences.Key<String> =
            stringPreferencesKey("adhan_sound_${prayer.name.lowercase()}")

        fun soundFileFor(prayer: Prayer): Preferences.Key<String> =
            stringPreferencesKey("adhan_sound_file_${prayer.name.lowercase()}")
    }
}
