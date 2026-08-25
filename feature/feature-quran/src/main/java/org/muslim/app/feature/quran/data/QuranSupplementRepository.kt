package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.core.database.dao.TafsirDao
import org.muslim.app.core.database.dao.TranslationDao
import org.muslim.app.core.database.entity.TafsirEntity
import org.muslim.app.core.database.entity.TranslationEntity
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class TranslationPackJson(
    val language: String,
    val translations: List<TranslationItemJson>,
)

@Serializable
private data class TranslationItemJson(
    val globalNumber: Int,
    val text: String,
)

@Serializable
private data class TafsirPackJson(
    val source: String,
    val entries: List<TafsirItemJson>,
)

@Serializable
private data class TafsirItemJson(
    val globalNumber: Int,
    val text: String,
)

@Serializable
private data class QuranEncSuraResponse(
    val result: List<QuranEncAyah> = emptyList(),
)

@Serializable
private data class QuranEncAyah(
    val sura: String,
    val aya: String,
    val translation: String,
)

/** Official, selectable Sunni tafsir packs fetched from QuranEnc. */
enum class OfficialTafsirSource(
    val storageKey: String,
    val apiKey: String,
    val displayName: String,
) {
    AlMuyassar(
        storageKey = "QuranEnc · At-Tafsir Al-Muyassar",
        apiKey = "arabic_moyassar",
        displayName = "At-Tafsir Al-Muyassar",
    ),
}

/**
 * Meaning translations + tafsir (PROJECT_PROMPT.md §6 Phase 2).
 *
 * Full packs are installed from JSON files (documented in `README`), keeping
 * religious content out of the binary and subject to independent review. A
 * tiny development sample ships in assets so the UI can be exercised offline.
 */
@Singleton
class QuranSupplementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val translationDao: TranslationDao,
    private val tafsirDao: TafsirDao,
    private val json: Json,
    private val quranRepository: QuranRepository,
) {

    fun observeTranslations(globalNumber: Int): Flow<List<Translation>> =
        translationDao.observeForAyah(globalNumber).map { list ->
            list.map { Translation(it.globalNumber, it.language, it.text) }
        }

    fun observeTafsir(globalNumber: Int): Flow<List<TafsirEntry>> =
        tafsirDao.observeForAyah(globalNumber).map { list ->
            list.map { TafsirEntry(it.globalNumber, it.source, it.text) }
        }

    /** Installed translation languages (BCP-47 tags), for the reader picker. */
    fun observeLanguages(): Flow<List<String>> = translationDao.observeLanguages()

    /** Installs a translation pack from a JSON file; returns the inserted count. */
    suspend fun installTranslationPack(file: File): Int {
        val pack = json.decodeFromString<TranslationPackJson>(file.readText())
        val entities = pack.translations.map {
            TranslationEntity(globalNumber = it.globalNumber, language = pack.language, text = it.text)
        }
        translationDao.insertAll(entities)
        return entities.size
    }

    fun observeInstalledTafsirSources(): Flow<List<String>> = tafsirDao.observeSources()

    /**
     * Downloads one complete, cited Sunni tafsir pack from QuranEnc. The API is
     * fetched surah by surah, validated against the local Mushaf numbering, and
     * committed only after all 6,236 ayahs have been received successfully.
     */
    suspend fun downloadOfficialTafsir(
        source: OfficialTafsirSource,
        onSurahDownloaded: (completedSurahs: Int) -> Unit = {},
    ): Int = withContext(Dispatchers.IO) {
        val globalNumbers = quranRepository.allAyahs()
            .associate { (it.surahNumber to it.numberInSurah) to it.globalNumber }
        val expectedGlobalNumbers = globalNumbers.values.toSet()
        val entries = ArrayList<TafsirEntity>(globalNumbers.size)
        for (surahNumber in 1..114) {
            val url = URL("https://quranenc.com/api/v1/translation/sura/${source.apiKey}/$surahNumber")
            val response = (url.openConnection() as HttpURLConnection).run {
                requestMethod = "GET"
                connectTimeout = NETWORK_TIMEOUT_MS
                readTimeout = NETWORK_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                try {
                    if (responseCode !in 200..299) error("QuranEnc HTTP $responseCode")
                    inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } finally {
                    disconnect()
                }
            }
            val items = json.decodeFromString<QuranEncSuraResponse>(response).result
            if (items.isEmpty()) error("QuranEnc returned no ayahs for surah $surahNumber")
            items.forEach { item ->
                val sourceSurah = item.sura.toIntOrNull() ?: error("Invalid QuranEnc surah")
                val sourceAyah = item.aya.toIntOrNull() ?: error("Invalid QuranEnc ayah")
                if (sourceSurah != surahNumber) error("Mismatched QuranEnc surah $sourceSurah")
                if (item.translation.isBlank()) error("Empty QuranEnc tafsir text for $sourceSurah:$sourceAyah")
                val globalNumber = globalNumbers[sourceSurah to sourceAyah]
                    ?: error("Unknown QuranEnc ayah ${item.sura}:${item.aya}")
                entries += TafsirEntity(
                    globalNumber = globalNumber,
                    source = source.storageKey,
                    text = item.translation,
                )
            }
            onSurahDownloaded(surahNumber)
        }
        if (entries.size != globalNumbers.size || entries.map { it.globalNumber }.toSet() != expectedGlobalNumbers) {
            error("Incomplete or duplicated QuranEnc tafsir pack")
        }
        tafsirDao.replaceSource(source.storageKey, entries)
        entries.size
    }

    /** Installs a tafsir pack from a JSON file; returns the inserted count. */
    suspend fun installTafsirPack(file: File): Int {
        val pack = json.decodeFromString<TafsirPackJson>(file.readText())
        val entities = pack.entries.map {
            TafsirEntity(globalNumber = it.globalNumber, source = pack.source, text = it.text)
        }
        tafsirDao.insertAll(entities)
        return entities.size
    }

    suspend fun removeTranslationLanguage(language: String) = translationDao.deleteLanguage(language)

    suspend fun removeTafsirSource(source: String) = tafsirDao.deleteSource(source)

    /** True when the sample development pack is already installed. */
    suspend fun hasSampleTranslation(): Boolean = translationDao.countForLanguage(SAMPLE_LANGUAGE) > 0

    /** True when the sample development tafsir pack is already installed. */
    suspend fun hasSampleTafsir(): Boolean = tafsirDao.countForSource(SAMPLE_TAFSIR_SOURCE) > 0

    /**
     * Seeds the tiny development samples (Al-Fatiha + Al-Ikhlas translations
     * and tafsir). This is UI scaffolding — production packs are imported
     * separately and reviewed.
     */
    suspend fun seedSampleIfEmpty() {
        if (hasSampleTranslation()) {
            if (!hasSampleTafsir()) seedSampleTafsir()
            return
        }
        runCatching {
            val text = context.assets.open(SAMPLE_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
            installTranslationPack(textToTempFile(text))
        }
        seedSampleTafsir()
    }

    private suspend fun seedSampleTafsir() {
        runCatching {
            val text = context.assets.open(TAFSIR_SAMPLE_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
            installTafsirPack(tafsirTextToTempFile(text))
        }
    }

    private fun textToTempFile(text: String): File =
        File(context.cacheDir, "sample_translation.json").apply { writeText(text) }

    private fun tafsirTextToTempFile(text: String): File =
        File(context.cacheDir, "sample_tafsir.json").apply { writeText(text) }

    private companion object {
        const val SAMPLE_ASSET = "quran_translations_sample.json"
        const val SAMPLE_LANGUAGE = "en"
        const val TAFSIR_SAMPLE_ASSET = "quran_tafsir_sample.json"
        const val SAMPLE_TAFSIR_SOURCE = "Sample"
        const val NETWORK_TIMEOUT_MS = 20_000
    }
}
