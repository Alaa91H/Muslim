package org.example.islamicapp.feature.quran.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.quran.data.QuranPrefsRepository
import org.example.islamicapp.feature.quran.domain.Ayah
import org.example.islamicapp.feature.quran.domain.LastRead
import org.example.islamicapp.feature.quran.domain.QuranRepository
import org.example.islamicapp.feature.quran.domain.Surah
import javax.inject.Inject

@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val surahNumber: Int = savedStateHandle["surahNumber"] ?: 1

    /** Global ayah number to scroll to (search/bookmarks/last-read), -1 = none. */
    val initialAyahGlobal: Int = savedStateHandle["ayah"] ?: -1

    data class UiState(
        val loading: Boolean = true,
        val surah: Surah? = null,
        val ayahs: List<Ayah> = emptyList(),
    )

    /** The ayah currently in view; the UI updates this as the user scrolls. */
    val currentAyah = MutableStateFlow<Ayah?>(null)

    val uiState: StateFlow<UiState> = combine(
        repository.observeSurahMetadata(surahNumber),
        repository.observeSurah(surahNumber),
    ) { surah, ayahs ->
        UiState(loading = false, surah = surah, ayahs = ayahs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    val isBookmarked: StateFlow<Boolean> = combine(
        currentAyah,
        repository.observeBookmarks(),
    ) { ayah, bookmarks ->
        ayah != null && bookmarks.any { it.ayah.globalNumber == ayah.globalNumber }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleBookmark() {
        val ayah = currentAyah.value ?: return
        viewModelScope.launch {
            if (repository.isBookmarked(ayah.globalNumber)) {
                repository.removeBookmark(ayah.globalNumber)
            } else {
                repository.addBookmark(ayah)
            }
        }
    }

    /** Persists the currently viewed ayah as the resume position. */
    fun saveLastRead() {
        val ayah = currentAyah.value ?: return
        viewModelScope.launch {
            prefsRepository.saveLastRead(
                LastRead(
                    surahNumber = ayah.surahNumber,
                    globalNumber = ayah.globalNumber,
                    numberInSurah = ayah.numberInSurah,
                )
            )
        }
    }
}
