package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.HajjCategory
import org.muslim.app.feature.learn.domain.HajjStep
import org.muslim.app.feature.learn.domain.HajjTopic
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.learn.domain.HajjUmrahContent

private val categoryIcons = mapOf(
    "info" to Icons.Filled.Info,
    "list" to Icons.AutoMirrored.Filled.List,
    "place" to Icons.Filled.Place,
    "calendar" to Icons.Filled.DateRange,
    "check" to Icons.Filled.CheckCircle,
    "warning" to Icons.Filled.Warning,
    "favorite" to Icons.Filled.Favorite,
)

/**
 * A comprehensive Hajj & Umrah reference: a hub of clearly separated
 * categories, each with its topics, and every topic as step-by-step rites —
 * what to do, why, and what to say — in Arabic with an English rendering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HajjUmrahScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    var category by remember { mutableStateOf<HajjCategory?>(null) }
    var topic by remember { mutableStateOf<HajjTopic?>(null) }

    // System back steps out of the topic, then the category, then the screen
    // (mirrors the toolbar arrow) — never skips straight to the More root.
    BackHandler(enabled = topic != null || category != null) {
        when {
            topic != null -> topic = null
            category != null -> category = null
        }
    }
    var showCalculator by remember { mutableStateOf(false) }
    val currentCategory = category
    val currentTopic = topic
    val hajjCheckedSteps by viewModel.hajjCheckedSteps.collectAsStateWithLifecycle()
    val hajjCompanionEnabled by viewModel.hajjCompanionEnabled.collectAsStateWithLifecycle()

    // English renderings are hidden for Arabic UI: each language shows its
    // own texts — an Arabic reader reads the Arabic content only.
    val showEnglishFallback = AppLanguage.showEnglishFallback()

    val title = when {
        currentTopic != null -> currentTopic!!.title
        currentCategory != null -> currentCategory!!.title
        else -> stringResource(R.string.hajj_title)
    }

    if (showCalculator) {
        HajjDaysCalculatorScreen(
            onBack = { showCalculator = false },
            modifier = modifier,
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when {
                                topic != null -> topic = null
                                category != null -> category = null
                                else -> onBack()
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            currentTopic != null -> {
                TopicDetail(
                    topic = currentTopic!!,
                    checkedSteps = hajjCheckedSteps,
                    onToggleStep = viewModel::toggleHajjStep,
                    showEnglishFallback = showEnglishFallback,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            currentCategory != null -> {
                TopicList(
                    category = currentCategory!!,
                    showEnglishFallback = showEnglishFallback,
                    modifier = Modifier.padding(innerPadding),
                    onOpen = { topic = it },
                )
            }
            else -> {
                CategoryHub(
                    showEnglishFallback = showEnglishFallback,
                    modifier = Modifier.padding(innerPadding),
                    onOpen = { category = it },
                    onOpenCalculator = { showCalculator = true },
                    hajjCompanionEnabled = hajjCompanionEnabled,
                    onToggleCompanion = viewModel::setHajjCompanionEnabled,
                )
            }
        }
    }
}

@Composable
private fun CategoryHub(
    showEnglishFallback: Boolean,
    modifier: Modifier = Modifier,
    onOpen: (HajjCategory) -> Unit,
    onOpenCalculator: () -> Unit,
    hajjCompanionEnabled: Boolean,
    onToggleCompanion: (Boolean) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Text(
                    text = stringResource(R.string.hajj_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenCalculator() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp).size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hajj_calc_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.hajj_calc_entry_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        if (showEnglishFallback) {
                            Text(
                                text = stringResource(R.string.hajj_calc_entry_subtitle_en),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp).size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hajj_companion_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.hajj_companion_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        if (showEnglishFallback) {
                            Text(
                                text = stringResource(R.string.hajj_companion_subtitle_en),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                    }
                    Switch(
                        checked = hajjCompanionEnabled,
                        onCheckedChange = onToggleCompanion,
                    )
                }
            }
        }
        items(HajjUmrahContent.CATEGORIES, key = { it.id }) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpen(category) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = categoryIcons[category.iconKey] ?: Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp).size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (showEnglishFallback) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = category.titleEn + " · " + category.subtitleEn,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.hajj_topics_count,
                                category.topics.size,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicList(
    category: HajjCategory,
    showEnglishFallback: Boolean,
    modifier: Modifier = Modifier,
    onOpen: (HajjTopic) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(category.topics, key = { it.id }) { topic ->
            ListItem(
                headlineContent = {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                supportingContent = {
                    Column {
                        Text(topic.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (showEnglishFallback) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = topic.summaryEn,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                    }
                },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = topic.steps.size.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(topic) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun TopicDetail(
    topic: HajjTopic,
    checkedSteps: Set<String>,
    onToggleStep: (String, Int) -> Unit,
    showEnglishFallback: Boolean,
    modifier: Modifier = Modifier,
) {
    val doneCount = topic.steps.indices.count { "${topic.id}:$it" in checkedSteps }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                if (showEnglishFallback) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = topic.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item(key = "progress") {
            ProgressHeader(total = topic.steps.size, done = doneCount)
        }

        itemsIndexed(topic.steps) { index, step ->
            StepCard(
                index = index,
                step = step,
                checked = "${topic.id}:$index" in checkedSteps,
                onCheckedChange = { onToggleStep(topic.id, index) },
                showEnglishFallback = showEnglishFallback,
            )
        }

        topic.notes?.let { notes ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.hajj_notes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        if (showEnglishFallback) {
                            topic.notesEn?.let { notesEn ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = notesEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(total: Int, done: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hajj_progress, done, total),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            if (total > 0 && done == total) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hajj_progress_done),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    index: Int,
    step: HajjStep,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showEnglishFallback: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (showEnglishFallback) {
                            Text(
                                text = step.titleEn,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            }

            Spacer(Modifier.height(10.dp))
            SectionText(
                label = stringResource(R.string.hajj_section_what),
                text = step.what,
            )
            if (showEnglishFallback) {
                Spacer(Modifier.height(8.dp))
                SectionText(
                    label = stringResource(R.string.hajj_section_what_en),
                    text = step.whatEn,
                    italic = true,
                )
            }

            step.evidence?.let { evidence ->
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(10.dp))
                SectionText(
                    label = stringResource(R.string.hajj_section_evidence),
                    text = evidence,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                if (showEnglishFallback) {
                    step.evidenceEn?.let { evidenceEn ->
                        Spacer(Modifier.height(6.dp))
                        SectionText(
                            label = stringResource(R.string.hajj_section_evidence_en),
                            text = evidenceEn,
                            italic = true,
                        )
                    }
                }
            }

            step.why?.let { why ->
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(10.dp))
                SectionText(
                    label = stringResource(R.string.hajj_section_why),
                    text = why,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                if (showEnglishFallback) {
                    step.whyEn?.let { whyEn ->
                        Spacer(Modifier.height(6.dp))
                        SectionText(
                            label = stringResource(R.string.hajj_section_why_en),
                            text = whyEn,
                            italic = true,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            step.say?.let { say ->
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(10.dp))
                SectionText(
                    label = stringResource(R.string.hajj_section_say),
                    text = say,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                if (showEnglishFallback) {
                    step.sayEn?.let { sayEn ->
                        Spacer(Modifier.height(6.dp))
                        SectionText(
                            label = stringResource(R.string.hajj_section_say_en),
                            text = sayEn,
                            italic = true,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionText(
    label: String,
    text: String,
    italic: Boolean = false,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = contentColor,
        )
    }
}

