package org.example.islamicapp.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.feature.adhkar.domain.DhikrProgress
import org.junit.Test

/**
 * Content-integrity tests: every shipped dhikr must be sourced, non-empty and
 * sensibly counted (PROJECT_PROMPT.md §10 — religious-content standards).
 */
class AdhkarRepositoryTest {

    private val repository = AdhkarRepository()

    @Test
    fun `covers the core daily categories`() {
        val ids = repository.categories.map { it.id }
        assertThat(ids).containsAtLeast(
            "morning", "evening", "sleep", "waking", "after_prayer", "general",
        )
    }

    @Test
    fun `every category has items`() {
        repository.categories.forEach { category ->
            assertThat(category.items).isNotEmpty()
        }
    }

    @Test
    fun `every item is sourced and non-empty`() {
        repository.categories.flatMap { it.items }.forEach { item ->
            assertThat(item.text).isNotEmpty()
            assertThat(item.translationEn).isNotEmpty()
            assertThat(item.reference).isNotEmpty()
            assertThat(item.count).isAtLeast(1)
        }
    }

    @Test
    fun `item ids are unique across categories`() {
        val ids = repository.categories.flatMap { it.items.map { item -> item.id } }
        assertThat(ids.distinct()).hasSize(ids.size)
    }

    @Test
    fun `short reminders exist for the periodic notification`() {
        assertThat(repository.shortReminders).isNotEmpty()
        repository.shortReminders.forEach {
            assertThat(it.text.length).isAtMost(60)
        }
    }
}

/** Pure counter logic for the per-dhikr repetition tracker. */
class DhikrProgressTest {

    @Test
    fun `increments up to the prescribed count then stops`() {
        var progress = DhikrProgress.reset(listOf("a"))
        repeat(5) { progress = progress.increment("a", 3) }
        assertThat(progress.countOf("a")).isEqualTo(3)
    }

    @Test
    fun `category completes only when every item completes`() {
        val category = org.example.islamicapp.feature.adhkar.domain.DhikrCategory(
            id = "c", titleAr = "ت", titleEn = "T",
            items = listOf(
                org.example.islamicapp.feature.adhkar.domain.DhikrItem("a", "a", "a", 1, "s"),
                org.example.islamicapp.feature.adhkar.domain.DhikrItem("b", "b", "b", 2, "s"),
            ),
        )
        var progress = DhikrProgress.reset(listOf("a", "b"))
        assertThat(progress.categoryComplete(category)).isFalse()
        progress = progress.increment("a", 1).increment("b", 2).increment("b", 2)
        assertThat(progress.categoryComplete(category)).isTrue()
    }
}
