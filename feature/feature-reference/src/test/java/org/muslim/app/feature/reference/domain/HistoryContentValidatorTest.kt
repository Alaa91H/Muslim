package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HistoryContentValidatorTest {
    @Test
    fun `current long form catalogue is structurally valid`() {
        assertThat(HistoryContentValidator.validate()).isEmpty()
    }

    @Test
    fun `every timeline era has a long form article`() {
        val articleEraIds = IslamicHistoryArticles.articles.map { it.eraId }

        assertThat(articleEraIds)
            .containsExactlyElementsIn(IslamicHistoryContent.timeline.map { it.id })
        assertThat(articleEraIds).containsNoDuplicates()
    }

    @Test
    fun `long form articles contain bilingual sections and sources`() {
        IslamicHistoryArticles.articles.forEach { article ->
            assertThat(article.sections.size).isAtLeast(3)
            assertThat(article.sourceIds).isNotEmpty()
            article.sections.forEach { section ->
                assertThat(section.paragraphs).isNotEmpty()
                section.paragraphs.forEach { paragraph ->
                    assertThat(paragraph.arabic).isNotEmpty()
                    assertThat(paragraph.english).isNotEmpty()
                }
            }
        }
    }

    @Test
    fun `validator rejects an unknown source reference`() {
        val brokenArticle = IslamicHistoryArticles.articles.first().copy(
            sourceIds = listOf("missing-source"),
        )

        val errors = HistoryContentValidator.validate(
            articles = listOf(brokenArticle),
        )

        assertThat(errors.joinToString("\n")).contains("unknown source")
    }
    @Test
    fun `states catalogue is chronological bilingual and source linked`() {
        val states = IslamicHistoryStates.states

        assertThat(states).hasSize(18)
        assertThat(states.map { it.id }).containsNoDuplicates()
        assertThat(states.mapNotNull { it.period.startCe }).isInOrder()
        states.forEach { state ->
            assertThat(state.title.arabic).isNotEmpty()
            assertThat(state.title.english).isNotEmpty()
            assertThat(state.summary.arabic).isNotEmpty()
            assertThat(state.summary.english).isNotEmpty()
            assertThat(state.sourceIds).isNotEmpty()
        }
    }

    @Test
    fun `states preserve overlapping regional histories`() {
        val year1250 = IslamicHistoryStates.states.filter { state ->
            val start = state.period.startCe ?: return@filter false
            val end = state.period.endCe ?: return@filter false
            1250 in start..end
        }

        assertThat(year1250.map { it.id }).containsAtLeast(
            "abbasid_caliphate",
            "seljuqs_rum",
            "almohads",
            "ayyubids",
            "nasrids",
            "mamluks",
        )
    }

    @Test
    fun `shared history source registry has unique ids`() {
        assertThat(IslamicHistorySources.all.map { it.id }).containsNoDuplicates()
        assertThat(IslamicHistorySources.byId("met_major_dynasties")).isNotNull()
    }

}
