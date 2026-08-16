package org.example.islamicapp.feature.adhkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.adhkar.data.AdhkarRepository
import org.example.islamicapp.feature.adhkar.domain.Dhikr
import org.example.islamicapp.feature.adhkar.domain.DhikrCategory
import javax.inject.Inject

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val repository: AdhkarRepository,
) : ViewModel() {

    private val all: StateFlow<List<Dhikr>> = repository.observeAdhkar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedCategory = MutableStateFlow<DhikrCategory?>(null)

    /** Cache of per-dhikr counters so each card collects a stable flow. */
    private val counts = mutableMapOf<Long, StateFlow<Int>>()

    /** Adhkar filtered by the selected category (null = all). */
    val adhkar: StateFlow<List<Dhikr>> = combine(all, selectedCategory) { list, category ->
        if (category == null) list else list.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: List<DhikrCategory> = DhikrCategory.entries

    fun selectCategory(category: DhikrCategory?) {
        selectedCategory.value = category
    }

    fun count(dhikrId: Long): StateFlow<Int> = counts.getOrPut(dhikrId) {
        repository.observeCount(dhikrId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    }

    fun increment(dhikrId: Long) {
        viewModelScope.launch { repository.increment(dhikrId) }
    }

    fun reset(dhikrId: Long) {
        viewModelScope.launch { repository.reset(dhikrId) }
    }
}
