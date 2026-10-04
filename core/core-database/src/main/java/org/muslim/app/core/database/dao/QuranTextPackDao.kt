package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.muslim.app.core.database.entity.QuranTextPackEntity

@Dao
interface QuranTextPackDao {
    @Query("SELECT * FROM quran_text_packs WHERE sourceAttribution != '' ORDER BY languageTag, title")
    fun observeAll(): Flow<List<QuranTextPackEntity>>

    @Query("SELECT * FROM quran_text_packs WHERE kind = :kind AND sourceAttribution != '' ORDER BY languageTag, title")
    fun observeKind(kind: String): Flow<List<QuranTextPackEntity>>

    @Query("SELECT * FROM quran_text_packs WHERE id = :id LIMIT 1")
    suspend fun get(id: String): QuranTextPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: QuranTextPackEntity)

    @Query("DELETE FROM quran_text_packs WHERE id = :id")
    suspend fun delete(id: String)
}
