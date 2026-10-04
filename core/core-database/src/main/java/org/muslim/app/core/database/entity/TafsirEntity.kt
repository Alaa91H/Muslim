package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * A tafsir (exegesis) entry for one ayah from a named source
 * (PROJECT_PROMPT.md §6 Phase 2: تفاسير متعددة). Tafsir packs are installed
 * through the import pipeline; content undergoes religious review before the
 * official release.
 */
@Entity(
    tableName = "tafsir",
    primaryKeys = ["globalNumber", "source"],
    indices = [Index(value = ["source"])],
)
data class TafsirEntity(
    val globalNumber: Int,
    /** Stable pack id, e.g. "arabic_muyassar". */
    val source: String,
    val text: String,
    /** Source-provided footnotes serialized as JSON without rewriting. */
    @ColumnInfo(defaultValue = "'[]'") val footnotes: String = "[]",
)
