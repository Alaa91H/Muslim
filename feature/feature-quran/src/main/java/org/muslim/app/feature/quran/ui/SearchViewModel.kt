package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.quran.data.QuranSearchQuery
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranRepository
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    data class UiState(
        val searching: Boolean = false,
        val results: List<Ayah> = emptyList(),
        /** Total word-level occurrences of the query across all matched ayahs. */
        val occurrences: Int = 0,
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
                    val results = repository.search(raw)
                    val tokens = ArabicText.normalize(raw)
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                    val occurrences = results.sumOf { ayah ->
                        val text = ArabicText.normalize(ayah.text)
                        tokens.sumOf { token -> countOccurrences(text, token) }
                    }
                    emit(
                        UiState(
                            searching = false,
                            results = results,
                            occurrences = occurrences,
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())
}

/** Counts non-overlapping occurrences of [token] in [text]. */
private fun countOccurrences(text: String, token: String): Int {
    if (token.isEmpty()) return 0
    var count = 0
    var index = text.indexOf(token)
    while (index >= 0) {
        count++
        index = text.indexOf(token, index + token.length)
    }
    return count
}
