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
        ScholarStudyPlanEntity::class,
        ScholarStudySessionEntity::class,
        ScholarReviewEventEntity::class,
        ScholarContentPackEntity::class,
    ],
    version = 7,
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
                    "ALTER TABLE scholar_books ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'Unspecified'",
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

        /**
         * v3 adds optional passage hierarchy metadata plus local study plans.
         * Existing passage rows retain their chapter/volume values and receive
         * safe defaults for the new section/order fields.
         */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scholar_passages ADD COLUMN section TEXT")
                database.execSQL(
                    "ALTER TABLE scholar_passages ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_study_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pathId TEXT NOT NULL,
                        sessionsPerWeek INTEGER NOT NULL,
                        minutesPerSession INTEGER NOT NULL,
                        targetPassagesPerSession INTEGER NOT NULL,
                        active INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_study_plans_pathId ON scholar_study_plans (pathId)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_study_plans_active ON scholar_study_plans (active)",
                )
            }
        }

        /**
         * v4 adds executable study sessions. It is additive and leaves all
         * catalog, annotations, plans and reading progress untouched.
         */
        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_study_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pathId TEXT NOT NULL,
                        planId INTEGER,
                        bookId TEXT NOT NULL,
                        targetPassageIds TEXT NOT NULL,
                        completedPassageIds TEXT NOT NULL,
                        plannedMinutes INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        startedAtEpochMillis INTEGER NOT NULL,
                        completedAtEpochMillis INTEGER
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_study_sessions_pathId " +
                        "ON scholar_study_sessions (pathId)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_study_sessions_status " +
                        "ON scholar_study_sessions (status)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_study_sessions_completedAtEpochMillis " +
                        "ON scholar_study_sessions (completedAtEpochMillis)",
                )
            }
        }

        /**
         * v5 enriches existing flashcards with adaptive spaced-review state.
         * Existing cards remain due exactly when they were before this upgrade.
         */
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE scholar_flashcards ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE scholar_flashcards ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5",
                )
                database.execSQL(
                    "ALTER TABLE scholar_flashcards ADD COLUMN lapseCount INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE scholar_flashcards ADD COLUMN lastReviewedAtEpochMillis INTEGER",
                )
                database.execSQL(
                    "ALTER TABLE scholar_flashcards ADD COLUMN lastRating TEXT",
                )
                database.execSQL(
                    """
                    UPDATE scholar_flashcards
                    SET intervalDays = CASE
                        WHEN reviewCount <= 0 THEN 0
                        WHEN reviewCount = 1 THEN 1
                        WHEN reviewCount = 2 THEN 3
                        WHEN reviewCount = 3 THEN 7
                        WHEN reviewCount = 4 THEN 14
                        ELSE 30
                    END
                    """.trimIndent(),
                )
            }
        }

        /**
         * v6 records immutable local review events for accurate activity
         * summaries and filtered review-center history.
         */
        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_review_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        flashcardId INTEGER NOT NULL,
                        passageId TEXT NOT NULL,
                        bookId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        reviewedAtEpochMillis INTEGER NOT NULL,
                        rating TEXT NOT NULL,
                        scheduledIntervalDays INTEGER NOT NULL,
                        lapseCountAfterReview INTEGER NOT NULL,
                        easeFactorAfterReview REAL NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_review_events_flashcardId " +
                        "ON scholar_review_events (flashcardId)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_review_events_bookId " +
                        "ON scholar_review_events (bookId)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_review_events_category " +
                        "ON scholar_review_events (category)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_review_events_reviewedAtEpochMillis " +
                        "ON scholar_review_events (reviewedAtEpochMillis)",
                )
            }
        }

        /**
         * v7 adds a local registry for bundled/imported content packs. Existing
         * catalog and study data remain untouched.
         */
        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scholar_content_packs (
                        packId TEXT NOT NULL,
                        packName TEXT NOT NULL,
                        packVersion INTEGER NOT NULL,
                        schemaVersion INTEGER NOT NULL,
                        licenseNotice TEXT NOT NULL,
                        sourceName TEXT NOT NULL,
                        sourceUrl TEXT,
                        originName TEXT,
                        bookIds TEXT NOT NULL,
                        imported INTEGER NOT NULL,
                        managed INTEGER NOT NULL,
                        installedAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(packId)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_content_packs_imported " +
                        "ON scholar_content_packs (imported)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_scholar_content_packs_updatedAtEpochMillis " +
                        "ON scholar_content_packs (updatedAtEpochMillis)",
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
