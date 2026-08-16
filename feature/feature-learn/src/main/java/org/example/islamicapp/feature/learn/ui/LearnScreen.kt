package org.example.islamicapp.feature.learn.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.islamicapp.feature.learn.R
import org.example.islamicapp.feature.learn.domain.LearnContent
import org.example.islamicapp.feature.learn.domain.LearnTopic

private val topicIcons = mapOf(
    "wudu" to Icons.Filled.WaterDrop,
    "ghusl" to Icons.Filled.Bathtub,
    "tayammum" to Icons.Filled.BeachAccess,
    "salah" to Icons.Filled.AutoStories,
    "special" to Icons.AutoMirrored.Filled.MenuBook,
    "rakats" to Icons.Filled.FormatListNumbered,
    "madhhab" to Icons.Filled.ChildCare,
)

/**
 * Learning hub (PROJECT_PROMPT.md §6 Phase 5): step-by-step guides for
 * wudu, ghusl, tayammum and the daily prayers, special prayers, the rak'ah
 * reference table and a neutral madhhab differences overview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<LearnTopic?>(null) }
    val topic = selected

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (topic == null) R.string.learn_title else topic.titleRes)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (topic == null) onBack() else selected = null }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (topic == null) {
            TopicList(
                modifier = Modifier.padding(innerPadding),
                onOpen = { selected = it },
            )
        } else {
            GuideContent(
                topic = topic,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun TopicList(modifier: Modifier = Modifier, onOpen: (LearnTopic) -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(LearnContent.topics, key = { it.id }) { topic ->
            ListItem(
                headlineContent = { Text(stringResource(topic.titleRes)) },
                supportingContent = { Text(stringResource(topic.subtitleRes)) },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(
                            imageVector = topicIcons[topic.id] ?: Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(8.dp).size(20.dp),
                        )
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
private fun GuideContent(topic: LearnTopic, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        itemsIndexed(topic.steps) { index, step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
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
                    Spacer(Modifier.height(0.dp))
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        step.dua?.let { dua ->
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = dua,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
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
                ) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
