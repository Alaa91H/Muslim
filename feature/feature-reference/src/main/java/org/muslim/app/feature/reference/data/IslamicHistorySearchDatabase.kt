package org.muslim.app.feature.reference.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Fts4
@Entity(tableName = "history_search_fts")
internal data class HistorySearchFtsEntity(
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "title_arabic") val titleArabic: String,
    @ColumnInfo(name = "title_english") val titleEnglish: String,
    @ColumnInfo(name = "summary_arabic") val summaryArabic: String,
    @ColumnInfo(name = "summary_english") val summaryEnglish: String,
    @ColumnInfo(name = "normalized_text") val normalizedText: String,
)

@Entity(
    tableName = "history_content_records",
    primaryKeys = ["entity_type", "entity_id"],
)
internal data class HistoryContentEntity(
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
)

@Entity(tableName = "history_content_meta")
internal data class HistoryContentMetaEntity(
    @PrimaryKey val id: Int = SINGLETON_CONTENT_META_ID,
    @ColumnInfo(name = "content_version") val contentVersion: Int,
    @ColumnInfo(name = "record_count") val recordCount: Int,
)

@Entity(tableName = "history_search_meta")
internal data class HistorySearchMetaEntity(
    @PrimaryKey val id: Int = SINGLETON_META_ID,
    @ColumnInfo(name = "content_version") val contentVersion: Int,
    @ColumnInfo(name = "document_count") val documentCount: Int,
)

@Dao
internal interface HistorySearchDao {
    @Query(
        """
        SELECT * FROM history_search_fts
        WHERE history_search_fts MATCH :matchQuery
        LIMIT :limit
        """,
    )
    suspend fun search(matchQuery: String, limit: Int): List<HistorySearchFtsEntity>

    @Query(
        """
        SELECT * FROM history_search_fts
        WHERE history_search_fts MATCH :matchQuery
          AND entity_type = :entityType
        LIMIT :limit
        """,
    )
    suspend fun searchByType(
        matchQuery: String,
        entityType: String,
        limit: Int,
    ): List<HistorySearchFtsEntity>

    @Query("DELETE FROM history_search_fts")
    suspend fun clearIndex()

    @Insert
    suspend fun insertAll(rows: List<HistorySearchFtsEntity>)

    @Query("SELECT * FROM history_search_meta WHERE id = 1 LIMIT 1")
    suspend fun metadata(): HistorySearchMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: HistorySearchMetaEntity)

    @Query(
        """
        SELECT * FROM history_content_records
        WHERE entity_type = :entityType AND entity_id = :entityId
        LIMIT 1
        """,
    )
    suspend fun content(
        entityType: String,
        entityId: String,
    ): HistoryContentEntity?

    @Query("DELETE FROM history_content_records")
    suspend fun clearContent()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(rows: List<HistoryContentEntity>)

    @Query("SELECT * FROM history_content_meta WHERE id = 1 LIMIT 1")
    suspend fun contentMetadata(): HistoryContentMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContentMetadata(metadata: HistoryContentMetaEntity)
}

@Database(
    entities = [
        HistorySearchFtsEntity::class,
        HistorySearchMetaEntity::class,
        HistoryContentEntity::class,
        HistoryContentMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class IslamicHistorySearchDatabase : RoomDatabase() {
    abstract fun searchDao(): HistorySearchDao

    companion object {
        @Volatile
        private var instance: IslamicHistorySearchDatabase? = null

        fun get(context: Context): IslamicHistorySearchDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IslamicHistorySearchDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }

        private const val DATABASE_NAME = "islamic_history_search.db"
    }
}

private const val SINGLETON_META_ID = 1
private const val SINGLETON_CONTENT_META_ID = 1
