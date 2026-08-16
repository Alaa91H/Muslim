package org.example.islamicapp.feature.hadith.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A hadith record (PROJECT_PROMPT.md §6 Phase 3). Content ships via the
 * import pipeline and is subject to independent religious review before the
 * official release; the bundled asset is a development sample.
 */
@Entity(
    tableName = "hadiths",
    indices = [Index(value = ["collection"])],
)
data class HadithEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Collection id: "nawawi40", "riyad", "bukhari", "muslim", ... */
    val collection: String,
    val chapter: String?,
    val numberInBook: Int?,
    val arabicText: String,
    val translation: String,
    /** "Sahih", "Hasan", "Da'if", or a short combined verdict. */
    val grade: String,
    /** Attribution, e.g. "رواه البخاري ومسلم". */
    val source: String,
)
