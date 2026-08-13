package org.example.islamicapp.feature.prayertimes.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.prayertimes.data.PrayerSettings
import org.example.islamicapp.feature.prayertimes.data.PrayerSettingsRepository
import org.example.islamicapp.feature.prayertimes.domain.AdhanSoundOption
import org.example.islamicapp.feature.prayertimes.domain.AsrMethod
import org.example.islamicapp.feature.prayertimes.domain.CalculationMethod
import org.example.islamicapp.feature.prayertimes.domain.HighLatitudeRule
import org.example.islamicapp.feature.prayertimes.domain.Prayer
import androidx.glance.appwidget.updateAll
import org.example.islamicapp.feature.prayertimes.notifications.AdhanPlaybackService
import org.example.islamicapp.feature.prayertimes.notifications.AdhanScheduler
import org.example.islamicapp.feature.prayertimes.widget.PrayerTimesWidget
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PrayerSettingsRepository,
    private val scheduler: AdhanScheduler,
) : ViewModel() {

    val settings: StateFlow<PrayerSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerSettings())

    fun setMethod(method: CalculationMethod) = update { it.copy(method = method) }

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

    fun setAdhanVolume(volume: Int) = update { it.copy(adhanVolume = volume.coerceIn(0, 100)) }

    fun setReminderMinutes(minutes: Int) = update { it.copy(reminderMinutes = minutes) }

    fun setHijriAdjustment(days: Int) = update { it.copy(hijriAdjustment = days) }

    /** Plays the adhan as configured for [prayer] so the user can preview it. */
    fun previewAdhan(prayer: Prayer) {
        val current = settings.value
        if (!current.adhanEnabled) return
        AdhanPlaybackService.start(
            context = context,
            prayer = prayer,
            vibrate = current.vibrateEnabled,
            soundOption = current.adhanSounds[prayer] ?: AdhanSoundOption.Default,
            volumePercent = current.adhanVolume,
        )
    }

    private fun update(transform: (PrayerSettings) -> PrayerSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            repository.save(updated)
            // Adhan alarms must reflect the new settings immediately.
            scheduler.schedule(updated)
            // The home-screen widget must reflect the new settings too.
            PrayerTimesWidget().updateAll(context)
        }
    }
}
