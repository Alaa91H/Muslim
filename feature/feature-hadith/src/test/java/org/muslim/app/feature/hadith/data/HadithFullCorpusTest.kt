package org.muslim.app.feature.hadith.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.hadith.domain.HadithCollection
import java.io.File
import java.util.zip.GZIPInputStream

/**
 * Guards the optional full Six-Books corpus (hadith_full.json) produced by
 * scripts/import-hadith.py. The file is git-ignored (tens of MB), so every test
 * skips cleanly when it is absent (e.g. on CI) and validates it hard whenever
 * it is bundled locally.
 */
class HadithFullCorpusTest {

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

    private fun fullCorpusFile(): File? = listOf(
        File("src/main/assets/hadith_full.json.gz"),
        File("feature/feature-hadith/src/main/assets/hadith_full.json.gz"),
        File("src/main/assets/hadith_full.json"),
        File("feature/feature-hadith/src/main/assets/hadith_full.json"),
    ).firstOrNull { it.exists() }

    private fun loadFullCorpus(): SeedFile? {
        val file = fullCorpusFile() ?: return null
        val text = if (file.name.endsWith(".gz")) {
            GZIPInputStream(file.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
            file.readText(Charsets.UTF_8)
        }
        return json.decodeFromString<SeedFile>(text)
    }

    @Test
    fun `full corpus when present is non-empty with a version above the sample`() {
        val corpus = loadFullCorpus() ?: run { assumeTrue(false); return }
        assertThat(corpus.hadiths).isNotEmpty()
        assertThat(corpus.version).isAtLeast(10)
    }

    @Test
    fun `full corpus records are complete and use only recognized collections`() {
        val corpus = loadFullCorpus() ?: run { assumeTrue(false); return }
        val recognized = HadithCollection.entries.map { it.id }.toSet()
        corpus.hadiths.forEach { item ->
            assertThat(item.arabic).isNotEmpty()
            assertThat(item.translation).isInstanceOf(String::class.java)
            assertThat(item.grade).isNotEmpty()
            assertThat(item.source).isNotEmpty()
            assertThat(item.collection).isIn(recognized)
        }
    }

    @Test
    fun `full corpus has no duplicate matn within a collection`() {
        val corpus = loadFullCorpus() ?: run { assumeTrue(false); return }
        val seen = mutableSetOf<String>()
        corpus.hadiths.forEach { item ->
            val key = "${item.collection}::${ArabicText.normalize(item.arabic)}"
            assertThat(key).isNotIn(seen)
            seen.add(key)
        }
    }
}
