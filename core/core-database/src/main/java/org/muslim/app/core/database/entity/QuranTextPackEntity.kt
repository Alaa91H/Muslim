package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/** Provenance and verified coverage for one installed Quran text work. */
@Entity(tableName = "quran_text_packs")
data class QuranTextPackEntity(
    @PrimaryKey val id: String,
    /** `meaning_translation` or `tafsir_translation`. */
    val kind: String,
    val languageTag: String,
    val title: String,
    val work: String,
    val translator: String,
    val publisher: String,
    @ColumnInfo(defaultValue = "''") val sourceAttribution: String,
    val sourceUrl: String,
    val license: String,
    val version: String,
    val reviewer: String,
    val reviewReference: String,
    val entryCount: Int,
    val footnoteCount: Int,
    val sha256: String,
    val verifiedAtEpochMillis: Long,
)
