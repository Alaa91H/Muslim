package org.example.islamicapp.feature.learn.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.learn.R
import org.example.islamicapp.feature.learn.domain.LearnTopic

/**
 * Learning guides (PROJECT_PROMPT.md §6 Phase 5): a step-by-step viewer with
 * an expandable neutral "known juristic differences" section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.learn_title)) },
                navigationIcon = {
                    if (state.selected != null) {
                        IconButton(onClick = { viewModel.selectTopic(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.learn_back))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val selected = state.selected
        if (selected == null) {
            TopicList(state.topics, isArabic, { viewModel.selectTopic(it) }, Modifier.padding(padding))
        } else {
            StepViewer(state, selected, isArabic, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun TopicList(
    topics: List<LearnTopic>,
    isArabic: Boolean,
    onTopic: (LearnTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.learn_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(topics, key = { it.id }) { topic ->
            Card(onClick = { onTopic(topic) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (isArabic) topic.titleAr else topic.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.learn_steps_count, topic.steps.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepViewer(
    state: LearnUiState,
    topic: LearnTopic,
    isArabic: Boolean,
    viewModel: LearnViewModel,
    modifier: Modifier = Modifier,
) {
    val step = topic.steps[state.stepIndex]

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                if (isArabic) topic.titleAr else topic.titleEn,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            LinearProgressIndicator(
                progress = { (state.stepIndex + 1f) / topic.steps.size },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.learn_step_of, state.stepIndex + 1, topic.steps.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isArabic) step.titleAr else step.titleEn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (isArabic) step.detailAr else step.detailEn,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                    )
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = viewModel::previousStep,
                    enabled = state.stepIndex > 0,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Button(onClick = viewModel::nextStep, enabled = state.stepIndex < topic.steps.lastIndex) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
        if (topic.differences.isNotEmpty()) {
            item {
                OutlinedButton(onClick = viewModel::toggleDifferences, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.learn_show_differences))
                }
            }
            if (state.showDifferences) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                stringResource(R.string.learn_differences_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            topic.differences.forEach { note ->
                                Text(
                                    if (isArabic) note.pointAr else note.pointEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
