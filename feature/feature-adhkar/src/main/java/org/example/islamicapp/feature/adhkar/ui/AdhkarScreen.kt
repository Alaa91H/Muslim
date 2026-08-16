package org.example.islamicapp.feature.adhkar.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.adhkar.R
import org.example.islamicapp.feature.adhkar.data.DhikrReminderRepository
import org.example.islamicapp.feature.adhkar.domain.DhikrCategory
import org.example.islamicapp.feature.adhkar.domain.DhikrItem

/**
 * Adhkar browser (PROJECT_PROMPT.md §6 Phase 4): themed categories with a
 * per-item repetition counter and an optional periodic reminder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.selected == null) R.string.adhkar_title
                            else R.string.adhkar_title,
                        ) + (state.selected?.let { " — ${it.title()}" } ?: ""),
                    )
                },
                navigationIcon = {
                    if (state.selected != null) {
                        IconButton(onClick = { viewModel.selectCategory(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.adhkar_back))
                        }
                    }
                },
                actions = {
                    if (state.selected != null) {
                        IconButton(onClick = viewModel::resetCategory) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.adhkar_reset))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val selected = state.selected
        if (selected == null) {
            CategoryList(
                state = state,
                onCategory = { viewModel.selectCategory(it) },
                onInterval = viewModel::setReminderInterval,
                modifier = Modifier.padding(padding),
            )
        } else {
            DhikrList(
                category = selected,
                state = state,
                onIncrement = viewModel::increment,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DhikrCategory.title(): String =
    if (androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "ar") titleAr else titleEn

@Composable
private fun CategoryList(
    state: AdhkarUiState,
    onCategory: (DhikrCategory) -> Unit,
    onInterval: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---- Periodic reminder settings (opt-in, fully controllable) ----
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(
                            stringResource(R.string.dhikr_reminder_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.dhikr_reminder_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DhikrReminderRepository.CHOICES_MINUTES.forEach { minutes ->
                            FilterChip(
                                selected = state.reminderIntervalMinutes == minutes,
                                onClick = { onInterval(minutes) },
                                label = { Text(intervalLabel(minutes)) },
                            )
                        }
                    }
                }
            }
        }

        items(state.categories, key = { it.id }) { category ->
            Card(
                onClick = { onCategory(category) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(category.title(), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.adhkar_item_count, category.items.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun intervalLabel(minutes: Int): String = when (minutes) {
    DhikrReminderRepository.DISABLED -> stringResource(R.string.dhikr_reminder_off)
    30 -> stringResource(R.string.dhikr_reminder_30m)
    60 -> stringResource(R.string.dhikr_reminder_1h)
    180 -> stringResource(R.string.dhikr_reminder_3h)
    360 -> stringResource(R.string.dhikr_reminder_6h)
    else -> "$minutes"
}

@Composable
private fun DhikrList(
    category: DhikrCategory,
    state: AdhkarUiState,
    onIncrement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(category.items, key = { it.id }) { item: DhikrItem ->
            val done = state.progress.countOf(item.id)
            val complete = state.progress.isComplete(item)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable {
                        if (!complete) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onIncrement(item.id)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (complete) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                    )
                    if (item.virtue != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            item.virtue,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.adhkar_counter, done, item.count),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.weight(1f))
                        if (complete) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.adhkar_done),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (item.count > 1) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { done.toFloat() / item.count },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        item.reference,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
