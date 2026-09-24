package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LearningAcademyTest {

    @Test
    fun `legacy topics are all represented by academy lessons with stable ids`() {
        val legacyIds = LearnContent.topics.map { it.id }
        val academyIds = LearningAcademyCatalog.lessons.map { it.id }

        assertThat(academyIds).containsExactlyElementsIn(legacyIds)
        assertThat(academyIds.distinct()).hasSize(academyIds.size)
    }

    @Test
    fun `academy hierarchy references only existing children`() {
        val courseIds = LearningAcademyCatalog.courses.map { it.id }.toSet()
        val moduleIds = LearningAcademyCatalog.modules.map { it.id }.toSet()
        val lessonIds = LearningAcademyCatalog.lessons.map { it.id }.toSet()

        LearningAcademyCatalog.paths.forEach { path ->
            assertThat(path.id).isNotEmpty()
            assertThat(path.courseIds).isNotEmpty()
            path.courseIds.forEach { courseId ->
                assertThat(courseIds).contains(courseId)
            }
        }

        LearningAcademyCatalog.courses.forEach { course ->
            assertThat(course.id).isNotEmpty()
            assertThat(course.moduleIds).isNotEmpty()
            course.moduleIds.forEach { moduleId ->
                assertThat(moduleIds).contains(moduleId)
            }
        }

        LearningAcademyCatalog.modules.forEach { module ->
            assertThat(module.id).isNotEmpty()
            assertThat(module.lessonIds).isNotEmpty()
            module.lessonIds.forEach { lessonId ->
                assertThat(lessonIds).contains(lessonId)
            }
        }
    }

    @Test
    fun `migrated lessons contain non empty structured content`() {
        LearningAcademyCatalog.lessons.forEach { lesson ->
            assertThat(lesson.id).isNotEmpty()
            assertThat(lesson.titleRes).isGreaterThan(0)
            assertThat(lesson.subtitleRes).isGreaterThan(0)
            assertThat(lesson.contentVersion).isGreaterThan(0)
            assertThat(lesson.sections).isNotEmpty()

            lesson.sections.forEach { section ->
                assertThat(section.id).isNotEmpty()
                assertThat(section.title).isNotEmpty()
                assertThat(section.blocks).isNotEmpty()

                section.blocks.forEach { block ->
                    when (block) {
                        is LearningContentBlock.Paragraph ->
                            assertThat(block.text).isNotEmpty()

                        is LearningContentBlock.Steps -> {
                            assertThat(block.items).isNotEmpty()
                            block.items.forEach { item ->
                                assertThat(item.title).isNotEmpty()
                                assertThat(item.body).isNotEmpty()
                            }
                        }

                        is LearningContentBlock.Callout ->
                            assertThat(block.body).isNotEmpty()

                        is LearningContentBlock.Evidence -> {
                            assertThat(block.text).isNotEmpty()
                            assertThat(block.referenceIds).isNotEmpty()
                        }

                        is LearningContentBlock.Comparison -> {
                            assertThat(block.items).isNotEmpty()
                            block.items.forEach { item ->
                                assertThat(item.label).isNotEmpty()
                                assertThat(item.body).isNotEmpty()
                            }
                        }

                        is LearningContentBlock.QuestionAnswer -> {
                            assertThat(block.question).isNotEmpty()
                            assertThat(block.answer).isNotEmpty()
                        }

                        is LearningContentBlock.Quiz -> {
                            assertThat(block.question).isNotEmpty()
                            assertThat(block.options).hasSizeAtLeast(2)
                            assertThat(block.options.map { it.id }).contains(block.correctOptionId)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `all migrated religious content requires scholarly review by default`() {
        LearningAcademyCatalog.lessons.forEach { lesson ->
            assertThat(lesson.reviewStatus).isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }
}
