package org.muslim.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
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

    /** User-adjusted Hijri date offset shared with the Ramadan feature and navigation. */
    val hijriAdjustment: StateFlow<Int> = settingsRepository.settings
        .map { it.hijriAdjustment }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Drives the app theme (light/dark/system + dynamic color). */
    val appPreferences: StateFlow<AppPreferences> = appPreferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())
}
