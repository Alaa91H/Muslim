package org.example.islamicapp.feature.quran.domain

import kotlinx.coroutines.flow.Flow

/** Provides the full Quran content from the offline Room database. */
interface QuranRepository {

    /** Seeds the pre-populated content from bundled assets on first launch. */
    suspend fun ensureSeeded()

    fun observeSurahs(): Flow<List<Surah>>

    /** Metadata of a single surah (for the reader header). */
    fun observeSurahMetadata(surahNumber: Int): Flow<Surah?>

    fun observeSurah(surahNumber: Int): Flow<List<Ayah>>

    /** Full-text search over the normalized Uthmani text. */
    suspend fun search(rawQuery: String): List<Ayah>

    fun observeBookmarks(): Flow<List<Bookmark>>

    suspend fun isBookmarked(globalNumber: Int): Boolean

    suspend fun addBookmark(ayah: Ayah)

    suspend fun removeBookmark(globalNumber: Int)
}
