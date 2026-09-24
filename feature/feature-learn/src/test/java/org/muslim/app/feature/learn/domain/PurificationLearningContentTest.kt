package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PurificationLearningContentTest {

    @Test
    fun `purification course covers the complete planned foundation set`() {
        assertThat(PurificationLearningContent.lessonIds).containsExactly(
            "tahara_intro",
            "water_impurity",
            "restroom_etiquette",
            "wudu",
            "wudu_nullifiers",
            "wiping_footwear",
            "ghusl",
            "tayammum",
            "menstruation_postpartum",
            "excused_person",
        )
    }

    @Test
    fun `expanded purification lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        PurificationLearningContent.lessons.forEach { expanded ->
            assertThat(academyById[expanded.id]).isEqualTo(expanded)
            assertThat(expanded.sections).isNotEmpty()
            assertThat(expanded.estimatedMinutes).isNotNull()
            assertThat(expanded.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `every purification evidence reference resolves inside its lesson`() {
        PurificationLearningContent.lessons.forEach { lesson ->
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
    fun `core purification lessons are genuinely long form`() {
        val byId = PurificationLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("wudu").sections.size).isAtLeast(6)
        assertThat(byId.getValue("ghusl").sections.size).isAtLeast(5)
        assertThat(byId.getValue("tayammum").sections.size).isAtLeast(5)
        assertThat(byId.getValue("menstruation_postpartum").sections.size).isAtLeast(5)
    }

    private fun referenceIdsFor(block: LearningContentBlock): List<String> = when (block) {
        is LearningContentBlock.Evidence -> block.referenceIds
        is LearningContentBlock.Comparison -> block.items.flatMap { it.referenceIds }
        is LearningContentBlock.QuestionAnswer -> block.referenceIds
        is LearningContentBlock.Quiz -> block.referenceIds
        is LearningContentBlock.Paragraph,
        is LearningContentBlock.Steps,
        is LearningContentBlock.Callout,
        -> emptyList()
    }
}
