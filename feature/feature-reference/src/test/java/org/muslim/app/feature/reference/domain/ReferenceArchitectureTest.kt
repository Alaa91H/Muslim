package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReferenceArchitectureTest {

    @Test
    fun `Arabic search normalizes hamza and diacritics`() {
        val islam = ReferenceLibrary.byId("islam")!!

        val plain = ReferenceLibrary.search(islam, "الايمان", RefLang.Arabic)
        val vocalized = ReferenceLibrary.search(islam, "الإِيمَان", RefLang.Arabic)

        assertThat(plain.map { it.id }).contains("pillars_faith")
        assertThat(vocalized.map { it.id }).contains("pillars_faith")
    }

    @Test
    fun `global search can discover topics outside the current book`() {
        val results = ReferenceLibrary.searchAll("الهجرة", RefLang.Arabic)

        assertThat(results).isNotEmpty()
        assertThat(results.map { it.book.id }).contains("sira")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `global search rejects non positive result limits`() {
        ReferenceLibrary.searchAll("الإسلام", RefLang.Arabic, limit = 0)
    }

    @Test
    fun `validator reports dangling citation references`() {
        val topic = reviewedTopic(
            paragraphCitationIds = listOf("missing"),
        )
        val book = sampleBook(topic)

        val issues = ReferenceContentValidator.validate(listOf(book))

        assertThat(issues.map { it.code }).contains("missing_citation")
    }

    @Test
    fun `validator accepts a reviewed sourced article`() {
        val topic = reviewedTopic(
            paragraphCitationIds = listOf("quran-3-19"),
        )
        val book = sampleBook(topic)

        val issues = ReferenceContentValidator.validate(listOf(book))

        assertThat(issues).isEmpty()
    }

    private fun sampleBook(topic: RefTopic) = ReferenceBook(
        id = "sample",
        titleAr = "كتاب",
        titleEn = "Book",
        subtitleAr = "وصف",
        subtitleEn = "Description",
        topics = listOf(topic),
        chapters = listOf(
            RefChapter(
                id = "basics",
                titleAr = "الأساسيات",
                titleEn = "Basics",
                topicIds = listOf(topic.id),
            ),
        ),
        contentRevision = 2,
    )

    private fun reviewedTopic(paragraphCitationIds: List<String>) = RefTopic(
        id = "faith",
        titleAr = "الإيمان",
        titleEn = "Faith",
        summaryAr = "ملخص موثق",
        summaryEn = "A sourced summary",
        sections = listOf(
            RefSection(
                id = "definition",
                titleAr = "التعريف",
                titleEn = "Definition",
                paragraphs = listOf(
                    RefParagraph(
                        ar = "نص عربي",
                        en = "English text",
                        citationIds = paragraphCitationIds,
                    ),
                ),
            ),
        ),
        citations = listOf(
            ReferenceCitation(
                id = "quran-3-19",
                kind = ReferenceSourceKind.Quran,
                titleAr = "القرآن الكريم",
                titleEn = "The Noble Quran",
                locator = "3:19",
            ),
        ),
        reviewStatus = ReferenceReviewStatus.Reviewed,
        lastReviewed = "2026-09-23",
    )
}
