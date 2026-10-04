package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * A Quran meaning translation for one ayah (PROJECT_PROMPT.md §6 Phase 2:
 * ترجمات المعاني). Installed from licensed translation packs via the import
 * pipeline; a small development sample ships in assets.
 */
@Entity(
    tableName = "translations",
    primaryKeys = ["globalNumber", "packId"],
    indices = [Index(value = ["language"])],
)
data class TranslationEntity(
    val globalNumber: Int,
    /** Stable source/work identifier; distinct works may share one language. */
    val packId: String,
    /** BCP-47 language tag, e.g. "en", "fr". */
    val language: String,
    val text: String,
    /** Source-provided footnotes serialized as JSON without rewriting. */
    @ColumnInfo(defaultValue = "'[]'") val footnotes: String = "[]",
)
