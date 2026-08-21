package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.LearnContent
import org.muslim.app.feature.learn.domain.LearnTopic

private val topicIcons = mapOf(
    "pillars_islam" to Icons.Filled.Mosque,
    "pillars_iman" to Icons.Filled.Book,
    "wudu" to Icons.Filled.WaterDrop,
    "ghusl" to Icons.Filled.Bathtub,
    "tayammum" to Icons.Filled.BeachAccess,
    "salah" to Icons.Filled.AutoStories,
    "salah_arkan" to Icons.Filled.Checklist,
    "salah_times" to Icons.Filled.Schedule,
    "adhan" to Icons.AutoMirrored.Filled.VolumeUp,
    "shurut" to Icons.Filled.VerifiedUser,
    "nullifiers" to Icons.Filled.Warning,
    "rawatib" to Icons.Filled.Loop,
    "rakats" to Icons.Filled.FormatListNumbered,
    "special" to Icons.AutoMirrored.Filled.MenuBook,
    "fasting" to Icons.Filled.Restaurant,
    "zakat" to Icons.Filled.AccountBalance,
    "funeral" to Icons.Filled.LocalFlorist,
    "madhhab" to Icons.Filled.ChildCare,
)

@Composable
private fun categoryTitleRes(category: String): Int = when (category) {
    LearnContent.CATEGORY_FAITH -> R.string.learn_category_faith
    LearnContent.CATEGORY_TAHARA -> R.string.learn_category_tahara
    LearnContent.CATEGORY_SALAH -> R.string.learn_category_salah
    LearnContent.CATEGORY_IBADAH -> R.string.learn_category_ibadah
    LearnContent.CATEGORY_REFERENCE -> R.string.learn_category_reference
    else -> R.string.learn_category_reference
}

/**
 * Learning hub (PROJECT_PROMPT.md §6 Phase 5): a complete, indexed learning
 * reference — the pillars of Islam and faith, purification (wudu / ghusl /
 * tayammum), the prayer in full detail (conditions, times, adhan, pillars,
 * nullifiers, rawatib, special prayers, rak'ah table), fasting, zakat,
 * funerals and a neutral madhhab differences overview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    var selected by remember { mutableStateOf<LearnTopic?>(null) }
    var showNames by remember { mutableStateOf(false) }
    var showHajj by remember { mutableStateOf(false) }
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val topic = selected

    // The system/hardware back button must step back through the internal
    // screens (topic → list, hajj/names → list) before leaving the screen,
    // mirroring the toolbar arrow — otherwise one back press exits to the
    // More root directly.
    BackHandler(enabled = showNames) { showNames = false }
    BackHandler(enabled = showHajj) { showHajj = false }
    BackHandler(enabled = topic != null) { selected = null }

    if (showNames) {
        NamesOfAllahScreen(
            onBack = { showNames = false },
            modifier = modifier,
        )
        return
    }

    if (showHajj) {
        HajjUmrahScreen(
            onBack = { showHajj = false },
            modifier = modifier,
        )
        return
    }

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
                actions = {
                    if (topic != null) {
                        val isFav = topic.id in favoriteIds
                        IconButton(onClick = { viewModel.toggleFavorite(topic.id) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(
                                    if (isFav) R.string.learn_remove_favorite else R.string.learn_add_favorite
                                ),
                                tint = if (isFav) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (topic == null) {
            TopicList(
                favoriteIds = favoriteIds,
                onToggleFavorite = viewModel::toggleFavorite,
                modifier = Modifier.padding(innerPadding),
                onOpen = { selected = it },
                onOpenNames = { showNames = true },
                onOpenHajj = { showHajj = true },
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
private fun TopicList(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (LearnTopic) -> Unit,
    onOpenNames: () -> Unit,
    onOpenHajj: () -> Unit,
) {
    val favoriteTopics = LearnContent.topics.filter { it.id in favoriteIds }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "names_of_allah") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.learn_topic_names_of_allah)) },
                supportingContent = { Text(stringResource(R.string.learn_topic_names_of_allah_sub)) },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(8.dp).size(20.dp),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenNames() },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        item(key = "hajj_umrah") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.learn_topic_hajj)) },
                supportingContent = { Text(stringResource(R.string.learn_topic_hajj_sub)) },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(8.dp).size(20.dp),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenHajj() },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        if (favoriteTopics.isNotEmpty()) {
            item(key = "favorites_header") {
                CategoryHeader(title = stringResource(R.string.learn_favorites_header))
            }
            items(favoriteTopics, key = { "favorite_${it.id}" }) { topic ->
                TopicListItem(
                    topic = topic,
                    isFavorite = true,
                    onToggleFavorite = onToggleFavorite,
                    onOpen = onOpen,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
        val byCategory = LearnContent.topics.groupBy { it.category }
        LearnContent.categoryOrder.forEach { category ->
            val topics = byCategory[category].orEmpty()
            if (topics.isEmpty()) return@forEach
            item(key = "category_$category") {
                CategoryHeader(title = stringResource(categoryTitleRes(category)))
            }
            items(topics, key = { it.id }) { topic ->
                TopicListItem(
                    topic = topic,
                    isFavorite = topic.id in favoriteIds,
                    onToggleFavorite = onToggleFavorite,
                    onOpen = onOpen,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
private fun TopicListItem(
    topic: LearnTopic,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onOpen: (LearnTopic) -> Unit,
) {
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
        trailingContent = {
            IconButton(onClick = { onToggleFavorite(topic.id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.learn_remove_favorite else R.string.learn_add_favorite
                    ),
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(topic) },
    )
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun GuideContent(topic: LearnTopic, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
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
