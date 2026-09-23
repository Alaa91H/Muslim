package org.muslim.app.feature.scholarlibrary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions") // One Room boundary for catalog and local study state.
interface ScholarLibraryDao {
    @Query("SELECT * FROM scholar_books ORDER BY category, title")
    fun observeBooks(): Flow<List<ScholarBookEntity>>

    @Query("SELECT * FROM scholar_books WHERE id = :bookId")
    suspend fun bookById(bookId: String): ScholarBookEntity?

    @Query(
        """
        SELECT * FROM scholar_passages
        WHERE bookId = :bookId
        ORDER BY
            CASE WHEN volume IS NULL THEN 0 ELSE 1 END,
            volume,
            chapter,
            CASE WHEN section IS NULL THEN 0 ELSE 1 END,
            section,
            orderIndex,
            id
        """,
    )
    fun observePassagesForBook(bookId: String): Flow<List<ScholarPassageEntity>>

    @Query("SELECT * FROM scholar_passages WHERE id = :passageId")
    suspend fun passageById(passageId: String): ScholarPassageEntity?

    @Query("SELECT COUNT(*) FROM scholar_books")
    suspend fun bookCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(books: List<ScholarBookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPassages(passages: List<ScholarPassageEntity>)

    @Query("DELETE FROM scholar_books")
    suspend fun clearBooks()

    @Query("DELETE FROM scholar_passages")
    suspend fun clearPassages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<ScholarNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ScholarNoteEntity): Long

    @Query("SELECT * FROM scholar_notes ORDER BY createdAtEpochMillis DESC")
    fun observeNotes(): Flow<List<ScholarNoteEntity>>

    @Query("DELETE FROM scholar_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: ScholarFlashcardEntity): Long

    @Query("SELECT * FROM scholar_flashcards ORDER BY dueAtEpochMillis, id")
    fun observeFlashcards(): Flow<List<ScholarFlashcardEntity>>

    @Query("SELECT * FROM scholar_flashcards WHERE id = :id LIMIT 1")
    suspend fun flashcardById(id: Long): ScholarFlashcardEntity?

    @Update
    suspend fun updateFlashcard(card: ScholarFlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewEvent(event: ScholarReviewEventEntity): Long

    @Query("SELECT * FROM scholar_review_events ORDER BY reviewedAtEpochMillis DESC, id DESC")
    fun observeReviewEvents(): Flow<List<ScholarReviewEventEntity>>

    @Transaction
    suspend fun applyFlashcardReview(
        card: ScholarFlashcardEntity,
        event: ScholarReviewEventEntity,
    ) {
        updateFlashcard(card)
        insertReviewEvent(event)
    }

    @Query("DELETE FROM scholar_flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Long)

    @Query("SELECT * FROM scholar_bookmarks ORDER BY createdAtEpochMillis DESC")
    fun observeBookmarks(): Flow<List<ScholarBookmarkEntity>>

    @Query("SELECT * FROM scholar_bookmarks WHERE passageId = :passageId LIMIT 1")
    suspend fun bookmarkByPassage(passageId: String): ScholarBookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: ScholarBookmarkEntity)

    @Query("DELETE FROM scholar_bookmarks WHERE passageId = :passageId")
    suspend fun deleteBookmark(passageId: String)

    @Query("SELECT * FROM scholar_highlights ORDER BY createdAtEpochMillis DESC")
    fun observeHighlights(): Flow<List<ScholarHighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: ScholarHighlightEntity): Long

    @Query("DELETE FROM scholar_highlights WHERE id = :id")
    suspend fun deleteHighlight(id: Long)

    @Query("SELECT * FROM scholar_reading_progress ORDER BY updatedAtEpochMillis DESC")
    fun observeReadingProgress(): Flow<List<ScholarReadingProgressEntity>>

    @Query("SELECT * FROM scholar_reading_progress WHERE bookId = :bookId LIMIT 1")
    suspend fun readingProgressByBook(bookId: String): ScholarReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReadingProgress(progress: ScholarReadingProgressEntity)

    @Query("SELECT * FROM scholar_study_plans ORDER BY active DESC, updatedAtEpochMillis DESC, id DESC")
    fun observeStudyPlans(): Flow<List<ScholarStudyPlanEntity>>

    @Query(
        """
        UPDATE scholar_study_plans
        SET active = 0, updatedAtEpochMillis = :updatedAt
        WHERE pathId = :pathId AND active = 1
        """,
    )
    suspend fun deactivateStudyPlansForPath(pathId: String, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyPlan(plan: ScholarStudyPlanEntity): Long

    @Query("DELETE FROM scholar_study_plans WHERE id = :id")
    suspend fun deleteStudyPlan(id: Long)

    @Query("UPDATE scholar_study_plans SET active = :active, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun updateStudyPlanActive(id: Long, active: Boolean, updatedAt: Long)


    @Query("SELECT * FROM scholar_study_sessions ORDER BY startedAtEpochMillis DESC, id DESC")
    fun observeStudySessions(): Flow<List<ScholarStudySessionEntity>>

    @Query(
        """
        SELECT * FROM scholar_study_sessions
        WHERE pathId = :pathId AND status = 'InProgress'
        ORDER BY startedAtEpochMillis DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun activeStudySessionForPath(pathId: String): ScholarStudySessionEntity?

    @Query("SELECT * FROM scholar_study_sessions WHERE id = :id LIMIT 1")
    suspend fun studySessionById(id: Long): ScholarStudySessionEntity?

    @Query(
        """
        SELECT * FROM scholar_study_plans
        WHERE pathId = :pathId AND active = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun activeStudyPlanForPath(pathId: String): ScholarStudyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudySession(session: ScholarStudySessionEntity): Long
}

@Dao
interface ScholarLibraryFtsDao {
    @Query("SELECT passage_id FROM scholar_passage_fts WHERE scholar_passage_fts MATCH :query LIMIT :limit")
    suspend fun searchPassageIds(query: String, limit: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRows(rows: List<ScholarPassageFtsEntity>)

    @Query("DELETE FROM scholar_passage_fts")
    suspend fun clearAll()
}
