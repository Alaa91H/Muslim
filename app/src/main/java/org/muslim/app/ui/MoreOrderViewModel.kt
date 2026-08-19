package org.muslim.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import javax.inject.Inject

/**
 * Drives the "More hub order" customization screen: exposes the current section
 * order and lets the user move sections up/down or reset to the default.
 */
@HiltViewModel
class MoreOrderViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val sectionOrder: StateFlow<List<String>> =
        appPreferencesRepository.preferences
            .map { it.moreSectionOrder }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences.DEFAULT_MORE_SECTION_ORDER)

    /** Moves the section at [index] one position up (toward the top). */
    fun moveUp(index: Int) = reorder { current ->
        if (index <= 0) current else current.toMutableList().also { list ->
            val item = list.removeAt(index)
            list.add(index - 1, item)
        }
    }

    /** Moves the section at [index] one position down (toward the bottom). */
    fun moveDown(index: Int) = reorder { current ->
        if (index >= current.lastIndex) current else current.toMutableList().also { list ->
            val item = list.removeAt(index)
            list.add(index + 1, item)
        }
    }

    fun reset() = reorder { AppPreferences.DEFAULT_MORE_SECTION_ORDER }

    private fun reorder(transform: (List<String>) -> List<String>) {
        viewModelScope.launch {
            val current = sectionOrder.value
            val next = transform(current)
            if (next != current) appPreferencesRepository.setMoreSectionOrder(next)
        }
    }
}
