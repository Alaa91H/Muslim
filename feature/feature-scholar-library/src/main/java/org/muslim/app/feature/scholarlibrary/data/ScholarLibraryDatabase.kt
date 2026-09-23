package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Private on-device database for the study library. It deliberately contains
 * only content bundled with clear metadata or material the user imports.
 */
@Database(
    entities = [
        ScholarBookEntity::class,
        ScholarPassageEntity::class,
        ScholarPassageFtsEntity::class,
        ScholarNoteEntity::class,
        ScholarFlashcardEntity::class,
        ScholarBookmarkEntity::class,
        ScholarHighlightEntity::class,
        ScholarReadingProgressEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class ScholarLibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): ScholarLibraryDao
    abstract fun ftsDao(): ScholarLibraryFtsDao

    companion object {
        private const val DB_NAME = "scholar_library.db"

        /**
         * v2 is additive. Existing books, imported packs, notes, flashcards and
         * the FTS index stay intact while richer metadata and study-state tables
         * are introduced.
         */
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN subtitle TEXT")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN language TEXT NOT NULL DEFAULT 'ar'")
                database.execSQL(
                    "ALTER TABLE scholar_books ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'Foundation'",
                )
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN publisher TEXT")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN edition TEXT")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN editor TEXT")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN publicationYear TEXT")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN volumeCount INTEGER")
                database.execSQL("ALTER TABLE scholar_books ADD COLUMN keywords TEXT NOT NULL DEFAULT ''")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_bookmarks (
                        passageId TEXT NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(passageId)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_bookmarks_createdAtEpochMillis " +
                        "ON scholar_bookmarks (createdAtEpochMillis)",
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_highlights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        passageId TEXT NOT NULL,
                        quote TEXT NOT NULL,
                        note TEXT,
                        style TEXT NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_highlights_passageId ON scholar_highlights (passageId)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_highlights_createdAtEpochMillis " +
                        "ON scholar_highlights (createdAtEpochMillis)",
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_reading_progress (
                        bookId TEXT NOT NULL,
                        lastPassageId TEXT,
                        status TEXT NOT NULL,
                        progressPercent INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(bookId)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_reading_progress_status " +
                        "ON scholar_reading_progress (status)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_reading_progress_updatedAtEpochMillis " +
                        "ON scholar_reading_progress (updatedAtEpochMillis)",
                )
            }
        }

        @Volatile
        private var instance: ScholarLibraryDatabase? = null

        fun getInstance(context: Context): ScholarLibraryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScholarLibraryDatabase::class.java,
                    DB_NAME,
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
