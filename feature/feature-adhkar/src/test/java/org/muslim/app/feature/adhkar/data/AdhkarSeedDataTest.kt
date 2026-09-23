package org.muslim.app.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test
import org.muslim.app.feature.adhkar.domain.DhikrCategory

class AdhkarSeedDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `seed adhkar are complete and structurally valid`() {
        val seedFile = locateSeedFile()
        val seed = json.decodeFromString<SeedFile>(seedFile.readText())

        assertThat(seed.adhkar).isNotEmpty()
        assertThat(seed.adhkar.map { it.id }).containsNoDuplicates()

        val supportedCategories = DhikrCategory.entries.map { it.id }.toSet()

        seed.adhkar.forEach { item ->
            assertThat(item.id).isGreaterThan(0L)
            assertThat(item.category).isIn(supportedCategories)
            assertThat(item.arabic.trim()).isNotEmpty()
            assertThat(item.source.trim()).isNotEmpty()
            assertThat(item.repetition).isGreaterThan(0)
            assertThat(item.arabic).doesNotContain("...")
            assertThat(item.arabic).doesNotContain("…")
        }
    }

    private fun locateSeedFile(): File {
        val candidates = listOf(
            File("src/main/assets/adhkar.json"),
            File("feature/feature-adhkar/src/main/assets/adhkar.json"),
        )
        return candidates.firstOrNull(File::isFile)
            ?: error("Unable to locate feature-adhkar/src/main/assets/adhkar.json")
    }

    @Serializable
    private data class SeedFile(
        val adhkar: List<SeedItem>,
    )

    @Serializable
    private data class SeedItem(
        val id: Long,
        val category: String,
        val arabic: String,
        val source: String,
        val repetition: Int,
    )
}
