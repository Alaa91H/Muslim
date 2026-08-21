package org.muslim.app.feature.adhkar.ui

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.feature.adhkar.data.AdhkarPrefs
import org.muslim.app.feature.adhkar.data.AdhkarPrefsRepository
import org.muslim.app.feature.adhkar.data.AdhkarReminderScheduler
import org.muslim.app.feature.adhkar.data.PeriodicAdhkarReminderScheduler
import org.muslim.app.feature.adhkar.data.AdhkarRepository
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.overlay.AdhkarOverlayService
import javax.inject.Inject

@HiltViewModel
class AdhkarSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: AdhkarPrefsRepository,
    private val adhkarRepository: AdhkarRepository,
    private val scheduler: AdhkarReminderScheduler,
    private val periodicScheduler: PeriodicAdhkarReminderScheduler,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val prefs: StateFlow<AdhkarPrefs> = prefsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdhkarPrefs())

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** A sample dhikr rendered in the floating-message preview. */
    private val _previewDhikr = MutableStateFlow<Dhikr?>(null)
    val previewDhikr: StateFlow<Dhikr?> = _previewDhikr.asStateFlow()

    init {
        // Re-arm reminders whenever the app starts (covers the reboot-free gap)
        // and load a sample dhikr for the live overlay preview.
        viewModelScope.launch {
            val current = prefsRepository.prefs.first()
            scheduler.schedule(current)
            periodicScheduler.schedule(current)
            _previewDhikr.value = adhkarRepository.randomDhikr(null, current.disabledDhikrIds, current.shortDhikrOnly)
        }
    }

    val overlayPermissionGranted: Boolean
        get() = Settings.canDrawOverlays(context)

    fun setOverlayEnabled(enabled: Boolean) = save { prefsRepository.setOverlayEnabled(enabled) }

    fun setOverlayDurationSeconds(seconds: Int) =
        save { prefsRepository.setOverlayDurationSeconds(seconds) }

    fun setOverlayBackgroundColor(rgb: Int) =
        save { prefsRepository.setOverlayBackgroundColor(rgb) }

    fun setOverlayBackgroundAlpha(alpha: Int) =
        save { prefsRepository.setOverlayBackgroundAlpha(alpha) }

    fun resetOverlayAppearance() =
        save { prefsRepository.resetOverlayAppearance() }

    fun setOverlayCornerRadiusDp(radius: Int) =
        save { prefsRepository.setOverlayCornerRadiusDp(radius) }

    fun setOverlayFontSizeSp(size: Int) =
        save { prefsRepository.setOverlayFontSizeSp(size) }

    fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) =
        save { prefsRepository.setMorningReminder(enabled, hour, minute) }

    fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) =
        save { prefsRepository.setEveningReminder(enabled, hour, minute) }

    fun setPeriodicReminderEnabled(enabled: Boolean) =
        save { prefsRepository.setPeriodicReminderEnabled(enabled) }

    fun setPeriodicReminderInterval(minutes: Int) =
        save { prefsRepository.setPeriodicReminderInterval(minutes) }

    fun setPeriodicReminderCategory(categoryId: String?) =
        save { prefsRepository.setPeriodicReminderCategory(categoryId) }

    fun setPeriodicReminderDhikr(id: Long?) =
        save { prefsRepository.setPeriodicReminderDhikr(id) }

    /** All adhkar, for the "pin a specific dhikr" picker. */
    val allAdhkar: List<Dhikr> = adhkarRepository.allDhikr()

    fun setShortDhikrOnly(enabled: Boolean) =
        save { prefsRepository.setShortDhikrOnly(enabled) }

    fun setPeriodicReminderWindow(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) =
        save { prefsRepository.setPeriodicReminderWindow(enabled, startHour, startMinute, endHour, endMinute) }

    /** Shows a dhikr above all apps right now (test action): the pinned one when set, else a random one. */
    fun testOverlay() {
        viewModelScope.launch {
            val current = prefsRepository.prefs.first()
            if (!Settings.canDrawOverlays(context)) return@launch
            val pinned = adhkarRepository.dhikrById(current.periodicReminderDhikrId)
                ?.takeIf { it.id !in current.disabledDhikrIds }
            val dhikr = pinned
                ?: adhkarRepository.randomDhikr(null, current.disabledDhikrIds, current.shortDhikrOnly)
                ?: return@launch
            AdhkarOverlayService.start(
                context,
                dhikr,
                current.overlayDurationSeconds,
                current.overlayBackgroundColor,
                current.overlayCornerRadiusDp,
                current.overlayFontSizeSp,
            )
        }
    }

    private fun save(transform: suspend () -> Unit) {
        viewModelScope.launch {
            transform()
            val current = prefsRepository.prefs.first()
            scheduler.schedule(current)
            periodicScheduler.schedule(current)
        }
    }
}
