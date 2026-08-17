package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muslim.app.feature.hadith.data.entity.HadithEntity
import org.muslim.app.feature.hadith.data.entity.HadithFtsEntity

/**
 * Self-contained hadith database (PROJECT_PROMPT.md §6 Phase 3). Kept in its
 * own `.db` file because hadith content is a large, independently-reviewed
 * corpus — the shared core-database stays focused on the Quran.
 */
@Database(
    entities = [HadithEntity::class, HadithFtsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HadithDatabase : RoomDatabase() {

    abstract fun hadithDao(): HadithDao
    abstract fun hadithFtsDao(): HadithFtsDao

    companion object {
        private const val DB_NAME = "hadith.db"

        @Volatile
        private var instance: HadithDatabase? = null

        fun getInstance(context: Context): HadithDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, HadithDatabase::class.java, DB_NAME)
                    .build()
                    .also { instance = it }
            }
    }
}
