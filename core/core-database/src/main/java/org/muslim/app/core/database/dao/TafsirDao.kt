package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.muslim.app.core.database.entity.TafsirEntity

@Dao
interface TafsirDao {

    @Query("SELECT * FROM tafsir WHERE globalNumber = :globalNumber ORDER BY source")
    fun observeForAyah(globalNumber: Int): Flow<List<TafsirEntity>>

    @Query("SELECT DISTINCT source FROM tafsir ORDER BY source")
    fun observeSources(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM tafsir WHERE globalNumber = :globalNumber")
    suspend fun countForAyah(globalNumber: Int): Int

    @Query("SELECT COUNT(*) FROM tafsir WHERE source = :source")
    suspend fun countForSource(source: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TafsirEntity>)

    @Query("DELETE FROM tafsir WHERE source = :source")
    suspend fun deleteSource(source: String)

    /** Replaces a complete source only after its validated rows are ready to commit. */
    @Transaction
    suspend fun replaceSource(source: String, entries: List<TafsirEntity>) {
        deleteSource(source)
        insertAll(entries)
    }
}
