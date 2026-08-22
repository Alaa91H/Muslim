package org.muslim.app.feature.quran.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class QuranBackup(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long,
    val bookmarks: List<QuranBackupBookmark> = emptyList(),
    val notes: List<QuranBackupNote> = emptyList(),
    val lastReadGlobalNumber: Int? = null,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class QuranBackupBookmark(
    val globalNumber: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val text: String,
    val addedAt: Long,
)

@Serializable
data class QuranBackupNote(
    val globalNumber: Int,
    val text: String,
    val updatedAt: Long,
)

object QuranBackupCodec {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun encode(backup: QuranBackup): String = json.encodeToString(backup)

    fun decode(value: String): QuranBackup = json.decodeFromString(value)
}
