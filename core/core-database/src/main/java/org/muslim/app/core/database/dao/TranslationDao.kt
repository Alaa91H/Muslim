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
    @Query("""SELECT t.* FROM translations t INNER JOIN quran_text_packs p ON p.id = t.packId
        WHERE t.globalNumber = :globalNumber AND p.kind = 'meaning_translation' AND p.sourceAttribution != ''
        ORDER BY t.language, p.title""")
    fun observeForAyah(globalNumber: Int): Flow<List<TranslationEntity>>

    /** True when at least one translation exists for the ayah. */
    @Query("SELECT COUNT(*) FROM translations WHERE globalNumber = :globalNumber")
    suspend fun countForAyah(globalNumber: Int): Int

    @Query("SELECT COUNT(*) FROM translations WHERE language = :language")
    suspend fun countForLanguage(language: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(translations: List<TranslationEntity>)

    @Query("DELETE FROM translations WHERE packId = :packId")
    suspend fun deletePack(packId: String)
}
