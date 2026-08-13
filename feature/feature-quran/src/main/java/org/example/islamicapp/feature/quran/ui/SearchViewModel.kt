package org.example.islamicapp.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.feature.quran.data.QuranSearchQuery
import org.example.islamicapp.feature.quran.domain.Ayah
import org.example.islamicapp.feature.quran.domain.QuranRepository
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    data class UiState(
        val searching: Boolean = false,
        val results: List<Ayah> = emptyList(),
        val idle: Boolean = true,
    )

    val uiState: StateFlow<UiState> = query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { raw ->
            val usable = QuranSearchQuery.isUsable(raw)
            if (!usable) {
                kotlinx.coroutines.flow.flowOf(UiState(idle = true))
            } else {
                kotlinx.coroutines.flow.flow {
                    emit(UiState(searching = true))
                    emit(UiState(searching = false, results = repository.search(raw)))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())
}
