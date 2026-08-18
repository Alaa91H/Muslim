package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.QuranWordFrequency
import org.muslim.app.feature.quran.data.QuranWordFrequencyResult
import org.muslim.app.feature.quran.domain.QuranRepository
import javax.inject.Inject

/** Computes the whole-mushaf word frequency once, off the main thread. */
@HiltViewModel
class QuranWordFrequencyViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val result: QuranWordFrequencyResult? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val result = QuranWordFrequency.compute(
                ayahTexts = repository.allAyahs().map { it.text },
            )
            _uiState.value = UiState(loading = false, result = result)
        }
    }
}
