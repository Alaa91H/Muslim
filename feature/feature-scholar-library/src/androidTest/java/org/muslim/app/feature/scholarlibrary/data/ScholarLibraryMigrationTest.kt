package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScholarLibraryMigrationTest {
    private lateinit var context: Context
    private val databaseName = "scholar-library-migration-test.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration2To3PreservesPassagesAndAddsHierarchyAndPlans() {
        createVersion2Database()

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(3) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            assertThat(oldVersion).isEqualTo(2)
                            assertThat(newVersion).isEqualTo(3)
                            ScholarLibraryDatabase.MIGRATION_2_3.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.query(
            "SELECT id, bookId, chapter, volume, page, text, section, orderIndex " +
                "FROM scholar_passages WHERE id = 'passage-one'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("passage-one")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("text"))).isEqualTo("legacy text")
            assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("section"))).isTrue()
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("orderIndex"))).isEqualTo(0)
        }

        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'scholar_study_plans'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("scholar_study_plans")
        }

        db.query("PRAGMA table_info(scholar_passages)").use { cursor ->
            val columns = buildSet {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
            assertThat(columns).containsAtLeast("section", "orderIndex")
        }

        helper.close()
    }

    @Test
    fun migration3To4PreservesPlansAndAddsStudySessions() {
        createVersion3Database()

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(4) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            assertThat(oldVersion).isEqualTo(3)
                            assertThat(newVersion).isEqualTo(4)
                            ScholarLibraryDatabase.MIGRATION_3_4.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.query("SELECT pathId, sessionsPerWeek FROM scholar_study_plans WHERE id = 1").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("hadith-reading-path")
            assertThat(cursor.getInt(1)).isEqualTo(3)
        }
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'scholar_study_sessions'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("scholar_study_sessions")
        }
        db.query("PRAGMA table_info(scholar_study_sessions)").use { cursor ->
            val columns = buildSet {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
            assertThat(columns).containsAtLeast(
                "pathId",
                "planId",
                "bookId",
                "targetPassageIds",
                "completedPassageIds",
                "plannedMinutes",
                "status",
                "startedAtEpochMillis",
                "completedAtEpochMillis",
            )
        }

        helper.close()
    }

    @Test
    fun migration4To5PreservesFlashcardsAndAddsAdaptiveReviewState() {
        createVersion4Database()

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(5) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            assertThat(oldVersion).isEqualTo(4)
                            assertThat(newVersion).isEqualTo(5)
                            ScholarLibraryDatabase.MIGRATION_4_5.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.query(
            """
            SELECT id, front, reviewCount, dueAtEpochMillis, intervalDays, easeFactor,
                   lapseCount, lastReviewedAtEpochMillis, lastRating
            FROM scholar_flashcards WHERE id = 1
            """.trimIndent(),
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("front"))).isEqualTo("question")
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("reviewCount"))).isEqualTo(3)
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("dueAtEpochMillis"))).isEqualTo(9000L)
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("intervalDays"))).isEqualTo(7)
            assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("easeFactor"))).isEqualTo(2.5)
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("lapseCount"))).isEqualTo(0)
            assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("lastReviewedAtEpochMillis"))).isTrue()
            assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("lastRating"))).isTrue()
        }

        helper.close()
    }

    @Test
    fun migration5To6PreservesFlashcardsAndAddsReviewHistory() {
        createVersion5Database()

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(6) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            assertThat(oldVersion).isEqualTo(5)
                            assertThat(newVersion).isEqualTo(6)
                            ScholarLibraryDatabase.MIGRATION_5_6.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.query(
            "SELECT id, front, reviewCount, intervalDays FROM scholar_flashcards WHERE id = 1",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("front"))).isEqualTo("question")
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("reviewCount"))).isEqualTo(3)
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("intervalDays"))).isEqualTo(7)
        }
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'scholar_review_events'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
        }
        db.query("PRAGMA table_info(scholar_review_events)").use { cursor ->
            val columns = buildSet {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
            assertThat(columns).containsAtLeast(
                "flashcardId",
                "passageId",
                "bookId",
                "category",
                "reviewedAtEpochMillis",
                "rating",
                "scheduledIntervalDays",
                "lapseCountAfterReview",
                "easeFactorAfterReview",
            )
        }

        helper.close()
    }

    @Test
    fun migration6To7PreservesReviewHistoryAndAddsPackRegistry() {
        createVersion6Database()

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(7) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            assertThat(oldVersion).isEqualTo(6)
                            assertThat(newVersion).isEqualTo(7)
                            ScholarLibraryDatabase.MIGRATION_6_7.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.query(
            "SELECT id, bookId, category, rating FROM scholar_review_events WHERE id = 1",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("bookId"))).isEqualTo("book-one")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("category"))).isEqualTo("Hadith")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("rating"))).isEqualTo("Good")
        }
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'scholar_content_packs'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
        }
        db.query("PRAGMA table_info(scholar_content_packs)").use { cursor ->
            val columns = buildSet {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
            assertThat(columns).containsAtLeast(
                "packId",
                "packName",
                "packVersion",
                "schemaVersion",
                "licenseNotice",
                "sourceName",
                "bookIds",
                "imported",
                "managed",
                "installedAtEpochMillis",
                "updatedAtEpochMillis",
            )
        }

        helper.close()
    }

    private fun createVersion6Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(6) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE scholar_review_events (
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
                            db.execSQL(
                                """
                                INSERT INTO scholar_review_events(
                                    id, flashcardId, passageId, bookId, category,
                                    reviewedAtEpochMillis, rating, scheduledIntervalDays,
                                    lapseCountAfterReview, easeFactorAfterReview
                                ) VALUES(
                                    1, 7, 'passage-one', 'book-one', 'Hadith',
                                    1000, 'Good', 7, 0, 2.5
                                )
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
        helper.writableDatabase
        helper.close()
    }

    private fun createVersion5Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(5) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE scholar_flashcards (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    passageId TEXT NOT NULL,
                                    front TEXT NOT NULL,
                                    back TEXT NOT NULL,
                                    createdAtEpochMillis INTEGER NOT NULL,
                                    reviewCount INTEGER NOT NULL,
                                    dueAtEpochMillis INTEGER NOT NULL,
                                    intervalDays INTEGER NOT NULL,
                                    easeFactor REAL NOT NULL,
                                    lapseCount INTEGER NOT NULL,
                                    lastReviewedAtEpochMillis INTEGER,
                                    lastRating TEXT
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO scholar_flashcards(
                                    id, passageId, front, back, createdAtEpochMillis,
                                    reviewCount, dueAtEpochMillis, intervalDays, easeFactor,
                                    lapseCount, lastReviewedAtEpochMillis, lastRating
                                ) VALUES(
                                    1, 'passage-one', 'question', 'answer', 1000,
                                    3, 9000, 7, 2.5, 0, 8000, 'Good'
                                )
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
        helper.writableDatabase
        helper.close()
    }

    private fun createVersion4Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(4) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE scholar_flashcards (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    passageId TEXT NOT NULL,
                                    front TEXT NOT NULL,
                                    back TEXT NOT NULL,
                                    reviewCount INTEGER NOT NULL,
                                    dueAtEpochMillis INTEGER NOT NULL,
                                    createdAtEpochMillis INTEGER NOT NULL
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO scholar_flashcards(
                                    id, passageId, front, back, reviewCount, dueAtEpochMillis, createdAtEpochMillis
                                ) VALUES(1, 'passage-one', 'question', 'answer', 3, 9000, 1000)
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
        helper.writableDatabase
        helper.close()
    }

    private fun createVersion3Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(3) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE scholar_study_plans (
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
                            db.execSQL(
                                """
                                INSERT INTO scholar_study_plans(
                                    id, pathId, sessionsPerWeek, minutesPerSession,
                                    targetPassagesPerSession, active, createdAtEpochMillis, updatedAtEpochMillis
                                ) VALUES(1, 'hadith-reading-path', 3, 45, 2, 1, 1000, 1000)
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
        helper.writableDatabase
        helper.close()
    }

    private fun createVersion2Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(2) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE scholar_passages (
                                    id TEXT NOT NULL,
                                    bookId TEXT NOT NULL,
                                    chapter TEXT NOT NULL,
                                    volume TEXT,
                                    page TEXT,
                                    text TEXT NOT NULL,
                                    PRIMARY KEY(id)
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO scholar_passages(id, bookId, chapter, volume, page, text)
                                VALUES('passage-one', 'book-one', 'chapter', '1', '10', 'legacy text')
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )
        helper.writableDatabase
        helper.close()
    }
}
