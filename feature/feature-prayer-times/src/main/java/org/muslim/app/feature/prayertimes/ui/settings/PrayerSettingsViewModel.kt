package org.muslim.app.feature.prayertimes.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.AsrMethod
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.NextPrayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.toPrayerParameters
import androidx.glance.appwidget.updateAll
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.prayertimes.notifications.AdhanPlaybackService
import org.muslim.app.feature.prayertimes.notifications.AdhanPlaybackStatus
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler
import org.muslim.app.feature.prayertimes.notifications.NextAdhanService
import org.muslim.app.feature.prayertimes.notifications.AdhanSoundPlayer
import org.muslim.app.feature.prayertimes.notifications.AdhanSoundRepository
import org.muslim.app.feature.prayertimes.data.CitiesRepository
import org.muslim.app.feature.prayertimes.domain.City
import org.muslim.app.feature.prayertimes.widget.PrayerTimesWidget
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerSettingsRepository,
    private val scheduler: AdhanScheduler,
    private val soundRepository: AdhanSoundRepository,
    private val calculator: PrayerTimesCalculator,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val soundPlayer: AdhanSoundPlayer,
) : ViewModel() {

    val settings: StateFlow<PrayerSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerSettings())

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val minuteTicker = flow {
        while (true) {
            emit(Unit)
            delay(60_000)
        }
    }

    /**
     * The real next prayer (name + local time) computed from the saved location,
     * refreshed every minute. Drives the live adhan notification preview.
     */
    val nextPrayerPreview: StateFlow<Pair<Prayer, LocalTime>?> =
        combine(repository.settings, minuteTicker) { settings, _ -> computeNextPrayer(settings) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun computeNextPrayer(settings: PrayerSettings): Pair<Prayer, LocalTime>? {
        val location = settings.location ?: return null
        val zone = ZoneId.of(location.timeZone)
        val now = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val coordinates = Coordinates(location.latitude, location.longitude, location.elevation)
        val params = settings.toPrayerParameters()
        var next = NextPrayer.nextPrayer(
            calculator.compute(today, coordinates, params, zone, settings.asrMethod, settings.adjustments).epochMillis,
            now,
        )
        if (next == null) {
            next = NextPrayer.nextPrayer(
                calculator.compute(
                    today.plusDays(1), coordinates, params, zone, settings.asrMethod, settings.adjustments,
                ).epochMillis,
                now,
            )
        }
        return next?.let {
            it.prayer to Instant.ofEpochMilli(it.atEpochMillis).atZone(zone).toLocalTime()
        }
    }

    /** Download progress (0..1) per prayer, present only while downloading. */
    private val _downloadProgress = MutableStateFlow<Map<Prayer, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<Prayer, Float>> = _downloadProgress.asStateFlow()

    /**
     * The method + country the automatic option currently resolves to for the
     * saved location (only meaningful while [PrayerSettings.methodChosenManually]
     * is false). Lets the settings screen show what "Automatic" picked today.
     */
    data class AutoMethodInfo(val method: CalculationMethod, val country: String, val city: String)

    val autoMethodInfo: StateFlow<AutoMethodInfo?> =
        repository.settings
            .map { resolveAutoMethodInfo(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun resolveAutoMethodInfo(settings: PrayerSettings): AutoMethodInfo? {
        val city = nearestCityFor(settings) ?: return null
        return AutoMethodInfo(
            method = CalculationMethod.suggestedFor(city.country),
            country = city.country,
            city = city.displayName,
        )
    }

    private fun nearestCityFor(settings: PrayerSettings): City? {
        val location = settings.location ?: return null
        return CitiesRepository.all.minByOrNull { city ->
            val dLat = city.latitude - location.latitude
            val dLon = city.longitude - location.longitude
            dLat * dLat + dLon * dLon
        }
    }

    fun setMethod(method: CalculationMethod) =
        update { it.copy(method = method, methodChosenManually = true) }

    /**
     * Switches back to the automatic method: the best-known method for the
     * saved location's country is resolved immediately (region-aware), and
     * future location changes keep re-suggesting until the user picks again.
     */
    fun setMethodAutomatic() {
        val current = settings.value
        val country = nearestCityFor(current)?.country
        val suggested = if (country != null) {
            CalculationMethod.suggestedFor(country)
        } else {
            CalculationMethod.MuslimWorldLeague
        }
        update { it.copy(method = suggested, methodChosenManually = false) }
    }

    fun setCustomFajrAngle(angle: Double) = update { it.copy(customFajrAngle = angle) }

    fun setCustomIshaAngle(angle: Double) = update { it.copy(customIshaAngle = angle) }

    fun setAsrMethod(asrMethod: AsrMethod) = update { it.copy(asrMethod = asrMethod) }

    fun setHighLatitudeRule(rule: HighLatitudeRule?) = update { it.copy(highLatitudeRule = rule) }

    fun setAdjustment(prayer: Prayer, minutes: Int) = update {
        it.copy(adjustments = it.adjustments.with(prayer, minutes))
    }

    fun setAdhanEnabled(enabled: Boolean) = update { it.copy(adhanEnabled = enabled) }

    fun setVibrateEnabled(enabled: Boolean) = update { it.copy(vibrateEnabled = enabled) }

    /** Per-prayer vibration override; every prayer can vibrate independently. */
    fun setVibrateEnabled(prayer: Prayer, enabled: Boolean) = update {
        it.copy(vibratePerPrayer = it.vibratePerPrayer + (prayer to enabled))
    }

    fun setAdhanSound(prayer: Prayer, option: AdhanSoundOption) = update {
        it.copy(adhanSounds = it.adhanSounds + (prayer to option))
    }

    /** Selects the bundled (offline) recording for a single prayer (default Makkah). */
    fun setBundledAdhanSound(prayer: Prayer, id: String) = update {
        it.copy(bundledAdhanSounds = it.bundledAdhanSounds + (prayer to id))
    }

    fun setAdhanVolume(volume: Int) = update { it.copy(adhanVolume = volume.coerceIn(0, 100)) }

    /** Per-prayer adhan volume override (0..100). */
    fun setAdhanVolume(prayer: Prayer, volume: Int) = update {
        it.copy(adhanVolumes = it.adhanVolumes + (prayer to volume.coerceIn(0, 100)))
    }

    /** True while an adhan preview is ringing (drives the preview/stop toggle). */
    val isPreviewing = AdhanPlaybackStatus.isPlaying

    /** Stops any adhan preview currently playing. */
    fun stopPreview() {
        AdhanPlaybackService.stop(context)
    }

    /** Live-adjusts the volume of the currently playing preview (0..100). */
    fun setLivePreviewVolume(volume: Int) {
        soundPlayer.setVolume(volume)
    }

    fun setReminderMinutes(minutes: Int) = update { it.copy(reminderMinutes = minutes) }

    fun setDndEnabled(enabled: Boolean) = update { it.copy(dndEnabled = enabled) }

    fun setDndDurationMinutes(minutes: Int) = update { it.copy(dndDurationMinutes = minutes) }

    fun setHijriAdjustment(days: Int) = update { it.copy(hijriAdjustment = days) }

    /**
     * Plays a bundled adhan recording so the user can preview it. [volumePercent]
     * is the level chosen in the customize dialog at that moment — the preview
     * must honour it (not the last-saved level), so moving the volume slider
     * is immediately audible. When the dialog confirms, the chosen level is
     * persisted via [setAdhanVolume].
     */
    fun previewBundled(
        prayer: Prayer,
        sound: org.muslim.app.core.common.prayer.BundledAdhanSound,
        volumePercent: Int = settings.value.adhanVolumeFor(prayer),
    ) {
        viewModelScope.launch {
            AdhanPlaybackService.start(
                context = context,
                prayer = prayer,
                vibrate = false,
                soundOption = AdhanSoundOption.Default,
                volumePercent = volumePercent.coerceIn(0, 100),
                soundPath = null,
                bundledSoundId = sound.id,
            )
        }
    }

    /** Plays the adhan as configured for [prayer] so the user can preview it. */
    fun previewAdhan(prayer: Prayer) {
        val current = settings.value
        if (!current.adhanEnabled) return
        viewModelScope.launch {
            val soundPath = soundRepository.customSoundFile(prayer)?.absolutePath
            AdhanPlaybackService.start(
                context = context,
                prayer = prayer,
                vibrate = current.vibrateFor(prayer),
                soundOption = current.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                volumePercent = current.adhanVolumeFor(prayer),
                soundPath = soundPath,
                bundledSoundId = current.bundledAdhanSounds[prayer]
                    ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID,
            )
        }
    }

    /** Binds a user-picked audio file to [prayer] (stored privately on-device). */
    fun setCustomSound(prayer: Prayer, uri: Uri) {
        viewModelScope.launch {
            soundRepository.setCustomSound(prayer, uri)
            reschedule()
        }
    }

    /** Downloads an adhan audio from [url] for [prayer], reporting progress. */
    fun downloadSound(prayer: Prayer, url: String) {
        viewModelScope.launch {
            _downloadProgress.value = _downloadProgress.value + (prayer to 0f)
            soundRepository.downloadSound(prayer, url) { progress ->
                _downloadProgress.value = _downloadProgress.value + (prayer to progress)
            }
            _downloadProgress.value = _downloadProgress.value - prayer
            reschedule()
        }
    }

    /** Reverts [prayer] to the bundled tone. */
    fun clearCustomSound(prayer: Prayer) {
        viewModelScope.launch {
            soundRepository.clearCustomSound(prayer)
            reschedule()
        }
    }

    private suspend fun reschedule() {
        val current = repository.settings.first()
        scheduler.schedule(current)
        // Keep the countdown notification in sync with the new settings.
        NextAdhanService.start(context)
        PrayerTimesWidget().updateAll(context)
    }

    private fun update(transform: (PrayerSettings) -> PrayerSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            repository.save(updated)
            // Adhan alarms must reflect the new settings immediately.
            scheduler.schedule(updated)
            NextAdhanService.start(context)
            // The home-screen widget must reflect the new settings too.
            PrayerTimesWidget().updateAll(context)
        }
    }
}
