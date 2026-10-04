package org.muslim.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.muslim.app.core.database.dao.AyahDao
import org.muslim.app.core.database.dao.BookmarkDao
import org.muslim.app.core.database.dao.SurahDao
import org.muslim.app.core.database.dao.TafsirDao
import org.muslim.app.core.database.dao.TasbihSessionDao
import org.muslim.app.core.database.dao.TranslationDao
import org.muslim.app.core.database.dao.QuranTextPackDao
import org.muslim.app.core.database.entity.AyahEntity
import org.muslim.app.core.database.entity.BookmarkEntity
import org.muslim.app.core.database.entity.SurahEntity
import org.muslim.app.core.database.entity.TafsirEntity
import org.muslim.app.core.database.entity.TasbihSessionEntity
import org.muslim.app.core.database.entity.TranslationEntity
import org.muslim.app.core.database.entity.QuranTextPackEntity

/**
 * The app's single Room database. Pre-populated content (the Quran here,
 * later adhkar/hadith) is imported from bundled assets on first launch so
 * everything works offline from the very first run (PROJECT_PROMPT.md §3.4).
 */
@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        BookmarkEntity::class,
        TranslationEntity::class,
        TafsirEntity::class,
        QuranTextPackEntity::class,
        TasbihSessionEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun translationDao(): TranslationDao
    abstract fun quranTextPackDao(): QuranTextPackDao
    abstract fun tafsirDao(): TafsirDao
    abstract fun tasbihSessionDao(): TasbihSessionDao

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

        /**
         * v3 → v4: removes the retired Quran full-text-search virtual table.
         * Quran content and user bookmarks remain untouched.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `ayah_fts`")
            }
        }

        /**
         * v4 → v5: durable Tasbih session history.
         *
         * DataStore remains responsible for lightweight current counter and UI
         * preferences; Room stores queryable session history for statistics,
         * recovery and future phone/Wear synchronization.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tasbih_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `phraseId` TEXT NOT NULL,
                        `mode` TEXT NOT NULL,
                        `target` INTEGER NOT NULL,
                        `roundsGoal` INTEGER NOT NULL,
                        `count` INTEGER NOT NULL,
                        `startedAtEpochMillis` INTEGER NOT NULL,
                        `lastUpdatedAtEpochMillis` INTEGER NOT NULL,
                        `endedAtEpochMillis` INTEGER,
                        `endReason` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tasbih_sessions_phraseId` ON `tasbih_sessions` (`phraseId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tasbih_sessions_startedAtEpochMillis` ON `tasbih_sessions` (`startedAtEpochMillis`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tasbih_sessions_endedAtEpochMillis` ON `tasbih_sessions` (`endedAtEpochMillis`)"
                )
            }
        }

        /** v5 → v6: source-aware religious text packs and preserved footnotes. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `quran_text_packs` (
                        `id` TEXT NOT NULL, `kind` TEXT NOT NULL, `languageTag` TEXT NOT NULL,
                        `title` TEXT NOT NULL, `work` TEXT NOT NULL, `translator` TEXT NOT NULL,
                        `publisher` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `license` TEXT NOT NULL,
                        `version` TEXT NOT NULL, `reviewer` TEXT NOT NULL, `reviewReference` TEXT NOT NULL,
                        `entryCount` INTEGER NOT NULL,
                        `footnoteCount` INTEGER NOT NULL, `sha256` TEXT NOT NULL,
                        `verifiedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `translations_v6` (
                        `globalNumber` INTEGER NOT NULL, `packId` TEXT NOT NULL, `language` TEXT NOT NULL,
                        `text` TEXT NOT NULL, `footnotes` TEXT NOT NULL DEFAULT '[]',
                        PRIMARY KEY(`globalNumber`, `packId`)
                    )""".trimIndent()
                )
                db.execSQL(
                    """INSERT INTO `translations_v6` (`globalNumber`, `packId`, `language`, `text`, `footnotes`)
                        SELECT `globalNumber`, 'legacy:' || `language`, `language`, `text`, '[]' FROM `translations`""".trimIndent()
                )
                db.execSQL("DROP TABLE `translations`")
                db.execSQL("ALTER TABLE `translations_v6` RENAME TO `translations`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_translations_language` ON `translations` (`language`)")
                db.execSQL("ALTER TABLE `tafsir` ADD COLUMN `footnotes` TEXT NOT NULL DEFAULT '[]'")
            }
        }

        /** v6 → v7: preserve the source's original contributor/review description separately from translator. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `quran_text_packs` ADD COLUMN `sourceAttribution` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                    )
                    .build()
                    .also { instance = it }
            }
    }
}
