package org.muslim.app.feature.scholarlibrary.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarHighlightStyle
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewRating
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySessionStatus

data class ScholarBackupCoreCounts(
    val notes: Int,
    val flashcards: Int,
    val bookmarks: Int,
    val highlights: Int,
)

data class ScholarBackupProgressCounts(
    val readingProgress: Int,
    val studyPlans: Int,
    val studySessions: Int,
    val reviewEvents: Int,
)

sealed interface ScholarStudyBackupRestoreResult {
    data class Success(
        val core: ScholarBackupCoreCounts,
        val progress: ScholarBackupProgressCounts,
    ) : ScholarStudyBackupRestoreResult

    data class Failure(val message: String) : ScholarStudyBackupRestoreResult
}

@Serializable
private data class StudyBackup(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val core: BackupCore,
    val progress: BackupProgress,
)

@Serializable
private data class BackupCore(
    val notes: List<BackupNote>,
    val flashcards: List<BackupFlashcard>,
    val bookmarks: List<BackupBookmark>,
    val highlights: List<BackupHighlight>,
)

@Serializable
private data class BackupProgress(
    val readingProgress: List<BackupReadingProgress>,
    val studyPlans: List<BackupStudyPlan>,
    val studySessions: List<BackupStudySession>,
    val reviewEvents: List<BackupReviewEvent>,
)

@Serializable
private data class BackupNote(
    val id: Long,
    val passageId: String,
    val text: String,
    val createdAtEpochMillis: Long,
)

@Serializable
private data class BackupFlashcard(
    val id: Long,
    val passageId: String,
    val front: String,
    val back: String,
    val createdAtEpochMillis: Long,
    val review: BackupFlashcardReview,
)

@Serializable
private data class BackupFlashcardReview(
    val reviewCount: Int,
    val dueAtEpochMillis: Long,
    val intervalDays: Int,
    val easeFactor: Double,
    val lapseCount: Int,
    val lastReviewedAtEpochMillis: Long?,
    val lastRating: String?,
)

@Serializable
private data class BackupBookmark(
    val passageId: String,
    val createdAtEpochMillis: Long,
)

@Serializable
private data class BackupHighlight(
    val id: Long,
    val passageId: String,
    val quote: String,
    val note: String?,
    val style: String,
    val createdAtEpochMillis: Long,
)

@Serializable
private data class BackupReadingProgress(
    val bookId: String,
    val lastPassageId: String?,
    val status: String,
    val progressPercent: Int,
    val updatedAtEpochMillis: Long,
)

@Serializable
private data class BackupStudyPlan(
    val id: Long,
    val pathId: String,
    val active: Boolean,
    val cadence: BackupStudyCadence,
    val timing: BackupUpdateTiming,
)

@Serializable
private data class BackupStudyCadence(
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val targetPassagesPerSession: Int,
)

@Serializable
private data class BackupUpdateTiming(
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Serializable
private data class BackupStudySession(
    val id: Long,
    val context: BackupSessionContext,
    val plannedMinutes: Int,
    val status: String,
    val targets: BackupSessionTargets,
    val timing: BackupSessionTiming,
)

@Serializable
private data class BackupSessionContext(
    val pathId: String,
    val planId: Long?,
    val bookId: String,
)

@Serializable
private data class BackupSessionTargets(
    val targetPassageIds: List<String>,
    val completedPassageIds: List<String>,
)

@Serializable
private data class BackupSessionTiming(
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
)

@Serializable
private data class BackupReviewEvent(
    val id: Long,
    val reference: BackupReviewReference,
    val reviewedAtEpochMillis: Long,
    val outcome: BackupReviewOutcome,
)

@Serializable
private data class BackupReviewReference(
    val flashcardId: Long,
    val passageId: String,
    val bookId: String,
    val category: String,
)

@Serializable
private data class BackupReviewOutcome(
    val rating: String,
    val scheduledIntervalDays: Int,
    val lapseCountAfterReview: Int,
    val easeFactorAfterReview: Double,
)

@Singleton
class ScholarStudyBackupManager @Inject constructor(
    private val libraryDao: ScholarLibraryDao,
    private val json: Json,
) {
    suspend fun exportBackup(): String {
        val backup = StudyBackup(
            schemaVersion = BACKUP_SCHEMA_VERSION,
            exportedAtEpochMillis = System.currentTimeMillis(),
            core = BackupCore(
                notes = libraryDao.observeNotes().first().map { it.toBackup() },
                flashcards = libraryDao.observeFlashcards().first().map { it.toBackup() },
                bookmarks = libraryDao.observeBookmarks().first().map { it.toBackup() },
                highlights = libraryDao.observeHighlights().first().map { it.toBackup() },
            ),
            progress = BackupProgress(
                readingProgress = libraryDao.observeReadingProgress().first().map { it.toBackup() },
                studyPlans = libraryDao.observeStudyPlans().first().map { it.toBackup() },
                studySessions = libraryDao.observeStudySessions().first().map { it.toBackup() },
                reviewEvents = libraryDao.observeReviewEvents().first().map { it.toBackup() },
            ),
        )
        return json.encodeToString(backup)
    }

    suspend fun restoreBackup(
        rawText: String,
        validPathIds: Set<String>,
    ): ScholarStudyBackupRestoreResult = runCatching {
        require(rawText.length <= BACKUP_MAX_CHARS) { "ملف النسخة الاحتياطية كبير جداً." }
        val backup = json.decodeFromString<StudyBackup>(rawText)
        validateBackup(backup, validPathIds)
        val entities = backup.toEntities()
        libraryDao.restoreStudyBackup(entities)
        ScholarStudyBackupRestoreResult.Success(
            core = ScholarBackupCoreCounts(
                notes = entities.core.notes.size,
                flashcards = entities.core.flashcards.size,
                bookmarks = entities.core.bookmarks.size,
                highlights = entities.core.highlights.size,
            ),
            progress = ScholarBackupProgressCounts(
                readingProgress = entities.progress.readingProgress.size,
                studyPlans = entities.progress.studyPlans.size,
                studySessions = entities.progress.studySessions.size,
                reviewEvents = entities.progress.reviewEvents.size,
            ),
        )
    }.getOrElse { error ->
        ScholarStudyBackupRestoreResult.Failure(
            error.message ?: "تعذر استعادة النسخة الاحتياطية.",
        )
    }

    private suspend fun validateBackup(
        backup: StudyBackup,
        validPathIds: Set<String>,
    ) {
        require(backup.schemaVersion == BACKUP_SCHEMA_VERSION) { "إصدار النسخة الاحتياطية غير مدعوم." }
        require(backup.exportedAtEpochMillis > 0) { "تاريخ النسخة الاحتياطية غير صالح." }
        require(backup.totalItems() <= BACKUP_MAX_ITEMS) { "النسخة الاحتياطية تحتوي على عدد سجلات أكبر من المسموح." }
        requireDistinctIds(backup)

        val books = libraryDao.observeBooks().first().associateBy { it.id }
        val passages = books.keys.flatMap { bookId ->
            libraryDao.observePassagesForBook(bookId).first()
        }.associateBy { it.id }

        validateCore(backup.core, passages)
        validateProgress(
            progress = backup.progress,
            books = books,
            passages = passages,
            validPathIds = validPathIds,
        )
    }

    private fun validateCore(
        core: BackupCore,
        passages: Map<String, ScholarPassageEntity>,
    ) {
        core.notes.forEach {
            requirePassage(passages, it.passageId)
            require(it.text.isNotBlank() && it.text.length <= 4_000) { "نص ملاحظة غير صالح." }
        }
        core.flashcards.forEach {
            requirePassage(passages, it.passageId)
            require(it.front.isNotBlank() && it.front.length <= 1_000) { "وجه بطاقة مراجعة غير صالح." }
            require(it.back.isNotBlank() && it.back.length <= 1_000) { "جواب بطاقة مراجعة غير صالح." }
            require(it.review.reviewCount >= 0 && it.review.intervalDays >= 0 && it.review.lapseCount >= 0) {
                "حالة جدولة بطاقة المراجعة غير صالحة."
            }
            require(it.review.easeFactor in 1.0..5.0) { "معامل سهولة بطاقة المراجعة غير صالح." }
            require(it.review.lastRating == null || ScholarReviewRating.fromId(it.review.lastRating) != null) {
                "آخر تقييم للبطاقة غير معروف."
            }
        }
        core.bookmarks.forEach { requirePassage(passages, it.passageId) }
        core.highlights.forEach {
            requirePassage(passages, it.passageId)
            require(it.quote.isNotBlank() && it.quote.length <= 30_000) { "نص تظليل غير صالح." }
            require(ScholarHighlightStyle.entries.any { style -> style.name == it.style }) {
                "نمط تظليل غير معروف."
            }
        }
    }

    private fun validateProgress(
        progress: BackupProgress,
        books: Map<String, ScholarBookEntity>,
        passages: Map<String, ScholarPassageEntity>,
        validPathIds: Set<String>,
    ) {
        progress.readingProgress.forEach { item ->
            require(item.bookId in books) { "النسخة تشير إلى كتاب غير مثبت: ${item.bookId}" }
            require(item.progressPercent in 0..100) { "نسبة تقدم غير صالحة." }
            require(ScholarReadingStatus.entries.any { it.name == item.status }) {
                "حالة قراءة غير معروفة."
            }
            item.lastPassageId?.let { passageId ->
                require(passages[passageId]?.bookId == item.bookId) {
                    "موضع القراءة لا ينتمي إلى الكتاب المحدد."
                }
            }
        }
        progress.studyPlans.forEach { plan ->
            require(plan.pathId in validPathIds) { "النسخة تشير إلى مسار دراسي غير موجود: ${plan.pathId}" }
            require(plan.cadence.sessionsPerWeek in 1..7) { "عدد جلسات الخطة غير صالح." }
            require(plan.cadence.minutesPerSession in 5..180) { "مدة جلسة الخطة غير صالحة." }
            require(plan.cadence.targetPassagesPerSession in 1..100) { "هدف المقاطع في الخطة غير صالح." }
        }
        progress.studySessions.forEach { session ->
            require(session.context.pathId in validPathIds) { "جلسة مرتبطة بمسار غير موجود." }
            require(session.context.bookId in books) { "جلسة مرتبطة بكتاب غير مثبت." }
            require(ScholarStudySessionStatus.entries.any { it.name == session.status }) {
                "حالة جلسة غير معروفة."
            }
            require(session.targets.targetPassageIds.isNotEmpty()) { "جلسة بدون أهداف." }
            require(session.targets.completedPassageIds.all { it in session.targets.targetPassageIds }) {
                "الجلسة تحتوي على مقاطع مكتملة خارج أهدافها."
            }
            session.targets.targetPassageIds.forEach { passageId ->
                require(passages[passageId]?.bookId == session.context.bookId) {
                    "هدف جلسة لا ينتمي إلى كتابها."
                }
            }
        }
        progress.reviewEvents.forEach { event ->
            require(passages[event.reference.passageId]?.bookId == event.reference.bookId) {
                "سجل مراجعة مرتبط بمصدر غير موجود."
            }
            require(ScholarCategory.entries.any { it.name == event.reference.category }) {
                "علم غير معروف في سجل المراجعة."
            }
            require(event.outcome.scheduledIntervalDays >= 0) { "فاصل مراجعة غير صالح." }
            require(ScholarReviewRating.fromId(event.outcome.rating) != null) {
                "تقييم غير معروف في سجل المراجعة."
            }
        }
    }

    private fun requireDistinctIds(backup: StudyBackup) {
        require(backup.core.notes.map { it.id }.isDistinct()) { "معرّفات الملاحظات مكررة." }
        require(backup.core.flashcards.map { it.id }.isDistinct()) { "معرّفات البطاقات مكررة." }
        require(backup.core.bookmarks.map { it.passageId }.isDistinct()) { "الإشارات المرجعية مكررة." }
        require(backup.core.highlights.map { it.id }.isDistinct()) { "معرّفات التظليلات مكررة." }
        require(backup.progress.readingProgress.map { it.bookId }.isDistinct()) { "تقدم القراءة مكرر." }
        require(backup.progress.studyPlans.map { it.id }.isDistinct()) { "معرّفات الخطط مكررة." }
        require(backup.progress.studySessions.map { it.id }.isDistinct()) { "معرّفات الجلسات مكررة." }
        require(backup.progress.reviewEvents.map { it.id }.isDistinct()) { "معرّفات سجلات المراجعة مكررة." }
    }

    private fun requirePassage(
        passages: Map<String, ScholarPassageEntity>,
        passageId: String,
    ) {
        require(passageId in passages) { "النسخة تشير إلى مقطع غير مثبت: $passageId" }
    }

    private fun StudyBackup.totalItems(): Int =
        core.notes.size +
            core.flashcards.size +
            core.bookmarks.size +
            core.highlights.size +
            progress.readingProgress.size +
            progress.studyPlans.size +
            progress.studySessions.size +
            progress.reviewEvents.size

    private fun StudyBackup.toEntities() = ScholarStudyBackupEntities(
        core = ScholarStudyBackupCoreEntities(
            notes = core.notes.map { it.toEntity() },
            flashcards = core.flashcards.map { it.toEntity() },
            bookmarks = core.bookmarks.map { it.toEntity() },
            highlights = core.highlights.map { it.toEntity() },
        ),
        progress = ScholarStudyBackupProgressEntities(
            readingProgress = progress.readingProgress.map { it.toEntity() },
            studyPlans = progress.studyPlans.map { it.toEntity() },
            studySessions = progress.studySessions.map { it.toEntity() },
            reviewEvents = progress.reviewEvents.map { it.toEntity() },
        ),
    )

    private fun ScholarNoteEntity.toBackup() =
        BackupNote(id, passageId, text, createdAtEpochMillis)

    private fun BackupNote.toEntity() =
        ScholarNoteEntity(id, passageId, text, createdAtEpochMillis)

    private fun ScholarFlashcardEntity.toBackup() = BackupFlashcard(
        id = id,
        passageId = passageId,
        front = front,
        back = back,
        createdAtEpochMillis = createdAtEpochMillis,
        review = BackupFlashcardReview(
            reviewCount = reviewState.reviewCount,
            dueAtEpochMillis = reviewState.dueAtEpochMillis,
            intervalDays = reviewState.intervalDays,
            easeFactor = reviewState.easeFactor,
            lapseCount = reviewState.lapseCount,
            lastReviewedAtEpochMillis = reviewState.lastReviewedAtEpochMillis,
            lastRating = reviewState.lastRating,
        ),
    )

    private fun BackupFlashcard.toEntity() = ScholarFlashcardEntity(
        id = id,
        passageId = passageId,
        front = front,
        back = back,
        createdAtEpochMillis = createdAtEpochMillis,
        reviewState = ScholarFlashcardReviewStateEntity(
            reviewCount = review.reviewCount,
            dueAtEpochMillis = review.dueAtEpochMillis,
            intervalDays = review.intervalDays,
            easeFactor = review.easeFactor,
            lapseCount = review.lapseCount,
            lastReviewedAtEpochMillis = review.lastReviewedAtEpochMillis,
            lastRating = review.lastRating,
        ),
    )

    private fun ScholarBookmarkEntity.toBackup() =
        BackupBookmark(passageId, createdAtEpochMillis)

    private fun BackupBookmark.toEntity() =
        ScholarBookmarkEntity(passageId, createdAtEpochMillis)

    private fun ScholarHighlightEntity.toBackup() =
        BackupHighlight(id, passageId, quote, note, style, createdAtEpochMillis)

    private fun BackupHighlight.toEntity() =
        ScholarHighlightEntity(id, passageId, quote, note, style, createdAtEpochMillis)

    private fun ScholarReadingProgressEntity.toBackup() =
        BackupReadingProgress(bookId, lastPassageId, status, progressPercent, updatedAtEpochMillis)

    private fun BackupReadingProgress.toEntity() =
        ScholarReadingProgressEntity(bookId, lastPassageId, status, progressPercent, updatedAtEpochMillis)

    private fun ScholarStudyPlanEntity.toBackup() = BackupStudyPlan(
        id = id,
        pathId = pathId,
        active = active,
        cadence = BackupStudyCadence(sessionsPerWeek, minutesPerSession, targetPassagesPerSession),
        timing = BackupUpdateTiming(createdAtEpochMillis, updatedAtEpochMillis),
    )

    private fun BackupStudyPlan.toEntity() = ScholarStudyPlanEntity(
        id = id,
        pathId = pathId,
        sessionsPerWeek = cadence.sessionsPerWeek,
        minutesPerSession = cadence.minutesPerSession,
        targetPassagesPerSession = cadence.targetPassagesPerSession,
        active = active,
        createdAtEpochMillis = timing.createdAtEpochMillis,
        updatedAtEpochMillis = timing.updatedAtEpochMillis,
    )

    private fun ScholarStudySessionEntity.toBackup() = BackupStudySession(
        id = id,
        context = BackupSessionContext(pathId, planId, bookId),
        plannedMinutes = plannedMinutes,
        status = status,
        targets = BackupSessionTargets(targetPassageIds.toStoredIdList(), completedPassageIds.toStoredIdList()),
        timing = BackupSessionTiming(startedAtEpochMillis, completedAtEpochMillis),
    )

    private fun BackupStudySession.toEntity() = ScholarStudySessionEntity(
        id = id,
        pathId = context.pathId,
        planId = context.planId,
        bookId = context.bookId,
        targetPassageIds = targets.targetPassageIds.toStoredIds(),
        completedPassageIds = targets.completedPassageIds.toStoredIds(),
        plannedMinutes = plannedMinutes,
        status = status,
        startedAtEpochMillis = timing.startedAtEpochMillis,
        completedAtEpochMillis = timing.completedAtEpochMillis,
    )

    private fun ScholarReviewEventEntity.toBackup() = BackupReviewEvent(
        id = id,
        reference = BackupReviewReference(flashcardId, passageId, bookId, category),
        reviewedAtEpochMillis = reviewedAtEpochMillis,
        outcome = BackupReviewOutcome(
            rating = outcome.rating,
            scheduledIntervalDays = outcome.scheduledIntervalDays,
            lapseCountAfterReview = outcome.lapseCountAfterReview,
            easeFactorAfterReview = outcome.easeFactorAfterReview,
        ),
    )

    private fun BackupReviewEvent.toEntity() = ScholarReviewEventEntity(
        id = id,
        flashcardId = reference.flashcardId,
        passageId = reference.passageId,
        bookId = reference.bookId,
        category = reference.category,
        reviewedAtEpochMillis = reviewedAtEpochMillis,
        outcome = ScholarReviewOutcomeEntity(
            rating = outcome.rating,
            scheduledIntervalDays = outcome.scheduledIntervalDays,
            lapseCountAfterReview = outcome.lapseCountAfterReview,
            easeFactorAfterReview = outcome.easeFactorAfterReview,
        ),
    )

    private fun <T> List<T>.isDistinct(): Boolean = distinct().size == size

    private companion object {
        const val BACKUP_SCHEMA_VERSION = 1
        const val BACKUP_MAX_CHARS = 10_000_000
        const val BACKUP_MAX_ITEMS = 200_000
    }
}
