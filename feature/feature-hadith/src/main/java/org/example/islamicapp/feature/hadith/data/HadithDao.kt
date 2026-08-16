package org.example.islamicapp.feature.hadith.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.islamicapp.feature.hadith.data.entity.HadithEntity

@Dao
interface HadithDao {

    @Query("SELECT * FROM hadiths ORDER BY collection, id")
    fun observeAll(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE collection = :collection ORDER BY id")
    fun observeCollection(collection: String): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE id = :id")
    suspend fun byId(id: Long): HadithEntity?

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hadiths: List<HadithEntity>)
}
