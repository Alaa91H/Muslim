package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.muslim.app.core.database.entity.AyahFtsEntity

/** Result row of a full-text search hit. */
data class AyahFtsHit(
    val globalNumber: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
)

@Dao
interface AyahFtsDao {

    @Query(
        """
        SELECT global_number AS globalNumber, surah_number AS surahNumber,
               number_in_surah AS numberInSurah
        FROM ayah_fts
        WHERE ayah_fts MATCH :query
        LIMIT 200
        """
    )
    suspend fun search(query: String): List<AyahFtsHit>

    @Query("SELECT COUNT(*) FROM ayah_fts")
    suspend fun count(): Int

    @Query("DELETE FROM ayah_fts")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<AyahFtsEntity>)
}
