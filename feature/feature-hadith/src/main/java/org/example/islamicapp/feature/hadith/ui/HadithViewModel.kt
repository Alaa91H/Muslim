package org.example.islamicapp.feature.hadith.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.hadith.data.HadithPrefsRepository
import org.example.islamicapp.feature.hadith.data.HadithRepository
import org.example.islamicapp.feature.hadith.domain.Hadith
import org.example.islamicapp.feature.hadith.domain.HadithCollection
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HadithViewModel @Inject constructor(
    private val repository: HadithRepository,
    private val prefsRepository: HadithPrefsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _collection = MutableStateFlow<HadithCollection?>(null)
    val collection: StateFlow<HadithCollection?> = _collection

    val bookmarkedIds: StateFlow<Set<Long>> = prefsRepository.bookmarkedIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _daily = MutableStateFlow<Hadith?>(null)
    val daily: StateFlow<Hadith?> = _daily

    val hadiths: StateFlow<List<Hadith>> = combine(_query, _collection) { q, collection -> q to collection }
        .flatMapLatest { (q, collection) ->
            if (q.isBlank()) {
                if (collection == null) {
                    repository.observeAll()
                } else {
                    repository.observeCollection(collection)
                }
            } else {
                flowOf(repository.search(q.trim()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { _daily.value = repository.hadithOfTheDay() }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setCollection(collection: HadithCollection?) {
        _collection.value = collection
    }

    fun toggleBookmark(id: Long) {
        viewModelScope.launch {
            val ids = prefsRepository.bookmarkedIds.first()
            if (id in ids) prefsRepository.removeBookmark(id) else prefsRepository.addBookmark(id)
        }
    }
}
