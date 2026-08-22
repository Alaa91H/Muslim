package org.muslim.app.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.AppThemeMode
import org.muslim.app.feature.settings.update.UpdateCheckScheduler
import org.muslim.app.feature.settings.update.UpdateChecker
import javax.inject.Inject

/**
 * App-wide settings hub (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة"):
 * appearance, language and accessibility preferences persisted via
 * [AppPreferencesRepository] (core-datastore).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> =
        appPreferencesRepository.preferences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    fun setThemeMode(mode: AppThemeMode) = launch { appPreferencesRepository.setThemeMode(mode) }

    fun setDynamicColor(enabled: Boolean) = launch { appPreferencesRepository.setDynamicColor(enabled) }

    fun setReduceAnimations(enabled: Boolean) = launch { appPreferencesRepository.setReduceAnimations(enabled) }

    fun setStartTab(route: String) = launch { appPreferencesRepository.setStartTab(route) }

    fun setTimeFormat24h(use24h: Boolean) = launch { appPreferencesRepository.setTimeFormat24h(use24h) }

    fun setAccessibilityReadingMode(enabled: Boolean) = launch {
        appPreferencesRepository.setAccessibilityReadingMode(enabled)
    }

    fun setAccessibilityHighContrast(enabled: Boolean) = launch {
        appPreferencesRepository.setAccessibilityHighContrast(enabled)
    }

    fun setVoiceNavigationEnabled(enabled: Boolean) = launch {
        appPreferencesRepository.setVoiceNavigationEnabled(enabled)
    }

    fun setMoreSectionOrder(order: List<String>) = launch { appPreferencesRepository.setMoreSectionOrder(order) }

    /**
     * Enables/disables the periodic update check (off by default). Turning it
     * on schedules the daily/weekly/monthly worker; turning it off cancels it.
     */
    fun setUpdateCheckEnabled(enabled: Boolean) = launch {
        appPreferencesRepository.setUpdateCheckEnabled(enabled)
        if (enabled) {
            UpdateCheckScheduler.schedule(context, preferences.value.updateCheckFrequency)
        } else {
            UpdateCheckScheduler.cancel(context)
        }
    }

    /** Changes the update-check cadence and re-anchors the periodic job. */
    fun setUpdateCheckFrequency(frequency: String) = launch {
        appPreferencesRepository.setUpdateCheckFrequency(frequency)
        if (preferences.value.updateCheckEnabled) {
            UpdateCheckScheduler.schedule(context, frequency)
        }
    }

    /**
     * Turns the fully-automatic update on/off (Session API install, one-time
     * confirmation shown by the screen before this is called). Requires the
     * periodic check to be enabled so a newly-found release auto-downloads.
     */
    fun setAutoUpdateEnabled(enabled: Boolean) = launch {
        appPreferencesRepository.setAutoUpdateEnabled(enabled)
    }

    /** Result of the last manual "check now" run (null until one is done). */
    private val _updateCheckResult = MutableStateFlow<UpdateChecker.Result?>(null)
    val updateCheckResult: StateFlow<UpdateChecker.Result?> = _updateCheckResult.asStateFlow()

    /** Human-readable failure detail of the last manual check (null on success). */
    private val _updateCheckError = MutableStateFlow<String?>(null)
    val updateCheckError: StateFlow<String?> = _updateCheckError.asStateFlow()

    /**
     * Runs an immediate check against the GitHub releases page. When a newer
     * version exists the update-available notification is posted; the caller
     * (settings screen) opens the update screen for the details. Failures are
     * surfaced with the underlying reason instead of crashing the screen.
     */
    fun checkForUpdatesNow() = launch {
        _updateCheckResult.value = null
        _updateCheckError.value = null
        runCatching { UpdateChecker(context).checkAndNotify() }
            .onSuccess { _updateCheckResult.value = it }
            .onFailure { e ->
                _updateCheckError.value = e.message?.takeIf { it.isNotBlank() }
                    ?: e.javaClass.simpleName
            }
    }

    /** Clears the last manual-check error (after the UI consumed it). */
    fun consumeUpdateCheckError() {
        _updateCheckError.value = null
    }

    /** Clears the last manual-check result (after the UI consumed it). */
    fun consumeUpdateCheckResult() {
        _updateCheckResult.value = null
    }

    /**
     * Suspends until the new language is persisted, so the caller can safely
     * recreate the activity afterwards without a read race.
     */
    suspend fun setLanguage(languageCode: String) = appPreferencesRepository.setLanguage(languageCode)

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
