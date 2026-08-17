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
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import androidx.glance.appwidget.updateAll
import org.muslim.app.feature.prayertimes.notifications.AdhanPlaybackService
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler
import org.muslim.app.feature.prayertimes.notifications.NextAdhanService
import org.muslim.app.feature.prayertimes.notifications.AdhanSoundRepository
import org.muslim.app.feature.prayertimes.widget.PrayerTimesWidget
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerSettingsRepository,
    private val scheduler: AdhanScheduler,
    private val soundRepository: AdhanSoundRepository,
) : ViewModel() {

    val settings: StateFlow<PrayerSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerSettings())

    /** Download progress (0..1) per prayer, present only while downloading. */
    private val _downloadProgress = MutableStateFlow<Map<Prayer, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<Prayer, Float>> = _downloadProgress.asStateFlow()

    fun setMethod(method: CalculationMethod) =
        update { it.copy(method = method, methodChosenManually = true) }

    fun setCustomFajrAngle(angle: Double) = update { it.copy(customFajrAngle = angle) }

    fun setCustomIshaAngle(angle: Double) = update { it.copy(customIshaAngle = angle) }

    fun setAsrMethod(asrMethod: AsrMethod) = update { it.copy(asrMethod = asrMethod) }

    fun setHighLatitudeRule(rule: HighLatitudeRule?) = update { it.copy(highLatitudeRule = rule) }

    fun setAdjustment(prayer: Prayer, minutes: Int) = update {
        it.copy(adjustments = it.adjustments.with(prayer, minutes))
    }

    fun setAdhanEnabled(enabled: Boolean) = update { it.copy(adhanEnabled = enabled) }

    fun setVibrateEnabled(enabled: Boolean) = update { it.copy(vibrateEnabled = enabled) }

    fun setAdhanSound(prayer: Prayer, option: AdhanSoundOption) = update {
        it.copy(adhanSounds = it.adhanSounds + (prayer to option))
    }

    /** Selects which bundled (offline) recording plays by default. */
    fun setBundledAdhanSound(id: String) = update { it.copy(bundledAdhanSound = id) }

    fun setAdhanVolume(volume: Int) = update { it.copy(adhanVolume = volume.coerceIn(0, 100)) }

    fun setReminderMinutes(minutes: Int) = update { it.copy(reminderMinutes = minutes) }

    fun setDndEnabled(enabled: Boolean) = update { it.copy(dndEnabled = enabled) }

    fun setDndDurationMinutes(minutes: Int) = update { it.copy(dndDurationMinutes = minutes) }

    fun setHijriAdjustment(days: Int) = update { it.copy(hijriAdjustment = days) }

    /** Plays a bundled adhan recording directly so the user can hear it before choosing. */
    fun previewBundled(sound: org.muslim.app.core.common.prayer.BundledAdhanSound) {
        val current = settings.value
        viewModelScope.launch {
            AdhanPlaybackService.start(
                context = context,
                prayer = Prayer.Fajr,
                vibrate = false,
                soundOption = AdhanSoundOption.Default,
                volumePercent = current.adhanVolume,
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
                vibrate = current.vibrateEnabled,
                soundOption = current.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                volumePercent = current.adhanVolume,
                soundPath = soundPath,
                bundledSoundId = current.bundledAdhanSound,
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
