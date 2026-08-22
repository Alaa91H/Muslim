package org.muslim.app.feature.learn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.learn.data.WillDraftRepository
import org.muslim.app.feature.learn.domain.WillDraft
import javax.inject.Inject

@HiltViewModel
class FuneralWillViewModel @Inject constructor(
    private val willDraftRepository: WillDraftRepository,
) : ViewModel() {
    val draft: StateFlow<WillDraft> = willDraftRepository.draft.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WillDraft(),
    )

    fun save(draft: WillDraft) {
        viewModelScope.launch { willDraftRepository.save(draft) }
    }

    fun clear() {
        viewModelScope.launch { willDraftRepository.clear() }
    }
}
