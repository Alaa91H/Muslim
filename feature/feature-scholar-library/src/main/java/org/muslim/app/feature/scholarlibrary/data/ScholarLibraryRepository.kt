package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.scholarlibrary.domain.Citation
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
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewRating
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewScheduler
import org.muslim.app.feature.scholarlibrary.domain.ScholarSearchFilters
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPath
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPlan
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySession
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySessionStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyStage
import org.muslim.app.feature.scholarlibrary.domain.SearchHit
import org.muslim.app.feature.scholarlibrary.domain.StudyBookmarkWithCitation
import org.muslim.app.feature.scholarlibrary.domain.StudyHighlightWithCitation
import org.muslim.app.feature.scholarlibrary.domain.StudyNoteWithCitation

sealed interface ScholarLibraryImportResult {
    data class Success(val importedBooks: Int, val importedPassages: Int) : ScholarLibraryImportResult
    data class Failure(val message: String) : ScholarLibraryImportResult
}

@Serializable
private data class ScholarPack(
    val schemaVersion: Int,
    val packName: String,
    val licenseNotice: String,
    val books: List<ScholarPackBook>,
)

@Serializable
private data class ScholarPackBook(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val authorDeathYearHijri: Int? = null,
    val description: String,
    val sourceName: String,
    val sourceUrl: String? = null,
    val licenseSummary: String,
    val passages: List<ScholarPackPassage>,
    val subtitle: String? = null,
    val language: String = "ar",
    val difficulty: String = ScholarDifficulty.Unspecified.name,
    val publisher: String? = null,
    val edition: String? = null,
    val editor: String? = null,
    val publicationYear: String? = null,
    val volumeCount: Int? = null,
    val keywords: List<String> = emptyList(),
)

@Serializable
private data class ScholarPackPassage(
    val id: String,
    val chapter: String,
    val volume: String? = null,
    val page: String? = null,
    val text: String,
    val section: String? = null,
    val orderIndex: Int = 0,
)

@Serializable
private data class ScholarStudyPathPack(
    val schemaVersion: Int,
    val notice: String,
    val paths: List<ScholarStudyPathItem>,
)

@Serializable
private data class ScholarStudyPathItem(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val level: String,
    val stages: List<ScholarStudyStageItem>,
)

@Serializable
private data class ScholarStudyStageItem(
    val id: String,
    val title: String,
    val description: String,
    val bookIds: List<String>,
)

/**
 * Local, citation-first study library. The shipped catalog is editorial metadata
 * and study guidance only. Full texts are added only through packs that state a
 * source and reuse permission, never by downloading or copying other libraries.
 */
@Singleton
class ScholarLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryDao: ScholarLibraryDao,
    private val ftsDao: ScholarLibraryFtsDao,
    private val json: Json,
) {
    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            if (libraryDao.bookCount() == 0) {
                val bundled = context.assets.open(BUNDLED_CATALOG).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val pack = decodeAndValidate(bundled)
                persistPack(pack, imported = false)
            }
            seeded.set(true)
        }
    }

    fun observeBooks(): Flow<List<ScholarBook>> = libraryDao.observeBooks().map { books ->
        books.map { it.toDomain() }
    }

    fun observeBookPassages(bookId: String): Flow<List<ScholarPassage>> =
        libraryDao.observePassagesForBook(bookId).map { passages -> passages.map { it.toDomain() } }

    fun observeNotes(): Flow<List<StudyNoteWithCitation>> = libraryDao.observeNotes().map { notes ->
        notes.mapNotNull { note ->
            citationForPassage(note.passageId)?.let { citation ->
                StudyNoteWithCitation(note.toDomain(), citation)
            }
        }
    }

    fun observeFlashcards(): Flow<List<FlashcardWithCitation>> = libraryDao.observeFlashcards().map { cards ->
        cards.mapNotNull { card ->
            val passage = libraryDao.passageById(card.passageId)?.toDomain() ?: return@mapNotNull null
            val book = libraryDao.bookById(passage.bookId)?.toDomain() ?: return@mapNotNull null
            FlashcardWithCitation(
                card = card.toDomain(),
                citation = Citation(
                    bookTitle = book.title,
                    author = book.author,
                    chapter = passage.chapter,
                    volume = passage.volume,
                    page = passage.page,
                    edition = book.edition,
                    publisher = book.publisher,
                    publicationYear = book.publicationYear,
                    section = passage.section,
                ),
                bookId = book.id,
                category = book.category,
            )
        }
    }

    fun observeBookmarks(): Flow<List<StudyBookmarkWithCitation>> = libraryDao.observeBookmarks().map { bookmarks ->
        bookmarks.mapNotNull { bookmark ->
            citationForPassage(bookmark.passageId)?.let { citation ->
                StudyBookmarkWithCitation(bookmark.toDomain(), citation)
            }
        }
    }

    fun observeHighlights(): Flow<List<StudyHighlightWithCitation>> = libraryDao.observeHighlights().map { highlights ->
        highlights.mapNotNull { highlight ->
            citationForPassage(highlight.passageId)?.let { citation ->
                StudyHighlightWithCitation(highlight.toDomain(), citation)
            }
        }
    }

    fun observeReadingProgress(): Flow<List<ScholarReadingProgress>> =
        libraryDao.observeReadingProgress().map { rows -> rows.map { it.toDomain() } }

    fun observeStudyPlans(): Flow<List<ScholarStudyPlan>> =
        libraryDao.observeStudyPlans().map { rows -> rows.map { it.toDomain() } }

    fun observeStudySessions(): Flow<List<ScholarStudySession>> =
        libraryDao.observeStudySessions().map { rows -> rows.map { it.toDomain() } }

    suspend fun book(bookId: String): ScholarBook? = libraryDao.bookById(bookId)?.toDomain()

    suspend fun authors(): List<ScholarAuthorSummary> {
        ensureSeeded()
        return ScholarLibraryIndex.authors(
            libraryDao.observeBooks().first().map { it.toDomain() },
        )
    }

    suspend fun bookOutline(bookId: String): List<ScholarBookOutlineSection> {
        ensureSeeded()
        return ScholarLibraryIndex.outline(
            libraryDao.observePassagesForBook(bookId).first().map { it.toDomain() },
        )
    }

    suspend fun bookHierarchy(bookId: String): ScholarBookHierarchy {
        ensureSeeded()
        return ScholarLibraryIndex.hierarchy(
            libraryDao.observePassagesForBook(bookId).first().map { it.toDomain() },
        )
    }

    suspend fun studyPaths(): List<ScholarStudyPath> {
        ensureSeeded()
        val raw = context.assets.open(BUNDLED_STUDY_PATHS).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val pack = json.decodeFromString<ScholarStudyPathPack>(raw)
        require(pack.schemaVersion == STUDY_PATH_SCHEMA_VERSION) { "إصدار مسارات الدراسة غير مدعوم." }
        require(pack.notice.isNotBlank()) { "يجب توضيح طبيعة المسارات الدراسية." }
        val bookIds = libraryDao.observeBooks().first().map { it.id }.toSet()
        return pack.paths.map { path ->
            require(ID_REGEX.matches(path.id) && path.title.isNotBlank() && path.summary.isNotBlank()) {
                "مسار دراسي غير صالح."
            }
            require(path.stages.isNotEmpty()) { "المسار الدراسي يجب أن يحتوي على مرحلة واحدة على الأقل." }
            val stages = path.stages.map { stage ->
                require(ID_REGEX.matches(stage.id) && stage.title.isNotBlank() && stage.description.isNotBlank()) {
                    "مرحلة دراسية غير صالحة."
                }
                require(stage.bookIds.isNotEmpty() && stage.bookIds.all { it in bookIds }) {
                    "المسار الدراسي يشير إلى كتاب غير موجود."
                }
                ScholarStudyStage(stage.id, stage.title, stage.description, stage.bookIds)
            }
            ScholarStudyPath(
                id = path.id,
                title = path.title,
                summary = path.summary,
                category = ScholarCategory.fromId(path.category),
                level = ScholarDifficulty.fromId(path.level),
                stages = stages,
            )
        }
    }

    suspend fun search(
        rawQuery: String,
        filters: ScholarSearchFilters = ScholarSearchFilters(),
    ): List<SearchHit> {
        ensureSeeded()
        val books = libraryDao.observeBooks().first().map { it.toDomain() }
        val booksById = books.associateBy { it.id }
        val eligibleBooks = books.filter { ScholarLibraryIndex.matches(it, filters) }.associateBy { it.id }
        if (eligibleBooks.isEmpty()) return emptyList()

        val normalized = ArabicText.normalizeForSearch(rawQuery.trim())
        val metadataMatches = if (normalized.isBlank()) {
            emptyList()
        } else {
            eligibleBooks.values.filter { book -> ScholarLibraryIndex.matchesMetadataQuery(book, normalized) }
        }

        val passageIds = linkedSetOf<String>()
        val ftsQuery = ScholarSearchQuery.build(rawQuery)
        if (ftsQuery.isNotEmpty()) {
            ftsDao.searchPassageIds(ftsQuery, SEARCH_CANDIDATE_LIMIT)
                .distinct()
                .forEach { id ->
                    val passage = libraryDao.passageById(id)
                    if (passage != null && passage.bookId in eligibleBooks) passageIds += id
                }
        }
        metadataMatches.forEach { book ->
            libraryDao.observePassagesForBook(book.id).first()
                .take(METADATA_MATCH_PASSAGES_PER_BOOK)
                .forEach { passageIds += it.id }
        }

        return passageIds.take(SEARCH_LIMIT).mapNotNull { id ->
            val passage = libraryDao.passageById(id)?.toDomain() ?: return@mapNotNull null
            val book = booksById[passage.bookId] ?: return@mapNotNull null
            SearchHit(
                passage = passage,
                citation = Citation(
                    bookTitle = book.title,
                    author = book.author,
                    chapter = passage.chapter,
                    volume = passage.volume,
                    page = passage.page,
                    edition = book.edition,
                    publisher = book.publisher,
                    publicationYear = book.publicationYear,
                    section = passage.section,
                ),
            )
        }
    }

    suspend fun addNote(passageId: String, text: String): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null || text.trim().length !in 1..NOTE_MAX_LENGTH) return false
        libraryDao.insertNote(
            ScholarNoteEntity(
                passageId = passageId,
                text = text.trim(),
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        return true
    }

    suspend fun deleteNote(id: Long) = libraryDao.deleteNote(id)

    suspend fun addFlashcard(passageId: String, front: String, back: String): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null) return false
        if (front.trim().length !in 1..FLASHCARD_SIDE_MAX_LENGTH) return false
        if (back.trim().length !in 1..FLASHCARD_SIDE_MAX_LENGTH) return false
        val now = System.currentTimeMillis()
        libraryDao.insertFlashcard(
            ScholarFlashcardEntity(
                passageId = passageId,
                front = front.trim(),
                back = back.trim(),
                reviewCount = 0,
                dueAtEpochMillis = now,
                createdAtEpochMillis = now,
            ),
        )
        return true
    }

    suspend fun reviewFlashcard(id: Long, rating: ScholarReviewRating): Boolean {
        val entity = libraryDao.flashcardById(id) ?: return false
        val schedule = ScholarReviewScheduler.schedule(
            card = entity.toDomain(),
            rating = rating,
            reviewedAtEpochMillis = System.currentTimeMillis(),
        )
        libraryDao.updateFlashcardReview(
            id = id,
            reviewCount = schedule.reviewCount,
            dueAt = schedule.dueAtEpochMillis,
            intervalDays = schedule.intervalDays,
            easeFactor = schedule.easeFactor,
            lapseCount = schedule.lapseCount,
            lastReviewedAt = schedule.lastReviewedAtEpochMillis,
            lastRating = schedule.lastRating.name,
        )
        return true
    }

    suspend fun deleteFlashcard(id: Long) = libraryDao.deleteFlashcard(id)

    suspend fun setBookmark(passageId: String, bookmarked: Boolean): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null) return false
        if (bookmarked) {
            libraryDao.upsertBookmark(
                ScholarBookmarkEntity(
                    passageId = passageId,
                    createdAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        } else {
            libraryDao.deleteBookmark(passageId)
        }
        return true
    }

    suspend fun addHighlight(
        passageId: String,
        quote: String,
        note: String? = null,
        style: ScholarHighlightStyle = ScholarHighlightStyle.Important,
    ): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null) return false
        val cleanQuote = quote.trim()
        val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
        if (cleanQuote.length !in 1..HIGHLIGHT_MAX_LENGTH) return false
        if (cleanNote != null && cleanNote.length > NOTE_MAX_LENGTH) return false
        libraryDao.insertHighlight(
            ScholarHighlightEntity(
                passageId = passageId,
                quote = cleanQuote,
                note = cleanNote,
                style = style.name,
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        return true
    }

    suspend fun deleteHighlight(id: Long) = libraryDao.deleteHighlight(id)

    suspend fun createStudyPlan(
        pathId: String,
        sessionsPerWeek: Int,
        minutesPerSession: Int,
        targetPassagesPerSession: Int,
    ): Boolean {
        ensureSeeded()
        if (studyPaths().none { it.id == pathId }) return false
        if (sessionsPerWeek !in 1..7) return false
        if (minutesPerSession !in MIN_SESSION_MINUTES..MAX_SESSION_MINUTES) return false
        if (targetPassagesPerSession !in 1..MAX_TARGET_PASSAGES_PER_SESSION) return false
        val now = System.currentTimeMillis()
        libraryDao.deactivateStudyPlansForPath(pathId, now)
        libraryDao.upsertStudyPlan(
            ScholarStudyPlanEntity(
                pathId = pathId,
                sessionsPerWeek = sessionsPerWeek,
                minutesPerSession = minutesPerSession,
                targetPassagesPerSession = targetPassagesPerSession,
                active = true,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        return true
    }

    suspend fun deleteStudyPlan(id: Long) = libraryDao.deleteStudyPlan(id)

    suspend fun startOrResumeStudySession(pathId: String): ScholarStudySession? {
        ensureSeeded()
        libraryDao.activeStudySessionForPath(pathId)?.let { entity ->
            val session = entity.toDomain()
            val targetsStillExist = session.targetPassageIds.all { id ->
                libraryDao.passageById(id)?.bookId == session.bookId
            }
            if (targetsStillExist) return session
            libraryDao.upsertStudySession(
                entity.copy(
                    status = ScholarStudySessionStatus.Abandoned.name,
                    completedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
        val plan = libraryDao.activeStudyPlanForPath(pathId) ?: return null
        val path = studyPaths().firstOrNull { it.id == pathId } ?: return null
        val progress = libraryDao.observeReadingProgress().first().map { it.toDomain() }
        val bookId = ScholarLibraryIndex.pathProgress(listOf(path), progress)
            .single()
            .currentBookId
            ?: return null
        val passages = libraryDao.observePassagesForBook(bookId).first().map { it.toDomain() }
        val bookProgress = progress.firstOrNull { it.bookId == bookId }
        val targets = ScholarLibraryIndex.sessionTargets(
            passages = passages,
            lastPassageId = bookProgress?.lastPassageId,
            targetCount = plan.targetPassagesPerSession,
        )
        if (targets.isEmpty()) return null

        val now = System.currentTimeMillis()
        val id = libraryDao.upsertStudySession(
            ScholarStudySessionEntity(
                pathId = pathId,
                planId = plan.id,
                bookId = bookId,
                targetPassageIds = targets.map { it.id }.toStoredIds(),
                completedPassageIds = emptyList<String>().toStoredIds(),
                plannedMinutes = plan.minutesPerSession,
                status = ScholarStudySessionStatus.InProgress.name,
                startedAtEpochMillis = now,
                completedAtEpochMillis = null,
            ),
        )
        return libraryDao.studySessionById(id)?.toDomain()
    }

    suspend fun studySessionPassages(sessionId: Long): List<ScholarPassage> {
        val session = libraryDao.studySessionById(sessionId)?.toDomain() ?: return emptyList()
        return session.targetPassageIds.mapNotNull { id -> libraryDao.passageById(id)?.toDomain() }
    }

    suspend fun completeNextSessionPassage(sessionId: Long, passageId: String): Boolean {
        val entity = libraryDao.studySessionById(sessionId) ?: return false
        val session = entity.toDomain()
        if (session.status != ScholarStudySessionStatus.InProgress) return false
        if (session.nextPassageId != passageId) return false
        val passage = libraryDao.passageById(passageId)?.toDomain() ?: return false
        if (passage.bookId != session.bookId) return false

        val passages = libraryDao.observePassagesForBook(session.bookId).first().map { it.toDomain() }
        val index = passages.indexOfFirst { it.id == passageId }
        if (index < 0) return false
        val percent = ((index + 1) * 100 / passages.size.coerceAtLeast(1)).coerceIn(0, 100)
        if (!updateReadingProgress(session.bookId, passageId, percent)) return false

        val completed = session.completedPassageIds + passageId
        val allDone = completed.size == session.targetPassageIds.size
        val now = System.currentTimeMillis()
        libraryDao.upsertStudySession(
            entity.copy(
                completedPassageIds = completed.toStoredIds(),
                status = if (allDone) {
                    ScholarStudySessionStatus.Completed.name
                } else {
                    ScholarStudySessionStatus.InProgress.name
                },
                completedAtEpochMillis = if (allDone) now else null,
            ),
        )
        return true
    }

    suspend fun markBookOpened(bookId: String): Boolean {
        ensureSeeded()
        if (libraryDao.bookById(bookId) == null) return false
        val current = libraryDao.readingProgressByBook(bookId)
        if (current == null) {
            libraryDao.upsertReadingProgress(
                ScholarReadingProgressEntity(
                    bookId = bookId,
                    lastPassageId = null,
                    status = ScholarReadingStatus.InProgress.name,
                    progressPercent = 0,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
        return true
    }

    suspend fun updateReadingProgress(
        bookId: String,
        lastPassageId: String?,
        progressPercent: Int,
    ): Boolean {
        ensureSeeded()
        if (libraryDao.bookById(bookId) == null) return false
        if (lastPassageId != null) {
            val passage = libraryDao.passageById(lastPassageId) ?: return false
            if (passage.bookId != bookId) return false
        }
        val percent = progressPercent.coerceIn(0, 100)
        val status = if (percent >= 100) ScholarReadingStatus.Completed else ScholarReadingStatus.InProgress
        libraryDao.upsertReadingProgress(
            ScholarReadingProgressEntity(
                bookId = bookId,
                lastPassageId = lastPassageId,
                status = status.name,
                progressPercent = percent,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        return true
    }

    /**
     * Imports a user-selected JSON pack. The pack must carry source and licence
     * information for every book; it is intentionally not a scraper or remote
     * downloader for third-party libraries. v1/v2 packs remain accepted while
     * v3 adds optional section/order hierarchy metadata for passages.
     */
    suspend fun importPack(rawText: String): ScholarLibraryImportResult = runCatching {
        ensureSeeded()
        if (rawText.length > PACK_MAX_CHARS) error("حزمة المكتبة كبيرة جداً؛ الحد الأقصى 5 ميغابايت من النص.")
        val pack = decodeAndValidate(rawText)
        persistPack(pack, imported = true)
        ScholarLibraryImportResult.Success(pack.books.size, pack.books.sumOf { it.passages.size })
    }.getOrElse { error ->
        ScholarLibraryImportResult.Failure(error.message ?: "تعذر استيراد حزمة المكتبة.")
    }

    suspend fun citationForPassage(passageId: String): Citation? {
        val passage = libraryDao.passageById(passageId) ?: return null
        val book = libraryDao.bookById(passage.bookId) ?: return null
        return Citation(
            bookTitle = book.title,
            author = book.author,
            chapter = passage.chapter,
            volume = passage.volume,
            page = passage.page,
            edition = book.edition,
            publisher = book.publisher,
            publicationYear = book.publicationYear,
            section = passage.section,
        )
    }

    private suspend fun persistPack(pack: ScholarPack, imported: Boolean) {
        val books = pack.books.map { item -> item.toEntity(imported) }
        val passages = pack.books.flatMap { book ->
            book.passages.map { passage -> passage.toEntity(book.id) }
        }
        libraryDao.upsertBooks(books)
        libraryDao.upsertPassages(passages)
        // FTS4 tables have no practical unique constraint for contentless rows;
        // rebuilding avoids stale/duplicated results after a pack replaces text.
        ftsDao.clearAll()
        val allRows = buildList {
            pack.books.forEach { book ->
                book.passages.forEach { passage ->
                    add(
                        ScholarPassageFtsEntity(
                            normalizedText = ArabicText.normalizeForSearch(passage.text),
                            passageId = passage.id,
                        ),
                    )
                }
            }
        }
        if (allRows.isNotEmpty()) ftsDao.upsertRows(allRows)
        if (!imported) return
        rebuildIndex()
    }

    private suspend fun rebuildIndex() {
        ftsDao.clearAll()
        val rows = mutableListOf<ScholarPassageFtsEntity>()
        libraryDao.observeBooks().map { books -> books.map { book -> book.id } }.let { bookIdsFlow ->
            val ids = bookIdsFlow.first()
            ids.forEach { bookId ->
                val passages = libraryDao.observePassagesForBook(bookId).first()
                rows += passages.map { passage ->
                    ScholarPassageFtsEntity(
                        normalizedText = ArabicText.normalizeForSearch(passage.text),
                        passageId = passage.id,
                    )
                }
            }
        }
        if (rows.isNotEmpty()) ftsDao.upsertRows(rows)
    }

    private fun decodeAndValidate(rawText: String): ScholarPack {
        val pack = json.decodeFromString<ScholarPack>(rawText)
        require(pack.schemaVersion in MIN_PACK_SCHEMA_VERSION..CURRENT_PACK_SCHEMA_VERSION) {
            "إصدار الحزمة غير مدعوم."
        }
        require(pack.packName.isNotBlank()) { "اسم الحزمة مطلوب." }
        require(pack.licenseNotice.isNotBlank()) { "يجب أن تتضمن الحزمة بيان ترخيص واضحاً." }
        require(pack.books.isNotEmpty() && pack.books.size <= MAX_BOOKS_PER_PACK) { "عدد الكتب في الحزمة غير صالح." }
        require(pack.books.map { it.id }.distinct().size == pack.books.size) { "معرّفات الكتب مكررة." }
        val passageIds = mutableSetOf<String>()
        pack.books.forEach { book ->
            require(ID_REGEX.matches(book.id)) { "معرّف كتاب غير صالح: ${book.id}" }
            require(book.title.isNotBlank() && book.author.isNotBlank()) { "عنوان الكتاب ومؤلفه مطلوبان." }
            require(book.sourceName.isNotBlank() && book.licenseSummary.isNotBlank()) {
                "يجب توضيح مصدر وترخيص كل كتاب."
            }
            require(book.language.isNotBlank() && book.language.length <= 20) { "لغة الكتاب غير صالحة." }
            require(ScholarDifficulty.entries.any { it.name.equals(book.difficulty, ignoreCase = true) }) {
                "مستوى الكتاب غير مدعوم."
            }
            require(book.volumeCount == null || book.volumeCount in 1..MAX_VOLUME_COUNT) {
                "عدد مجلدات الكتاب غير صالح."
            }
            require(book.keywords.size <= MAX_KEYWORDS_PER_BOOK && book.keywords.all { it.length <= MAX_KEYWORD_LENGTH }) {
                "الكلمات المفتاحية للكتاب تتجاوز الحدود المسموح بها."
            }
            require(book.passages.isNotEmpty() && book.passages.size <= MAX_PASSAGES_PER_BOOK) {
                "لا بد من وجود نص واحد على الأقل لكل كتاب ضمن الحدود المسموح بها."
            }
            book.passages.forEach { passage ->
                require(ID_REGEX.matches(passage.id) && passageIds.add(passage.id)) {
                    "معرّف مقطع مكرر أو غير صالح."
                }
                require(passage.chapter.isNotBlank() && passage.text.trim().length in 1..PASSAGE_MAX_LENGTH) {
                    "نص أو فصل المقطع غير صالح."
                }
                require(passage.section == null || passage.section.length <= MAX_SECTION_LENGTH) {
                    "عنوان قسم المقطع طويل جداً."
                }
                require(passage.orderIndex >= 0) { "ترتيب المقطع يجب ألا يكون سالباً." }
            }
        }
        return pack
    }

    private fun ScholarPackBook.toEntity(imported: Boolean) = ScholarBookEntity(
        id = id,
        title = title,
        author = author,
        category = ScholarCategory.fromId(category).name,
        authorDeathYearHijri = authorDeathYearHijri,
        description = description,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        licenseSummary = licenseSummary,
        imported = imported,
        subtitle = subtitle,
        language = language,
        difficulty = ScholarDifficulty.fromId(difficulty).name,
        publisher = publisher,
        edition = edition,
        editor = editor,
        publicationYear = publicationYear,
        volumeCount = volumeCount,
        keywords = keywords.joinToString(KEYWORD_SEPARATOR),
    )

    private fun ScholarPackPassage.toEntity(bookId: String) = ScholarPassageEntity(
        id = id,
        bookId = bookId,
        chapter = chapter,
        volume = volume,
        page = page,
        text = text,
        section = section,
        orderIndex = orderIndex,
    )

    private companion object {
        const val BUNDLED_CATALOG = "scholar_library_catalog.json"
        const val BUNDLED_STUDY_PATHS = "scholar_study_paths.json"
        const val STUDY_PATH_SCHEMA_VERSION = 1
        const val MIN_PACK_SCHEMA_VERSION = 1
        const val CURRENT_PACK_SCHEMA_VERSION = 3
        const val SEARCH_LIMIT = 100
        const val SEARCH_CANDIDATE_LIMIT = 500
        const val METADATA_MATCH_PASSAGES_PER_BOOK = 20
        const val PACK_MAX_CHARS = 5_000_000
        const val MAX_BOOKS_PER_PACK = 1_000
        const val MAX_PASSAGES_PER_BOOK = 20_000
        const val PASSAGE_MAX_LENGTH = 30_000
        const val NOTE_MAX_LENGTH = 4_000
        const val FLASHCARD_SIDE_MAX_LENGTH = 1_000
        const val HIGHLIGHT_MAX_LENGTH = 30_000
        const val MAX_SECTION_LENGTH = 300
        const val MIN_SESSION_MINUTES = 5
        const val MAX_SESSION_MINUTES = 180
        const val MAX_TARGET_PASSAGES_PER_SESSION = 100
        const val MAX_VOLUME_COUNT = 500
        const val MAX_KEYWORDS_PER_BOOK = 100
        const val MAX_KEYWORD_LENGTH = 120
        const val KEYWORD_SEPARATOR = "\u001F"
        val ID_REGEX = Regex("[A-Za-z0-9_-]{3,120}")
    }
}

/** Builds a conservative FTS4 query from Arabic or Latin user input. */
object ScholarSearchQuery {
    private val special = setOf('"', '*', '(', ')', ':', '^', '-', '+')

    fun build(rawQuery: String): String = rawQuery
        .trim()
        .split(Regex("\\s+"))
        .map { ArabicText.normalizeForSearch(it).filterNot { char -> char in special } }
        .filter { it.isNotBlank() }
        .take(8)
        .joinToString(" AND ") { "$it*" }
}
