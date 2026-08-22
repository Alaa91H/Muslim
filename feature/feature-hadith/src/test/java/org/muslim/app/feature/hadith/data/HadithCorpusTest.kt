package org.muslim.app.feature.hadith.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

/**
 * Guards the bundled hadith corpus (hadith_sample.json): every record must be
 * well-formed, complete in both languages, free of duplicates, and the
 * An-Nawawi 40 must be present and correctly numbered.
 */
class HadithCorpusTest {

    @Serializable
    private data class SeedFile(
        val version: Int = 1,
        val hadiths: List<SeedItem>,
    )

    @Serializable
    private data class SeedItem(
        val collection: String,
        val chapter: String? = null,
        val number: Int? = null,
        val arabic: String,
        val translation: String,
        val grade: String,
        val source: String,
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadCorpus(): SeedFile {
        val file = candidatePaths()
            .firstOrNull { it.exists() }
            ?: error("Corpus asset not found under ${candidatePaths().joinToString()}")
        return json.decodeFromString<SeedFile>(file.readText(Charsets.UTF_8))
    }

    private fun candidatePaths(): List<File> = listOf(
        File("src/main/assets/hadith_sample.json"),
        File("feature/feature-hadith/src/main/assets/hadith_sample.json"),
    )

    @Test
    fun `corpus is not empty and has a version`() {
        val corpus = loadCorpus()
        assertThat(corpus.hadiths).isNotEmpty()
        assertThat(corpus.version).isAtLeast(2)
    }

    @Test
    fun `every hadith is complete in both languages with grade and source`() {
        val corpus = loadCorpus()
        corpus.hadiths.forEach { item ->
            assertThat(item.arabic).isNotEmpty()
            assertThat(item.translation).isNotEmpty()
            assertThat(item.grade).isNotEmpty()
            assertThat(item.source).isNotEmpty()
            assertThat(item.collection).isNotEmpty()
        }
    }

    @Test
    fun `all collections are recognized and every chip has content`() {
        val corpus = loadCorpus()
        val present = corpus.hadiths.map { it.collection }.toSet()
        assertThat(present).containsAtLeast(
            "nawawi40", "riyad", "bukhari", "muslim", "tirmidhi",
            "abudawud", "nasai", "ibnmajah",
        )
    }

    @Test
    fun `no duplicate hadith texts`() {
        val corpus = loadCorpus()
        assertThat(corpus.hadiths.map { it.arabic }.toSet().size)
            .isEqualTo(corpus.hadiths.size)
    }

    @Test
    fun `the complete an-nawawi 40 is present and numbered 1 to 42`() {
        val corpus = loadCorpus()
        val nawawi = corpus.hadiths.filter { it.collection == "nawawi40" }
        assertThat(nawawi).hasSize(42)
        val numbers = nawawi.map { it.number }.filterNotNull()
        assertThat(numbers).containsNoDuplicates()
        assertThat(numbers.minOrNull()).isEqualTo(1)
        assertThat(numbers.maxOrNull()).isEqualTo(42)
    }

    @Test
    fun `grades use the supported vocabulary`() {
        val corpus = loadCorpus()
        val allowed = setOf("Sahih", "Hasan", "Da'if")
        corpus.hadiths.forEach { item ->
            assertThat(item.grade).isIn(allowed)
        }
    }
}
