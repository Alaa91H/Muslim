package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IslamicHistorySearchTest {
    @Test
    fun `blank query returns no results`() {
        assertThat(IslamicHistorySearch.search("   ")).isEmpty()
    }

    @Test
    fun `arabic normalization handles diacritics and letter variants`() {
        val personResults = IslamicHistorySearch.search("الخُوارِزمي")
        val placeResults = IslamicHistorySearch.search("غرناطه")

        assertThat(personResults.map { it.id }).contains("al_khwarizmi")
        assertThat(placeResults.map { it.id }).contains("nasrids")
    }

    @Test
    fun `english search spans multiple entity kinds`() {
        assertThat(IslamicHistorySearch.search("Baghdad").map { it.id })
            .containsAtLeast("baghdad", "baghdad_founded_762")
        assertThat(IslamicHistorySearch.search("waqf").map { it.id }).contains("waqf")
        assertThat(IslamicHistorySearch.search("Mughal").map { it.id }).contains("mughals")
    }

    @Test
    fun `type filter restricts results to requested kind`() {
        val results = IslamicHistorySearch.search(
            query = "medicine",
            type = HistorySearchType.Person,
        )

        assertThat(results).isNotEmpty()
        assertThat(results.all { it.type == HistorySearchType.Person }).isTrue()
    }

    @Test
    fun `title matches rank before body only matches`() {
        val results = IslamicHistorySearch.search("Baghdad")

        assertThat(results).isNotEmpty()
        assertThat(results.first().title.english.lowercase()).contains("baghdad")
    }
}
