package org.example.islamicapp.feature.hadith.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.islamicapp.core.common.text.ArabicText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.islamicapp.feature.hadith.domain.Hadith
import org.example.islamicapp.feature.hadith.domain.HadithCollection
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class HadithSeedFile(
    val note: String = "",
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

/**
 * Hadith repository (PROJECT_PROMPT.md §6 Phase 3): seeds the sample corpus
 * from bundled assets, exposes collections and full-text search, and picks a
 * deterministic "hadith of the day".
 */
@Singleton
class HadithRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hadithDao: HadithDao,
    private val hadithFtsDao: HadithFtsDao,
    private val json: Json,
) {

    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            if (hadithDao.count() == 0) {
                val text = context.assets.open(SEED_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val file = json.decodeFromString<HadithSeedFile>(text)
                val entities = file.hadiths.mapIndexed { index, item ->
                    org.example.islamicapp.feature.hadith.data.entity.HadithEntity(
                        id = (index + 1).toLong(),
                        collection = item.collection,
                        chapter = item.chapter,
                        numberInBook = item.number,
                        arabicText = item.arabic,
                        translation = item.translation,
                        grade = item.grade,
                        source = item.source,
                    )
                }
                hadithDao.insertAll(entities)
                hadithFtsDao.insertAll(
                    entities.map {
                        org.example.islamicapp.feature.hadith.data.entity.HadithFtsEntity(
                            normalizedText = ArabicText.normalize(it.arabicText),
                            hadithId = it.id,
                        )
                    }
                )
            }
            seeded.set(true)
        }
    }

    fun observeAll(): Flow<List<Hadith>> = hadithDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeCollection(collection: HadithCollection): Flow<List<Hadith>> =
        hadithDao.observeCollection(collection.id).map { list -> list.map { it.toDomain() } }

    /** Full-text search over the normalized Arabic text. */
    suspend fun search(rawQuery: String): List<Hadith> {
        ensureSeeded()
        val match = HadithSearchQuery.build(rawQuery)
        if (match.isEmpty()) return emptyList()
        return hadithFtsDao.searchIds(match).mapNotNull { id -> hadithDao.byId(id)?.toDomain() }
    }

    /** Deterministic daily hadith (rotates through the corpus day by day). */
    suspend fun hadithOfTheDay(): Hadith? {
        ensureSeeded()
        val all = hadithDao.observeAll().first()
        if (all.isEmpty()) return null
        val day = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        return all[(day % all.size).toInt()].toDomain()
    }

    suspend fun byId(id: Long): Hadith? = hadithDao.byId(id)?.toDomain()

    private fun org.example.islamicapp.feature.hadith.data.entity.HadithEntity.toDomain() = Hadith(
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
        const val SEED_ASSET = "hadith_sample.json"
    }
}
