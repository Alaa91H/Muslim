package org.example.islamicapp.feature.quran.data

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.islamicapp.core.common.text.ArabicText
import org.example.islamicapp.core.database.AppDatabase
import org.example.islamicapp.core.database.dao.AyahDao
import org.example.islamicapp.core.database.dao.AyahFtsDao
import org.example.islamicapp.core.database.dao.BookmarkDao
import org.example.islamicapp.core.database.dao.SurahDao
import org.example.islamicapp.core.database.entity.AyahEntity
import org.example.islamicapp.core.database.entity.AyahFtsEntity
import org.example.islamicapp.core.database.entity.BookmarkEntity
import org.example.islamicapp.core.database.entity.SurahEntity
import org.example.islamicapp.feature.quran.domain.Ayah
import org.example.islamicapp.feature.quran.domain.Bookmark
import org.example.islamicapp.feature.quran.domain.QuranRepository
import org.example.islamicapp.feature.quran.domain.Surah
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first Quran repository (PROJECT_PROMPT.md §3.4): the full Uthmani
 * text ships as bundled assets and is imported into Room once, on first
 * launch — the app then works fully offline, including full-text search.
 */
@Singleton
class QuranRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val ayahFtsDao: AyahFtsDao,
    private val bookmarkDao: BookmarkDao,
) : QuranRepository {

    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    override suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            database.withTransaction {
                val surahCount = surahDao.count()
                val ayahCount = ayahDao.count()
                val ayahs = when {
                    surahCount == 0 && ayahCount == 0 -> importBundledQuran()
                    surahCount > 0 && ayahCount > 0 -> ayahDao.getAll()
                    else -> {
                        // A prior process stopped during initial import. Reset the
                        // incomplete state inside this transaction and import again.
                        surahDao.clearAll()
                        ayahDao.clearAll()
                        ayahFtsDao.clearAll()
                        importBundledQuran()
                    }
                }

                // Version 2 added FTS. Existing installations already have ayahs,
                // so rebuild the index whenever it is absent or incomplete.
                if (ayahFtsDao.count() != ayahs.size) {
                    ayahFtsDao.clearAll()
                    ayahFtsDao.insertAll(buildAyahFtsRows(ayahs))
                }
            }
            seeded.set(true)
        }
    }

    override fun observeSurahs(): Flow<List<Surah>> = flow {
        ensureSeeded()
        emitAll(surahDao.observeAll().map { list -> list.map { it.toDomain() } })
    }

    override fun observeSurahMetadata(surahNumber: Int): Flow<Surah?> = flow {
        ensureSeeded()
        emitAll(surahDao.observeAll().map { list -> list.firstOrNull { it.number == surahNumber }?.toDomain() })
    }

    override fun observeSurah(surahNumber: Int): Flow<List<Ayah>> = flow {
        ensureSeeded()
        emitAll(ayahDao.observeSurah(surahNumber).map { list -> list.map { it.toDomain() } })
    }

    override suspend fun search(rawQuery: String): List<Ayah> {
        ensureSeeded()
        val match = QuranSearchQuery.build(rawQuery)
        if (match.isEmpty()) return emptyList()
        val hits = ayahFtsDao.search(match)
        return hits.mapNotNull { hit -> ayahDao.byGlobal(hit.globalNumber)?.toDomain() }
    }

    override fun observeBookmarks(): Flow<List<Bookmark>> = flow {
        ensureSeeded()
        emitAll(
            bookmarkDao.observeAll().map { bookmarks ->
                val surahNames = surahDao.observeAll().first().associate { it.number to it.arabicName }
                bookmarks.map { b ->
                    Bookmark(
                        ayah = Ayah(
                            globalNumber = b.globalNumber,
                            surahNumber = b.surahNumber,
                            numberInSurah = b.numberInSurah,
                            juz = 0,
                            page = 0,
                            text = b.text,
                        ),
                        surahName = surahNames[b.surahNumber] ?: "",
                        addedAt = b.addedAt,
                    )
                }
            }
        )
    }

    override suspend fun isBookmarked(globalNumber: Int): Boolean =
        ensureSeeded().let { bookmarkDao.exists(globalNumber) > 0 }

    override suspend fun addBookmark(ayah: Ayah) {
        ensureSeeded()
        bookmarkDao.insert(
            BookmarkEntity(
                globalNumber = ayah.globalNumber,
                surahNumber = ayah.surahNumber,
                numberInSurah = ayah.numberInSurah,
                text = ayah.text,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun removeBookmark(globalNumber: Int) {
        bookmarkDao.delete(
            BookmarkEntity(
                globalNumber = globalNumber,
                surahNumber = 0,
                numberInSurah = 0,
                text = "",
                addedAt = 0,
            )
        )
    }

    private suspend fun importBundledQuran(): List<AyahEntity> {
        val surahs = QuranAssetParser.parseSurahs(readAssetText("quran_surahs.json"))
        val ayahs = context.assets.open("quran_ayahs.txt")
            .bufferedReader(Charsets.UTF_8)
            .use { QuranAssetParser.parseAyahs(it) }
        surahDao.insertAll(surahs)
        ayahDao.insertAll(ayahs)
        return ayahs
    }

    private fun readAssetText(name: String): String =
        context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun SurahEntity.toDomain() = Surah(
        number = number,
        arabicName = arabicName,
        englishName = englishName,
        translation = translation,
        revelationType = revelationType,
        ayahCount = ayahCount,
    )

    private fun AyahEntity.toDomain() = Ayah(
        globalNumber = globalNumber,
        surahNumber = surahNumber,
        numberInSurah = numberInSurah,
        juz = juz,
        page = page,
        text = text,
    )
}

/** Builds the FTS rows from canonical ayah rows for initial seeding and upgrades. */
internal fun buildAyahFtsRows(ayahs: List<AyahEntity>): List<AyahFtsEntity> =
    ayahs.map {
        AyahFtsEntity(
            normalizedText = ArabicText.normalize(it.text),
            globalNumber = it.globalNumber,
            surahNumber = it.surahNumber,
            numberInSurah = it.numberInSurah,
        )
    }
