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
}
