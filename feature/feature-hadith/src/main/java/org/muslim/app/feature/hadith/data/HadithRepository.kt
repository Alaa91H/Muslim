package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
import org.muslim.app.feature.hadith.domain.HadithCollection

@Serializable
private data class HadithSeedFile(
    val note: String = "",
    val version: Int = 1,
    val hadiths: List<HadithSeedItem>,
)

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

/** Visible preparation state for the bundled corpus; no network is involved. */
sealed interface HadithCorpusState {
    data object NotStarted : HadithCorpusState
    data class Importing(val importedCount: Int) : HadithCorpusState
    data object Ready : HadithCorpusState
    data class Failed(val message: String) : HadithCorpusState
}

/**
 * Offline hadith repository. The large corpus is stored as compressed NDJSON and
 * seeded in bounded batches on Dispatchers.IO; browse and search always return
 * Room-backed pages rather than a complete in-memory list.
 */
@Singleton
class HadithRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hadithDao: HadithDao,
    private val hadithFtsDao: HadithFtsDao,
    private val prefsRepository: HadithPrefsRepository,
    private val json: Json,
) : HadithOfTheDaySource {
    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()
    private val mutableCorpusState = MutableStateFlow<HadithCorpusState>(HadithCorpusState.NotStarted)

    val corpusState: StateFlow<HadithCorpusState> = mutableCorpusState

    suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            try {
                withContext(Dispatchers.IO) {
                    val fullCorpusAvailable = hasAsset(FULL_NDJSON_ASSET)
                    val targetVersion = if (fullCorpusAvailable) FULL_CORPUS_VERSION else sampleCorpusVersion()
                    val seededVersion = prefsRepository.seedVersion.firstOrNull() ?: 0
                    if (hadithDao.count() == 0 || seededVersion != targetVersion) {
                        mutableCorpusState.value = HadithCorpusState.Importing(0)
                        hadithDao.clearAll()
                        hadithFtsDao.clearAll()
                        if (fullCorpusAvailable) {
                            seedCompressedCorpus()
                        } else {
                            seedSampleCorpus()
                        }
                        prefsRepository.setSeedVersion(targetVersion)
                    }
                }
                seeded.set(true)
                mutableCorpusState.value = HadithCorpusState.Ready
            } catch (error: Throwable) {
                mutableCorpusState.value = HadithCorpusState.Failed(
                    error.message ?: "Unable to prepare the offline hadith library.",
                )
                throw error
            }
        }
    }

    /** Room invalidates the source automatically after a corpus replacement. */
    fun pagedHadiths(
        rawQuery: String,
        collection: HadithCollection?,
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
                    match.isNotEmpty() -> hadithFtsDao.pagedSearch(match)
                    collection != null -> hadithDao.pagedCollection(collection.id)
                    else -> hadithDao.pagedAll()
                }
            },
        ).flow.map { page -> page.map { entity -> entity.toDomain() } }
    }

    override suspend fun isDailyNotificationEnabled(): Boolean =
        prefsRepository.dailyNotificationEnabled.first()

    /** Reads one deterministic row, not the entire library, for the daily card. */
    override suspend fun hadithOfTheDay(): Hadith? {
        ensureSeeded()
        val count = hadithDao.count()
        if (count == 0) return null
        val day = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val offset = Math.floorMod(day, count.toLong()).toInt()
        return hadithDao.byOffset(offset)?.toDomain()
    }

    suspend fun count(): Int {
        ensureSeeded()
        return hadithDao.count()
    }

    suspend fun byId(id: Long): Hadith? = hadithDao.byId(id)?.toDomain()

    private suspend fun seedCompressedCorpus() {
        GZIPInputStream(context.assets.open(FULL_NDJSON_ASSET)).bufferedReader(Charsets.UTF_8).use { reader ->
            val batch = ArrayList<HadithSeedItem>(INSERT_BATCH_SIZE)
            var imported = 0
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                batch += json.decodeFromString<HadithSeedItem>(line)
                if (batch.size == INSERT_BATCH_SIZE) {
                    persistBatch(batch, imported)
                    imported += batch.size
                    batch.clear()
                    mutableCorpusState.value = HadithCorpusState.Importing(imported)
                }
            }
            if (batch.isNotEmpty()) {
                persistBatch(batch, imported)
                imported += batch.size
                mutableCorpusState.value = HadithCorpusState.Importing(imported)
            }
        }
    }

    private suspend fun seedSampleCorpus() {
        val seed = context.assets.open(SAMPLE_ASSET).bufferedReader(Charsets.UTF_8).use { reader ->
            json.decodeFromString<HadithSeedFile>(reader.readText())
        }
        seed.hadiths.chunked(INSERT_BATCH_SIZE).forEachIndexed { batchIndex, batch ->
            persistBatch(batch, batchIndex * INSERT_BATCH_SIZE)
            mutableCorpusState.value = HadithCorpusState.Importing((batchIndex + 1) * INSERT_BATCH_SIZE)
        }
    }

    private suspend fun persistBatch(items: List<HadithSeedItem>, offset: Int) {
        val entities = items.mapIndexed { index, item -> item.toEntity((offset + index + 1).toLong()) }
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

    private fun sampleCorpusVersion(): Int = runCatching {
        context.assets.open(SAMPLE_ASSET).bufferedReader(Charsets.UTF_8).use { reader ->
            json.decodeFromString<HadithSeedFile>(reader.readText()).version
        }
    }.getOrDefault(1)

    private fun hasAsset(name: String): Boolean = runCatching {
        context.assets.open(name).close()
        true
    }.getOrDefault(false)

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
        const val SAMPLE_ASSET = "hadith_sample.json"
        const val FULL_NDJSON_ASSET = "hadith_full.ndjson.gz"
        const val FULL_CORPUS_VERSION = 2
        const val INSERT_BATCH_SIZE = 150
        const val PAGE_SIZE = 24
        const val PREFETCH_DISTANCE = 8
    }
}
