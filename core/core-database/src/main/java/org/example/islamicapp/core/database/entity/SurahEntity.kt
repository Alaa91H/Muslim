package org.example.islamicapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A Quran surah's metadata row (PROJECT_PROMPT.md §6 Phase 2). */
@Entity(
    tableName = "surahs",
    indices = [Index(value = ["number"], unique = true)],
)
data class SurahEntity(
    @PrimaryKey val number: Int,
    val arabicName: String,
    val englishName: String,
    val translation: String,
    /** "Meccan" or "Medinan". */
    val revelationType: String,
    val ayahCount: Int,
)
