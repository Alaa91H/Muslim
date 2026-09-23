package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
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
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarContentPack
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty

sealed interface ScholarLibraryImportResult {
    data class Success(
        val importedBooks: Int,
        val importedPassages: Int,
        val packName: String,
        val packVersion: Int,
        val replacedExisting: Boolean,
    ) : ScholarLibraryImportResult

    data class Failure(val message: String) : ScholarLibraryImportResult
}

@Serializable
private data class ScholarPack(
    val schemaVersion: Int,
    val packName: String,
    val licenseNotice: String,
    val books: List<ScholarPackBook>,
    val packId: String? = null,
    val packVersion: Int = 1,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
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
    val subtitle: String? = null,
    val language: String = "ar",
    val difficulty: String = ScholarDifficulty.Unspecified.name,
    val publisher: String? = null,
    val edition: String? = null,
    val editor: String? = null,
    val publicationYear: String? = null,
    val volumeCount: Int? = null,
    val keywords: List<String> = emptyList(),
)

@Serializable
private data class ScholarPackPassage(
    val id: String,
    val chapter: String,
    val volume: String? = null,
    val page: String? = null,
    val text: String,
    val section: String? = null,
    val orderIndex: Int = 0,
)

private fun ScholarPack.effectivePackId(): String =
    packId?.takeIf { it.isNotBlank() } ?: run {
        val material = buildString {
            append(packName.trim())
            append('|')
            append(books.map { it.id }.sorted().joinToString(","))
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(material.toByteArray(Charsets.UTF_8))
        "legacy-" + digest.take(12).joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

private fun ScholarPack.effectiveSourceName(originName: String?): String =
    sourceName?.takeIf { it.isNotBlank() }
        ?: books.map { it.sourceName }.distinct().take(3).joinToString("، ").takeIf { it.isNotBlank() }
        ?: originName?.takeIf { it.isNotBlank() }
        ?: "مصادر الكتب داخل الحزمة"

private fun ScholarPack.toRegistryEntity(
    imported: Boolean,
    originName: String?,
    existing: ScholarContentPackEntity?,
): ScholarContentPackEntity {
    val now = System.currentTimeMillis()
    return ScholarContentPackEntity(
        packId = effectivePackId(),
        identity = ScholarContentPackIdentityEntity(
            packName = packName.trim(),
            packVersion = packVersion,
            schemaVersion = schemaVersion,
        ),
        source = ScholarContentPackSourceEntity(
            licenseNotice = licenseNotice.trim(),
            sourceName = effectiveSourceName(originName),
            sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotEmpty() },
            originName = originName?.trim()?.takeIf { it.isNotEmpty() },
        ),
        installation = ScholarContentPackInstallationEntity(
            bookIds = books.map { it.id }.toStoredIds(),
            imported = imported,
            managed = schemaVersion >= 4 && !packId.isNullOrBlank(),
            installedAtEpochMillis = existing?.installation?.installedAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
        ),
    )
}

@Singleton
class ScholarContentPackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryDao: ScholarLibraryDao,
    private val ftsDao: ScholarLibraryFtsDao,
    private val json: Json,
) {
    private val seeded = AtomicBoolean(false)
    private val seedMutex = Mutex()

    val contentPacks: Flow<List<ScholarContentPack>>
        get() = libraryDao.observeContentPacks().map { rows -> rows.map { it.toDomain() } }

    suspend fun ensureSeeded() {
        if (seeded.get()) return
        seedMutex.withLock {
            if (seeded.get()) return
            val bundledRaw = context.assets.open(BUNDLED_CATALOG)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val bundledPack = decodeAndValidate(bundledRaw)
            val bundledId = bundledPack.effectivePackId()
            if (libraryDao.bookCount() == 0) {
                persistPack(
                    pack = bundledPack,
                    imported = false,
                    originName = BUNDLED_PACK_ORIGIN,
                    existing = null,
                )
            } else if (libraryDao.contentPackById(bundledId) == null) {
                libraryDao.upsertContentPack(
                    bundledPack.toRegistryEntity(
                        imported = false,
                        originName = BUNDLED_PACK_ORIGIN,
                        existing = null,
                    ),
                )
            }
            registerLegacyImports()
            seeded.set(true)
        }
    }

    suspend fun importPack(
        rawText: String,
        originName: String? = null,
    ): ScholarLibraryImportResult = runCatching {
        ensureSeeded()
        require(rawText.length <= PACK_MAX_CHARS) {
            "حزمة المكتبة كبيرة جداً؛ الحد الأقصى 5 ميغابايت من النص."
        }
        val pack = decodeAndValidate(rawText)
        val packId = pack.effectivePackId()
        val existing = libraryDao.contentPackById(packId)
        require(existing == null || pack.packVersion >= existing.identity.packVersion) {
            "لا يمكن تثبيت إصدار أقدم من الحزمة المثبتة."
        }

        val newBookIds = pack.books.map { it.id }.toSet()
        val conflictingOwner = libraryDao.observeContentPacks().first().firstOrNull { installed ->
            installed.packId != packId &&
                installed.packId != LEGACY_IMPORTS_PACK_ID &&
                installed.installation.bookIds.toStoredIdList().any { it in newBookIds }
        }
        require(conflictingOwner == null) {
            "تتعارض الحزمة مع كتب مملوكة لحزمة أخرى: ${conflictingOwner?.identity?.packName}."
        }

        validateNonDestructiveUpdate(pack, existing, newBookIds)
        persistPack(pack, imported = true, originName = originName, existing = existing)
        transferLegacyOwnership(newBookIds)

        ScholarLibraryImportResult.Success(
            importedBooks = pack.books.size,
            importedPassages = pack.books.sumOf { it.passages.size },
            packName = pack.packName,
            packVersion = pack.packVersion,
            replacedExisting = existing != null,
        )
    }.getOrElse { error ->
        ScholarLibraryImportResult.Failure(error.message ?: "تعذر استيراد حزمة المكتبة.")
    }

    private suspend fun registerLegacyImports() {
        val registeredBookIds = libraryDao.observeContentPacks().first()
            .flatMap { it.installation.bookIds.toStoredIdList() }
            .toSet()
        val legacyImportedBooks = libraryDao.observeBooks().first()
            .filter { it.imported && it.id !in registeredBookIds }
        if (legacyImportedBooks.isEmpty()) return

        val now = System.currentTimeMillis()
        libraryDao.upsertContentPack(
            ScholarContentPackEntity(
                packId = LEGACY_IMPORTS_PACK_ID,
                identity = ScholarContentPackIdentityEntity(
                    packName = "استيرادات سابقة",
                    packVersion = 1,
                    schemaVersion = 3,
                ),
                source = ScholarContentPackSourceEntity(
                    licenseNotice = "بيانات الكتب محفوظة بترخيصها ومصدرها الفردي داخل الفهرس.",
                    sourceName = "استيرادات تمت قبل إضافة إدارة الحزم",
                    sourceUrl = null,
                    originName = null,
                ),
                installation = ScholarContentPackInstallationEntity(
                    bookIds = legacyImportedBooks.map { it.id }.toStoredIds(),
                    imported = true,
                    managed = false,
                    installedAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                ),
            ),
        )
    }

    private suspend fun validateNonDestructiveUpdate(
        pack: ScholarPack,
        existing: ScholarContentPackEntity?,
        newBookIds: Set<String>,
    ) {
        val previousPackBookIds = existing
            ?.installation
            ?.bookIds
            ?.toStoredIdList()
            ?.toSet()
            .orEmpty()
        require(newBookIds.containsAll(previousPackBookIds)) {
            "التحديث الآمن لا يسمح بحذف كتاب موجود من الحزمة."
        }

        val overwrittenBookIds = newBookIds.filterTo(mutableSetOf()) { bookId ->
            libraryDao.bookById(bookId) != null
        }
        val protectedBookIds = previousPackBookIds + overwrittenBookIds
        protectedBookIds.forEach { bookId ->
            val newBook = pack.books.firstOrNull { it.id == bookId } ?: return@forEach
            val previousPassageIds = libraryDao.observePassagesForBook(bookId).first().map { it.id }.toSet()
            val newPassageIds = newBook.passages.map { it.id }.toSet()
            require(newPassageIds.containsAll(previousPassageIds)) {
                "التحديث الآمن لا يسمح بحذف مقاطع مرتبطة بالكتاب: ${newBook.title}."
            }
        }
    }

    private suspend fun transferLegacyOwnership(newBookIds: Set<String>) {
        libraryDao.contentPackById(LEGACY_IMPORTS_PACK_ID)?.let { legacy ->
            val previousLegacyIds = legacy.installation.bookIds.toStoredIdList()
            val remaining = previousLegacyIds.filterNot { it in newBookIds }
            when {
                remaining.isEmpty() -> libraryDao.deleteContentPack(LEGACY_IMPORTS_PACK_ID)
                remaining.size != previousLegacyIds.size -> libraryDao.upsertContentPack(
                    legacy.copy(
                        installation = legacy.installation.copy(
                            bookIds = remaining.toStoredIds(),
                            updatedAtEpochMillis = System.currentTimeMillis(),
                        ),
                    ),
                )
            }
        }
    }

    private suspend fun persistPack(
        pack: ScholarPack,
        imported: Boolean,
        originName: String?,
        existing: ScholarContentPackEntity?,
    ) {
        val books = pack.books.map { item -> item.toEntity(imported) }
        val passages = pack.books.flatMap { book ->
            book.passages.map { passage -> passage.toEntity(book.id) }
        }
        libraryDao.installContentPack(
            pack = pack.toRegistryEntity(imported, originName, existing),
            books = books,
            passages = passages,
        )
        if (imported) {
            rebuildIndex()
        } else {
            ftsDao.clearAll()
            val rows = passages.map { passage ->
                ScholarPassageFtsEntity(
                    normalizedText = ArabicText.normalizeForSearch(passage.text),
                    passageId = passage.id,
                )
            }
            if (rows.isNotEmpty()) ftsDao.upsertRows(rows)
        }
    }

    private suspend fun rebuildIndex() {
        ftsDao.clearAll()
        val rows = mutableListOf<ScholarPassageFtsEntity>()
        val ids = libraryDao.observeBooks().first().map { it.id }
        ids.forEach { bookId ->
            rows += libraryDao.observePassagesForBook(bookId).first().map { passage ->
                ScholarPassageFtsEntity(
                    normalizedText = ArabicText.normalizeForSearch(passage.text),
                    passageId = passage.id,
                )
            }
        }
        if (rows.isNotEmpty()) ftsDao.upsertRows(rows)
    }

    private fun decodeAndValidate(rawText: String): ScholarPack {
        val pack = json.decodeFromString<ScholarPack>(rawText)
        require(pack.schemaVersion in MIN_PACK_SCHEMA_VERSION..CURRENT_PACK_SCHEMA_VERSION) {
            "إصدار الحزمة غير مدعوم."
        }
        require(pack.packName.isNotBlank() && pack.packName.length <= MAX_PACK_NAME_LENGTH) {
            "اسم الحزمة مطلوب ويجب أن يكون ضمن الحد المسموح."
        }
        require(pack.licenseNotice.isNotBlank()) { "يجب أن تتضمن الحزمة بيان ترخيص واضحاً." }
        require(pack.packVersion in 1..MAX_PACK_VERSION) { "رقم إصدار الحزمة غير صالح." }
        if (pack.schemaVersion >= 4) {
            require(!pack.packId.isNullOrBlank() && ID_REGEX.matches(pack.packId)) {
                "حزم الإصدار 4 تتطلب packId ثابتاً وصالحاً."
            }
            require(!pack.sourceName.isNullOrBlank() && pack.sourceName.length <= MAX_PACK_SOURCE_LENGTH) {
                "حزم الإصدار 4 تتطلب اسم مصدر واضحاً للحزمة."
            }
            require(pack.sourceUrl == null || pack.sourceUrl.length <= MAX_SOURCE_URL_LENGTH) {
                "رابط مصدر الحزمة طويل جداً."
            }
        }
        require(pack.books.isNotEmpty() && pack.books.size <= MAX_BOOKS_PER_PACK) {
            "عدد الكتب في الحزمة غير صالح."
        }
        require(pack.books.map { it.id }.distinct().size == pack.books.size) { "معرّفات الكتب مكررة." }
        val passageIds = mutableSetOf<String>()
        pack.books.forEach { book ->
            validateBook(book, passageIds)
        }
        return pack
    }

    private fun validateBook(
        book: ScholarPackBook,
        passageIds: MutableSet<String>,
    ) {
        require(ID_REGEX.matches(book.id)) { "معرّف كتاب غير صالح: ${book.id}" }
        require(book.title.isNotBlank() && book.author.isNotBlank()) { "عنوان الكتاب ومؤلفه مطلوبان." }
        require(book.sourceName.isNotBlank() && book.licenseSummary.isNotBlank()) {
            "يجب توضيح مصدر وترخيص كل كتاب."
        }
        require(book.language.isNotBlank() && book.language.length <= 20) { "لغة الكتاب غير صالحة." }
        require(ScholarDifficulty.entries.any { it.name.equals(book.difficulty, ignoreCase = true) }) {
            "مستوى الكتاب غير مدعوم."
        }
        require(book.volumeCount == null || book.volumeCount in 1..MAX_VOLUME_COUNT) {
            "عدد مجلدات الكتاب غير صالح."
        }
        require(book.keywords.size <= MAX_KEYWORDS_PER_BOOK && book.keywords.all { it.length <= MAX_KEYWORD_LENGTH }) {
            "الكلمات المفتاحية للكتاب تتجاوز الحدود المسموح بها."
        }
        require(book.passages.isNotEmpty() && book.passages.size <= MAX_PASSAGES_PER_BOOK) {
            "لا بد من وجود نص واحد على الأقل لكل كتاب ضمن الحدود المسموح بها."
        }
        book.passages.forEach { passage ->
            require(ID_REGEX.matches(passage.id) && passageIds.add(passage.id)) {
                "معرّف مقطع مكرر أو غير صالح."
            }
            require(passage.chapter.isNotBlank() && passage.text.trim().length in 1..PASSAGE_MAX_LENGTH) {
                "نص أو فصل المقطع غير صالح."
            }
            require(passage.section == null || passage.section.length <= MAX_SECTION_LENGTH) {
                "عنوان قسم المقطع طويل جداً."
            }
            require(passage.orderIndex >= 0) { "ترتيب المقطع يجب ألا يكون سالباً." }
        }
    }

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
        subtitle = subtitle,
        language = language,
        difficulty = ScholarDifficulty.fromId(difficulty).name,
        publisher = publisher,
        edition = edition,
        editor = editor,
        publicationYear = publicationYear,
        volumeCount = volumeCount,
        keywords = keywords.joinToString(KEYWORD_SEPARATOR),
    )

    private fun ScholarPackPassage.toEntity(bookId: String) = ScholarPassageEntity(
        id = id,
        bookId = bookId,
        chapter = chapter,
        volume = volume,
        page = page,
        text = text,
        section = section,
        orderIndex = orderIndex,
    )

    private companion object {
        const val BUNDLED_CATALOG = "scholar_library_catalog.json"
        const val BUNDLED_PACK_ORIGIN = "bundled:scholar_library_catalog.json"
        const val LEGACY_IMPORTS_PACK_ID = "legacy-imports"
        const val MIN_PACK_SCHEMA_VERSION = 1
        const val CURRENT_PACK_SCHEMA_VERSION = 4
        const val PACK_MAX_CHARS = 5_000_000
        const val MAX_BOOKS_PER_PACK = 1_000
        const val MAX_PASSAGES_PER_BOOK = 20_000
        const val PASSAGE_MAX_LENGTH = 30_000
        const val MAX_SECTION_LENGTH = 300
        const val MAX_VOLUME_COUNT = 500
        const val MAX_KEYWORDS_PER_BOOK = 100
        const val MAX_KEYWORD_LENGTH = 120
        const val MAX_PACK_NAME_LENGTH = 200
        const val MAX_PACK_VERSION = 1_000_000
        const val MAX_PACK_SOURCE_LENGTH = 300
        const val MAX_SOURCE_URL_LENGTH = 2_000
        const val KEYWORD_SEPARATOR = "\u001F"
        val ID_REGEX = Regex("[A-Za-z0-9_-]{3,120}")
    }
}
