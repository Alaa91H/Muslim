package org.muslim.app.feature.hadith.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
import org.muslim.app.feature.hadith.data.entity.HadithEntity
import org.muslim.app.feature.hadith.data.entity.HadithFtsEntity

@Dao
interface HadithFtsDao {

    /** FTS4 MATCH query; the caller normalizes the raw query first. */
    @Query("SELECT hadith_id FROM hadith_fts WHERE hadith_fts MATCH :query")
    suspend fun searchIds(query: String): List<Long>

    /** SQL-side join keeps matching results paged and avoids materializing all ids. */
    @Query("""
        SELECT hadiths.* FROM hadiths
        INNER JOIN hadith_fts ON hadiths.id = hadith_fts.hadith_id
        WHERE hadith_fts MATCH :query
        ORDER BY hadiths.collection, hadiths.id
    """)
    fun pagedSearch(query: String): PagingSource<Int, HadithEntity>

    @Query("SELECT COUNT(*) FROM hadith_fts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<HadithFtsEntity>)

    @Query("DELETE FROM hadith_fts")
    suspend fun clearAll()
}
