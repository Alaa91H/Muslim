package org.muslim.app.feature.hadith.data

import android.content.Context
import java.io.InputStream
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.hadith.data.entity.HadithEntity
import org.muslim.app.feature.hadith.data.entity.HadithFtsEntity
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithChapter
import org.muslim.app.feature.hadith.domain.HadithCollection

@Serializable
private data class HadithSeedItem(
    val collection: String,
    val chapter: String? = null,
    val number: Int? = null,
    val arabic: String,
    val translation: String,
    val grade: String,
    val source: String,
)

/** Visible, collection-specific preparation state. No runtime network is involved. */
sealed interface HadithCorpusState {
    data object Catalogue : HadithCorpusState
    data class Importing(val collection: HadithCollection, val importedCount: Int) : HadithCorpusState
    data class Ready(val collection: HadithCollection) : HadithCorpusState
    data class Failed(val collection: HadithCollection, val message: String) : HadithCorpusState
}

/**
 * Offline, collection-on-demand Hadith repository.
 *
 * The catalogue is tiny metadata. Entering a book streams that book's dedicated
 * gzip asset to Room in bounded batches; entering another book discards the
 * previous book first. Browse/search data is Room-backed Paging, so neither the
 * complete library nor a complete book is materialized in the app heap.
 */
@Singleton
class HadithRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hadithDao: HadithDao,
    private val hadithFtsDao: HadithFtsDao,
    private val prefsRepository: HadithPrefsRepository,
    private val json: Json,
) : HadithOfTheDaySource {
    private val seedMutex = Mutex()
    private val mutableCorpusState = MutableStateFlow<HadithCorpusState>(HadithCorpusState.Catalogue)
    private val mutableActiveCollection = MutableStateFlow<HadithCollection?>(null)

    val corpusState: StateFlow<HadithCorpusState> = mutableCorpusState
    val activeCollection: StateFlow<HadithCollection?> = mutableActiveCollection

    /**
     * Streams and persists exactly one selected collection. A Room table is
     * intentionally a one-book cache: this bounds persistent data and makes
     * load-on-entry verifiable even on devices with constrained storage.
     */
    suspend fun ensureCollectionLoaded(collection: HadithCollection) {
        require(collection.isBundled) { "Only bundled Hadith collections can be opened." }
        if (mutableActiveCollection.value == collection && mutableCorpusState.value is HadithCorpusState.Ready) return
        seedMutex.withLock {
            if (mutableActiveCollection.value == collection && mutableCorpusState.value is HadithCorpusState.Ready) return
            try {
                mutableCorpusState.value = HadithCorpusState.Importing(collection, 0)
                withContext(Dispatchers.IO) {
                    // Drop a legacy all-books cache or a previous one-book cache before
                    // opening the source asset. This happens before reading the requested
                    // gzip stream and never creates an all-library in-memory collection.
                    hadithFtsDao.clearAll()
                    hadithDao.clearAll()
                    seedCompressedCollection(collection)
                }
                mutableActiveCollection.value = collection
                mutableCorpusState.value = HadithCorpusState.Ready(collection)
            } catch (error: Throwable) {
                mutableActiveCollection.value = null
                mutableCorpusState.value = HadithCorpusState.Failed(
                    collection,
                    error.message ?: "Unable to prepare this offline hadith book.",
                )
                throw error
            }
        }
    }

    /** Room invalidates the source automatically when another book is opened. */
    fun pagedHadiths(
        rawQuery: String,
        collection: HadithCollection,
        chapter: String?,
    ): Flow<PagingData<Hadith>> {
        val match = HadithSearchQuery.build(rawQuery)
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                when {
                    match.isNotEmpty() -> hadithFtsDao.pagedSearch(collection.id, match)
                    chapter != null -> hadithDao.pagedChapter(collection.id, chapter)
                    else -> hadithDao.pagedCollection(collection.id)
                }
            },
        ).flow.map { page -> page.map { entity -> entity.toDomain() } }
    }

    /** Emits only compact chapter metadata for the currently loaded collection. */
    fun chapters(collection: HadithCollection): Flow<List<HadithChapter>> =
        hadithDao.observeChapters(collection.id).map { rows ->
            rows.map { row ->
                HadithChapter(
                    title = row.title,
                    firstHadithNumber = row.firstHadithNumber,
                    lastHadithNumber = row.lastHadithNumber,
                    hadithCount = row.hadithCount,
                )
            }
        }

    override suspend fun isDailyNotificationEnabled(): Boolean =
        prefsRepository.dailyNotificationEnabled.first()

    /** Reads one deterministic row from the user-opened collection only. */
    override suspend fun hadithOfTheDay(): Hadith? {
        val collection = mutableActiveCollection.value ?: return null
        val count = hadithDao.countCollection(collection.id)
        if (count == 0) return null
        val day = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val offset = Math.floorMod(day, count.toLong()).toInt()
        return hadithDao.byCollectionOffset(collection.id, offset)?.toDomain()
    }

    suspend fun byId(id: Long): Hadith? = hadithDao.byId(id)?.toDomain()

    private suspend fun seedCompressedCollection(collection: HadithCollection) {
        openCollectionAsset(collection).bufferedReader(Charsets.UTF_8).use { reader ->
            val batch = ArrayList<HadithSeedItem>(INSERT_BATCH_SIZE)
            var imported = 0
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                batch += json.decodeFromString<HadithSeedItem>(line)
                if (batch.size == INSERT_BATCH_SIZE) {
                    persistBatch(collection, batch, imported)
                    imported += batch.size
                    batch.clear()
                    mutableCorpusState.value = HadithCorpusState.Importing(collection, imported)
                }
            }
            if (batch.isNotEmpty()) {
                persistBatch(collection, batch, imported)
                imported += batch.size
                mutableCorpusState.value = HadithCorpusState.Importing(collection, imported)
            }
        }
    }

    /**
     * Android's asset packager can transparently uncompress an asset ending in
     * `.gz` and expose it without that suffix. Support both representations so
     * the repository remains stream-only on every generated APK variant.
     */
    private fun openCollectionAsset(collection: HadithCollection): InputStream {
        val asset = "$BOOK_ASSET_DIRECTORY/${collection.id}.ndjson.gz"
        val unpackedAsset = "$BOOK_ASSET_DIRECTORY/${collection.id}.ndjson"
        return runCatching { GZIPInputStream(context.assets.open(asset)) }
            .recoverCatching { context.assets.open(unpackedAsset) }
            .getOrElse { error ->
                throw IllegalStateException("The selected offline Hadith book is unavailable.", error)
            }
    }

    private suspend fun persistBatch(
        collection: HadithCollection,
        items: List<HadithSeedItem>,
        offset: Int,
    ) {
        val entities = items.mapIndexed { index, item ->
            // IDs are stable per collection, so bookmarks cannot collide when the
            // one-book cache later moves to a different book.
            item.toEntity((collection.ordinal + 1) * BOOK_ID_STRIDE + offset + index + 1L)
        }
        hadithDao.insertAll(entities)
        hadithFtsDao.insertAll(
            entities.map { entity ->
                HadithFtsEntity(
                    normalizedText = ArabicText.normalizeForSearch(entity.arabicText),
                    hadithId = entity.id,
                )
            },
        )
    }

    private fun HadithSeedItem.toEntity(id: Long) = HadithEntity(
        id = id,
        collection = collection,
        chapter = chapter,
        numberInBook = number,
        arabicText = arabic,
        translation = translation,
        grade = grade,
        source = source,
    )

    private fun HadithEntity.toDomain() = Hadith(
        id = id,
        collection = HadithCollection.fromId(collection),
        chapter = chapter,
        numberInBook = numberInBook,
        arabicText = arabicText,
        translation = translation,
        grade = grade,
        source = source,
    )

    private companion object {
        const val BOOK_ASSET_DIRECTORY = "hadith_books"
        const val INSERT_BATCH_SIZE = 150
        const val PAGE_SIZE = 24
        const val PREFETCH_DISTANCE = 8
        const val BOOK_ID_STRIDE = 1_000_000L
    }
}
