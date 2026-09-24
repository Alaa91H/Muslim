package org.muslim.app.feature.learn.domain

internal object FastingLearningContent {
    val lessons: List<LearningLesson> =
        FastingCoreContent.lessons +
            FastingRamadanContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
