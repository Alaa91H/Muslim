package org.muslim.app.feature.learn.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimAppScaffold
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            placeholder = {
                Text(stringResource(R.string.learn_search_hint))
            },
        )

        IslamicCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        R.string.learn_overall_progress,
                        progress.completed,
                        progress.total,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                LinearProgressIndicator(
                    progress = { progress.fraction },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        continueTopic?.let { topic ->
            IslamicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onContinue(topic) },
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

        if (mistakeCount > 0) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LearningMistakesScreen(
    quizAnswers: Map<String, String>,
    onBack: () -> Unit,
    onOpenLesson: (LearnTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mistakes = LearningAssessmentCatalog.incorrectEntries(quizAnswers)
    BackHandler(onBack = onBack)

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.learn_review_mistakes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (mistakes.isEmpty()) {
                item {
                    IslamicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
