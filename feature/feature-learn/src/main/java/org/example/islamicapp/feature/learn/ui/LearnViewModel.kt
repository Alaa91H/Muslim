package org.example.islamicapp.feature.learn.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.islamicapp.feature.learn.data.LearnRepository
import org.example.islamicapp.feature.learn.domain.LearnTopic
import javax.inject.Inject

data class LearnUiState(
    val topics: List<LearnTopic> = emptyList(),
    /** Currently open topic, or null to show the topic list. */
    val selected: LearnTopic? = null,
    /** Step index within the open topic. */
    val stepIndex: Int = 0,
    /** Whether the neutral madhhab-differences section is visible. */
    val showDifferences: Boolean = false,
)

@HiltViewModel
class LearnViewModel @Inject constructor(
    repository: LearnRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState(topics = repository.topics))
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    fun selectTopic(topic: LearnTopic?) {
        _uiState.update { it.copy(selected = topic, stepIndex = 0, showDifferences = false) }
    }

    fun nextStep() {
        _uiState.update { state ->
            val topic = state.selected ?: return@update state
            state.copy(stepIndex = (state.stepIndex + 1).coerceAtMost(topic.steps.lastIndex))
        }
    }

    fun previousStep() {
        _uiState.update { it.copy(stepIndex = (it.stepIndex - 1).coerceAtLeast(0)) }
    }

    fun toggleDifferences() {
        _uiState.update { it.copy(showDifferences = !it.showDifferences) }
    }
}
