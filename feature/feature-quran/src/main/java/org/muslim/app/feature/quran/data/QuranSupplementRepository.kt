package org.muslim.app.feature.quran.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.muslim.app.core.database.AppDatabase
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.core.database.dao.TafsirDao
import org.muslim.app.core.database.dao.TranslationDao
import org.muslim.app.core.database.dao.QuranTextPackDao
import org.muslim.app.core.database.entity.TafsirEntity
import org.muslim.app.core.database.entity.TranslationEntity
import org.muslim.app.core.database.entity.QuranTextPackEntity
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class QuranEncSuraResponse(
    val result: List<QuranEncAyah> = emptyList(),
)

@Serializable
private data class QuranEncAyah(
    val sura: String,
    val aya: String,
    val translation: String,
    val footnotes: String? = null,
)

@Serializable
private data class QuranEncTranslationList(val translations: List<QuranEncCatalogEntry> = emptyList())

private data class ResolvedOfficialQuranTextSource(
    val source: OfficialQuranTextSource,
    val languageTag: String,
    val attribution: String,
    val work: String,
)

@Serializable
internal data class QuranEncCatalogEntry(
    val key: String,
    @SerialName("language_iso_code") val languageIsoCode: String,
    val version: String,
    val title: String,
    val description: String,
)

/** Source work classification is explicit; never infer tafsir vs. meaning from language or title. */
enum class OfficialQuranTextKind(val packKind: String) {
    Meaning(QuranTextPackKind.MeaningTranslation.wireValue),
    TranslatedTafsir(QuranTextPackKind.TafsirTranslation.wireValue),
    OriginalTafsir(QuranTextPackKind.OriginalTafsir.wireValue),
}

data class OfficialQuranTextSource(
    val storageKey: String,
    val apiKey: String,
    val languageTag: String,
    val title: String,
    val sourceUrl: String,
    val translator: String,
    val publisher: String,
    val sourceAttribution: String,
    val version: String,
    val reviewEvidence: String?,
    val kind: OfficialQuranTextKind,
) {
    val canDownload: Boolean
        get() = !reviewEvidence.isNullOrBlank() && version.isNotBlank() && when (kind) {
            OfficialQuranTextKind.Meaning -> true
            OfficialQuranTextKind.TranslatedTafsir -> storageKey == "uzbek_moyassar"
            OfficialQuranTextKind.OriginalTafsir -> false
        }
}

internal fun mapQuranEncMeaningSource(entry: QuranEncCatalogEntry) = OfficialQuranTextSource(
    storageKey = entry.key,
    apiKey = entry.key,
    languageTag = entry.languageIsoCode,
    title = entry.title,
    sourceUrl = "https://quranenc.com/en/browse/${entry.key}",
    translator = extractTranslator(entry.description) ?: "Not specified by the source",
    publisher = extractPublisher(entry.description) ?: "Not specified by the source",
    sourceAttribution = entry.description,
    version = entry.version,
    reviewEvidence = extractReviewEvidence(entry.description),
    kind = OfficialQuranTextKind.Meaning,
)

internal fun quranEncIndexCard(html: String, key: String): String? {
    val marker = Regex("""data-share-key\s*=\s*["']${Regex.escape(key)}["']""")
        .find(html)?.range?.first ?: return null
    val cardStart = html.lastIndexOf("<div class=\"tab_card", marker)
    if (cardStart < 0) return null
    val nextCard = html.indexOf("<div class=\"tab_card", marker)
    return html.substring(cardStart, if (nextCard < 0) html.length else nextCard)
}

internal fun parseQuranEncEditionVersion(html: String, key: String): String? {
    val card = quranEncIndexCard(html, key) ?: return null
    return Regex("""\bV([0-9]+(?:\.[0-9]+)+)\b""")
        .find(card)?.groupValues?.get(1)
}

internal fun quranEncUzbekMuyassarSource() = OfficialQuranTextSource(
    storageKey = "uzbek_moyassar",
    apiKey = "uzbek_moyassar",
    languageTag = "uz",
    title = "Uzbek Translation of At-Tafsir Al-Muyassar",
    sourceUrl = "https://quranenc.com/en/browse/uzbek_moyassar",
    translator = "Ismail Yaqub",
    publisher = "QuranEnc",
    sourceAttribution = "Translation of At-Tafsīr Al-Muyassar into Uzbek, translated by Ismail Yaqub and reviewed by members of the Islamic Center IxlosOrg, as reported by QuranEnc.",
    version = "1.0.0",
    reviewEvidence = "QuranEnc reports review by members of the Islamic Center IxlosOrg.",
    kind = OfficialQuranTextKind.TranslatedTafsir,
)

/** QuranEnc's interpretation works are absent from its translations-list API. Keep these
 * separately typed and non-downloadable until the publisher supplies translator, reviewer,
 * and edition-version metadata through a verifiable catalogue contract. */
internal fun quranEncTranslatedTafsirCandidates(): List<OfficialQuranTextSource> = listOf(
    "uzbek_mokhtasar" to ("uz" to "Uzbek Translation of Al-Mukhtasar"),
    "turkish_mokhtasar" to ("tr" to "Turkish Translation of Al-Mukhtasar"),
    "indonesian_mokhtasar" to ("id" to "Indonesian Translation of Al-Mukhtasar"),
    "japanese_mokhtasar" to ("ja" to "Japanese Translation of Al-Mukhtasar"),
    "thai_mokhtasar" to ("th" to "Thai Translation of Al-Mukhtasar"),
    "khmer_mokhtasar" to ("km" to "Khmer Translation of Al-Mukhtasar"),
    "persian_mokhtasar" to ("fa" to "Persian Translation of Al-Mukhtasar"),
    "kurdish_mokhtasar" to ("ku" to "Kurdish Translation of Al-Mukhtasar"),
    "hindi_mokhtasar" to ("hi" to "Hindi Translation of Al-Mukhtasar"),
    "bengali_mokhtasar" to ("bn" to "Bengali Translation of Al-Mukhtasar"),
    "telugu_mokhtasar" to ("te" to "Telugu Translation of Al-Mukhtasar"),
    "malayalam_mokhtasar" to ("ml" to "Malayalam Translation of Al-Mukhtasar"),
    "fulani_mokhtasar" to ("ff" to "Fulani Translation of Al-Mukhtasar"),
).map { (key, languageAndTitle) ->
    val (language, title) = languageAndTitle
    OfficialQuranTextSource(
        storageKey = key,
        apiKey = key,
        languageTag = language,
        title = title,
        sourceUrl = "https://quranenc.com/en/browse/$key",
        translator = "Not specified by the source",
        publisher = "Tafsir Center for Quranic Studies",
        sourceAttribution = "QuranEnc identifies this as a translation of Al-Mukhtasar in interpreting the Noble Quran and names Tafsir Center for Quranic Studies as issuer; translator, reviewer, and edition version are not available in its public API.",
        version = "Not specified by the source",
        reviewEvidence = null,
        kind = OfficialQuranTextKind.TranslatedTafsir,
    )
}

internal fun extractTranslator(description: String): String? =
    Regex("(?i)translated by\\s+(.+?)(?:[,.;]|$)").find(description)?.groupValues?.get(1)?.trim()

internal fun extractPublisher(description: String): String? =
    Regex("(?i)issued by\\s+(.+?)(?:[,.;]|$)").find(description)?.groupValues?.get(1)?.trim()

internal fun extractReviewEvidence(description: String): String? {
    val reviewClaim = Regex("(?i)(reviewed by|under the supervision of)").containsMatchIn(description)
    return description.takeIf { reviewClaim && it.isNotBlank() }
        ?.let { "QuranEnc source description reports review/supervision: $it" }
}

/**
 * Meaning translations + tafsir (PROJECT_PROMPT.md §6 Phase 2).
 *
 * Full packs are fetched from attributed public sources or imported as JSON,
 * then checked before an atomic local install. Partial placeholder samples are
 * intentionally not used as production translations or tafsir.
 */
@Singleton
class QuranSupplementRepository @Inject constructor(
    private val translationDao: TranslationDao,
    private val tafsirDao: TafsirDao,
    private val packDao: QuranTextPackDao,
    private val database: AppDatabase,
    private val json: Json,
    private val quranRepository: QuranRepository,
) {

    fun observeTranslations(globalNumber: Int): Flow<List<Translation>> = combine(
        translationDao.observeForAyah(globalNumber),
        packDao.observeKind(QuranTextPackKind.MeaningTranslation.wireValue),
    ) { rows, packs ->
        val byId = packs.associateBy(QuranTextPackEntity::id)
        rows.mapNotNull { row ->
            byId[row.packId]?.let { pack ->
                Translation(
                    globalNumber = row.globalNumber,
                    language = row.language,
                    text = row.text,
                    packId = pack.id,
                    title = pack.title,
                    translator = pack.translator,
                    publisher = pack.publisher,
                    sourceAttribution = pack.sourceAttribution,
                    version = pack.version,
                    sourceUrl = pack.sourceUrl,
                    footnotes = json.decodeFromString(row.footnotes),
                )
            }
        }
    }

    fun observeTafsir(globalNumber: Int): Flow<List<TafsirEntry>> = combine(
        tafsirDao.observeForAyah(globalNumber),
        packDao.observeAll(),
    ) { rows, packs ->
        val byId = packs.associateBy(QuranTextPackEntity::id)
        rows.mapNotNull { row ->
            byId[row.source]?.let { pack ->
                TafsirEntry(
                    globalNumber = row.globalNumber,
                    source = row.source,
                    text = row.text,
                    language = pack.languageTag,
                    title = pack.title,
                    translator = pack.translator,
                    publisher = pack.publisher,
                    sourceAttribution = pack.sourceAttribution,
                    version = pack.version,
                    sourceUrl = pack.sourceUrl,
                    footnotes = json.decodeFromString(row.footnotes),
                )
            }
        }
    }

    /** Languages are visible only when at least one validated meaning pack is installed. */
    fun observeLanguages(): Flow<List<String>> =
        packDao.observeKind(QuranTextPackKind.MeaningTranslation.wireValue)
            .map { packs -> packs.map(QuranTextPackEntity::languageTag).distinct().sorted() }

    /** Verified source catalogue; each installed work keeps its language and attribution. */
    fun observeInstalledTextPacks(): Flow<List<QuranTextPackEntity>> = packDao.observeAll()

    /** Installs only a complete, checksummed, provenance-bearing 6,236-ayah pack. */
    suspend fun installContentPack(file: File): Int = withContext(Dispatchers.IO) {
        installContentPack(file.readText(Charsets.UTF_8))
    }

    /** Installs a caller-provided pack document after full provenance and content verification. */
    suspend fun installContentPack(contents: String): Int = withContext(Dispatchers.IO) {
        val pack = json.decodeFromString<QuranTextPackFile>(contents)
        val expected = quranRepository.allAyahs().map { it.globalNumber }.toSet()
        QuranTextPackValidator.validate(pack, expected)
        val manifest = pack.manifest
        val metadata = QuranTextPackEntity(
            id = manifest.id,
            kind = manifest.kind,
            languageTag = manifest.languageTag,
            title = manifest.title,
            work = manifest.work,
            translator = manifest.translator,
            publisher = manifest.publisher,
            sourceAttribution = manifest.sourceAttribution,
            sourceUrl = manifest.sourceUrl,
            license = manifest.license,
            version = manifest.version,
            reviewer = manifest.reviewer,
            reviewReference = manifest.reviewReference,
            entryCount = pack.entries.size,
            footnoteCount = manifest.footnoteCount,
            sha256 = manifest.sha256.lowercase(),
            verifiedAtEpochMillis = System.currentTimeMillis(),
        )
        database.withTransaction {
            translationDao.deletePack(manifest.id)
            tafsirDao.deleteSource(manifest.id)
            packDao.delete(manifest.id)
            if (manifest.kind == QuranTextPackKind.MeaningTranslation.wireValue) {
                translationDao.insertAll(pack.entries.map { entry ->
                    TranslationEntity(
                        globalNumber = entry.globalNumber,
                        packId = manifest.id,
                        language = manifest.languageTag,
                        text = entry.text,
                        footnotes = json.encodeToString(entry.footnotes),
                    )
                })
            } else {
                tafsirDao.insertAll(pack.entries.map { entry ->
                    TafsirEntity(
                        globalNumber = entry.globalNumber,
                        source = manifest.id,
                        text = entry.text,
                        footnotes = json.encodeToString(entry.footnotes),
                    )
                })
            }
            packDao.insert(metadata)
        }
        pack.entries.size
    }

    fun observeInstalledTafsirSources(): Flow<List<String>> = packDao.observeAll()
        .map { packs -> packs.filter { it.kind != QuranTextPackKind.MeaningTranslation.wireValue }.map { it.id } }

    /** QuranEnc meaning translations plus separately classified tafsir works. */
    suspend fun listOfficialSources(languageTag: String): List<OfficialQuranTextSource> = withContext(Dispatchers.IO) {
        val requestedLanguage = languageTag.takeUnless { it.equals("auto", ignoreCase = true) }
            ?: Locale.getDefault().toLanguageTag()
        val language = requestedLanguage.substringBefore('-').lowercase()
        val catalogUrl = URL("https://quranenc.com/api/v1/translations/list?localization=en")
        val catalogJson = catalogUrl.openConnection().run {
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            getInputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        val translations = json.decodeFromString<QuranEncTranslationList>(catalogJson).translations
            .map(::mapQuranEncMeaningSource)
            .distinctBy(OfficialQuranTextSource::storageKey)
            .sortedWith(
                compareBy<OfficialQuranTextSource> { !it.languageTag.equals(language, ignoreCase = true) }
                    .thenBy(OfficialQuranTextSource::languageTag)
                    .thenBy(OfficialQuranTextSource::title),
            )
        val uzbekMuyassar = quranEncUzbekMuyassarSource()
        val pageCard = fetchQuranEncIndexCard(uzbekMuyassar.apiKey)
        val currentVersion = pageCard?.let { parseQuranEncEditionVersion(it, uzbekMuyassar.apiKey) }
        val indexedUzbekMuyassar = if (
            pageCard != null && currentVersion == uzbekMuyassar.version &&
            pageCard.contains(uzbekMuyassar.title) && pageCard.contains(uzbekMuyassar.translator) &&
            pageCard.contains("IxlosOrg")
        ) uzbekMuyassar else uzbekMuyassar.copy(
            reviewEvidence = null,
            sourceAttribution = "QuranEnc source metadata is unavailable or changed. Refresh before downloading.",
        )
        translations + quranEncTranslatedTafsirCandidates() + listOf(
            indexedUzbekMuyassar,
            OfficialQuranTextSource(
                storageKey = "arabic_moyassar",
                apiKey = "arabic_moyassar",
                languageTag = "ar",
                title = "At-Tafsir Al-Muyassar (Arabic original)",
                sourceUrl = "https://quranenc.com/ar/browse/arabic_moyassar",
                translator = "Original Arabic work",
                publisher = "QuranEnc",
                sourceAttribution = "The source page identifies the Arabic work; edition version and reviewer metadata are not exposed by the translations API.",
                version = "Not specified by the source API",
                reviewEvidence = null,
                kind = OfficialQuranTextKind.OriginalTafsir,
            ),
        )
    }

    private fun fetchQuranEncIndexCard(key: String): String? {
        val connection = URL("https://quranenc.com/en").openConnection() as HttpURLConnection
        connection.connectTimeout = NETWORK_TIMEOUT_MS
        connection.readTimeout = NETWORK_TIMEOUT_MS
        return try {
            if (connection.responseCode !in 200..299) return null
            val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            quranEncIndexCard(html, key)
        } finally {
            connection.disconnect()
        }
    }

    /** Download, validate complete ayah coverage and footnotes, then atomically install one official pack. */
    suspend fun downloadOfficialText(
        source: OfficialQuranTextSource,
        onSurahDownloaded: (completedSurahs: Int) -> Unit = {},
    ): Int = withContext(Dispatchers.IO) {
        val resolved = resolveOfficialTextSource(source)
        val globalNumbers = quranRepository.allAyahs()
            .associate { (it.surahNumber to it.numberInSurah) to it.globalNumber }
        val entries = downloadOfficialTextEntries(source.apiKey, globalNumbers, onSurahDownloaded)
        installOfficialTextPack(source, resolved, entries, globalNumbers.values.toSet())
        entries.size
    }

    private fun resolveOfficialTextSource(source: OfficialQuranTextSource): ResolvedOfficialQuranTextSource {
        require(source.storageKey == source.apiKey) { "QuranEnc pack identity does not match its source key" }
        require(source.canDownload) {
            "This source has no explicit review evidence and cannot be installed as verified content"
        }
        val catalogEntry = when (source.kind) {
            OfficialQuranTextKind.Meaning -> fetchQuranEncMeaningEntry(source.apiKey)
            OfficialQuranTextKind.TranslatedTafsir -> {
                validateQuranEncTafsirSource(source)
                null
            }
            OfficialQuranTextKind.OriginalTafsir -> error("Original tafsir packs cannot be installed as translations")
        }
        val currentSource = if (catalogEntry != null) {
            val mapped = mapQuranEncMeaningSource(catalogEntry)
            require(catalogEntry.languageIsoCode.equals(source.languageTag, ignoreCase = true)) {
                "QuranEnc source language changed; refresh the catalogue"
            }
            mapped
        } else source
        require(currentSource.canDownload) { "QuranEnc no longer provides sufficient source review metadata" }
        val sourceLanguage = catalogEntry?.languageIsoCode ?: currentSource.languageTag
        val sourceDescription = catalogEntry?.description ?: currentSource.sourceAttribution
        val sourceWork = if (currentSource.kind == OfficialQuranTextKind.TranslatedTafsir) {
            "At-Tafsir Al-Muyassar"
        } else {
            currentSource.title
        }
        return ResolvedOfficialQuranTextSource(currentSource, sourceLanguage, sourceDescription, sourceWork)
    }

    private fun fetchQuranEncMeaningEntry(apiKey: String): QuranEncCatalogEntry {
        val catalogUrl = URL("https://quranenc.com/api/v1/translations/list?localization=en")
        val catalogJson = catalogUrl.openConnection().run {
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            getInputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        return json.decodeFromString<QuranEncTranslationList>(catalogJson)
            .translations.firstOrNull { it.key == apiKey }
            ?: error("QuranEnc source metadata is unavailable")
    }

    private fun validateQuranEncTafsirSource(source: OfficialQuranTextSource) {
        require(source == quranEncUzbekMuyassarSource()) {
            "This translated tafsir source is not approved for direct installation"
        }
        val card = fetchQuranEncIndexCard(source.apiKey)
            ?: error("QuranEnc tafsir source card is unavailable")
        val currentVersion = parseQuranEncEditionVersion(card, source.apiKey)
            ?: error("QuranEnc tafsir edition version is unavailable")
        require(
            currentVersion == source.version && card.contains(source.title) &&
                card.contains(source.translator) && card.contains("IxlosOrg"),
        ) { "QuranEnc tafsir edition metadata changed; refresh the catalogue" }
    }

    private fun downloadOfficialTextEntries(
        apiKey: String,
        globalNumbers: Map<Pair<Int, Int>, Int>,
        onSurahDownloaded: (Int) -> Unit,
    ): List<QuranTextPackEntry> = buildList {
        for (surahNumber in 1..114) {
            addAll(downloadOfficialSurahEntries(apiKey, surahNumber, globalNumbers))
            onSurahDownloaded(surahNumber)
        }
    }

    private fun downloadOfficialSurahEntries(
        apiKey: String,
        surahNumber: Int,
        globalNumbers: Map<Pair<Int, Int>, Int>,
    ): List<QuranTextPackEntry> {
        val url = URL("https://quranenc.com/api/v1/translation/sura/$apiKey/$surahNumber")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = NETWORK_TIMEOUT_MS
        connection.readTimeout = NETWORK_TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/json")
        val response = try {
            if (connection.responseCode !in 200..299) error("QuranEnc HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
        val items = json.decodeFromString<QuranEncSuraResponse>(response).result
        require(items.isNotEmpty()) { "QuranEnc returned no ayahs for surah $surahNumber" }
        return items.map { item -> mapQuranEncAyah(item, surahNumber, globalNumbers) }
    }

    private fun mapQuranEncAyah(
        item: QuranEncAyah,
        expectedSurah: Int,
        globalNumbers: Map<Pair<Int, Int>, Int>,
    ): QuranTextPackEntry {
        val surahNumber = item.sura.toIntOrNull() ?: error("Invalid QuranEnc surah")
        val ayahNumber = item.aya.toIntOrNull() ?: error("Invalid QuranEnc ayah")
        require(surahNumber == expectedSurah) { "Mismatched QuranEnc surah $surahNumber" }
        require(item.translation.isNotBlank()) { "Empty QuranEnc text for $surahNumber:$ayahNumber" }
        val globalNumber = globalNumbers[surahNumber to ayahNumber]
            ?: error("Unknown QuranEnc ayah ${item.sura}:${item.aya}")
        return QuranTextPackEntry(
            globalNumber = globalNumber,
            text = item.translation,
            footnotes = item.footnotes?.takeIf(String::isNotBlank)?.let(::listOf).orEmpty(),
        )
    }

    private suspend fun installOfficialTextPack(
        source: OfficialQuranTextSource,
        resolved: ResolvedOfficialQuranTextSource,
        entries: List<QuranTextPackEntry>,
        expectedGlobalNumbers: Set<Int>,
    ) {
        val footnoteCount = entries.sumOf { it.footnotes.size }
        val packJson = QuranTextPackFile(
            schemaVersion = QuranTextPackValidator.SUPPORTED_SCHEMA_VERSION,
            manifest = QuranTextPackManifest(
                id = source.storageKey,
                kind = source.kind.packKind,
                languageTag = resolved.languageTag,
                title = resolved.source.title,
                work = resolved.work,
                translator = resolved.source.translator,
                publisher = resolved.source.publisher,
                sourceAttribution = resolved.attribution,
                sourceUrl = resolved.source.sourceUrl,
                license = "QuranEnc reuse terms: no modification/addition/deletion; credit publisher and QuranEnc; show edition version and source transcript; report translation notes; use latest edition; no inappropriate ads. See https://quranenc.com/en/home/api/",
                version = resolved.source.version,
                reviewer = requireNotNull(resolved.source.reviewEvidence),
                reviewReference = resolved.source.sourceUrl,
                expectedAyahCount = QuranTextPackValidator.FULL_QURAN_AYAH_COUNT,
                footnoteCount = footnoteCount,
                sha256 = sha256(json.encodeToString(entries)),
                reviewStatus = "source_reviewed",
            ),
            entries = entries,
        )
        QuranTextPackValidator.validate(packJson, expectedGlobalNumbers)
        val metadata = packJson.manifest.toEntity(entries.size)
        database.withTransaction {
            translationDao.deletePack(source.storageKey)
            tafsirDao.deleteSource(source.storageKey)
            packDao.delete(source.storageKey)
            when (source.kind) {
                OfficialQuranTextKind.Meaning -> translationDao.insertAll(entries.map { entry ->
                    TranslationEntity(
                        globalNumber = entry.globalNumber,
                        packId = source.storageKey,
                        language = resolved.languageTag,
                        text = entry.text,
                        footnotes = json.encodeToString(entry.footnotes),
                    )
                })
                else -> tafsirDao.insertAll(entries.map { entry ->
                    TafsirEntity(
                        globalNumber = entry.globalNumber,
                        source = source.storageKey,
                        text = entry.text,
                        footnotes = json.encodeToString(entry.footnotes),
                    )
                })
            }
            packDao.insert(metadata)
        }
    }

    suspend fun removeTranslationLanguage(language: String) {
        val packs = packDao.observeKind(QuranTextPackKind.MeaningTranslation.wireValue).first()
            .filter { it.languageTag.equals(language, ignoreCase = true) }
        database.withTransaction {
            packs.forEach { pack ->
                translationDao.deletePack(pack.id)
                packDao.delete(pack.id)
            }
        }
    }

    suspend fun removeTafsirSource(source: String) {
        database.withTransaction {
            tafsirDao.deleteSource(source)
            packDao.delete(source)
        }
    }

    suspend fun removeLegacySampleTafsir() = tafsirDao.deleteSource("Sample")

    /** Removes the legacy placeholder that was never a production tafsir. */
    private companion object {
        const val NETWORK_TIMEOUT_MS = 20_000

    }
}

private fun QuranTextPackManifest.toEntity(entryCount: Int) = QuranTextPackEntity(
    id = id,
    kind = kind,
    languageTag = languageTag,
    title = title,
    work = work,
    translator = translator,
    publisher = publisher,
    sourceAttribution = sourceAttribution,
    sourceUrl = sourceUrl,
    license = license,
    version = version,
    reviewer = reviewer,
    reviewReference = reviewReference,
    entryCount = entryCount,
    footnoteCount = footnoteCount,
    sha256 = sha256,
    verifiedAtEpochMillis = System.currentTimeMillis(),
)

private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8))
    .joinToString("") { "%02x".format(it) }
