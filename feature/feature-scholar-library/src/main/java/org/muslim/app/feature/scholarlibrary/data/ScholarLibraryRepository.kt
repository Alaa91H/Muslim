package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.scholarlibrary.domain.Citation
import org.muslim.app.feature.scholarlibrary.domain.FlashcardWithCitation
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarNote
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.SearchHit
import org.muslim.app.feature.scholarlibrary.domain.StudyFlashcard
import org.muslim.app.feature.scholarlibrary.domain.StudyNoteWithCitation

sealed interface ScholarLibraryImportResult {
    data class Success(val importedBooks: Int, val importedPassages: Int) : ScholarLibraryImportResult
    data class Failure(val message: String) : ScholarLibraryImportResult
}

@Serializable
private data class ScholarPack(
    val schemaVersion: Int,
    val packName: String,
    val licenseNotice: String,
    val books: List<ScholarPackBook>,
)

@Serializable
private data class ScholarPackBook(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val authorDeathYearHijri: Int? = null,
    val description: String,
    val sourceName: String,
    val sourceUrl: String? = null,
    val licenseSummary: String,
    val passages: List<ScholarPackPassage>,
)

@Serializable
private data class ScholarPackPassage(
    val id: String,
    val chapter: String,
    val volume: String? = null,
    val page: String? = null,
    val text: String,
)

/**
 * Local, citation-first study library. The shipped catalog is editorial metadata
 * and study guidance only. Full texts are added only through packs that state a
 * source and reuse permission, never by downloading or copying other libraries.
 */
@Singleton
class ScholarLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryDao: ScholarLibraryDao,
    private val ftsDao: ScholarLibraryFtsDao,
    private val json: Json,
) {
    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            if (libraryDao.bookCount() == 0) {
                val bundled = context.assets.open(BUNDLED_CATALOG).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val pack = decodeAndValidate(bundled)
                persistPack(pack, imported = false)
            }
            seeded.set(true)
        }
    }

    fun observeBooks(): Flow<List<ScholarBook>> = libraryDao.observeBooks().map { books ->
        books.map { it.toDomain() }
    }

    fun observeBookPassages(bookId: String): Flow<List<ScholarPassage>> =
        libraryDao.observePassagesForBook(bookId).map { passages -> passages.map { it.toDomain() } }

    fun observeNotes(): Flow<List<StudyNoteWithCitation>> = libraryDao.observeNotes().map { notes ->
        notes.mapNotNull { note ->
            citationForPassage(note.passageId)?.let { citation ->
                StudyNoteWithCitation(note.toDomain(), citation)
            }
        }
    }

    fun observeFlashcards(): Flow<List<FlashcardWithCitation>> = libraryDao.observeFlashcards().map { cards ->
        cards.mapNotNull { card ->
            citationForPassage(card.passageId)?.let { citation ->
                FlashcardWithCitation(card.toDomain(), citation)
            }
        }
    }

    suspend fun book(bookId: String): ScholarBook? = libraryDao.bookById(bookId)?.toDomain()

    suspend fun search(rawQuery: String): List<SearchHit> {
        ensureSeeded()
        val query = ScholarSearchQuery.build(rawQuery)
        if (query.isEmpty()) return emptyList()
        return ftsDao.searchPassageIds(query, SEARCH_LIMIT).distinct().mapNotNull { id ->
            val passage = libraryDao.passageById(id)?.toDomain() ?: return@mapNotNull null
            val citation = citationForPassage(id) ?: return@mapNotNull null
            SearchHit(passage, citation)
        }
    }

    suspend fun addNote(passageId: String, text: String): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null || text.trim().length !in 1..NOTE_MAX_LENGTH) return false
        libraryDao.insertNote(
            ScholarNoteEntity(
                passageId = passageId,
                text = text.trim(),
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
        return true
    }

    suspend fun deleteNote(id: Long) = libraryDao.deleteNote(id)

    suspend fun addFlashcard(passageId: String, front: String, back: String): Boolean {
        ensureSeeded()
        if (libraryDao.passageById(passageId) == null) return false
        if (front.trim().length !in 1..FLASHCARD_SIDE_MAX_LENGTH) return false
        if (back.trim().length !in 1..FLASHCARD_SIDE_MAX_LENGTH) return false
        val now = System.currentTimeMillis()
        libraryDao.insertFlashcard(
            ScholarFlashcardEntity(
                passageId = passageId,
                front = front.trim(),
                back = back.trim(),
                reviewCount = 0,
                dueAtEpochMillis = now,
                createdAtEpochMillis = now,
            ),
        )
        return true
    }

    /** Applies a short local spaced-repetition interval after a review. */
    suspend fun reviewFlashcard(id: Long, remembered: Boolean) {
        val card = libraryDao.observeFlashcards().map { cards -> cards.firstOrNull { it.id == id } }.first() ?: return
        val nextCount = if (remembered) card.reviewCount + 1 else 0
        val interval = if (remembered) REVIEW_INTERVALS_DAYS[minOf(nextCount - 1, REVIEW_INTERVALS_DAYS.lastIndex)] else 0
        val due = Instant.now().plus(Duration.ofDays(interval.toLong())).toEpochMilli()
        libraryDao.updateFlashcardReview(id, nextCount, due)
    }

    suspend fun deleteFlashcard(id: Long) = libraryDao.deleteFlashcard(id)

    /**
     * Imports a user-selected JSON pack. The pack must carry source and licence
     * information for every book; it is intentionally not a scraper or remote
     * downloader for third-party libraries.
     */
    suspend fun importPack(rawText: String): ScholarLibraryImportResult = runCatching {
        ensureSeeded()
        if (rawText.length > PACK_MAX_CHARS) error("حزمة المكتبة كبيرة جداً؛ الحد الأقصى 5 ميغابايت من النص.")
        val pack = decodeAndValidate(rawText)
        persistPack(pack, imported = true)
        ScholarLibraryImportResult.Success(pack.books.size, pack.books.sumOf { it.passages.size })
    }.getOrElse { error ->
        ScholarLibraryImportResult.Failure(error.message ?: "تعذر استيراد حزمة المكتبة.")
    }

    suspend fun citationForPassage(passageId: String): Citation? {
        val passage = libraryDao.passageById(passageId) ?: return null
        val book = libraryDao.bookById(passage.bookId) ?: return null
        return Citation(
            bookTitle = book.title,
            author = book.author,
            chapter = passage.chapter,
            volume = passage.volume,
            page = passage.page,
        )
    }

    private suspend fun persistPack(pack: ScholarPack, imported: Boolean) {
        val books = pack.books.map { item -> item.toEntity(imported) }
        val passages = pack.books.flatMap { book ->
            book.passages.map { passage -> passage.toEntity(book.id) }
        }
        libraryDao.upsertBooks(books)
        libraryDao.upsertPassages(passages)
        // FTS4 tables have no practical unique constraint for contentless rows;
        // rebuilding avoids stale/duplicated results after a pack replaces text.
        ftsDao.clearAll()
        val allRows = buildList {
            // The bundled corpus is seeded once and user packs only add data.
            // Re-indexing the current pack is sufficient because imported ids are
            // unique and bundled rows are inserted before it.
            pack.books.forEach { book ->
                book.passages.forEach { passage ->
                    add(
                        ScholarPassageFtsEntity(
                            normalizedText = ArabicText.normalizeForSearch(passage.text),
                            passageId = passage.id,
                        ),
                    )
                }
            }
        }
        if (allRows.isNotEmpty()) ftsDao.upsertRows(allRows)
        // The first seed completes the index. Later imports retain searchable
        // bundled content by rebuilding it in the caller only when needed.
        if (!imported) return
        rebuildIndex()
    }

    private suspend fun rebuildIndex() {
        ftsDao.clearAll()
        val rows = mutableListOf<ScholarPassageFtsEntity>()
        libraryDao.observeBooks().map { books -> books.map { book -> book.id } }.let { bookIdsFlow ->
            val ids = bookIdsFlow.first()
            ids.forEach { bookId ->
                val passages = libraryDao.observePassagesForBook(bookId).first()
                rows += passages.map { passage ->
                    ScholarPassageFtsEntity(
                        normalizedText = ArabicText.normalizeForSearch(passage.text),
                        passageId = passage.id,
                    )
                }
            }
        }
        if (rows.isNotEmpty()) ftsDao.upsertRows(rows)
    }

    private fun decodeAndValidate(rawText: String): ScholarPack {
        val pack = json.decodeFromString<ScholarPack>(rawText)
        require(pack.schemaVersion == SCHEMA_VERSION) { "إصدار الحزمة غير مدعوم." }
        require(pack.packName.isNotBlank()) { "اسم الحزمة مطلوب." }
        require(pack.licenseNotice.isNotBlank()) { "يجب أن تتضمن الحزمة بيان ترخيص واضحاً." }
        require(pack.books.isNotEmpty() && pack.books.size <= MAX_BOOKS_PER_PACK) { "عدد الكتب في الحزمة غير صالح." }
        require(pack.books.map { it.id }.distinct().size == pack.books.size) { "معرّفات الكتب مكررة." }
        val passageIds = mutableSetOf<String>()
        pack.books.forEach { book ->
            require(ID_REGEX.matches(book.id)) { "معرّف كتاب غير صالح: ${book.id}" }
            require(book.title.isNotBlank() && book.author.isNotBlank()) { "عنوان الكتاب ومؤلفه مطلوبان." }
            require(book.sourceName.isNotBlank() && book.licenseSummary.isNotBlank()) {
                "يجب توضيح مصدر وترخيص كل كتاب."
            }
            require(book.passages.isNotEmpty() && book.passages.size <= MAX_PASSAGES_PER_BOOK) {
                "لا بد من وجود نص واحد على الأقل لكل كتاب ضمن الحدود المسموح بها."
            }
            book.passages.forEach { passage ->
                require(ID_REGEX.matches(passage.id) && passageIds.add(passage.id)) { "معرّف مقطع مكرر أو غير صالح." }
                require(passage.chapter.isNotBlank() && passage.text.trim().length in 1..PASSAGE_MAX_LENGTH) {
                    "نص أو فصل المقطع غير صالح."
                }
            }
        }
        return pack
    }

    private fun ScholarBookEntity.toDomain() = ScholarBook(
        id = id,
        title = title,
        author = author,
        category = ScholarCategory.fromId(category),
        authorDeathYearHijri = authorDeathYearHijri,
        description = description,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        licenseSummary = licenseSummary,
        imported = imported,
    )

    private fun ScholarPassageEntity.toDomain() = ScholarPassage(
        id = id,
        bookId = bookId,
        chapter = chapter,
        volume = volume,
        page = page,
        text = text,
    )

    private fun ScholarNoteEntity.toDomain() = ScholarNote(id, passageId, text, createdAtEpochMillis)

    private fun ScholarFlashcardEntity.toDomain() = StudyFlashcard(
        id = id,
        passageId = passageId,
        front = front,
        back = back,
        reviewCount = reviewCount,
        dueAtEpochMillis = dueAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun ScholarPackBook.toEntity(imported: Boolean) = ScholarBookEntity(
        id = id,
        title = title,
        author = author,
        category = ScholarCategory.fromId(category).name,
        authorDeathYearHijri = authorDeathYearHijri,
        description = description,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        licenseSummary = licenseSummary,
        imported = imported,
    )

    private fun ScholarPackPassage.toEntity(bookId: String) = ScholarPassageEntity(
        id = id,
        bookId = bookId,
        chapter = chapter,
        volume = volume,
        page = page,
        text = text,
    )

    private companion object {
        const val BUNDLED_CATALOG = "scholar_library_catalog.json"
        const val SCHEMA_VERSION = 1
        const val SEARCH_LIMIT = 100
        const val PACK_MAX_CHARS = 5_000_000
        const val MAX_BOOKS_PER_PACK = 1_000
        const val MAX_PASSAGES_PER_BOOK = 20_000
        const val PASSAGE_MAX_LENGTH = 30_000
        const val NOTE_MAX_LENGTH = 4_000
        const val FLASHCARD_SIDE_MAX_LENGTH = 1_000
        val ID_REGEX = Regex("[A-Za-z0-9_-]{3,120}")
        val REVIEW_INTERVALS_DAYS = intArrayOf(1, 3, 7, 14, 30)
    }
}

/** Builds a conservative FTS4 query from Arabic or Latin user input. */
object ScholarSearchQuery {
    private val special = setOf('"', '*', '(', ')', ':', '^', '-', '+')

    fun build(rawQuery: String): String = rawQuery
        .trim()
        .split(Regex("\\s+"))
        .map { ArabicText.normalizeForSearch(it).filterNot { char -> char in special } }
        .filter { it.isNotBlank() }
        .take(8)
        .joinToString(" AND ") { "$it*" }
}
