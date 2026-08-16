package org.example.islamicapp.feature.hadith.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Content-integrity tests (PROJECT_PROMPT.md §10): every shipped hadith must
 * carry text, narrator, source and grading.
 */
class HadithRepositoryTest {

    private val repository = HadithRepository()

    @Test
    fun `collection is a meaningful curated set`() {
        assertThat(repository.hadiths.size).isAtLeast(25)
    }

    @Test
    fun `every hadith is fully sourced`() {
        repository.hadiths.forEach { hadith ->
            assertThat(hadith.text).isNotEmpty()
            assertThat(hadith.titleAr).isNotEmpty()
            assertThat(hadith.narrator).isNotEmpty()
            assertThat(hadith.reference).isNotEmpty()
            assertThat(hadith.gradeAr).isNotEmpty()
        }
    }

    @Test
    fun `ids are unique`() {
        val ids = repository.hadiths.map { it.id }
        assertThat(ids.distinct()).hasSize(ids.size)
    }

    @Test
    fun `search matches text title and narrator`() {
        assertThat(repository.search("النيات")).isNotEmpty()
        assertThat(repository.search("عمر")).isNotEmpty()
        assertThat(repository.search("جبريل")).isNotEmpty()
        assertThat(repository.search("")).hasSize(repository.hadiths.size)
    }

    @Test
    fun `hadith of the day is deterministic and rotating`() {
        val a = repository.hadithOfTheDay(100)
        val b = repository.hadithOfTheDay(100)
        val c = repository.hadithOfTheDay(101)
        assertThat(a.id).isEqualTo(b.id)
        assertThat(repository.hadithOfTheDay(100 + repository.hadiths.size).id)
            .isEqualTo(a.id)
        assertThat(c.id).isNotEqualTo(a.id) // consecutive days rotate
    }
}
