package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.feature.quran.domain.Bookmark
import org.muslim.app.feature.quran.domain.QuranRepository
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    repository: QuranRepository,
) : ViewModel() {

    val bookmarks: StateFlow<List<Bookmark>> = repository.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
