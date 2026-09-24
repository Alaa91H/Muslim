package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EthicsNewMuslimLearningContentTest {

    @Test
    fun `ethics and new Muslim courses cover the planned lesson set`() {
        assertThat(EthicsNewMuslimLearningContent.lessonIds).containsExactly(
            "ethics_foundation",
            "ethics_speech",
            "ethics_family",
            "ethics_neighbours",
            "ethics_conflict",
            "ethics_privacy",
            "ethics_work_digital",
            "new_muslim_welcome",
            "new_muslim_belief",
            "new_muslim_prayer",
            "new_muslim_purification",
            "new_muslim_quran",
            "new_muslim_daily_life",
            "new_muslim_roadmap",
        ).inOrder()
    }

    @Test
    fun `all lessons are wired into academy catalog and await scholarly review`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        EthicsNewMuslimLearningContent.lessons.forEach { lesson ->
            assertThat(academyById[lesson.id]).isEqualTo(lesson)
            assertThat(lesson.sections).isNotEmpty()
            assertThat(lesson.estimatedMinutes).isNotNull()
            assertThat(lesson.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `lesson and section identifiers remain unique`() {
        val lessons = EthicsNewMuslimLearningContent.lessons
        assertThat(lessons.map { it.id }.toSet()).hasSize(lessons.size)

        lessons.forEach { lesson ->
            val sectionIds = lesson.sections.map { it.id }
            assertThat(sectionIds.toSet()).hasSize(sectionIds.size)
        }
    }

    @Test
    fun `every evidence reference resolves inside its lesson`() {
        EthicsNewMuslimLearningContent.lessons.forEach { lesson ->
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
    fun `new Muslim roadmap is complete and multilingual`() {
        assertThat(NewMuslimRoadmapContent.stages.map { it.days })
            .containsExactly(7, 30, 90)
            .inOrder()

        NewMuslimRoadmapContent.stages.forEach { stage ->
            assertThat(stage.checklist).hasSize(4)
            BeginnerLanguage.entries.forEach { language ->
                assertThat(stage.title.resolve(language)).isNotEmpty()
                assertThat(stage.goal.resolve(language)).isNotEmpty()
                stage.checklist.forEach { item ->
                    assertThat(item.resolve(language)).isNotEmpty()
                }
            }
        }
    }

    @Test
    fun `new Muslim path reuses core academy courses`() {
        val path = LearningAcademyCatalog.paths.single { it.id == "new_muslim" }

        assertThat(path.courseIds).containsAtLeast(
            LearnContent.CATEGORY_NEW_MUSLIM,
            LearnContent.CATEGORY_FAITH,
            LearnContent.CATEGORY_TAHARA,
            LearnContent.CATEGORY_SALAH,
            LearnContent.CATEGORY_QURAN,
            LearnContent.CATEGORY_ETHICS,
        )
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
