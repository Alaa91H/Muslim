package org.example.islamicapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.islamicapp.core.database.entity.TafsirEntity

@Dao
interface TafsirDao {

    @Query("SELECT * FROM tafsir WHERE globalNumber = :globalNumber ORDER BY source")
    fun observeForAyah(globalNumber: Int): Flow<List<TafsirEntity>>

    @Query("SELECT COUNT(*) FROM tafsir WHERE globalNumber = :globalNumber")
    suspend fun countForAyah(globalNumber: Int): Int

    @Query("SELECT COUNT(*) FROM tafsir WHERE source = :source")
    suspend fun countForSource(source: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TafsirEntity>)

    @Query("DELETE FROM tafsir WHERE source = :source")
    suspend fun deleteSource(source: String)
}
