package org.muslim.app.feature.learn.domain

internal object PrayerLearningContent {
    val lessons: List<LearningLesson> =
        PrayerFoundationsContent.lessons +
            PrayerPracticeContent.lessons +
            PrayerSituationsContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
