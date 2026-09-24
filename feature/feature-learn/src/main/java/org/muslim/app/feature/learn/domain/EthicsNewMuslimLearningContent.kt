package org.muslim.app.feature.learn.domain

internal object EthicsNewMuslimLearningContent {
    val lessons: List<LearningLesson> =
        EthicsDailyLifeContent.lessons +
            NewMuslimLearningContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
