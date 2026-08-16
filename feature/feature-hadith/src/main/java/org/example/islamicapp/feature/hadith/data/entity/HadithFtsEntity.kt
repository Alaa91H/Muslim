package org.example.islamicapp.feature.hadith.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * FTS4 index over the hadith Arabic texts for instant offline search.
 * [normalizedText] is the diacritic-stripped text (see `ArabicText.normalize`)
 * so a plain query matches the vocalized original.
 */
@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "hadith_fts")
data class HadithFtsEntity(
    @ColumnInfo(name = "normalized_text") val normalizedText: String,
    @ColumnInfo(name = "hadith_id") val hadithId: Long,
)
