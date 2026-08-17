package org.muslim.app.feature.hadith.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.muslim.app.feature.hadith.data.entity.HadithFtsEntity

@Dao
interface HadithFtsDao {

    /** FTS4 MATCH query; the caller normalizes the raw query first. */
    @Query("SELECT hadith_id FROM hadith_fts WHERE hadith_fts MATCH :query")
    suspend fun searchIds(query: String): List<Long>

    @Query("SELECT COUNT(*) FROM hadith_fts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<HadithFtsEntity>)
}
