package org.example.islamicapp.feature.tasbih.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.tasbih.data.TasbihRepository
import org.example.islamicapp.feature.tasbih.domain.TasbihPhrase
import org.example.islamicapp.feature.tasbih.domain.TasbihState
import javax.inject.Inject

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val repository: TasbihRepository,
) : ViewModel() {

    val state: StateFlow<TasbihState> = repository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasbihState(0, 33, TasbihPhrase.SubhanAllah, emptyList()))

    fun increment() = viewModelScope.launch { repository.increment() }

    fun reset() = viewModelScope.launch { repository.reset() }

    fun setTarget(target: Int) = viewModelScope.launch { repository.setTarget(target) }

    fun setPhrase(phrase: TasbihPhrase) = viewModelScope.launch { repository.setPhrase(phrase) }
}
