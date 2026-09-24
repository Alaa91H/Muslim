package org.muslim.app.feature.learn.domain

internal object HadithSeerahLearningContent {
    val lessons: List<LearningLesson> =
        HadithSunnahContent.lessons +
            SeerahLearningContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
