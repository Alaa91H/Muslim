package org.muslim.app.feature.scholarlibrary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scholar_books",
    indices = [Index(value = ["category"]), Index(value = ["title"])],
)
data class ScholarBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val authorDeathYearHijri: Int?,
    val description: String,
    val sourceName: String,
    val sourceUrl: String?,
    val licenseSummary: String,
    val imported: Boolean,
)

@Entity(
    tableName = "scholar_passages",
    indices = [Index(value = ["bookId"])],
)
data class ScholarPassageEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val chapter: String,
    val volume: String?,
    val page: String?,
    val text: String,
)

/** Normalized Arabic content index for private, offline full-text study search. */
@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "scholar_passage_fts")
data class ScholarPassageFtsEntity(
    @ColumnInfo(name = "normalized_text") val normalizedText: String,
    @ColumnInfo(name = "passage_id") val passageId: String,
)

@Entity(
    tableName = "scholar_notes",
    indices = [Index(value = ["passageId"])],
)
data class ScholarNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passageId: String,
    val text: String,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "scholar_flashcards",
    indices = [Index(value = ["passageId"]), Index(value = ["dueAtEpochMillis"])],
)
data class ScholarFlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passageId: String,
    val front: String,
    val back: String,
    val reviewCount: Int,
    val dueAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
)
