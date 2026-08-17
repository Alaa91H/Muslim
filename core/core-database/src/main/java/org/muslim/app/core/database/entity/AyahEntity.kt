package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A Quran ayah row (Uthmani text, sourced from Tanzil's open dataset).
 *
 * [globalNumber] is the continuous numbering across the whole mushaf
 * (1..6236); [numberInSurah] is the per-surah numbering.
 */
@Entity(
    tableName = "ayahs",
    indices = [
        Index(value = ["surahNumber", "numberInSurah"], unique = true),
        Index(value = ["globalNumber"], unique = true),
    ],
)
data class AyahEntity(
    @PrimaryKey val globalNumber: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    /** 1-based juz' (1..30). */
    val juz: Int,
    /** 1-based mushaf page. */
    val page: Int,
    val text: String,
)
