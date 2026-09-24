package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FaithAqidahLearningContentTest {

    @Test
    fun `faith course covers the planned lesson set`() {
        assertThat(FaithAqidahLearningContent.lessons.map { it.id }).containsExactly(
            "pillars_islam",
            "pillars_iman",
            "faith_tawhid_worship",
            "faith_knowing_allah",
            "faith_angels",
            "faith_books",
            "faith_messengers",
            "faith_last_day",
            "faith_qadar",
            "faith_questions",
        ).inOrder()
    }

    @Test
    fun `legacy faith ids are replaced by long form academy lessons`() {
        val byId = LearningAcademyCatalog.lessons.associateBy { it.id }

        assertThat(byId.getValue("pillars_islam").sections.size).isAtLeast(4)
        assertThat(byId.getValue("pillars_iman").sections.size).isAtLeast(4)
        assertThat(byId.getValue("pillars_islam"))
            .isEqualTo(FaithAqidahLearningContent.lessons.first { it.id == "pillars_islam" })
        assertThat(byId.getValue("pillars_iman"))
            .isEqualTo(FaithAqidahLearningContent.lessons.first { it.id == "pillars_iman" })
    }

    @Test
    fun `all faith lessons remain pending scholarly review`() {
        FaithAqidahLearningContent.lessons.forEach { lesson ->
            assertThat(lesson.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
            assertThat(lesson.sections).isNotEmpty()
            assertThat(lesson.estimatedMinutes).isNotNull()
        }
    }

    @Test
    fun `faith references resolve inside each lesson`() {
        FaithAqidahLearningContent.lessons.forEach { lesson ->
            val referenceIds = lesson.references.map { it.id }.toSet()
            assertThat(referenceIds).hasSize(lesson.references.size)

            lesson.sections
                .flatMap { it.blocks }
                .flatMap(::referenceIdsFor)
                .forEach { referenceId ->
                    assertThat(referenceIds).contains(referenceId)
                }
        }
    }

    @Test
    fun `faith path is available as a dedicated academy path`() {
        val path = LearningAcademyCatalog.paths.single { it.id == "faith" }

        assertThat(path.courseIds)
            .containsExactly(LearnContent.CATEGORY_FAITH)
    }

    @Test
    fun `sensitive faith lessons include explicit boundaries`() {
        val byId = FaithAqidahLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("faith_tawhid_worship").sections.size).isAtLeast(4)
        assertThat(byId.getValue("faith_knowing_allah").sections.size).isAtLeast(4)
        assertThat(byId.getValue("faith_qadar").sections.size).isAtLeast(4)
        assertThat(byId.getValue("faith_questions").sections.size).isAtLeast(4)
    }

    private fun referenceIdsFor(block: LearningContentBlock): List<String> = when (block) {
        is LearningContentBlock.Evidence -> block.referenceIds
        is LearningContentBlock.Comparison -> block.items.flatMap { it.referenceIds }
        is LearningContentBlock.QuestionAnswer -> block.referenceIds
        is LearningContentBlock.Quiz -> block.referenceIds
        is LearningContentBlock.Paragraph,
        is LearningContentBlock.Steps,
        is LearningContentBlock.Callout -> emptyList()
    }
}
