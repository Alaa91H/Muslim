package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LearningProgressPlannerTest {

    @Test
    fun `continue learning keeps the last opened lesson when unfinished`() {
        val lessonId = LearnContent.topics.first().id

        assertThat(
            LearningProgressPlanner.continueLessonId(
                lastOpenedLessonId = lessonId,
                completedLessonIds = emptySet(),
            ),
        ).isEqualTo(lessonId)
    }

    @Test
    fun `continue learning advances after a completed lesson`() {
        val first = LearnContent.topics[0]
        val second = LearnContent.topics[1]

        assertThat(
            LearningProgressPlanner.continueLessonId(
                lastOpenedLessonId = first.id,
                completedLessonIds = setOf(first.id),
            ),
        ).isEqualTo(second.id)
    }

    @Test
    fun `overall summary counts only known completed lessons`() {
        val known = LearnContent.topics.take(3).map { it.id }.toSet()
        val summary = LearningProgressPlanner.overallSummary(
            completedLessonIds = known + "unknown_lesson",
        )

        assertThat(summary.completed).isEqualTo(3)
        assertThat(summary.total).isEqualTo(LearnContent.topics.size)
    }

    @Test
    fun `category summary stays scoped to its category`() {
        val faithIds = LearnContent.topics
            .filter { it.category == LearnContent.CATEGORY_FAITH }
            .take(2)
            .map { it.id }
            .toSet()

        val summary = LearningProgressPlanner.categorySummary(
            category = LearnContent.CATEGORY_FAITH,
            completedLessonIds = faithIds,
        )

        assertThat(summary.completed).isEqualTo(faithIds.size)
        assertThat(summary.total).isEqualTo(
            LearnContent.topics.count { it.category == LearnContent.CATEGORY_FAITH },
        )
    }
}
