package org.example.islamicapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.core.datastore.AppPreferences
import org.example.islamicapp.core.datastore.AppPreferencesRepository
import org.example.islamicapp.core.datastore.prayer.PrayerSettingsRepository
import org.example.islamicapp.core.datastore.prayer.SelectedLocation
import javax.inject.Inject

/** App-level state needed by the navigation shell (location + app preferences). */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: PrayerSettingsRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val location: StateFlow<SelectedLocation?> = settingsRepository.settings
        .map { it.location }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Drives the app theme (light/dark/system + dynamic color). */
    val appPreferences: StateFlow<AppPreferences> = appPreferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())
}
