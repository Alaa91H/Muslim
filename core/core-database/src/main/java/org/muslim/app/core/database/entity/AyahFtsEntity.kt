package org.muslim.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * FTS4 index over the Quran ayahs for instant offline text search
 * (PROJECT_PROMPT.md §6 Phase 2: بحث نصي في القرآن).
 *
 * [normalizedText] is the diacritic-stripped Uthmani text (see
 * `ArabicText.normalize`) so a plain query like "رحمة" matches
 * "ٱلرَّحْمَٰنِ" — the raw mushaf text is not indexable as-is because
 * tashkeel splits tokens.
 */
@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "ayah_fts")
data class AyahFtsEntity(
    @ColumnInfo(name = "normalized_text") val normalizedText: String,
    @ColumnInfo(name = "global_number") val globalNumber: Int,
    @ColumnInfo(name = "surah_number") val surahNumber: Int,
    @ColumnInfo(name = "number_in_surah") val numberInSurah: Int,
)
