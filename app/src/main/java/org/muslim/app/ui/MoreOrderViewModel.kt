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
 * Drives the "More hub" customization screen: exposes the current section
 * order + visibility and lets the user reorder (drag & drop) or show/hide
 * each section. Both are persisted in DataStore and applied live by
 * [MoreScreen].
 */
@HiltViewModel
class MoreOrderViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val sectionOrder: StateFlow<List<String>> =
        appPreferencesRepository.preferences
            .map { it.moreSectionOrder }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences.DEFAULT_MORE_SECTION_ORDER)

    val hiddenSections: StateFlow<Set<String>> =
        appPreferencesRepository.preferences
            .map { it.hiddenMoreSections }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Applies a full reorder (used on drag-drop end). */
    fun setOrder(order: List<String>) {
        val normalized = AppPreferences.decodeSectionOrder(order.joinToString(","))
        if (normalized != sectionOrder.value) {
            viewModelScope.launch { appPreferencesRepository.setMoreSectionOrder(normalized) }
        }
    }

    /** Shows or hides one section. */
    fun setSectionHidden(sectionId: String, hidden: Boolean) {
        viewModelScope.launch {
            val next = hiddenSections.value.toMutableSet().apply {
                if (hidden) add(sectionId) else remove(sectionId)
            }
            appPreferencesRepository.setHiddenMoreSections(next)
        }
    }

    fun reset() {
        viewModelScope.launch {
            appPreferencesRepository.setMoreSectionOrder(AppPreferences.DEFAULT_MORE_SECTION_ORDER)
            appPreferencesRepository.setHiddenMoreSections(emptySet())
        }
    }
}
