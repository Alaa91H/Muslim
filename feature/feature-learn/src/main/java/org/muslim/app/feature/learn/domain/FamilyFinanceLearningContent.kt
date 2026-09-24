package org.muslim.app.feature.learn.domain

internal object FamilyFinanceLearningContent {
    val lessons: List<LearningLesson> =
        FamilySocialLearningContent.lessons +
            FinanceTransactionsLearningContent.lessons

    val lessonIds: Set<String> = lessons.mapTo(linkedSetOf()) { it.id }
}
