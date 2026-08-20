package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.QuranWordFrequency
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.QuranSearchQuery
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.QuranWordSearch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    /**
     * Most frequent whole-mushaf word forms (computed once, off the main
     * thread) — powers the live autocomplete suggestions while typing.
     */
    private val topWords = MutableStateFlow<List<String>>(emptyList())

    /**
     * Autocomplete suggestions: top frequent words that start with the typed
     * (normalized) query, excluding the exact query itself. Empty while the
     * query is too short or no suggestions match.
     */
    val suggestions: StateFlow<List<String>> = combine(query, topWords) { raw, words ->
        val needle = ArabicText.normalize(raw.trim())
        if (needle.length < 2) emptyList()
        else words.asSequence()
            .filter { it.startsWith(needle) && it != needle }
            .take(8)
            .toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())

    /** Recent search queries, newest first (shown when the field is empty). */
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    init {
        // Restore the last committed search so reopening the screen resumes it.
        viewModelScope.launch {
            prefsRepository.lastSearch.first().takeIf { it.isNotBlank() }?.let { query.value = it }
        }
        viewModelScope.launch {
            prefsRepository.searchHistory.collect { _searchHistory.value = it }
        }
        // Precompute the frequent-word list for autocomplete (one full scan).
        viewModelScope.launch(Dispatchers.Default) {
            topWords.value = QuranWordFrequency.compute(
                ayahTexts = repository.allAyahs().map { it.text },
                topN = 300,
            ).entries.map { it.word }
        }
    }

    /** Applies an autocomplete suggestion (fills the field; the debounced search runs). */
    fun applySuggestion(word: String) {
        query.value = word
        saveCurrentSearch()
    }

    /** Commits the current query to the persisted history (IME search / result tap). */
    fun saveCurrentSearch() {
        val raw = query.value
        if (QuranSearchQuery.isUsable(raw)) {
            viewModelScope.launch { prefsRepository.recordSearch(raw.trim()) }
        }
    }

    /** Re-runs a previous query and moves it back to the front of the history. */
    fun searchFromHistory(queryText: String) {
        query.value = queryText
        if (QuranSearchQuery.isUsable(queryText)) {
            viewModelScope.launch { prefsRepository.recordSearch(queryText.trim()) }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { prefsRepository.clearSearchHistory() }
    }

    /** PREFIX = root-friendly (matches the FTS token* behaviour); EXACT = whole word only. */
    val matchMode = MutableStateFlow(QuranWordSearch.MatchMode.PREFIX)

    data class Match(
        val ayah: Ayah,
        /** Word-level matches in this ayah. */
        val occurrences: Int,
        /** Word spans (normalized offsets) to highlight. */
        val spans: List<IntRange>,
    )

    data class UiState(
        val searching: Boolean = false,
        val matches: List<Match> = emptyList(),
        /** Total word-level occurrences of the query across all matched ayahs. */
        val occurrences: Int = 0,
        /** Matched surahs with their occurrence counts ("أين ذُكرت"). */
        val surahBreakdown: List<QuranWordSearch.SurahOccurrence> = emptyList(),
        val idle: Boolean = true,
    )

    val uiState: StateFlow<UiState> = combine(
        query.debounce(300).distinctUntilChanged(),
        matchMode,
    ) { raw, mode ->
        raw to mode
    }
        .distinctUntilChanged()
        .flatMapLatest { (raw, mode) ->
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
                    val matches = results.map { ayah ->
                        Match(
                            ayah = ayah,
                            occurrences = QuranWordSearch.countMatches(ayah.text, tokens, mode),
                            spans = QuranWordSearch.matchSpans(ayah.text, tokens, mode),
                        )
                    }
                    // In EXACT mode the FTS prefix search can return ayahs where
                    // no whole word equals the token — drop them for accuracy.
                    val filtered = if (mode == QuranWordSearch.MatchMode.EXACT) {
                        matches.filter { it.occurrences > 0 }
                    } else {
                        matches
                    }
                    emit(
                        UiState(
                            searching = false,
                            matches = filtered,
                            occurrences = filtered.sumOf { it.occurrences },
                            surahBreakdown = QuranWordSearch.surahBreakdown(
                                filtered.map { it.ayah },
                                tokens,
                                mode,
                            ),
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())
}