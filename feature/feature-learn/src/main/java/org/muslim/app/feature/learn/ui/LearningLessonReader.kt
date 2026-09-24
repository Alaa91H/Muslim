package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.LearningQuizKey
import org.muslim.app.feature.learn.domain.LearnTopic
import org.muslim.app.feature.learn.domain.LearningAcademyCatalog
import org.muslim.app.feature.learn.domain.LearningAssessmentCatalog
import org.muslim.app.feature.learn.domain.LearningAssessmentEntry
import org.muslim.app.feature.learn.domain.LearningCalloutTone
import org.muslim.app.feature.learn.domain.LearningContentBlock
import org.muslim.app.feature.learn.domain.LearningFeatureDestination
import org.muslim.app.feature.learn.domain.LearningFeatureLink
import org.muslim.app.feature.learn.domain.LearningLesson
import org.muslim.app.feature.learn.domain.LearningReference
import org.muslim.app.feature.learn.domain.LearningStepItem

@Composable
internal fun LearningLessonReader(
    topic: LearnTopic,
    completed: Boolean,
    quizAnswers: Map<String, String>,
    onSetCompleted: (Boolean) -> Unit,
    onAnswerQuiz: (quizId: String, optionId: String) -> Unit,
    onOpenFeature: (LearningFeatureDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lesson = remember(topic.id) { LearningAcademyCatalog.lessonFor(topic) }
    val assessments = remember(topic.id) { LearningAssessmentCatalog.forLesson(topic.id) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val sectionIndices = remember(lesson.id) { sectionHeaderIndices(lesson) }
    val answeredCount = assessments.count { entry ->
        LearningQuizKey.of(lesson.id, entry.quiz.id) in quizAnswers
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        lessonIntroItems(
            lesson = lesson,
            completed = completed,
            answeredCount = answeredCount,
            assessmentCount = assessments.size,
            sectionIndices = sectionIndices,
            onSectionClick = { sectionId ->
                sectionIndices[sectionId]?.let { target ->
                    scope.launch { listState.animateScrollToItem(target) }
                }
            },
        )
        lessonContentItems(
            lesson = lesson,
            quizAnswers = quizAnswers,
            onAnswerQuiz = onAnswerQuiz,
        )
        lessonAssessmentItems(
            lesson = lesson,
            assessments = assessments,
            quizAnswers = quizAnswers,
            onAnswerQuiz = onAnswerQuiz,
        )
        lessonTailItems(
            lesson = lesson,
            completed = completed,
            onSetCompleted = onSetCompleted,
            onOpenFeature = onOpenFeature,
        )
    }
}

private fun LazyListScope.lessonIntroItems(
    lesson: LearningLesson,
    completed: Boolean,
    answeredCount: Int,
    assessmentCount: Int,
    sectionIndices: Map<String, Int>,
    onSectionClick: (String) -> Unit,
) {
    item(key = "${lesson.id}_progress") {
        LessonProgressCard(
            completed = completed,
            answered = answeredCount,
            quizCount = assessmentCount,
        )
    }

    lesson.estimatedMinutes?.let { minutes ->
        item(key = "${lesson.id}_metadata") {
            LessonMetadataCard(
                estimatedMinutes = minutes,
                contentVersion = lesson.contentVersion,
            )
        }
    }

    item(key = "${lesson.id}_toc") {
        LessonContentsCard(
            lesson = lesson,
            onSectionClick = { sectionId ->
                if (sectionId in sectionIndices) onSectionClick(sectionId)
            },
        )
    }
}

private fun LazyListScope.lessonContentItems(
    lesson: LearningLesson,
    quizAnswers: Map<String, String>,
    onAnswerQuiz: (quizId: String, optionId: String) -> Unit,
) {
    lesson.sections.forEach { section ->
        item(key = "${lesson.id}_${section.id}_header") {
            LessonSectionHeader(title = section.title)
        }

        section.blocks.forEachIndexed { blockIndex, block ->
            when (block) {
                is LearningContentBlock.Steps -> {
                    itemsIndexed(
                        items = block.items,
                        key = { stepIndex, _ ->
                            "${lesson.id}_${section.id}_${blockIndex}_step_$stepIndex"
                        },
                    ) { stepIndex, step ->
                        InteractiveStepCard(
                            index = stepIndex,
                            step = step,
                        )
                    }
                }

                is LearningContentBlock.Quiz -> {
                    item(key = "${lesson.id}_${section.id}_quiz_${block.id}") {
                        InteractiveQuizCard(
                            quiz = block,
                            selectedOptionId = quizAnswers[
                                LearningQuizKey.of(lesson.id, block.id)
                            ],
                            onAnswer = { optionId -> onAnswerQuiz(block.id, optionId) },
                        )
                    }
                }

                else -> {
                    item(key = "${lesson.id}_${section.id}_block_$blockIndex") {
                        InteractiveLearningBlockCard(block)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.lessonAssessmentItems(
    lesson: LearningLesson,
    assessments: List<LearningAssessmentEntry>,
    quizAnswers: Map<String, String>,
    onAnswerQuiz: (quizId: String, optionId: String) -> Unit,
) {
    if (assessments.isEmpty()) return

    item(key = "${lesson.id}_assessment_header") {
        LessonSectionHeader(title = stringResource(R.string.learn_knowledge_check))
    }
    items(
        items = assessments,
        key = { entry -> "${lesson.id}_assessment_${entry.quiz.id}" },
    ) { entry ->
        AssessmentCard(
            entry = entry,
            selectedOptionId = quizAnswers[
                LearningQuizKey.of(lesson.id, entry.quiz.id)
            ],
            onAnswer = { optionId -> onAnswerQuiz(entry.quiz.id, optionId) },
        )
    }
}

private fun LazyListScope.lessonTailItems(
    lesson: LearningLesson,
    completed: Boolean,
    onSetCompleted: (Boolean) -> Unit,
    onOpenFeature: (LearningFeatureDestination) -> Unit,
) {
    lesson.featureLink?.let { link ->
        item(key = "${lesson.id}_feature_link") {
            ReaderFeatureLinkCard(
                link = link,
                onOpenFeature = onOpenFeature,
            )
        }
    }

    if (lesson.references.isNotEmpty()) {
        item(key = "${lesson.id}_references_header") {
            LessonSectionHeader(title = stringResource(R.string.learn_references))
        }
        items(
            items = lesson.references,
            key = { reference -> "${lesson.id}_reference_${reference.id}" },
        ) { reference ->
            ReaderReferenceCard(reference)
        }
    }

    item(key = "${lesson.id}_completion") {
        LessonCompletionCard(
            completed = completed,
            onSetCompleted = onSetCompleted,
        )
    }
}

private fun sectionHeaderIndices(lesson: LearningLesson): Map<String, Int> {
    var currentIndex = 1
    if (lesson.estimatedMinutes != null) currentIndex += 1
    currentIndex += 1

    return buildMap {
        lesson.sections.forEach { section ->
            put(section.id, currentIndex)
            currentIndex += 1
            section.blocks.forEach { block ->
                currentIndex += when (block) {
                    is LearningContentBlock.Steps -> block.items.size
                    else -> 1
                }
            }
        }
    }
}

@Composable
private fun LessonProgressCard(
    completed: Boolean,
    answered: Int,
    quizCount: Int,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (completed) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    if (completed) R.string.learn_lesson_completed else R.string.learn_lesson_in_progress
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (quizCount > 0) {
                val fraction = answered.toFloat() / quizCount.toFloat()
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.learn_quiz_progress, answered, quizCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LessonMetadataCard(
    estimatedMinutes: Int,
    contentVersion: Int,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.learn_estimated_minutes, estimatedMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.learn_content_version, contentVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.learn_scholar_review_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LessonContentsCard(
    lesson: LearningLesson,
    onSectionClick: (String) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.learn_contents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            lesson.sections.forEachIndexed { index, section ->
                Text(
                    text = "${index + 1}. ${section.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSectionClick(section.id) }
                        .padding(vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun LessonSectionHeader(title: String) {
    MuslimSectionHeader(
        title = title,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun InteractiveStepCard(
    index: Int,
    step: LearningStepItem,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                step.supplementalText?.let { supplemental ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = supplemental,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveLearningBlockCard(block: LearningContentBlock) {
    when (block) {
        is LearningContentBlock.Paragraph -> ReaderParagraphCard(block)
        is LearningContentBlock.Callout -> ReaderCalloutCard(block)
        is LearningContentBlock.Evidence -> ReaderEvidenceCard(block)
        is LearningContentBlock.Comparison -> ReaderComparisonCard(block)
        is LearningContentBlock.QuestionAnswer -> ReaderQuestionAnswerCard(block)
        is LearningContentBlock.Quiz,
        is LearningContentBlock.Steps -> Unit
    }
}

@Composable
private fun ReaderParagraphCard(block: LearningContentBlock.Paragraph) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReaderCalloutCard(block: LearningContentBlock.Callout) {
    val container = when (block.tone) {
        LearningCalloutTone.INFO -> MaterialTheme.colorScheme.secondaryContainer
        LearningCalloutTone.IMPORTANT -> MaterialTheme.colorScheme.primaryContainer
        LearningCalloutTone.WARNING -> MaterialTheme.colorScheme.errorContainer
        LearningCalloutTone.DIFFERENCE_OF_OPINION -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val content = when (block.tone) {
        LearningCalloutTone.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
        LearningCalloutTone.IMPORTANT -> MaterialTheme.colorScheme.onPrimaryContainer
        LearningCalloutTone.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        LearningCalloutTone.DIFFERENCE_OF_OPINION -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = container,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = content,
                )
            }
            Text(
                text = block.body,
                style = MaterialTheme.typography.bodyMedium,
                color = content,
            )
        }
    }
}

@Composable
private fun ReaderEvidenceCard(block: LearningContentBlock.Evidence) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.heading?.let { heading ->
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun ReaderComparisonCard(block: LearningContentBlock.Comparison) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            block.intro?.let { intro ->
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            block.items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderQuestionAnswerCard(block: LearningContentBlock.QuestionAnswer) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = block.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = block.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AssessmentCard(
    entry: LearningAssessmentEntry,
    selectedOptionId: String?,
    onAnswer: (String) -> Unit,
) {
    InteractiveQuizCard(
        quiz = entry.quiz,
        selectedOptionId = selectedOptionId,
        onAnswer = onAnswer,
    )
}

@Composable
private fun InteractiveQuizCard(
    quiz: LearningContentBlock.Quiz,
    selectedOptionId: String?,
    onAnswer: (String) -> Unit,
) {
    val isCorrect = selectedOptionId == quiz.correctOptionId

    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = quiz.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            quiz.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnswer(option.id) }
                        .padding(vertical = 2.dp),
                ) {
                    RadioButton(
                        selected = option.id == selectedOptionId,
                        onClick = { onAnswer(option.id) },
                    )
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            selectedOptionId?.let {
                Text(
                    text = stringResource(
                        if (isCorrect) R.string.learn_quiz_correct else R.string.learn_quiz_incorrect
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                quiz.explanation?.let { explanation ->
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderFeatureLinkCard(
    link: LearningFeatureLink,
    onOpenFeature: (LearningFeatureDestination) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(link.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(link.bodyRes),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { onOpenFeature(link.destination) }) {
                Text(stringResource(link.actionRes))
            }
        }
    }
}

@Composable
private fun ReaderReferenceCard(reference: LearningReference) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = reference.citation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            reference.locator?.let { locator ->
                Text(
                    text = locator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            reference.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LessonCompletionCard(
    completed: Boolean,
    onSetCompleted: (Boolean) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    if (completed) R.string.learn_completion_done_body else R.string.learn_completion_body
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (completed) {
                OutlinedButton(onClick = { onSetCompleted(false) }) {
                    Text(stringResource(R.string.learn_mark_incomplete))
                }
            } else {
                Button(onClick = { onSetCompleted(true) }) {
                    Text(stringResource(R.string.learn_mark_completed))
                }
            }
        }
    }
}
