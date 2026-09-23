package org.muslim.app.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DuasRepositoryTest {

    @Test
    fun `extended duas are complete sourced and structurally valid`() {
        val duas = DuasRepository().allDuas

        assertThat(duas).isNotEmpty()
        assertThat(duas.map { it.id }).containsNoDuplicates()

        duas.forEach { dua ->
            assertThat(dua.arabic.trim()).isNotEmpty()
            assertThat(dua.source.trim()).isNotEmpty()
            assertThat(dua.repetition).isGreaterThan(0)
            assertThat(dua.arabic).doesNotContain("...")
            assertThat(dua.arabic).doesNotContain("…")
            assertThat(dua.translation).doesNotContain("...")
            assertThat(dua.translation).doesNotContain("…")
        }
    }

    @Test
    fun `extended dua ids remain stable across repository instances`() {
        val first = DuasRepository().allDuas.map { it.id }
        val second = DuasRepository().allDuas.map { it.id }

        assertThat(second).containsExactlyElementsIn(first).inOrder()
    }
}
