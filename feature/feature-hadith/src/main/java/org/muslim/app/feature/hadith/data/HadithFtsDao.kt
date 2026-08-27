package org.muslim.app.feature.hadith.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.muslim.app.feature.hadith.data.entity.HadithEntity
import org.muslim.app.feature.hadith.data.entity.HadithFtsEntity

@Dao
interface HadithFtsDao {

    /**
     * SQL-side join keeps a selected book's matching rows paged. The collection
     * predicate is intentional: only the user-opened book is stored and searched.
     */
    @Query(
        """
        SELECT hadiths.* FROM hadiths
        INNER JOIN hadith_fts ON hadiths.id = hadith_fts.hadith_id
        WHERE hadith_fts MATCH :query AND hadiths.collection = :collection
        ORDER BY hadiths.id
        """,
    )
    fun pagedSearch(collection: String, query: String): PagingSource<Int, HadithEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<HadithFtsEntity>)

    @Query("DELETE FROM hadith_fts")
    suspend fun clearAll()
}
