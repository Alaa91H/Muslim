package org.example.islamicapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * A Quran meaning translation for one ayah (PROJECT_PROMPT.md §6 Phase 2:
 * ترجمات المعاني). Installed from licensed translation packs via the import
 * pipeline; a small development sample ships in assets.
 */
@Entity(
    tableName = "translations",
    primaryKeys = ["globalNumber", "language"],
    indices = [Index(value = ["language"])],
)
data class TranslationEntity(
    val globalNumber: Int,
    /** BCP-47 language tag, e.g. "en", "fr". */
    val language: String,
    val text: String,
)
