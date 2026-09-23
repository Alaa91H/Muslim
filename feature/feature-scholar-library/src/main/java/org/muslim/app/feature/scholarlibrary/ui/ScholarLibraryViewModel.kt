package org.muslim.app.feature.scholarlibrary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryImportResult
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryRepository
import org.muslim.app.feature.scholarlibrary.domain.FlashcardWithCitation
import org.muslim.app.feature.scholarlibrary.domain.ScholarAuthorSummary
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarBookHierarchy
import org.muslim.app.feature.scholarlibrary.domain.ScholarBookOutlineSection
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarHighlightStyle
import org.muslim.app.feature.scholarlibrary.domain.ScholarLibraryIndex
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.ScholarPathProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewEvent
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewRating
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewScheduler
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewSummary
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewActivity
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyActivity
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyActivitySummary
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyAnalytics
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategoryMastery
import org.muslim.app.feature.scholarlibrary.domain.ScholarSearchFilters
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPath
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPlan
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySession
import org.muslim.app.feature.scholarlibrary.domain.ScholarWeeklyStudySummary
import org.muslim.app.feature.scholarlibrary.domain.SearchHit
import org.muslim.app.feature.scholarlibrary.domain.StudyBookmarkWithCitation
import org.muslim.app.feature.scholarlibrary.domain.StudyHighlightWithCitation
import org.muslim.app.feature.scholarlibrary.domain.StudyNoteWithCitation

internal data class ScholarLibraryUiState(
    val loading: Boolean = true,
    val books: List<ScholarBook> = emptyList(),
    val authors: List<ScholarAuthorSummary> = emptyList(),
    val studyPaths: List<ScholarStudyPath> = emptyList(),
    val pathProgress: List<ScholarPathProgress> = emptyList(),
    val studyPlans: List<ScholarStudyPlan> = emptyList(),
    val studySessions: List<ScholarStudySession> = emptyList(),
    val weeklyStudySummaries: List<ScholarWeeklyStudySummary> = emptyList(),
    val selectedStudySession: ScholarStudySession? = null,
    val selectedSessionPassages: List<ScholarPassage> = emptyList(),
    val selectedSessionPathId: String? = null,
    val catalogMetadataLoading: Boolean = true,
    val selectedCategory: ScholarCategory? = null,
    val selectedDifficulty: ScholarDifficulty? = null,
    val selectedAuthorName: String? = null,
    val query: String = "",
    val searchResults: List<SearchHit> = emptyList(),
    val selectedBook: ScholarBook? = null,
    val selectedBookPassages: List<ScholarPassage> = emptyList(),
    val selectedBookOutline: List<ScholarBookOutlineSection> = emptyList(),
    val selectedBookHierarchy: ScholarBookHierarchy? = null,
    val notes: List<StudyNoteWithCitation> = emptyList(),
    val flashcards: List<FlashcardWithCitation> = emptyList(),
    val reviewSummary: ScholarReviewSummary = ScholarReviewSummary(0, 0, 0, 0, 0),
    val categoryMastery: List<ScholarCategoryMastery> = emptyList(),
    val reviewEvents: List<ScholarReviewEvent> = emptyList(),
    val studyActivitySummary: ScholarStudyActivitySummary = ScholarStudyActivitySummary(
        review = ScholarReviewActivity(
            reviewsToday = 0,
            reviewsLast7Days = 0,
            cardsReviewedLast7Days = 0,
            againLast7Days = 0,
            hardLast7Days = 0,
            goodLast7Days = 0,
            easyLast7Days = 0,
        ),
        study = ScholarStudyActivity(
            completedSessionsLast7Days = 0,
            studiedPassagesLast7Days = 0,
            dueCards = 0,
        ),
    ),
    val bookmarks: List<StudyBookmarkWithCitation> = emptyList(),
    val highlights: List<StudyHighlightWithCitation> = emptyList(),
    val readingProgress: List<ScholarReadingProgress> = emptyList(),
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
    private var sessionJob: Job? = null

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
            try {
                repository.ensureSeeded()
                refreshCatalogMetadata()
            } catch (_: Exception) {
                update {
                    it.copy(
                        catalogMetadataLoading = false,
                        statusMessage = "تعذر تجهيز المسارات الدراسية.",
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeNotes().collect { notes -> update { it.copy(notes = notes) } }
        }
        viewModelScope.launch {
            repository.observeFlashcards().collect { cards ->
                val now = System.currentTimeMillis()
                update {
                    it.copy(
                        flashcards = cards,
                        reviewSummary = ScholarReviewScheduler.reviewSummary(cards.map { item -> item.card }, now),
                        categoryMastery = ScholarReviewScheduler.categoryMastery(cards, now),
                        studyActivitySummary = studyActivitySummary(
                            state = it,
                            cards = cards,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeBookmarks().collect { bookmarks -> update { it.copy(bookmarks = bookmarks) } }
        }
        viewModelScope.launch {
            repository.observeHighlights().collect { highlights -> update { it.copy(highlights = highlights) } }
        }
        viewModelScope.launch {
            repository.observeReadingProgress().collect { progress ->
                update {
                    it.copy(
                        readingProgress = progress,
                        pathProgress = ScholarLibraryIndex.pathProgress(it.studyPaths, progress),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeStudyPlans().collect { plans ->
                update { it.copy(studyPlans = plans) }
            }
        }
        viewModelScope.launch {
            repository.observeStudySessions().collect { sessions ->
                update { state ->
                    val selected = state.selectedStudySession?.let { current ->
                        sessions.firstOrNull { it.id == current.id } ?: current
                    }
                    state.copy(
                        studySessions = sessions,
                        selectedStudySession = selected,
                        weeklyStudySummaries = state.studyPaths.map { path ->
                            ScholarLibraryIndex.weeklyStudySummary(
                                pathId = path.id,
                                sessions = sessions,
                                nowEpochMillis = System.currentTimeMillis(),
                            )
                        },
                        studyActivitySummary = studyActivitySummary(
                            state = state,
                            sessions = sessions,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeReviewEvents().collect { events ->
                update {
                    it.copy(
                        reviewEvents = events,
                        studyActivitySummary = studyActivitySummary(
                            state = it,
                            reviewEvents = events,
                        ),
                    )
                }
            }
        }
    }

    fun selectCategory(category: ScholarCategory?) {
        update { it.copy(selectedCategory = category) }
        refreshSearch()
    }

    fun selectDifficulty(difficulty: ScholarDifficulty?) {
        update { it.copy(selectedDifficulty = difficulty) }
        refreshSearch()
    }

    fun selectAuthor(authorName: String?) {
        update { it.copy(selectedAuthorName = authorName) }
        refreshSearch()
    }

    fun clearSearchFilters() {
        update {
            it.copy(
                selectedCategory = null,
                selectedDifficulty = null,
                selectedAuthorName = null,
            )
        }
        refreshSearch()
    }

    fun updateQuery(query: String) {
        update { it.copy(query = query, searchResults = if (query.isBlank()) emptyList() else it.searchResults) }
        refreshSearch()
    }

    private fun refreshSearch() {
        searchJob?.cancel()
        val snapshot = mutableState.value
        if (snapshot.query.isBlank()) {
            update { it.copy(searchResults = emptyList()) }
            return
        }
        val filters = ScholarSearchFilters(
            category = snapshot.selectedCategory,
            difficulty = snapshot.selectedDifficulty,
            authorName = snapshot.selectedAuthorName,
        )
        searchJob = viewModelScope.launch {
            val results = runCatching { repository.search(snapshot.query, filters) }.getOrDefault(emptyList())
            val current = mutableState.value
            if (
                current.query == snapshot.query &&
                current.selectedCategory == filters.category &&
                current.selectedDifficulty == filters.difficulty &&
                current.selectedAuthorName == filters.authorName
            ) {
                update { it.copy(searchResults = results) }
            }
        }
    }

    fun loadBook(bookId: String) {
        bookJob?.cancel()
        bookJob = viewModelScope.launch {
            val book = repository.book(bookId)
            val outline = if (book == null) emptyList() else repository.bookOutline(bookId)
            val hierarchy = if (book == null) null else repository.bookHierarchy(bookId)
            update {
                it.copy(
                    selectedBook = book,
                    selectedBookPassages = emptyList(),
                    selectedBookOutline = outline,
                    selectedBookHierarchy = hierarchy,
                )
            }
            if (book != null) {
                repository.markBookOpened(bookId)
                repository.observeBookPassages(bookId).collect { passages ->
                    update { it.copy(selectedBookPassages = passages) }
                }
            }
        }
    }

    fun clearBook() {
        bookJob?.cancel()
        update {
            it.copy(
                selectedBook = null,
                selectedBookPassages = emptyList(),
                selectedBookOutline = emptyList(),
                selectedBookHierarchy = null,
            )
        }
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

    fun reviewFlashcard(id: Long, rating: ScholarReviewRating) {
        viewModelScope.launch {
            val updated = repository.reviewFlashcard(id, rating)
            update {
                it.copy(
                    statusMessage = if (updated) {
                        when (rating) {
                            ScholarReviewRating.Again -> "ستعود البطاقة قريباً لتثبيت الاستدعاء."
                            ScholarReviewRating.Hard -> "تم حفظ تقييم صعب وتقصير الفاصل التالي."
                            ScholarReviewRating.Good -> "تم حفظ تقييم جيد وتحديد المراجعة التالية."
                            ScholarReviewRating.Easy -> "تم حفظ تقييم سهل وتوسيع الفاصل التالي."
                        }
                    } else {
                        "تعذر تحديث البطاقة."
                    },
                )
            }
        }
    }

    fun createReviewCardFromPassage(passage: ScholarPassage) {
        val front = buildString {
            append("مراجعة: ")
            append(passage.chapter)
            passage.section?.takeIf { it.isNotBlank() }?.let { append(" — ").append(it) }
        }.take(1_000)
        val back = passage.text.take(1_000)
        addFlashcard(passage.id, front, back)
    }

    fun deleteFlashcard(id: Long) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
            update { it.copy(statusMessage = "حُذفت البطاقة.") }
        }
    }

    fun toggleBookmark(passageId: String) {
        val shouldAdd = mutableState.value.bookmarks.none { it.bookmark.passageId == passageId }
        viewModelScope.launch {
            val updated = repository.setBookmark(passageId, shouldAdd)
            update {
                it.copy(
                    statusMessage = when {
                        !updated -> "تعذر تحديث الإشارة المرجعية."
                        shouldAdd -> "أُضيف المقطع إلى الإشارات المرجعية."
                        else -> "أُزيل المقطع من الإشارات المرجعية."
                    },
                )
            }
        }
    }

    fun togglePassageHighlight(passage: ScholarPassage) {
        val existing = mutableState.value.highlights.firstOrNull { it.highlight.passageId == passage.id }
        viewModelScope.launch {
            val updated = if (existing == null) {
                repository.addHighlight(
                    passageId = passage.id,
                    quote = passage.text,
                    style = ScholarHighlightStyle.Important,
                )
            } else {
                repository.deleteHighlight(existing.highlight.id)
                true
            }
            update {
                it.copy(
                    statusMessage = when {
                        !updated -> "تعذر تحديث التظليل."
                        existing == null -> "حُفظ المقطع ضمن التظليلات."
                        else -> "أُزيل التظليل."
                    },
                )
            }
        }
    }

    fun markStudied(bookId: String, passageId: String, progressPercent: Int) {
        viewModelScope.launch {
            val updated = repository.updateReadingProgress(bookId, passageId, progressPercent)
            update {
                it.copy(
                    statusMessage = if (updated) {
                        if (progressPercent >= 100) "اكتملت دراسة هذا الكتاب." else "حُفظ تقدمك الدراسي."
                    } else {
                        "تعذر تحديث تقدم القراءة."
                    },
                )
            }
        }
    }

    fun deleteHighlight(id: Long) {
        viewModelScope.launch {
            repository.deleteHighlight(id)
            update { it.copy(statusMessage = "حُذف التظليل.") }
        }
    }

    fun createDailyStudyPlan(pathId: String) {
        saveStudyPlan(
            pathId = pathId,
            sessionsPerWeek = 7,
            minutesPerSession = 20,
            targetPassagesPerSession = 1,
            successMessage = "تم تفعيل خطة يومية للمسار.",
        )
    }

    fun createWeeklyStudyPlan(pathId: String) {
        saveStudyPlan(
            pathId = pathId,
            sessionsPerWeek = 3,
            minutesPerSession = 45,
            targetPassagesPerSession = 2,
            successMessage = "تم تفعيل خطة أسبوعية للمسار.",
        )
    }

    fun deleteStudyPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteStudyPlan(id)
            update { it.copy(statusMessage = "حُذفت خطة الدراسة.") }
        }
    }

    fun loadStudySession(pathId: String) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            val session = repository.startOrResumeStudySession(pathId)
            val passages = session?.let { repository.studySessionPassages(it.id) }.orEmpty()
            update {
                it.copy(
                    selectedSessionPathId = pathId,
                    selectedStudySession = session,
                    selectedSessionPassages = passages,
                    statusMessage = if (session == null) {
                        "لا توجد جلسة متاحة؛ فعّل خطة وتأكد من وجود مادة متبقية في المسار."
                    } else {
                        it.statusMessage
                    },
                )
            }
        }
    }

    fun completeNextStudySessionPassage(passageId: String) {
        val session = mutableState.value.selectedStudySession ?: return
        viewModelScope.launch {
            val completed = repository.completeNextSessionPassage(session.id, passageId)
            update {
                it.copy(
                    statusMessage = if (completed) {
                        "تم تسجيل دراسة المقطع وتحديث تقدم الكتاب."
                    } else {
                        "تعذر تسجيل المقطع؛ يجب إكمال أهداف الجلسة بالترتيب."
                    },
                )
            }
        }
    }

    fun startNextStudySession() {
        val pathId = mutableState.value.selectedSessionPathId ?: return
        loadStudySession(pathId)
    }

    private fun saveStudyPlan(
        pathId: String,
        sessionsPerWeek: Int,
        minutesPerSession: Int,
        targetPassagesPerSession: Int,
        successMessage: String,
    ) {
        viewModelScope.launch {
            val saved = repository.createStudyPlan(
                pathId = pathId,
                sessionsPerWeek = sessionsPerWeek,
                minutesPerSession = minutesPerSession,
                targetPassagesPerSession = targetPassagesPerSession,
            )
            update {
                it.copy(statusMessage = if (saved) successMessage else "تعذر حفظ خطة الدراسة.")
            }
        }
    }

    fun importPack(rawText: String) {
        viewModelScope.launch {
            update { it.copy(statusMessage = "يجري فحص الحزمة واستيرادها محلياً…") }
            when (val result = repository.importPack(rawText)) {
                is ScholarLibraryImportResult.Success -> {
                    runCatching { refreshCatalogMetadata() }
                    update {
                        it.copy(
                            statusMessage = "تم استيراد ${result.importedBooks} كتب و${result.importedPassages} مقاطع مرخّصة.",
                        )
                    }
                }
                is ScholarLibraryImportResult.Failure -> update { it.copy(statusMessage = result.message) }
            }
        }
    }

    fun consumeStatusMessage() = update { it.copy(statusMessage = null) }

    private suspend fun refreshCatalogMetadata() {
        val authors = repository.authors()
        val paths = repository.studyPaths()
        update {
            it.copy(
                authors = authors,
                studyPaths = paths,
                pathProgress = ScholarLibraryIndex.pathProgress(paths, it.readingProgress),
                weeklyStudySummaries = paths.map { path ->
                    ScholarLibraryIndex.weeklyStudySummary(
                        pathId = path.id,
                        sessions = it.studySessions,
                        nowEpochMillis = System.currentTimeMillis(),
                    )
                },
                catalogMetadataLoading = false,
            )
        }
    }

    private fun studyActivitySummary(
        state: ScholarLibraryUiState,
        cards: List<FlashcardWithCitation> = state.flashcards,
        sessions: List<ScholarStudySession> = state.studySessions,
        reviewEvents: List<ScholarReviewEvent> = state.reviewEvents,
    ): ScholarStudyActivitySummary {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        return ScholarStudyAnalytics.activitySummary(
            reviewEvents = reviewEvents,
            sessions = sessions,
            cards = cards.map { it.card },
            nowEpochMillis = now,
            todayStartEpochMillis = todayStart,
            sevenDaysStartEpochMillis = now - SEVEN_DAYS_MILLIS,
        )
    }

    private fun update(transform: (ScholarLibraryUiState) -> ScholarLibraryUiState) {
        mutableState.value = transform(mutableState.value)
    }

    private companion object {
        const val SEVEN_DAYS_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
