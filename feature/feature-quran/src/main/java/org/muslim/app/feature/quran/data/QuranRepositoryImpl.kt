package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.core.database.dao.AyahDao
import org.muslim.app.core.database.dao.AyahFtsDao
import org.muslim.app.core.database.dao.BookmarkDao
import org.muslim.app.core.database.dao.SurahDao
import org.muslim.app.core.database.entity.AyahEntity
import org.muslim.app.core.database.entity.AyahFtsEntity
import org.muslim.app.core.database.entity.BookmarkEntity
import org.muslim.app.core.database.entity.SurahEntity
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.Bookmark
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Surah
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
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val ayahFtsDao: AyahFtsDao,
    private val bookmarkDao: BookmarkDao,
    private val prefs: QuranPrefsRepository,
) : QuranRepository {

    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    override suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            val needsImport = surahDao.count() == 0 && ayahDao.count() == 0
            val ftsStale = prefs.ftsIndexStale.first()
            if (needsImport || ftsStale) {
                val surahs = QuranAssetParser.parseSurahs(readAssetText("quran_surahs.json"))
                val ayahs = context.assets.open("quran_ayahs.txt")
                    .bufferedReader(Charsets.UTF_8)
                    .use { QuranAssetParser.parseAyahs(it) }
                if (needsImport) {
                    surahDao.insertAll(surahs)
                    ayahDao.insertAll(ayahs)
                }
                // Rebuild the search index whenever the normalization changed
                // (or on first import) so prefix searches stay accurate.
                if (ayahFtsDao.count() == 0 || ftsStale) {
                    ayahFtsDao.clear()
                    ayahFtsDao.insertAll(
                        ayahs.map {
                            AyahFtsEntity(
                                normalizedText = ArabicText.normalize(it.text),
                                globalNumber = it.globalNumber,
                                surahNumber = it.surahNumber,
                                numberInSurah = it.numberInSurah,
                            )
                        }
                    )
                }
                prefs.markFtsIndexCurrent()
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

    override suspend fun allAyahs(): List<Ayah> {
        ensureSeeded()
        return ayahDao.observeAll().first().map { it.toDomain() }
    }

    override suspend fun ayahByGlobal(globalNumber: Int): Ayah? {
        ensureSeeded()
        return ayahDao.byGlobal(globalNumber)?.toDomain()
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

    override suspend fun allSurahRanges(): Map<Int, List<Int>> =
        ayahDao.allSurahAyahPairs()
            .groupBy({ it.surahNumber }, { it.globalNumber })

    private fun AyahEntity.toDomain() = Ayah(
        globalNumber = globalNumber,
        surahNumber = surahNumber,
        numberInSurah = numberInSurah,
        juz = juz,
        page = page,
        text = text,
    )
}
