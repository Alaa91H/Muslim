package org.example.islamicapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A user bookmark on a Quran ayah (device-local, privacy-first). */
@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["surahNumber"])],
)
data class BookmarkEntity(
    @PrimaryKey val globalNumber: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    /** A copy of the ayah text so the bookmarks screen needs no join. */
    val text: String,
    val addedAt: Long,
)
