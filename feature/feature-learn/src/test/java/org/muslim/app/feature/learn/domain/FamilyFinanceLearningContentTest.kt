package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyFinanceLearningContentTest {

    @Test
    fun `family and finance courses cover the planned lesson set`() {
        assertThat(FamilyFinanceLearningContent.lessonIds).containsExactly(
            "family_intro",
            "family_spouse_selection",
            "family_marriage_contract",
            "family_marital_life",
            "family_parenting",
            "family_kinship",
            "family_conflict_separation",
            "finance_intro",
            "finance_sale_contracts",
            "finance_riba",
            "finance_debt_loans",
            "finance_ecommerce",
            "finance_business_investing",
            "finance_tools_guide",
        ).inOrder()
    }

    @Test
    fun `all family and finance lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        FamilyFinanceLearningContent.lessons.forEach { lesson ->
            assertThat(academyById[lesson.id]).isEqualTo(lesson)
            assertThat(lesson.sections).isNotEmpty()
            assertThat(lesson.estimatedMinutes).isNotNull()
            assertThat(lesson.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `family lessons link to Family Life and finance lessons link to Finance`() {
        FamilySocialLearningContent.lessons.forEach { lesson ->
            assertThat(lesson.featureLink).isNotNull()
            assertThat(lesson.featureLink?.destination)
                .isEqualTo(LearningFeatureDestination.FAMILY_LIFE)
        }

        FinanceTransactionsLearningContent.lessons.forEach { lesson ->
            assertThat(lesson.featureLink).isNotNull()
            assertThat(lesson.featureLink?.destination)
                .isEqualTo(LearningFeatureDestination.FINANCE)
        }
    }

    @Test
    fun `lesson section and evidence identifiers remain valid`() {
        FamilyFinanceLearningContent.lessons.forEach { lesson ->
            val sectionIds = lesson.sections.map { it.id }
            assertThat(sectionIds.toSet()).hasSize(sectionIds.size)

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
    fun `family and finance paths reuse related academy courses`() {
        val familyPath = LearningAcademyCatalog.paths.single { it.id == "family" }
        val financePath = LearningAcademyCatalog.paths.single { it.id == "finance" }

        assertThat(familyPath.courseIds).containsAtLeast(
            LearnContent.CATEGORY_FAMILY,
            LearnContent.CATEGORY_ETHICS,
        )
        assertThat(financePath.courseIds).containsAtLeast(
            LearnContent.CATEGORY_FINANCE,
            LearnContent.CATEGORY_IBADAH,
        )
    }

    @Test
    fun `high risk family and finance lessons are long form`() {
        val byId = FamilyFinanceLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("family_marriage_contract").sections.size).isAtLeast(4)
        assertThat(byId.getValue("family_conflict_separation").sections.size).isAtLeast(4)
        assertThat(byId.getValue("finance_riba").sections.size).isAtLeast(4)
        assertThat(byId.getValue("finance_debt_loans").sections.size).isAtLeast(4)
        assertThat(byId.getValue("finance_business_investing").sections.size).isAtLeast(4)
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
