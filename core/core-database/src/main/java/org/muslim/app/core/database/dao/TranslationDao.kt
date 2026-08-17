package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.muslim.app.core.database.entity.TranslationEntity

@Dao
interface TranslationDao {

    /** Translations of [globalNumber]'s ayah, one per installed language. */
    @Query("SELECT * FROM translations WHERE globalNumber = :globalNumber ORDER BY language")
    fun observeForAyah(globalNumber: Int): Flow<List<TranslationEntity>>

    /** True when at least one translation exists for the ayah. */
    @Query("SELECT COUNT(*) FROM translations WHERE globalNumber = :globalNumber")
    suspend fun countForAyah(globalNumber: Int): Int

    @Query("SELECT COUNT(*) FROM translations WHERE language = :language")
    suspend fun countForLanguage(language: String): Int

    /** Installed translation languages (BCP-47 tags), for the reader picker. */
    @Query("SELECT DISTINCT language FROM translations ORDER BY language")
    fun observeLanguages(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(translations: List<TranslationEntity>)

    @Query("DELETE FROM translations WHERE language = :language")
    suspend fun deleteLanguage(language: String)
}
