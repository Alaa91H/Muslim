package org.muslim.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.AppThemeMode
import javax.inject.Inject

/**
 * App-wide settings hub (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة"):
 * appearance, language and accessibility preferences persisted via
 * [AppPreferencesRepository] (core-datastore).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
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

    fun setMoreSectionOrder(order: List<String>) = launch { appPreferencesRepository.setMoreSectionOrder(order) }

    /**
     * Suspends until the new language is persisted, so the caller can safely
     * recreate the activity afterwards without a read race.
     */
    suspend fun setLanguage(languageCode: String) = appPreferencesRepository.setLanguage(languageCode)

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
