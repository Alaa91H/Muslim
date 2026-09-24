package org.muslim.app.feature.learn.domain

object LearningQuizKey {
    private const val SEPARATOR = "::"

    fun of(lessonId: String, quizId: String): String =
        lessonId + SEPARATOR + quizId
}
