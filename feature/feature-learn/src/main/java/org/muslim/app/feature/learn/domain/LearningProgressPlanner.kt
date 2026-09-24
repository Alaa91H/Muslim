package org.muslim.app.feature.learn.domain

data class LearningProgressSummary(
    val completed: Int,
    val total: Int,
) {
    val fraction: Float
        get() = if (total == 0) 0f else completed.toFloat() / total.toFloat()
}

object LearningProgressPlanner {

    fun continueLessonId(
        lastOpenedLessonId: String?,
        completedLessonIds: Set<String>,
        topics: List<LearnTopic> = LearnContent.topics,
    ): String? {
        if (topics.isEmpty()) return null

        val lastIndex = lastOpenedLessonId
            ?.let { id -> topics.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }

        if (lastIndex != null && topics[lastIndex].id !in completedLessonIds) {
            return topics[lastIndex].id
        }

        if (lastIndex != null) {
            topics.drop(lastIndex + 1)
                .firstOrNull { it.id !in completedLessonIds }
                ?.let { return it.id }
        }

        return topics.firstOrNull { it.id !in completedLessonIds }?.id
            ?: lastOpenedLessonId
            ?: topics.first().id
    }

    fun categorySummary(
        category: String,
        completedLessonIds: Set<String>,
        topics: List<LearnTopic> = LearnContent.topics,
    ): LearningProgressSummary {
        val categoryTopics = topics.filter { it.category == category }
        return LearningProgressSummary(
            completed = categoryTopics.count { it.id in completedLessonIds },
            total = categoryTopics.size,
        )
    }

    fun overallSummary(
        completedLessonIds: Set<String>,
        topics: List<LearnTopic> = LearnContent.topics,
    ): LearningProgressSummary = LearningProgressSummary(
        completed = topics.count { it.id in completedLessonIds },
        total = topics.size,
    )
}
