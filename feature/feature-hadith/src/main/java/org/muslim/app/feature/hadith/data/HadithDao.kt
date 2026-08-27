package org.muslim.app.feature.hadith.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.muslim.app.feature.hadith.data.entity.HadithEntity

/** Compact SQL row used for a loaded book's chapter index. */
data class HadithChapterRow(
    val title: String,
    val firstHadithNumber: Int?,
    val lastHadithNumber: Int?,
    val hadithCount: Int,
)

@Dao
interface HadithDao {

    /** Page-by-page browse source for the one collection currently stored in Room. */
    @Query("SELECT * FROM hadiths WHERE collection = :collection ORDER BY id")
    fun pagedCollection(collection: String): PagingSource<Int, HadithEntity>

    /**
     * A chapter selection remains SQL-side. `COALESCE` groups legacy null chapter
     * labels into a stable empty key instead of materializing rows in the UI.
     */
    @Query(
        """
        SELECT * FROM hadiths
        WHERE collection = :collection AND COALESCE(chapter, '') = :chapter
        ORDER BY id
        """,
    )
    fun pagedChapter(collection: String, chapter: String): PagingSource<Int, HadithEntity>

    /** Builds only the chapter metadata for the currently loaded book. */
    @Query(
        """
        SELECT
            COALESCE(chapter, '') AS title,
            MIN(numberInBook) AS firstHadithNumber,
            MAX(numberInBook) AS lastHadithNumber,
            COUNT(*) AS hadithCount
        FROM hadiths
        WHERE collection = :collection
        GROUP BY COALESCE(chapter, '')
        ORDER BY MIN(id)
        """,
    )
    fun observeChapters(collection: String): Flow<List<HadithChapterRow>>

    @Query("SELECT * FROM hadiths WHERE id = :id")
    suspend fun byId(id: Long): HadithEntity?

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM hadiths WHERE collection = :collection")
    suspend fun countCollection(collection: String): Int

    /** A single deterministic row from the currently loaded book. */
    @Query("SELECT * FROM hadiths WHERE collection = :collection ORDER BY id LIMIT 1 OFFSET :offset")
    suspend fun byCollectionOffset(collection: String, offset: Int): HadithEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hadiths: List<HadithEntity>)

    @Query("DELETE FROM hadiths")
    suspend fun clearAll()
}
