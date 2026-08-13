package org.example.islamicapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.feature.prayertimes.data.PrayerSettingsRepository
import org.example.islamicapp.feature.prayertimes.data.SelectedLocation
import javax.inject.Inject

/** App-level state needed by the navigation shell (the selected location). */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: PrayerSettingsRepository,
) : ViewModel() {

    val location: StateFlow<SelectedLocation?> = settingsRepository.settings
        .map { it.location }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
