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
        val methodChosenManually = prefs[Keys.METHOD_MANUAL] ?: false
        val storedMethod = enumOr(prefs[Keys.METHOD], CalculationMethod.MuslimWorldLeague)
        PrayerSettings(
            // Pre-v1.25.9 automatic mode was country-derived. Preserve an
            // explicit historical choice but migrate the automatic path to
            // the documented global MWL baseline.
            method = storedMethod.takeIf { methodChosenManually }
                ?: CalculationMethod.MuslimWorldLeague,
            methodChosenManually = methodChosenManually,
            customFajrAngle = prefs[Keys.CUSTOM_FAJR]?.toDouble() ?: 18.0,
            customIshaAngle = prefs[Keys.CUSTOM_ISHA]?.toDouble() ?: 17.0,
            asrMethod = enumOr(prefs[Keys.ASR], AsrMethod.Standard),
            // Older installs did not persist a high-latitude rule. Migrate a
            // missing or invalid legacy value to the documented global default
            // instead of retaining location-dependent automatic behaviour.
            highLatitudeRule = prefs[Keys.HIGH_LAT]
                ?.let { runCatching { HighLatitudeRule.valueOf(it) }.getOrNull() }
                ?: HighLatitudeRule.SeventhOfTheNight,
            adjustments = PrayerAdjustments(
                fajr = prefs[Keys.ADJ_FAJR] ?: 0,
                sunrise = prefs[Keys.ADJ_SUNRISE] ?: 0,
                dhuhr = prefs[Keys.ADJ_DHUHR] ?: 0,
                asr = prefs[Keys.ADJ_ASR] ?: 0,
                maghrib = prefs[Keys.ADJ_MAGHRIB] ?: 0,
                isha = prefs[Keys.ADJ_ISHA] ?: 0,
            ),
            // A location without its own validated IANA zone cannot provide a
            // trustworthy timetable. Treat an incomplete legacy record as no
            // location and let the user select/save it again rather than
            // assigning the current device zone to arbitrary coordinates.
            location = prefs[Keys.LOCATION_ZONE]
                ?.takeIf { it.isNotBlank() }
                ?.let { timeZone ->
                    SelectedLocation(
                        name = prefs[Keys.LOCATION_NAME] ?: "",
                        latitude = prefs[Keys.LOCATION_LAT] ?: 0.0,
                        longitude = prefs[Keys.LOCATION_LNG] ?: 0.0,
                        timeZone = timeZone,
                        elevation = prefs[Keys.LOCATION_ELEVATION] ?: 0.0,
                    )
                }
                ?.takeIf { prefs[Keys.LOCATION_NAME] != null },
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
            useGlobalAdhanVolume = prefs[Keys.USE_GLOBAL_ADHAN_VOLUME] ?: false,
            adhanVolumes = Prayer.entries
                .mapNotNull { prayer ->
                    prefs[Keys.adhanVolumeFor(prayer)]?.let { prayer to it.coerceIn(0, 100) }
                }
                .toMap(),
            vibratePerPrayer = Prayer.entries
                .mapNotNull { prayer ->
                    prefs[Keys.vibrateFor(prayer)]?.let { prayer to it }
                }
                .toMap(),
            bundledAdhanSounds = Prayer.entries
                .mapNotNull { prayer ->
                    prefs[Keys.bundledSoundFor(prayer)]?.let { prayer to it }
                }
                .toMap(),
            reminderMinutes = prefs[Keys.REMINDER_MINUTES] ?: 15,
            adhanNotificationDismissible = prefs[Keys.ADHAN_NOTIFICATION_DISMISSIBLE] ?: false,
            stopAdhanOnNotificationDismiss = prefs[Keys.STOP_ADHAN_ON_NOTIFICATION_DISMISS] ?: false,
            dndEnabled = prefs[Keys.DND_ENABLED] ?: false,
            dndDurationMinutes = (prefs[Keys.DND_DURATION] ?: 10).coerceIn(1, 180),
            hijriAdjustment = prefs[Keys.HIJRI_ADJUSTMENT] ?: 0,
        )
    }

    /** Persists a full settings snapshot. */
    suspend fun save(newSettings: PrayerSettings) {
        context.prayerSettingsDataStore.edit { prefs ->
            prefs[Keys.METHOD] = newSettings.method.name
            prefs[Keys.METHOD_MANUAL] = newSettings.methodChosenManually
            prefs[Keys.CUSTOM_FAJR] = newSettings.customFajrAngle.toFloat()
            prefs[Keys.CUSTOM_ISHA] = newSettings.customIshaAngle.toFloat()
            prefs[Keys.ASR] = newSettings.asrMethod.name
            prefs[Keys.HIGH_LAT] = newSettings.highLatitudeRule.name
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
                prefs[Keys.LOCATION_ELEVATION] = location.elevation
            } else {
                prefs.remove(Keys.LOCATION_NAME)
                prefs.remove(Keys.LOCATION_LAT)
                prefs.remove(Keys.LOCATION_LNG)
                prefs.remove(Keys.LOCATION_ZONE)
                prefs.remove(Keys.LOCATION_ELEVATION)
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
            prefs[Keys.USE_GLOBAL_ADHAN_VOLUME] = newSettings.useGlobalAdhanVolume
            Prayer.entries.forEach { prayer ->
                val volume = newSettings.adhanVolumes[prayer]
                if (volume != null) {
                    prefs[Keys.adhanVolumeFor(prayer)] = volume.coerceIn(0, 100)
                } else {
                    prefs.remove(Keys.adhanVolumeFor(prayer))
                }
                val vibrate = newSettings.vibratePerPrayer[prayer]
                if (vibrate != null) {
                    prefs[Keys.vibrateFor(prayer)] = vibrate
                } else {
                    prefs.remove(Keys.vibrateFor(prayer))
                }
            }
            Prayer.entries.forEach { prayer ->
                prefs[Keys.bundledSoundFor(prayer)] =
                    newSettings.bundledAdhanSounds[prayer] ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID
            }
            prefs[Keys.REMINDER_MINUTES] = newSettings.reminderMinutes
            prefs[Keys.ADHAN_NOTIFICATION_DISMISSIBLE] = newSettings.adhanNotificationDismissible
            prefs[Keys.STOP_ADHAN_ON_NOTIFICATION_DISMISS] = newSettings.stopAdhanOnNotificationDismiss
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
        val METHOD_MANUAL = booleanPreferencesKey("method_chosen_manually")
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
        val LOCATION_ELEVATION = doublePreferencesKey("location_elevation")
        val ADHAN_ENABLED = booleanPreferencesKey("adhan_enabled")
        val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        val ADHAN_VOLUME = intPreferencesKey("adhan_volume")
        val USE_GLOBAL_ADHAN_VOLUME = booleanPreferencesKey("use_global_adhan_volume")
        fun adhanVolumeFor(prayer: Prayer): Preferences.Key<Int> =
            intPreferencesKey("adhan_volume_${prayer.name.lowercase()}")
        fun vibrateFor(prayer: Prayer): Preferences.Key<Boolean> =
            booleanPreferencesKey("adhan_vibrate_${prayer.name.lowercase()}")
        fun bundledSoundFor(prayer: Prayer): Preferences.Key<String> =
            stringPreferencesKey("bundled_adhan_sound_${prayer.name.lowercase()}")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        val ADHAN_NOTIFICATION_DISMISSIBLE = booleanPreferencesKey("adhan_notification_dismissible")
        val STOP_ADHAN_ON_NOTIFICATION_DISMISS = booleanPreferencesKey("stop_adhan_on_notification_dismiss")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val DND_DURATION = intPreferencesKey("dnd_duration_minutes")
        val HIJRI_ADJUSTMENT = intPreferencesKey("hijri_adjustment")

        fun soundFor(prayer: Prayer): Preferences.Key<String> =
            stringPreferencesKey("adhan_sound_${prayer.name.lowercase()}")

        fun soundFileFor(prayer: Prayer): Preferences.Key<String> =
            stringPreferencesKey("adhan_sound_file_${prayer.name.lowercase()}")
    }
}
