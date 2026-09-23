package org.muslim.app.feature.learn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.learn.data.FuneralWillIntroVisibility
import org.muslim.app.feature.learn.data.FuneralWillPreferencesRepository
import org.muslim.app.feature.learn.data.WillDraftRepository
import org.muslim.app.feature.learn.domain.WillDraft
import javax.inject.Inject

@HiltViewModel
class FuneralWillViewModel @Inject constructor(
    private val willDraftRepository: WillDraftRepository,
    private val preferencesRepository: FuneralWillPreferencesRepository,
) : ViewModel() {
    init {
        viewModelScope.launch {
            willDraftRepository.migrateLegacyIfNeeded()
        }
    }

    val draft: StateFlow<WillDraft> = willDraftRepository.draft.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WillDraft(),
    )

    val storageError: StateFlow<Boolean> = willDraftRepository.storageState
        .map { it.encryptionError }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val introVisibility: StateFlow<FuneralWillIntroVisibility> =
        preferencesRepository.introVisibility.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FuneralWillIntroVisibility(),
        )

    fun save(draft: WillDraft) {
        viewModelScope.launch { willDraftRepository.save(draft) }
    }

    fun clear() {
        viewModelScope.launch { willDraftRepository.clear() }
    }

    fun dismissDraftIntro() {
        viewModelScope.launch { preferencesRepository.dismissDraftIntro() }
    }

    fun dismissLegalNotice() {
        viewModelScope.launch { preferencesRepository.dismissLegalNotice() }
    }

    fun dismissPrivacyNotice() {
        viewModelScope.launch { preferencesRepository.dismissPrivacyNotice() }
    }

    fun restoreIntroCards() {
        viewModelScope.launch { preferencesRepository.restoreIntroCards() }
    }
}
