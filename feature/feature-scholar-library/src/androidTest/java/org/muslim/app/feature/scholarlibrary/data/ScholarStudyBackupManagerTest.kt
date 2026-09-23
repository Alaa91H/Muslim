package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScholarStudyBackupManagerTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun backupRoundTripRestoresAllLocalStudyLayers() = runBlocking {
        val sourceDb = newDatabase()
        val targetDb = newDatabase()
        try {
            seedReferenceContent(sourceDb.libraryDao())
            seedReferenceContent(targetDb.libraryDao())
            seedStudyState(sourceDb.libraryDao())

            val raw = ScholarStudyBackupManager(sourceDb.libraryDao(), json).exportBackup()
            val result = ScholarStudyBackupManager(targetDb.libraryDao(), json).restoreBackup(
                rawText = raw,
                validPathIds = setOf(PATH_ID),
            )

            assertThat(result).isInstanceOf(ScholarStudyBackupRestoreResult.Success::class.java)
            assertRestored(targetDb.libraryDao())
        } finally {
            sourceDb.close()
            targetDb.close()
        }
    }

    @Test
    fun restoreRejectsMissingReferenceContentBeforeWriting() = runBlocking {
        val sourceDb = newDatabase()
        val targetDb = newDatabase()
        try {
            seedReferenceContent(sourceDb.libraryDao())
            sourceDb.libraryDao().insertNote(
                ScholarNoteEntity(
                    passageId = PASSAGE_ID,
                    text = "ملاحظة مرتبطة بالمصدر",
                    createdAtEpochMillis = 1_000L,
                ),
            )

            val raw = ScholarStudyBackupManager(sourceDb.libraryDao(), json).exportBackup()
            val result = ScholarStudyBackupManager(targetDb.libraryDao(), json).restoreBackup(
                rawText = raw,
                validPathIds = emptySet(),
            )

            assertThat(result).isInstanceOf(ScholarStudyBackupRestoreResult.Failure::class.java)
            assertThat(targetDb.libraryDao().observeNotes().first()).isEmpty()
        } finally {
            sourceDb.close()
            targetDb.close()
        }
    }

    private fun newDatabase(): ScholarLibraryDatabase =
        Room.inMemoryDatabaseBuilder(context, ScholarLibraryDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private suspend fun seedReferenceContent(dao: ScholarLibraryDao) {
        dao.upsertBooks(
            listOf(
                ScholarBookEntity(
                    id = BOOK_ID,
                    title = "كتاب الاختبار",
                    author = "مؤلف",
                    category = "Hadith",
                    authorDeathYearHijri = null,
                    description = "وصف",
                    sourceName = "مصدر مرخّص",
                    sourceUrl = null,
                    licenseSummary = "إذن اختبار",
                    imported = true,
                ),
            ),
        )
        dao.upsertPassages(
            listOf(
                ScholarPassageEntity(
                    id = PASSAGE_ID,
                    bookId = BOOK_ID,
                    chapter = "باب",
                    volume = "1",
                    page = "1",
                    text = "نص مرجعي",
                    section = "قسم",
                    orderIndex = 0,
                ),
            ),
        )
    }

    private suspend fun seedStudyState(dao: ScholarLibraryDao) {
        dao.insertNote(ScholarNoteEntity(1, PASSAGE_ID, "ملاحظة", 1_000L))
        dao.insertFlashcard(
            ScholarFlashcardEntity(
                id = 1,
                passageId = PASSAGE_ID,
                front = "سؤال",
                back = "جواب",
                createdAtEpochMillis = 1_100L,
                reviewState = ScholarFlashcardReviewStateEntity(
                    reviewCount = 2,
                    dueAtEpochMillis = 2_000L,
                    intervalDays = 3,
                    easeFactor = 2.5,
                    lapseCount = 0,
                    lastReviewedAtEpochMillis = 1_500L,
                    lastRating = "Good",
                ),
            ),
        )
        dao.upsertBookmark(ScholarBookmarkEntity(PASSAGE_ID, 1_200L))
        dao.insertHighlight(
            ScholarHighlightEntity(
                id = 1,
                passageId = PASSAGE_ID,
                quote = "نص مرجعي",
                note = "تعليق",
                style = "Important",
                createdAtEpochMillis = 1_300L,
            ),
        )
        dao.upsertReadingProgress(
            ScholarReadingProgressEntity(
                bookId = BOOK_ID,
                lastPassageId = PASSAGE_ID,
                status = "InProgress",
                progressPercent = 50,
                updatedAtEpochMillis = 1_400L,
            ),
        )
        dao.upsertStudyPlan(
            ScholarStudyPlanEntity(
                id = 1,
                pathId = PATH_ID,
                sessionsPerWeek = 3,
                minutesPerSession = 45,
                targetPassagesPerSession = 1,
                active = true,
                createdAtEpochMillis = 1_000L,
                updatedAtEpochMillis = 1_400L,
            ),
        )
        dao.upsertStudySession(
            ScholarStudySessionEntity(
                id = 1,
                pathId = PATH_ID,
                planId = 1,
                bookId = BOOK_ID,
                targetPassageIds = listOf(PASSAGE_ID).toStoredIds(),
                completedPassageIds = listOf(PASSAGE_ID).toStoredIds(),
                plannedMinutes = 45,
                status = "Completed",
                startedAtEpochMillis = 1_500L,
                completedAtEpochMillis = 1_900L,
            ),
        )
        dao.insertReviewEvent(
            ScholarReviewEventEntity(
                id = 1,
                flashcardId = 1,
                passageId = PASSAGE_ID,
                bookId = BOOK_ID,
                category = "Hadith",
                reviewedAtEpochMillis = 1_800L,
                outcome = ScholarReviewOutcomeEntity(
                    rating = "Good",
                    scheduledIntervalDays = 3,
                    lapseCountAfterReview = 0,
                    easeFactorAfterReview = 2.5,
                ),
            ),
        )
    }

    private suspend fun assertRestored(dao: ScholarLibraryDao) {
        assertThat(dao.observeNotes().first()).hasSize(1)
        assertThat(dao.observeFlashcards().first()).hasSize(1)
        assertThat(dao.observeBookmarks().first()).hasSize(1)
        assertThat(dao.observeHighlights().first()).hasSize(1)
        assertThat(dao.observeReadingProgress().first()).hasSize(1)
        assertThat(dao.observeStudyPlans().first()).hasSize(1)
        assertThat(dao.observeStudySessions().first()).hasSize(1)
        assertThat(dao.observeReviewEvents().first()).hasSize(1)
    }

    private companion object {
        const val BOOK_ID = "backup-test-book"
        const val PASSAGE_ID = "backup-test-passage"
        const val PATH_ID = "backup-test-path"
    }
}
