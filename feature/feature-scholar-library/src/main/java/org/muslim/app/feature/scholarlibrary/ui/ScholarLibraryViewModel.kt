package org.muslim.app.feature.scholarlibrary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryImportResult
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryRepository
import org.muslim.app.feature.scholarlibrary.domain.FlashcardWithCitation
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.SearchHit
import org.muslim.app.feature.scholarlibrary.domain.StudyNoteWithCitation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ScholarLibraryUiState(
    val loading: Boolean = true,
    val books: List<ScholarBook> = emptyList(),
    val selectedCategory: ScholarCategory? = null,
    val query: String = "",
    val searchResults: List<SearchHit> = emptyList(),
    val selectedBook: ScholarBook? = null,
    val selectedBookPassages: List<ScholarPassage> = emptyList(),
    val notes: List<StudyNoteWithCitation> = emptyList(),
    val flashcards: List<FlashcardWithCitation> = emptyList(),
    val statusMessage: String? = null,
)

@HiltViewModel
class ScholarLibraryViewModel @Inject constructor(
    private val repository: ScholarLibraryRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ScholarLibraryUiState())
    internal val state: StateFlow<ScholarLibraryUiState> = mutableState.asStateFlow()

    private var searchJob: Job? = null
    private var bookJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { repository.ensureSeeded() }
                .onFailure { update { it.copy(loading = false, statusMessage = "تعذر تجهيز فهرس المكتبة.") } }
                .onSuccess { update { it.copy(loading = false) } }
        }
        viewModelScope.launch {
            repository.observeBooks().collect { books -> update { it.copy(books = books) } }
        }
        viewModelScope.launch {
            repository.observeNotes().collect { notes -> update { it.copy(notes = notes) } }
        }
        viewModelScope.launch {
            repository.observeFlashcards().collect { cards -> update { it.copy(flashcards = cards) } }
        }
    }

    fun selectCategory(category: ScholarCategory?) = update { it.copy(selectedCategory = category) }

    fun updateQuery(query: String) {
        update { it.copy(query = query, searchResults = if (query.isBlank()) emptyList() else it.searchResults) }
        searchJob?.cancel()
        if (query.isBlank()) return
        searchJob = viewModelScope.launch {
            val results = runCatching { repository.search(query) }.getOrDefault(emptyList())
            if (mutableState.value.query == query) update { it.copy(searchResults = results) }
        }
    }

    fun loadBook(bookId: String) {
        bookJob?.cancel()
        bookJob = viewModelScope.launch {
            val book = repository.book(bookId)
            update { it.copy(selectedBook = book, selectedBookPassages = emptyList()) }
            if (book != null) {
                repository.observeBookPassages(bookId).collect { passages ->
                    update { it.copy(selectedBookPassages = passages) }
                }
            }
        }
    }

    fun clearBook() {
        bookJob?.cancel()
        update { it.copy(selectedBook = null, selectedBookPassages = emptyList()) }
    }

    fun addNote(passageId: String, text: String) {
        viewModelScope.launch {
            val saved = repository.addNote(passageId, text)
            update {
                it.copy(statusMessage = if (saved) "حُفظت الملاحظة مع مرجعها." else "تعذر حفظ الملاحظة؛ تحقّق من النص.")
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            update { it.copy(statusMessage = "حُذفت الملاحظة.") }
        }
    }

    fun addFlashcard(passageId: String, front: String, back: String) {
        viewModelScope.launch {
            val saved = repository.addFlashcard(passageId, front, back)
            update {
                it.copy(statusMessage = if (saved) "أُضيفت البطاقة إلى المراجعة." else "تعذر حفظ البطاقة؛ اجعل السؤال والجواب غير فارغين.")
            }
        }
    }

    fun reviewFlashcard(id: Long, remembered: Boolean) {
        viewModelScope.launch {
            repository.reviewFlashcard(id, remembered)
            update {
                it.copy(
                    statusMessage = if (remembered) "حُدد موعد المراجعة التالية." else "أُعيدت البطاقة للمراجعة الآن.",
                )
            }
        }
    }

    fun deleteFlashcard(id: Long) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
            update { it.copy(statusMessage = "حُذفت البطاقة.") }
        }
    }

    fun importPack(rawText: String) {
        viewModelScope.launch {
            update { it.copy(statusMessage = "يجري فحص الحزمة واستيرادها محلياً…") }
            when (val result = repository.importPack(rawText)) {
                is ScholarLibraryImportResult.Success -> update {
                    it.copy(
                        statusMessage = "تم استيراد ${result.importedBooks} كتب و${result.importedPassages} مقاطع مرخّصة.",
                    )
                }
                is ScholarLibraryImportResult.Failure -> update { it.copy(statusMessage = result.message) }
            }
        }
    }

    fun consumeStatusMessage() = update { it.copy(statusMessage = null) }

    private fun update(transform: (ScholarLibraryUiState) -> ScholarLibraryUiState) {
        mutableState.value = transform(mutableState.value)
    }
}
