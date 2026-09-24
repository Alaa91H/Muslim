package org.muslim.app.feature.learn.domain

internal object QuranTajweedLearningContent {
    val lessons: List<LearningLesson> =
        QuranFoundationsContent.lessons +
            TajweedCourseContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
