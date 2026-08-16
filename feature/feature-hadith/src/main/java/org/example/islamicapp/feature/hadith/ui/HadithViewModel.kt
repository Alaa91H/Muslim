package org.example.islamicapp.feature.hadith.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.hadith.data.HadithBookmarksRepository
import org.example.islamicapp.feature.hadith.data.HadithDto
import org.example.islamicapp.feature.hadith.notifications.HadithOfDayScheduler
import org.example.islamicapp.feature.hadith.data.HadithLibraryRepository
import org.example.islamicapp.feature.hadith.data.HadithRepository
import org.example.islamicapp.feature.hadith.data.LoadedBook
import org.example.islamicapp.feature.hadith.domain.Hadith
import javax.inject.Inject

data class HadithUiState(
    val query: String = "",
    val hadiths: List<Hadith> = emptyList(),
    val bookmarksOnly: Boolean = false,
    val bookmarks: Set<String> = emptySet(),
    val dailyNotificationEnabled: Boolean = false,
    /** Currently expanded card, or null. */
    val expandedId: String? = null,

    // ---- Complete library ----
    /** Which tab is open: 0 = Nawawi selection, 1 = full library. */
    val tab: Int = 0,
    /** Open book in the library, or null to show the catalog. */
    val openBook: LoadedBook? = null,
    val loadingBook: Boolean = false,
    val libraryError: String? = null,
    /** Library search results across the open book. */
    val bookResults: List<HadithDto> = emptyList(),
    val bookQuery: String = "",
)

@HiltViewModel
class HadithViewModel @Inject constructor(
    private val repository: HadithRepository,
    private val bookmarksRepository: HadithBookmarksRepository,
    private val dailyScheduler: HadithOfDayScheduler,
    val library: HadithLibraryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val bookmarksOnly = MutableStateFlow(false)
    private val expanded = MutableStateFlow<String?>(null)
    private val tab = MutableStateFlow(0)
    private val openBook = MutableStateFlow<LoadedBook?>(null)
    private val loadingBook = MutableStateFlow(false)
    private val libraryError = MutableStateFlow<String?>(null)
    private val bookQuery = MutableStateFlow("")

    val uiState: StateFlow<HadithUiState> = combine(
        query,
        bookmarksOnly,
        expanded,
        bookmarksRepository.bookmarks,
        bookmarksRepository.dailyNotificationEnabled,
    ) { q, only, exp, bookmarks, daily ->
        val base = if (only) {
            repository.hadiths.filter { it.id in bookmarks }
        } else {
            repository.search(q)
        }
        HadithUiState(
            query = q,
            hadiths = base,
            bookmarksOnly = only,
            bookmarks = bookmarks,
            dailyNotificationEnabled = daily,
            expandedId = exp,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HadithUiState())

    /** Library screen state (kept separate from the selection tab). */
    val libraryState: StateFlow<HadithUiState> = combine(
        combine(openBook, loadingBook, libraryError) { book, loading, error ->
            Triple(book, loading, error)
        },
        combine(tab, bookQuery) { t, q -> t to q },
    ) { (book, loading, error), (t, q) ->
        HadithUiState(
            tab = t,
            openBook = book,
            loadingBook = loading,
            libraryError = error,
            bookQuery = q,
            bookResults = if (book == null || q.isBlank()) emptyList()
            else book.hadiths.filter { it.text.contains(q, ignoreCase = true) }.take(200),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HadithUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setBookmarksOnly(value: Boolean) {
        bookmarksOnly.value = value
    }

    fun toggleExpanded(id: String) {
        expanded.value = if (expanded.value == id) null else id
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch { bookmarksRepository.toggleBookmark(id) }
    }

    fun setDailyNotification(enabled: Boolean) {
        viewModelScope.launch {
            bookmarksRepository.setDailyNotification(enabled)
            dailyScheduler.schedule(enabled)
        }
    }

    // ---- Library actions ----

    fun setTab(index: Int) {
        tab.value = index
    }

    fun setBookQuery(value: String) {
        bookQuery.value = value
    }

    /** Opens a book: bundled instantly, downloaded once then cached forever. */
    fun openBookById(bookId: String) {
        viewModelScope.launch {
            loadingBook.value = true
            libraryError.value = null
            val result = library.load(bookId)
            loadingBook.value = false
            result.fold(
                onSuccess = { openBook.value = it; bookQuery.value = "" },
                onFailure = { libraryError.value = it.message },
            )
        }
    }

    fun closeBook() {
        openBook.value = null
    }
}
