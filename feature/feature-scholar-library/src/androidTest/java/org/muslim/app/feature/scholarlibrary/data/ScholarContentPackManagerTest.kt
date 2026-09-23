package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScholarContentPackManagerTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun managedPackUpdatePreservesIdentityAndRejectsUnsafeChanges() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, ScholarLibraryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val manager = ScholarContentPackManager(
                context = context,
                libraryDao = database.libraryDao(),
                ftsDao = database.ftsDao(),
                json = json,
            )

            val installed = manager.importPack(PACK_VERSION_ONE, "pack-v1.json")
            assertThat(installed).isInstanceOf(ScholarLibraryImportResult.Success::class.java)
            val first = installed as ScholarLibraryImportResult.Success
            assertThat(first.replacedExisting).isFalse()
            assertThat(first.packVersion).isEqualTo(1)

            val updated = manager.importPack(PACK_VERSION_TWO, "pack-v2.json")
            assertThat(updated).isInstanceOf(ScholarLibraryImportResult.Success::class.java)
            val second = updated as ScholarLibraryImportResult.Success
            assertThat(second.replacedExisting).isTrue()
            assertThat(second.packVersion).isEqualTo(2)

            val registered = manager.contentPacks.first().first { it.id == PACK_ID }
            assertThat(registered.version).isEqualTo(2)
            assertThat(registered.installation.managed).isTrue()
            assertThat(registered.installation.bookIds).containsExactly(BOOK_ID)
            assertThat(database.libraryDao().observePassagesForBook(BOOK_ID).first().map { it.id })
                .containsExactly(PASSAGE_ONE, PASSAGE_TWO)

            val downgrade = manager.importPack(PACK_VERSION_ONE, "pack-v1.json")
            assertThat(downgrade).isInstanceOf(ScholarLibraryImportResult.Failure::class.java)

            val destructive = manager.importPack(PACK_VERSION_THREE_WITH_REMOVAL, "pack-v3.json")
            assertThat(destructive).isInstanceOf(ScholarLibraryImportResult.Failure::class.java)

            val passagesAfterRejectedUpdate =
                database.libraryDao().observePassagesForBook(BOOK_ID).first().map { it.id }
            assertThat(passagesAfterRejectedUpdate).containsExactly(PASSAGE_ONE, PASSAGE_TWO)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val PACK_ID = "test-managed-pack"
        const val BOOK_ID = "test-managed-book"
        const val PASSAGE_ONE = "test-managed-passage-one"
        const val PASSAGE_TWO = "test-managed-passage-two"

        val PACK_VERSION_ONE = """
            {
              "schemaVersion": 4,
              "packId": "test-managed-pack",
              "packVersion": 1,
              "packName": "حزمة اختبار",
              "sourceName": "مصدر اختبار",
              "licenseNotice": "إذن اختبار",
              "books": [
                {
                  "id": "test-managed-book",
                  "title": "كتاب اختبار",
                  "author": "مؤلف",
                  "category": "Hadith",
                  "description": "وصف",
                  "sourceName": "مصدر مرخّص",
                  "licenseSummary": "إذن اختبار",
                  "passages": [
                    {
                      "id": "test-managed-passage-one",
                      "chapter": "باب",
                      "text": "المقطع الأول",
                      "orderIndex": 0
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val PACK_VERSION_TWO = """
            {
              "schemaVersion": 4,
              "packId": "test-managed-pack",
              "packVersion": 2,
              "packName": "حزمة اختبار",
              "sourceName": "مصدر اختبار",
              "licenseNotice": "إذن اختبار",
              "books": [
                {
                  "id": "test-managed-book",
                  "title": "كتاب اختبار محدث",
                  "author": "مؤلف",
                  "category": "Hadith",
                  "description": "وصف",
                  "sourceName": "مصدر مرخّص",
                  "licenseSummary": "إذن اختبار",
                  "passages": [
                    {
                      "id": "test-managed-passage-one",
                      "chapter": "باب",
                      "text": "المقطع الأول بعد التحديث",
                      "orderIndex": 0
                    },
                    {
                      "id": "test-managed-passage-two",
                      "chapter": "باب ثان",
                      "text": "المقطع الثاني",
                      "orderIndex": 1
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val PACK_VERSION_THREE_WITH_REMOVAL = """
            {
              "schemaVersion": 4,
              "packId": "test-managed-pack",
              "packVersion": 3,
              "packName": "حزمة اختبار",
              "sourceName": "مصدر اختبار",
              "licenseNotice": "إذن اختبار",
              "books": [
                {
                  "id": "test-managed-book",
                  "title": "كتاب اختبار",
                  "author": "مؤلف",
                  "category": "Hadith",
                  "description": "وصف",
                  "sourceName": "مصدر مرخّص",
                  "licenseSummary": "إذن اختبار",
                  "passages": [
                    {
                      "id": "test-managed-passage-two",
                      "chapter": "باب ثان",
                      "text": "المقطع الثاني",
                      "orderIndex": 0
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    }
}
