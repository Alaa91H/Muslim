package org.example.islamicapp.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.feature.quran.data.QuranPrefsRepository
import org.example.islamicapp.feature.quran.domain.LastRead
import org.example.islamicapp.feature.quran.domain.QuranRepository
import org.example.islamicapp.feature.quran.domain.Surah
import javax.inject.Inject

@HiltViewModel
class SurahListViewModel @Inject constructor(
    repository: QuranRepository,
    prefsRepository: QuranPrefsRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val surahs: List<Surah> = emptyList(),
        val lastRead: LastRead? = null,
    )

    val uiState: StateFlow<UiState> = combine(
        repository.observeSurahs().onStart { emit(emptyList()) },
        prefsRepository.lastRead,
    ) { surahs, lastRead ->
        UiState(loading = surahs.isEmpty(), surahs = surahs, lastRead = lastRead)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())
}
