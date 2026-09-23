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
