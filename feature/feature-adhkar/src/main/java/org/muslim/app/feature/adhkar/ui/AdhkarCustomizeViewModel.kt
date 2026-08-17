package org.muslim.app.feature.adhkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.adhkar.data.AdhkarPrefsRepository
import org.muslim.app.feature.adhkar.data.AdhkarRepository
import org.muslim.app.feature.adhkar.domain.Dhikr
import javax.inject.Inject

/** A dhikr plus whether the user chose to show it in the library/reminders. */
data class DhikrVisibility(val dhikr: Dhikr, val enabled: Boolean)

@HiltViewModel
class AdhkarCustomizeViewModel @Inject constructor(
    private val adhkarRepository: AdhkarRepository,
    private val prefsRepository: AdhkarPrefsRepository,
) : ViewModel() {

    val visibility: StateFlow<List<DhikrVisibility>> = combine(
        adhkarRepository.observeAdhkar(),
        prefsRepository.prefs,
    ) { adhkar, prefs ->
        adhkar.map { DhikrVisibility(it, prefs.isDhikrEnabled(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEnabled(dhikrId: Long, enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setDhikrEnabled(dhikrId, enabled) }
    }
}
