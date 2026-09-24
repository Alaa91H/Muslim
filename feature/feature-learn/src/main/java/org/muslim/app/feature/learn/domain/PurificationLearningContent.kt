package org.muslim.app.feature.learn.domain

internal object PurificationLearningContent {
    val lessons: List<LearningLesson> =
        PurificationBasicsContent.lessons +
            PurificationAblutionContent.lessons +
            PurificationMajorContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
