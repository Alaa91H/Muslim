package org.muslim.app.feature.learn.domain

internal object ZakatLearningContent {
    val lessons: List<LearningLesson> =
        ZakatCoreContent.lessons +
            ZakatAssetsContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
