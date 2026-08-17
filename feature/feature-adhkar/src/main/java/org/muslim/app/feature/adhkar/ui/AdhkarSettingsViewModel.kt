package org.muslim.app.feature.adhkar.ui

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.adhkar.data.AdhkarPrefs
import org.muslim.app.feature.adhkar.data.AdhkarPrefsRepository
import org.muslim.app.feature.adhkar.data.AdhkarReminderScheduler
import org.muslim.app.feature.adhkar.data.AdhkarRepository
import org.muslim.app.feature.adhkar.overlay.AdhkarOverlayService
import javax.inject.Inject

@HiltViewModel
class AdhkarSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: AdhkarPrefsRepository,
    private val adhkarRepository: AdhkarRepository,
    private val scheduler: AdhkarReminderScheduler,
) : ViewModel() {

    val prefs: StateFlow<AdhkarPrefs> = prefsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdhkarPrefs())

    init {
        // Re-arm reminders whenever the app starts (covers the reboot-free gap).
        viewModelScope.launch {
            scheduler.schedule(prefsRepository.prefs.first())
        }
    }

    val overlayPermissionGranted: Boolean
        get() = Settings.canDrawOverlays(context)

    fun setOverlayEnabled(enabled: Boolean) = save { prefsRepository.setOverlayEnabled(enabled) }

    fun setOverlayDurationSeconds(seconds: Int) =
        save { prefsRepository.setOverlayDurationSeconds(seconds) }

    fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) =
        save { prefsRepository.setMorningReminder(enabled, hour, minute) }

    fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) =
        save { prefsRepository.setEveningReminder(enabled, hour, minute) }

    /** Shows a random enabled dhikr above all apps right now (test action). */
    fun testOverlay() {
        viewModelScope.launch {
            val current = prefsRepository.prefs.first()
            if (!Settings.canDrawOverlays(context)) return@launch
            val dhikr = adhkarRepository.randomDhikr(null, current.disabledDhikrIds) ?: return@launch
            AdhkarOverlayService.start(context, dhikr, current.overlayDurationSeconds)
        }
    }

    private fun save(transform: suspend () -> Unit) {
        viewModelScope.launch {
            transform()
            scheduler.schedule(prefsRepository.prefs.first())
        }
    }
}
