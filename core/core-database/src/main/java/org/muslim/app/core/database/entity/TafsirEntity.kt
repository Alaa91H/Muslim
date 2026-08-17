package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index

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
    /** Source id, e.g. "saadi", "muyassar", "ibn-kathir". */
    val source: String,
    val text: String,
)
