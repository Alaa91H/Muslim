package org.muslim.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One continuous tasbih counting session.
 *
 * Session history lives in Room because it is durable, queryable user data;
 * lightweight UI preferences remain in DataStore.
 */
@Entity(
    tableName = "tasbih_sessions",
    indices = [
        Index(value = ["phraseId"]),
        Index(value = ["startedAtEpochMillis"]),
        Index(value = ["endedAtEpochMillis"]),
    ],
)
data class TasbihSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseId: String,
    val mode: String,
    val target: Int,
    val roundsGoal: Int,
    val count: Long,
    val startedAtEpochMillis: Long,
    val lastUpdatedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val endReason: String? = null,
)
