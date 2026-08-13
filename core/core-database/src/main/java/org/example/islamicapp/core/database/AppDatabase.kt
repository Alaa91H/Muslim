package org.example.islamicapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.example.islamicapp.core.database.dao.AyahDao
import org.example.islamicapp.core.database.dao.AyahFtsDao
import org.example.islamicapp.core.database.dao.BookmarkDao
import org.example.islamicapp.core.database.dao.SurahDao
import org.example.islamicapp.core.database.entity.AyahEntity
import org.example.islamicapp.core.database.entity.AyahFtsEntity
import org.example.islamicapp.core.database.entity.BookmarkEntity
import org.example.islamicapp.core.database.entity.SurahEntity

/**
 * The app's single Room database. Pre-populated content (the Quran here,
 * later adhkar/hadith) is imported from bundled assets on first launch so
 * everything works offline from the very first run (PROJECT_PROMPT.md §3.4).
 */
@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        AyahFtsEntity::class,
        BookmarkEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun ayahFtsDao(): AyahFtsDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        private const val DB_NAME = "manara.db"

        /**
         * v1 → v2: FTS4 search index + user bookmarks. DDL mirrors what Room
         * generates for the declared entities (see schemas/2.json).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE VIRTUAL TABLE IF NOT EXISTS `ayah_fts` USING FTS4(
                        `normalized_text` TEXT NOT NULL,
                        `global_number` INTEGER NOT NULL,
                        `surah_number` INTEGER NOT NULL,
                        `number_in_surah` INTEGER NOT NULL,
                        tokenize=unicode61
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bookmarks` (
                        `globalNumber` INTEGER NOT NULL,
                        `surahNumber` INTEGER NOT NULL,
                        `numberInSurah` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`globalNumber`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_bookmarks_surahNumber` ON `bookmarks` (`surahNumber`)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
