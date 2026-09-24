package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZakatLearningContentTest {

    @Test
    fun `zakat course covers the complete planned lesson set`() {
        assertThat(ZakatLearningContent.lessonIds).containsExactly(
            "zakat",
            "zakat_nisab_haul",
            "zakat_recipients",
            "zakat_fitr",
            "zakat_cash_metals",
            "zakat_business_investments",
            "zakat_debts_jewelry",
            "zakat_crops_livestock",
            "zakat_calculator_guide",
        )
    }

    @Test
    fun `expanded zakat lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        ZakatLearningContent.lessons.forEach { expanded ->
            assertThat(academyById[expanded.id]).isEqualTo(expanded)
            assertThat(expanded.sections).isNotEmpty()
            assertThat(expanded.estimatedMinutes).isNotNull()
            assertThat(expanded.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `every zakat evidence reference resolves inside its lesson`() {
        ZakatLearningContent.lessons.forEach { lesson ->
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
    fun `core zakat lessons are genuinely long form`() {
        val byId = ZakatLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("zakat").sections.size).isAtLeast(5)
        assertThat(byId.getValue("zakat_nisab_haul").sections.size).isAtLeast(5)
        assertThat(byId.getValue("zakat_crops_livestock").sections.size).isAtLeast(4)
        assertThat(byId.getValue("zakat_calculator_guide").sections.size).isAtLeast(5)
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
