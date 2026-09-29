package org.muslim.app.feature.learn.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimProgressHeader
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimSearchBar
import org.muslim.app.core.ui.theme.MuslimTopBar
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.LearnContent
import org.muslim.app.feature.learn.domain.LearnTopic
import org.muslim.app.feature.learn.domain.LearningAssessmentCatalog
import org.muslim.app.feature.learn.domain.LearningAssessmentEntry
import org.muslim.app.feature.learn.domain.LearningProgressSummary
import org.muslim.app.feature.learn.domain.LearningQuizKey

@Composable
internal fun LearningHubControls(
    query: String,
    onQueryChange: (String) -> Unit,
    progress: LearningProgressSummary,
    continueTopic: LearnTopic?,
    mistakeCount: Int,
    onContinue: (LearnTopic) -> Unit,
    onReviewMistakes: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small)) {
        MuslimSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.learn_search_hint),
            clearContentDescription = stringResource(R.string.learn_search_clear),
        )
        OverallProgressCard(progress)
        continueTopic?.let { topic ->
            ContinueLearningCard(
                topic = topic,
                onContinue = { onContinue(topic) },
            )
        }
        if (mistakeCount > 0) {
            MistakeShortcutCard(
                mistakeCount = mistakeCount,
                onReviewMistakes = onReviewMistakes,
            )
        }
    }
}

@Composable
private fun OverallProgressCard(progress: LearningProgressSummary) {
    MuslimProgressHeader(
        title = stringResource(
            R.string.learn_overall_progress,
            progress.completed,
            progress.total,
        ),
        progress = progress.fraction,
    )
}

@Composable
private fun ContinueLearningCard(
    topic: LearnTopic,
    onContinue: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onContinue),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.learn_continue_learning),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(topic.titleRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.learn_continue_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun MistakeShortcutCard(
    mistakeCount: Int,
    onReviewMistakes: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewMistakes),
        containerColor = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.learn_review_mistakes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(R.string.learn_review_mistakes_count, mistakeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
internal fun LearningCategoryProgress(
    summary: LearningProgressSummary,
) {
    if (summary.total == 0) return
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(
                R.string.learn_category_progress,
                summary.completed,
                summary.total,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { summary.fraction },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun LearningMistakesScreen(
    quizAnswers: Map<String, String>,
    onBack: () -> Unit,
    onOpenLesson: (LearnTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mistakes = LearningAssessmentCatalog.incorrectEntries(quizAnswers)
    BackHandler(onBack = onBack)

    MuslimScreen(
        modifier = modifier,
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.learn_review_mistakes),
                onNavigateBack = onBack,
                navigationContentDescription = stringResource(R.string.learn_back),
            )
        },
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Compact,
            ),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            if (mistakes.isEmpty()) {
                item {
                    IslamicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small)) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.learn_no_mistakes),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            } else {
                items(
                    count = mistakes.size,
                    key = { index ->
                        val entry = mistakes[index]
                        "${entry.lessonId}::${entry.quiz.id}"
                    },
                ) { index ->
                    MistakeReviewCard(
                        entry = mistakes[index],
                        quizAnswers = quizAnswers,
                        onOpenLesson = onOpenLesson,
                    )
                }
            }
        }
    }

}

@Composable
private fun MistakeReviewCard(
    entry: LearningAssessmentEntry,
    quizAnswers: Map<String, String>,
    onOpenLesson: (LearnTopic) -> Unit,
) {
    val topic = LearnContent.topics.firstOrNull { it.id == entry.lessonId } ?: return
    val selectedId = quizAnswers[LearningQuizKey.of(entry.lessonId, entry.quiz.id)]
    val selectedText = entry.quiz.options.firstOrNull { it.id == selectedId }?.text.orEmpty()

    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(topic.titleRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = entry.quiz.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (selectedText.isNotBlank()) {
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            entry.quiz.explanation?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Button(onClick = { onOpenLesson(topic) }) {
                Text(stringResource(R.string.learn_open_lesson))
            }
        }
    }
}
