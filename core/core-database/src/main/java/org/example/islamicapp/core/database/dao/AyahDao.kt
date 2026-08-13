package org.example.islamicapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.islamicapp.core.database.entity.AyahEntity

@Dao
interface AyahDao {

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah")
    fun observeSurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs ORDER BY globalNumber")
    fun observeAll(): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE globalNumber = :globalNumber")
    suspend fun byGlobal(globalNumber: Int): AyahEntity?

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(ayahs: List<AyahEntity>)
}
