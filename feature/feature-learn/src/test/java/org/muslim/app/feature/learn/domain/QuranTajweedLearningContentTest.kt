package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranTajweedLearningContentTest {

    @Test
    fun `Quran and Tajweed course covers the planned lesson set`() {
        assertThat(QuranTajweedLearningContent.lessonIds).containsExactly(
            "quran_intro",
            "quran_etiquette",
            "quran_structure",
            "quran_understanding",
            "quran_learning_plan",
            "tajweed_intro",
            "tajweed_makharij",
            "tajweed_noon_meem",
            "tajweed_madd",
            "tajweed_qalqalah",
            "tajweed_waqf",
            "tajweed_practice",
        ).inOrder()
    }

    @Test
    fun `Quran and Tajweed lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        QuranTajweedLearningContent.lessons.forEach { lesson ->
            assertThat(academyById[lesson.id]).isEqualTo(lesson)
            assertThat(lesson.sections).isNotEmpty()
            assertThat(lesson.estimatedMinutes).isNotNull()
            assertThat(lesson.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `lesson and section ids remain unique`() {
        val lessons = QuranTajweedLearningContent.lessons
        assertThat(lessons.map { it.id }.toSet()).hasSize(lessons.size)

        lessons.forEach { lesson ->
            val sectionIds = lesson.sections.map { it.id }
            assertThat(sectionIds.toSet()).hasSize(sectionIds.size)
        }
    }

    @Test
    fun `every Quran evidence reference resolves inside its lesson`() {
        QuranTajweedLearningContent.lessons.forEach { lesson ->
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
    fun `core Quran and Tajweed lessons are long form`() {
        val byId = QuranTajweedLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("quran_intro").sections.size).isAtLeast(4)
        assertThat(byId.getValue("quran_understanding").sections.size).isAtLeast(4)
        assertThat(byId.getValue("tajweed_intro").sections.size).isAtLeast(4)
        assertThat(byId.getValue("tajweed_noon_meem").sections.size).isAtLeast(4)
        assertThat(byId.getValue("tajweed_practice").sections.size).isAtLeast(4)
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
