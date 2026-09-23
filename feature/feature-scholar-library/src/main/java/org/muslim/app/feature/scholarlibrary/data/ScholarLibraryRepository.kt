package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
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
import org.muslim.app.feature.scholarlibrary.domain.ScholarContentPack
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarHighlightStyle
import org.muslim.app.feature.scholarlibrary.domain.ScholarLibraryIndex
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewEvent
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
    data class Success(
        val importedBooks: Int,
        val importedPassages: Int,
        val packName: String,
        val packVersion: Int,
        val replacedExisting: Boolean,
    ) : ScholarLibraryImportResult

    data class Failure(val message: String) : ScholarLibraryImportResult
}

@Serializable
private data class ScholarPack(
    val schemaVersion: Int,
    val packName: String,
    val licenseNotice: String,
    val books: List<ScholarPackBook>,
    val packId: String? = null,
    val packVersion: Int = 1,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
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

private const val LEGACY_IMPORTS_PACK_ID = "legacy-imports"
private const val BUNDLED_PACK_ORIGIN = "bundled:scholar_library_catalog.json"

private fun ScholarPack.effectivePackId(): String =
    packId?.takeIf { it.isNotBlank() } ?: run {
        val material = buildString {
            append(packName.trim())
            append('|')
            append(books.map { it.id }.sorted().joinToString(","))
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(material.toByteArray(Charsets.UTF_8))
        "legacy-" + digest.take(12).joinToString("") { byte -> "%02x".format(byte) }
    }

private fun ScholarPack.effectiveSourceName(originName: String?): String =
    sourceName?.takeIf { it.isNotBlank() }
        ?: books.map { it.sourceName }.distinct().take(3).joinToString("، ").takeIf { it.isNotBlank() }
        ?: originName?.takeIf { it.isNotBlank() }
        ?: "مصادر الكتب داخل الحزمة"

private fun ScholarPack.toRegistryEntity(
    imported: Boolean,
    originName: String?,
    existing: ScholarContentPackEntity?,
): ScholarContentPackEntity {
    val now = System.currentTimeMillis()
    return ScholarContentPackEntity(
        packId = effectivePackId(),
        identity = ScholarContentPackIdentityEntity(
            packName = packName.trim(),
            packVersion = packVersion,
            schemaVersion = schemaVersion,
        ),
        source = ScholarContentPackSourceEntity(
            licenseNotice = licenseNotice.trim(),
            sourceName = effectiveSourceName(originName),
            sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotEmpty() },
            originName = originName?.trim()?.takeIf { it.isNotEmpty() },
        ),
        installation = ScholarContentPackInstallationEntity(
            bookIds = books.map { it.id }.toStoredIds(),
            imported = imported,
            managed = schemaVersion >= 4 && !packId.isNullOrBlank(),
            installedAtEpochMillis = existing?.installation?.installedAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
        ),
    )
}

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
            val bundledRaw = context.assets.open(BUNDLED_CATALOG)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val bundledPack = decodeAndValidate(bundledRaw)
            val bundledId = bundledPack.effectivePackId()
            if (libraryDao.bookCount() == 0) {
                persistPack(
                    pack = bundledPack,
                    imported = false,
                    originName = BUNDLED_PACK_ORIGIN,
                    existing = null,
                )
            } else if (libraryDao.contentPackById(bundledId) == null) {
                libraryDao.upsertContentPack(
                    bundledPack.toRegistryEntity(
                        imported = false,
                        originName = BUNDLED_PACK_ORIGIN,
                        existing = null,
                    ),
                )
            }

            val registeredBookIds = libraryDao.observeContentPacks().first()
                .flatMap { it.installation.bookIds.toStoredIdList() }
                .toSet()
            val legacyImportedBooks = libraryDao.observeBooks().first()
                .filter { it.imported && it.id !in registeredBookIds }
            if (legacyImportedBooks.isNotEmpty()) {
                val now = System.currentTimeMillis()
                libraryDao.upsertContentPack(
                    ScholarContentPackEntity(
                        packId = LEGACY_IMPORTS_PACK_ID,
                        identity = ScholarContentPackIdentityEntity(
                            packName = "استيرادات سابقة",
                            packVersion = 1,
                            schemaVersion = 3,
                        ),
                        source = ScholarContentPackSourceEntity(
                            licenseNotice = "بيانات الكتب محفوظة بترخيصها ومصدرها الفردي داخل الفهرس.",
                            sourceName = "استيرادات تمت قبل إضافة إدارة الحزم",
                            sourceUrl = null,
                            originName = null,
                        ),
                        installation = ScholarContentPackInstallationEntity(
                            bookIds = legacyImportedBooks.map { it.id }.toStoredIds(),
                            imported = true,
                            managed = false,
                            installedAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        ),
                    ),
                )
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

    val readingProgress: Flow<List<ScholarReadingProgress>>
        get() = libraryDao.observeReadingProgress().map { rows -> rows.map { it.toDomain() } }

    val studyPlans: Flow<List<ScholarStudyPlan>>
        get() = libraryDao.observeStudyPlans().map { rows -> rows.map { it.toDomain() } }

    val studySessions: Flow<List<ScholarStudySession>>
        get() = libraryDao.observeStudySessions().map { rows -> rows.map { it.toDomain() } }

    val reviewEvents: Flow<List<ScholarReviewEvent>>
        get() = libraryDao.observeReviewEvents().map { rows -> rows.map { it.toDomain() } }

    val contentPacks: Flow<List<ScholarContentPack>>
        get() = libraryDao.observeContentPacks().map { rows -> rows.map { it.toDomain() } }

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
                createdAtEpochMillis = now,
                reviewState = ScholarFlashcardReviewStateEntity(
                    dueAtEpochMillis = now,
                ),
            ),
        )
        return true
    }

    suspend fun reviewFlashcard(id: Long, rating: ScholarReviewRating): Boolean {
        val entity = libraryDao.flashcardById(id) ?: return false
        val passage = libraryDao.passageById(entity.passageId) ?: return false
        val book = libraryDao.bookById(passage.bookId) ?: return false
        val reviewedAt = System.currentTimeMillis()
        val schedule = ScholarReviewScheduler.schedule(
            card = entity.toDomain(),
            rating = rating,
            reviewedAtEpochMillis = reviewedAt,
        )
        val updatedCard = entity.copy(
            reviewState = ScholarFlashcardReviewStateEntity(
                reviewCount = schedule.reviewCount,
                dueAtEpochMillis = schedule.dueAtEpochMillis,
                intervalDays = schedule.intervalDays,
                easeFactor = schedule.easeFactor,
                lapseCount = schedule.lapseCount,
                lastReviewedAtEpochMillis = schedule.lastReviewedAtEpochMillis,
                lastRating = schedule.lastRating.name,
            ),
        )
        val reviewEvent = ScholarReviewEventEntity(
            flashcardId = entity.id,
            passageId = passage.id,
            bookId = book.id,
            category = book.category,
            reviewedAtEpochMillis = reviewedAt,
            outcome = ScholarReviewOutcomeEntity(
                rating = schedule.lastRating.name,
                scheduledIntervalDays = schedule.intervalDays,
                lapseCountAfterReview = schedule.lapseCount,
                easeFactorAfterReview = schedule.easeFactor,
            ),
        )
        libraryDao.applyFlashcardReview(updatedCard, reviewEvent)
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
     * Imports a user-selected JSON pack. v4 adds stable pack identity/version
     * metadata while v1-v3 remain accepted as legacy, locally managed imports.
     * Updates are intentionally non-destructive to citation-linked study data.
     */
    suspend fun importPack(
        rawText: String,
        originName: String? = null,
    ): ScholarLibraryImportResult = runCatching {
        ensureSeeded()
        if (rawText.length > PACK_MAX_CHARS) error("حزمة المكتبة كبيرة جداً؛ الحد الأقصى 5 ميغابايت من النص.")
        val pack = decodeAndValidate(rawText)
        val packId = pack.effectivePackId()
        val existing = libraryDao.contentPackById(packId)
        require(existing == null || pack.packVersion >= existing.identity.packVersion) {
            "لا يمكن تثبيت إصدار أقدم من الحزمة المثبتة."
        }

        val newBookIds = pack.books.map { it.id }.toSet()
        val conflictingOwner = libraryDao.observeContentPacks().first().firstOrNull { installed ->
            installed.packId != packId &&
                installed.packId != LEGACY_IMPORTS_PACK_ID &&
                installed.installation.bookIds.toStoredIdList().any { it in newBookIds }
        }
        require(conflictingOwner == null) {
            "تتعارض الحزمة مع كتب مملوكة لحزمة أخرى: ${conflictingOwner?.identity?.packName}."
        }

        existing?.let { current ->
            val previousBookIds = current.installation.bookIds.toStoredIdList().toSet()
            require(newBookIds.containsAll(previousBookIds)) {
                "التحديث الآمن لا يسمح بحذف كتاب موجود من الحزمة."
            }
            previousBookIds.forEach { bookId ->
                val newBook = pack.books.first { it.id == bookId }
                val previousPassageIds = libraryDao.observePassagesForBook(bookId).first().map { it.id }.toSet()
                val newPassageIds = newBook.passages.map { it.id }.toSet()
                require(newPassageIds.containsAll(previousPassageIds)) {
                    "التحديث الآمن لا يسمح بحذف مقاطع مرتبطة بالكتاب: ${newBook.title}."
                }
            }
        }

        persistPack(
            pack = pack,
            imported = true,
            originName = originName,
            existing = existing,
        )

        libraryDao.contentPackById(LEGACY_IMPORTS_PACK_ID)?.let { legacy ->
            val previousLegacyIds = legacy.installation.bookIds.toStoredIdList()
            val remaining = previousLegacyIds.filterNot { it in newBookIds }
            if (remaining.isEmpty()) {
                libraryDao.deleteContentPack(LEGACY_IMPORTS_PACK_ID)
            } else if (remaining.size != previousLegacyIds.size) {
                libraryDao.upsertContentPack(
                    legacy.copy(
                        installation = legacy.installation.copy(
                            bookIds = remaining.toStoredIds(),
                            updatedAtEpochMillis = System.currentTimeMillis(),
                        ),
                    ),
                )
            }
        }

        ScholarLibraryImportResult.Success(
            importedBooks = pack.books.size,
            importedPassages = pack.books.sumOf { it.passages.size },
            packName = pack.packName,
            packVersion = pack.packVersion,
            replacedExisting = existing != null,
        )
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

    private suspend fun persistPack(
        pack: ScholarPack,
        imported: Boolean,
        originName: String?,
        existing: ScholarContentPackEntity?,
    ) {
        val books = pack.books.map { item -> item.toEntity(imported) }
        val passages = pack.books.flatMap { book ->
            book.passages.map { passage -> passage.toEntity(book.id) }
        }
        libraryDao.installContentPack(
            pack = pack.toRegistryEntity(imported, originName, existing),
            books = books,
            passages = passages,
        )
        if (imported) {
            rebuildIndex()
            return
        }
        ftsDao.clearAll()
        val allRows = passages.map { passage ->
            ScholarPassageFtsEntity(
                normalizedText = ArabicText.normalizeForSearch(passage.text),
                passageId = passage.id,
            )
        }
        if (allRows.isNotEmpty()) ftsDao.upsertRows(allRows)
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
        require(pack.packName.isNotBlank() && pack.packName.length <= MAX_PACK_NAME_LENGTH) {
            "اسم الحزمة مطلوب ويجب أن يكون ضمن الحد المسموح."
        }
        require(pack.licenseNotice.isNotBlank()) { "يجب أن تتضمن الحزمة بيان ترخيص واضحاً." }
        require(pack.packVersion in 1..MAX_PACK_VERSION) { "رقم إصدار الحزمة غير صالح." }
        if (pack.schemaVersion >= 4) {
            require(!pack.packId.isNullOrBlank() && ID_REGEX.matches(pack.packId)) {
                "حزم الإصدار 4 تتطلب packId ثابتاً وصالحاً."
            }
            require(!pack.sourceName.isNullOrBlank() && pack.sourceName.length <= MAX_PACK_SOURCE_LENGTH) {
                "حزم الإصدار 4 تتطلب اسم مصدر واضحاً للحزمة."
            }
            require(pack.sourceUrl == null || pack.sourceUrl.length <= MAX_SOURCE_URL_LENGTH) {
                "رابط مصدر الحزمة طويل جداً."
            }
        }
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
        const val CURRENT_PACK_SCHEMA_VERSION = 4
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
        const val MAX_PACK_NAME_LENGTH = 200
        const val MAX_PACK_VERSION = 1_000_000
        const val MAX_PACK_SOURCE_LENGTH = 300
        const val MAX_SOURCE_URL_LENGTH = 2_000
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
