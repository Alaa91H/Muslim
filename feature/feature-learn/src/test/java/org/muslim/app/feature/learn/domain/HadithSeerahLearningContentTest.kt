package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HadithSeerahLearningContentTest {

    @Test
    fun `Hadith and Seerah courses cover the planned lesson set`() {
        assertThat(HadithSeerahLearningContent.lessonIds).containsExactly(
            "sunnah_intro",
            "hadith_anatomy",
            "hadith_grades",
            "hadith_verification",
            "hadith_understanding",
            "hadith_library_guide",
            "seerah_method",
            "seerah_early_life",
            "seerah_revelation_makkah",
            "seerah_hijrah",
            "seerah_madinah",
            "seerah_major_events",
            "seerah_character_legacy",
        ).inOrder()
    }

    @Test
    fun `Hadith and Seerah lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        HadithSeerahLearningContent.lessons.forEach { lesson ->
            assertThat(academyById[lesson.id]).isEqualTo(lesson)
            assertThat(lesson.sections).isNotEmpty()
            assertThat(lesson.estimatedMinutes).isNotNull()
            assertThat(lesson.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `lesson and section ids remain unique`() {
        val lessons = HadithSeerahLearningContent.lessons
        assertThat(lessons.map { it.id }.toSet()).hasSize(lessons.size)

        lessons.forEach { lesson ->
            val sectionIds = lesson.sections.map { it.id }
            assertThat(sectionIds.toSet()).hasSize(sectionIds.size)
        }
    }

    @Test
    fun `every evidence reference resolves inside its lesson`() {
        HadithSeerahLearningContent.lessons.forEach { lesson ->
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
    fun `core methodology and Seerah lessons are long form`() {
        val byId = HadithSeerahLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("sunnah_intro").sections.size).isAtLeast(4)
        assertThat(byId.getValue("hadith_grades").sections.size).isAtLeast(4)
        assertThat(byId.getValue("hadith_understanding").sections.size).isAtLeast(4)
        assertThat(byId.getValue("seerah_method").sections.size).isAtLeast(4)
        assertThat(byId.getValue("seerah_major_events").sections.size).isAtLeast(5)
        assertThat(byId.getValue("seerah_character_legacy").sections.size).isAtLeast(4)
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
