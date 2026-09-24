package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.learn.domain.LearningQuizKey
import javax.inject.Inject
import javax.inject.Singleton

private val Context.learnPrefsDataStore by preferencesDataStore(name = "learn_prefs")

/**
 * Persists learning-hub state.
 *
 * Topic ids remain the stable persistence key so favourites and progress
 * survive content-model migrations. Quiz records use stable lesson/quiz ids
 * rather than list positions.
 */
@Singleton
class LearnPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val favoriteIds: Flow<Set<String>> =
        context.learnPrefsDataStore.data.map { it[Keys.FAVORITES] ?: emptySet() }

    val completedLessonIds: Flow<Set<String>> =
        context.learnPrefsDataStore.data.map { it[Keys.COMPLETED_LESSONS] ?: emptySet() }

    val lastOpenedLessonId: Flow<String?> =
        context.learnPrefsDataStore.data.map { it[Keys.LAST_OPENED_LESSON] }

    val quizAnswers: Flow<Map<String, String>> =
        context.learnPrefsDataStore.data.map { prefs ->
            prefs[Keys.QUIZ_ANSWERS]
                .orEmpty()
                .mapNotNull(QuizAnswerRecord::decode)
                .associate { it.quizKey to it.optionId }
        }

    suspend fun setFavorite(id: String, favorite: Boolean) {
        context.learnPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (favorite) current.add(id) else current.remove(id)
            prefs[Keys.FAVORITES] = current
        }
    }

    suspend fun setLessonCompleted(id: String, completed: Boolean) {
        context.learnPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_LESSONS]?.toMutableSet() ?: mutableSetOf()
            if (completed) current.add(id) else current.remove(id)
            prefs[Keys.COMPLETED_LESSONS] = current
        }
    }

    suspend fun setLastOpenedLesson(id: String) {
        context.learnPrefsDataStore.edit { prefs ->
            prefs[Keys.LAST_OPENED_LESSON] = id
        }
    }

    suspend fun setQuizAnswer(
        lessonId: String,
        quizId: String,
        optionId: String,
    ) {
        val quizKey = LearningQuizKey.of(lessonId, quizId)
        context.learnPrefsDataStore.edit { prefs ->
            val records = prefs[Keys.QUIZ_ANSWERS]?.toMutableSet() ?: mutableSetOf()
            records.removeAll { QuizAnswerRecord.decode(it)?.quizKey == quizKey }
            records.add(QuizAnswerRecord(quizKey, optionId).encode())
            prefs[Keys.QUIZ_ANSWERS] = records
        }
    }

    private data class QuizAnswerRecord(
        val quizKey: String,
        val optionId: String,
    ) {
        fun encode(): String = quizKey + RECORD_SEPARATOR + optionId

        companion object {
            private const val RECORD_SEPARATOR = "\u001F"

            fun decode(value: String): QuizAnswerRecord? {
                val separatorIndex = value.indexOf(RECORD_SEPARATOR)
                if (separatorIndex <= 0 || separatorIndex == value.lastIndex) return null
                return QuizAnswerRecord(
                    quizKey = value.substring(0, separatorIndex),
                    optionId = value.substring(separatorIndex + RECORD_SEPARATOR.length),
                )
            }
        }
    }

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorite_topic_ids")
        val COMPLETED_LESSONS = stringSetPreferencesKey("completed_lesson_ids")
        val LAST_OPENED_LESSON = stringPreferencesKey("last_opened_lesson_id")
        val QUIZ_ANSWERS = stringSetPreferencesKey("quiz_answer_records")
    }
}
