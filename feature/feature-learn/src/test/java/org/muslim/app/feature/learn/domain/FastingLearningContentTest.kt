package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FastingLearningContentTest {

    @Test
    fun `fasting course covers the complete planned lesson set`() {
        assertThat(FastingLearningContent.lessonIds).containsExactly(
            "fasting",
            "fasting_day",
            "fasting_nullifiers",
            "fasting_exemptions",
            "fasting_women",
            "fasting_travel_illness",
            "ramadan_sunnahs",
            "laylat_qadr_itikaf",
            "voluntary_fasting",
        )
    }

    @Test
    fun `expanded fasting lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        FastingLearningContent.lessons.forEach { expanded ->
            assertThat(academyById[expanded.id]).isEqualTo(expanded)
            assertThat(expanded.sections).isNotEmpty()
            assertThat(expanded.estimatedMinutes).isNotNull()
            assertThat(expanded.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `every fasting evidence reference resolves inside its lesson`() {
        FastingLearningContent.lessons.forEach { lesson ->
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
    fun `core fasting lessons are genuinely long form`() {
        val byId = FastingLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("fasting").sections.size).isAtLeast(5)
        assertThat(byId.getValue("fasting_nullifiers").sections.size).isAtLeast(5)
        assertThat(byId.getValue("fasting_exemptions").sections.size).isAtLeast(4)
        assertThat(byId.getValue("fasting_women").sections.size).isAtLeast(4)
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
