package org.example.islamicapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.islamicapp.core.database.entity.SurahEntity

@Dao
interface SurahDao {

    @Query("SELECT * FROM surahs ORDER BY number")
    fun observeAll(): Flow<List<SurahEntity>>

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(surahs: List<SurahEntity>)

    @Query("DELETE FROM surahs")
    suspend fun clearAll()
}
