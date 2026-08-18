package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchHistoryTest {

    @Test
    fun `record adds the newest query first`() {
        assertThat(SearchHistory.record(emptyList(), "نور"))
            .containsExactly("نور").inOrder()
        assertThat(SearchHistory.record(listOf("نور"), "رحمة"))
            .containsExactly("رحمة", "نور").inOrder()
    }

    @Test
    fun `record deduplicates and moves the repeat to the front`() {
        val result = SearchHistory.record(listOf("رحمة", "نور", "الله"), "نور")
        assertThat(result).containsExactly("نور", "رحمة", "الله").inOrder()
    }

    @Test
    fun `record caps the history at the maximum size`() {
        val full = (1..SearchHistory.MAX_ENTRIES).map { "كلمة$it" }
        val result = SearchHistory.record(full, "جديد")
        assertThat(result).hasSize(SearchHistory.MAX_ENTRIES)
        assertThat(result.first()).isEqualTo("جديد")
        // The seed list is newest-first, so "كلمة10" is the oldest and gets dropped.
        assertThat(result).doesNotContain("كلمة10")
        assertThat(result.last()).isEqualTo("كلمة9")
    }

    @Test
    fun `record ignores blank queries and trims surrounding whitespace`() {
        assertThat(SearchHistory.record(listOf("نور"), "   ")).containsExactly("نور")
        assertThat(SearchHistory.record(emptyList(), "  رحمة  ")).containsExactly("رحمة")
    }

    @Test
    fun `encode and decode round-trip without losing or reordering entries`() {
        val history = listOf("الله", "رحمة", "نور")
        assertThat(SearchHistory.decode(SearchHistory.encode(history)))
            .containsExactlyElementsIn(history).inOrder()
    }

    @Test
    fun `decode handles null and blank stored values`() {
        assertThat(SearchHistory.decode(null)).isEmpty()
        assertThat(SearchHistory.decode("")).isEmpty()
        assertThat(SearchHistory.decode("   ")).isEmpty()
    }
}
