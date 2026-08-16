package org.example.islamicapp.feature.tasbih.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.tasbih.data.TasbihRepository
import org.example.islamicapp.feature.tasbih.domain.TasbihState
import java.time.LocalDate
import javax.inject.Inject

data class TasbihUiState(
    val counter: TasbihState = TasbihState(),
    /** Last 7 days totals for the weekly chart (oldest → newest). */
    val week: List<Pair<LocalDate, Int>> = emptyList(),
)

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val repository: TasbihRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.stats.collect { stats ->
                _uiState.update { state ->
                    val today = LocalDate.now()
                    val week = (6L downTo 0L).map { minus ->
                        val day = today.minusDays(minus)
                        day to (stats.history[day] ?: 0)
                    }
                    state.copy(
                        counter = state.counter.copy(
                            target = stats.target,
                            todayTotal = stats.today,
                        ),
                        week = week,
                    )
                }
            }
        }
    }

    fun tap() {
        _uiState.update { it.copy(counter = it.counter.tap()) }
        viewModelScope.launch { repository.addToToday(1) }
    }

    fun resetCycle() {
        _uiState.update { it.copy(counter = it.counter.resetCycle()) }
    }

    fun resetToday() {
        _uiState.update { it.copy(counter = it.counter.copy(todayTotal = 0)) }
        viewModelScope.launch { repository.resetToday() }
    }

    fun setTarget(target: Int) {
        _uiState.update { it.copy(counter = it.counter.withTarget(target)) }
        viewModelScope.launch { repository.setTarget(target) }
    }
}
