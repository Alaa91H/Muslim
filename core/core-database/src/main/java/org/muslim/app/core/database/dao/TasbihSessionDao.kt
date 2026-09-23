package org.muslim.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.muslim.app.core.database.entity.TasbihSessionEntity

@Dao
interface TasbihSessionDao {

    @Query(
        """
        SELECT * FROM tasbih_sessions
        WHERE endedAtEpochMillis IS NULL
        ORDER BY lastUpdatedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getActive(): TasbihSessionEntity?

    @Query(
        """
        SELECT * FROM tasbih_sessions
        WHERE endedAtEpochMillis IS NULL
        ORDER BY lastUpdatedAtEpochMillis DESC
        LIMIT 1
        """
    )
    fun observeActive(): Flow<TasbihSessionEntity?>

    @Query(
        """
        SELECT * FROM tasbih_sessions
        ORDER BY startedAtEpochMillis DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<TasbihSessionEntity>>

    @Insert
    suspend fun insert(session: TasbihSessionEntity): Long

    @Update
    suspend fun update(session: TasbihSessionEntity)

    @Query(
        """
        UPDATE tasbih_sessions
        SET endedAtEpochMillis = :endedAtEpochMillis,
            lastUpdatedAtEpochMillis = :endedAtEpochMillis,
            endReason = :reason
        WHERE endedAtEpochMillis IS NULL
        """
    )
    suspend fun endActive(endedAtEpochMillis: Long, reason: String)
}
