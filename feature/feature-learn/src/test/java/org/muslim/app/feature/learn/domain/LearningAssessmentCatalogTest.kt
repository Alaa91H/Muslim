package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LearningAssessmentCatalogTest {

    @Test
    fun `assessment keys and option ids are stable and unique`() {
        val keys = LearningAssessmentCatalog.entries.map { entry ->
            LearningQuizKey.of(entry.lessonId, entry.quiz.id)
        }

        assertThat(keys.toSet()).hasSize(keys.size)

        LearningAssessmentCatalog.entries.forEach { entry ->
            val optionIds = entry.quiz.options.map { it.id }
            assertThat(optionIds.toSet()).hasSize(optionIds.size)
            assertThat(optionIds).contains(entry.quiz.correctOptionId)
            assertThat(entry.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `every assessment points to an academy lesson`() {
        val lessonIds = LearningAcademyCatalog.lessons.map { it.id }.toSet()

        LearningAssessmentCatalog.entries.forEach { entry ->
            assertThat(lessonIds).contains(entry.lessonId)
        }
    }

    @Test
    fun `incorrect entries disappear after selecting the correct answer`() {
        val entry = LearningAssessmentCatalog.entries.first()
        val wrongOption = entry.quiz.options.first { it.id != entry.quiz.correctOptionId }
        val key = LearningQuizKey.of(entry.lessonId, entry.quiz.id)

        assertThat(
            LearningAssessmentCatalog.incorrectEntries(mapOf(key to wrongOption.id)),
        ).containsExactly(entry)

        assertThat(
            LearningAssessmentCatalog.incorrectEntries(
                mapOf(key to entry.quiz.correctOptionId),
            ),
        ).isEmpty()
    }
}
