package org.example.islamicapp.feature.prayertimes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.prayertimes.data.PrayerSettings
import org.example.islamicapp.feature.prayertimes.data.PrayerSettingsRepository
import org.example.islamicapp.feature.prayertimes.domain.AsrMethod
import org.example.islamicapp.feature.prayertimes.domain.CalculationMethod
import org.example.islamicapp.feature.prayertimes.domain.HighLatitudeRule
import org.example.islamicapp.feature.prayertimes.domain.Prayer
import org.example.islamicapp.feature.prayertimes.notifications.AdhanScheduler
import javax.inject.Inject

@HiltViewModel
class PrayerSettingsViewModel @Inject constructor(
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

    fun setReminderMinutes(minutes: Int) = update { it.copy(reminderMinutes = minutes) }

    fun setHijriAdjustment(days: Int) = update { it.copy(hijriAdjustment = days) }

    private fun update(transform: (PrayerSettings) -> PrayerSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            repository.save(updated)
            // Adhan alarms must reflect the new settings immediately.
            scheduler.schedule(updated)
        }
    }
}
