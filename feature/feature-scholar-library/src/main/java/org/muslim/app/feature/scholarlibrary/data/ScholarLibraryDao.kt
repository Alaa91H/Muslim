package org.muslim.app.feature.scholarlibrary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions") // One Room transaction boundary for catalog, notes, and flashcards.
interface ScholarLibraryDao {
    @Query("SELECT * FROM scholar_books ORDER BY category, title")
    fun observeBooks(): Flow<List<ScholarBookEntity>>

    @Query("SELECT * FROM scholar_books WHERE id = :bookId")
    suspend fun bookById(bookId: String): ScholarBookEntity?

    @Query("SELECT * FROM scholar_passages WHERE bookId = :bookId ORDER BY id")
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

    @Query("UPDATE scholar_flashcards SET reviewCount = :reviewCount, dueAtEpochMillis = :dueAt WHERE id = :id")
    suspend fun updateFlashcardReview(id: Long, reviewCount: Int, dueAt: Long)

    @Query("DELETE FROM scholar_flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Long)
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
