package org.example.islamicapp.feature.quran.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.islamicapp.core.database.entity.AyahEntity
import org.example.islamicapp.core.database.entity.SurahEntity
import java.io.Reader

/**
 * Pure parsers for the bundled Quran assets (PROJECT_PROMPT.md §3.7 — kept
 * free of Android framework types so they run as plain JVM unit tests).
 *
 * - `quran_ayahs.txt`: one line per ayah, pipe-delimited:
 *   `surah|numberInSurah|global|juz|page|text`
 * - `quran_surahs.json`: compact array of surah metadata.
 *
 * Source: Tanzil's Uthmani text via the alquran.cloud dataset (attribution in
 * README.md). A final religious review against a printed Madani mushaf is
 * still required before release (PROJECT_PROMPT.md §10).
 */
internal object QuranAssetParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseAyahs(reader: Reader): List<AyahEntity> {
        val ayahs = ArrayList<AyahEntity>(6_236)
        reader.forEachLine { line ->
            val parts = line.split('|')
            if (parts.size < 6) return@forEachLine
            ayahs += AyahEntity(
                globalNumber = parts[2].toInt(),
                surahNumber = parts[0].toInt(),
                numberInSurah = parts[1].toInt(),
                juz = parts[3].toInt(),
                page = parts[4].toInt(),
                text = parts.drop(5).joinToString("|"),
            )
        }
        return ayahs
    }

    fun parseSurahs(jsonText: String): List<SurahEntity> {
        val metadata = json.decodeFromString<List<SurahMetadata>>(jsonText)
        return metadata.map {
            SurahEntity(
                number = it.number,
                arabicName = it.arabic,
                englishName = it.english,
                translation = it.translation,
                revelationType = it.revelation,
                ayahCount = it.ayahs,
            )
        }
    }

    @Serializable
    internal data class SurahMetadata(
        val number: Int,
        val arabic: String,
        val english: String,
        val translation: String,
        val revelation: String,
        val ayahs: Int,
    )
}
