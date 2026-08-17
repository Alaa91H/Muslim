package org.muslim.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.muslim.app.core.database.dao.AyahDao
import org.muslim.app.core.database.dao.AyahFtsDao
import org.muslim.app.core.database.dao.BookmarkDao
import org.muslim.app.core.database.dao.SurahDao
import org.muslim.app.core.database.dao.TafsirDao
import org.muslim.app.core.database.dao.TranslationDao
import org.muslim.app.core.database.entity.AyahEntity
import org.muslim.app.core.database.entity.AyahFtsEntity
import org.muslim.app.core.database.entity.BookmarkEntity
import org.muslim.app.core.database.entity.SurahEntity
import org.muslim.app.core.database.entity.TafsirEntity
import org.muslim.app.core.database.entity.TranslationEntity

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
        TranslationEntity::class,
        TafsirEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun ayahFtsDao(): AyahFtsDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun translationDao(): TranslationDao
    abstract fun tafsirDao(): TafsirDao

    companion object {
        private const val DB_NAME = "muslim.db"

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

        /**
         * v2 → v3: meaning translations + tafsir entries (imported packs).
         * DDL mirrors Room's generated schema for the new entities.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `translations` (
                        `globalNumber` INTEGER NOT NULL,
                        `language` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        PRIMARY KEY(`globalNumber`, `language`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_translations_language` ON `translations` (`language`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tafsir` (
                        `globalNumber` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        PRIMARY KEY(`globalNumber`, `source`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tafsir_source` ON `tafsir` (`source`)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
