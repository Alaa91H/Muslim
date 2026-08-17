package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.muslim.app.core.database.entity.SurahEntity

@Dao
interface SurahDao {

    @Query("SELECT * FROM surahs ORDER BY number")
    fun observeAll(): Flow<List<SurahEntity>>

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(surahs: List<SurahEntity>)
}
