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
    val subtitle: String? = null,
    @ColumnInfo(defaultValue = "'ar'") val language: String = "ar",
    @ColumnInfo(defaultValue = "'Unspecified'") val difficulty: String = "Unspecified",
    val publisher: String? = null,
    val edition: String? = null,
    val editor: String? = null,
    val publicationYear: String? = null,
    val volumeCount: Int? = null,
    @ColumnInfo(defaultValue = "''") val keywords: String = "",
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
    val section: String? = null,
    @ColumnInfo(defaultValue = "0") val orderIndex: Int = 0,
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
    @ColumnInfo(defaultValue = "0") val intervalDays: Int = 0,
    @ColumnInfo(defaultValue = "2.5") val easeFactor: Double = 2.5,
    @ColumnInfo(defaultValue = "0") val lapseCount: Int = 0,
    val lastReviewedAtEpochMillis: Long? = null,
    val lastRating: String? = null,
)

@Entity(
    tableName = "scholar_bookmarks",
    indices = [Index(value = ["createdAtEpochMillis"])],
)
data class ScholarBookmarkEntity(
    @PrimaryKey val passageId: String,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "scholar_highlights",
    indices = [Index(value = ["passageId"]), Index(value = ["createdAtEpochMillis"])],
)
data class ScholarHighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passageId: String,
    val quote: String,
    val note: String?,
    val style: String,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "scholar_reading_progress",
    indices = [Index(value = ["status"]), Index(value = ["updatedAtEpochMillis"])],
)
data class ScholarReadingProgressEntity(
    @PrimaryKey val bookId: String,
    val lastPassageId: String?,
    val status: String,
    val progressPercent: Int,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "scholar_study_plans",
    indices = [Index(value = ["pathId"]), Index(value = ["active"])],
)
data class ScholarStudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pathId: String,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val targetPassagesPerSession: Int,
    val active: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)


@Entity(
    tableName = "scholar_study_sessions",
    indices = [
        Index(value = ["pathId"]),
        Index(value = ["status"]),
        Index(value = ["completedAtEpochMillis"]),
    ],
)
data class ScholarStudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pathId: String,
    val planId: Long?,
    val bookId: String,
    val targetPassageIds: String,
    val completedPassageIds: String,
    val plannedMinutes: Int,
    val status: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
)
