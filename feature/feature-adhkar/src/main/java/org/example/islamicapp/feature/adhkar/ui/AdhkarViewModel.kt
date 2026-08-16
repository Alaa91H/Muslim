package org.example.islamicapp.feature.adhkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.adhkar.data.AdhkarRepository
import org.example.islamicapp.feature.adhkar.data.DhikrReminderRepository
import org.example.islamicapp.feature.adhkar.notifications.DhikrReminderScheduler
import org.example.islamicapp.feature.adhkar.domain.DhikrCategory
import org.example.islamicapp.feature.adhkar.domain.DhikrProgress
import javax.inject.Inject

data class AdhkarUiState(
    val categories: List<DhikrCategory> = emptyList(),
    /** Currently open category, or null to show the category list. */
    val selected: DhikrCategory? = null,
    val progress: DhikrProgress = DhikrProgress(),
    /** Selected periodic-reminder interval in minutes (0 = off). */
    val reminderIntervalMinutes: Int = DhikrReminderRepository.DEFAULT_INTERVAL_MINUTES,
)

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val repository: AdhkarRepository,
    private val reminderRepository: DhikrReminderRepository,
    private val reminderScheduler: DhikrReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdhkarUiState(categories = repository.categories))
    val uiState: StateFlow<AdhkarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.intervalMinutes.collect { minutes ->
                _uiState.update { it.copy(reminderIntervalMinutes = minutes) }
            }
        }
    }

    fun selectCategory(category: DhikrCategory?) {
        _uiState.update { state ->
            state.copy(
                selected = category,
                progress = DhikrProgress.reset(category?.items?.map { it.id }.orEmpty()),
            )
        }
    }

    /** Registers one repetition of the given dhikr. */
    fun increment(itemId: String) {
        val category = _uiState.value.selected ?: return
        val item = category.items.firstOrNull { it.id == itemId } ?: return
        _uiState.update { it.copy(progress = it.progress.increment(itemId, item.count)) }
    }

    /** Resets the counters of the open category. */
    fun resetCategory() {
        val category = _uiState.value.selected ?: return
        _uiState.update { it.copy(progress = DhikrProgress.reset(category.items.map { it.id })) }
    }

    fun setReminderInterval(minutes: Int) {
        viewModelScope.launch {
            reminderRepository.setInterval(minutes)
            reminderScheduler.schedule(minutes)
        }
    }
}
