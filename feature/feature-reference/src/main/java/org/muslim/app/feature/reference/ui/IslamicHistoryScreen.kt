package org.muslim.app.feature.reference.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.HistoryEra
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryPerson
import org.muslim.app.feature.reference.domain.HistoricalMapLayer
import org.muslim.app.feature.reference.domain.IslamicHistoryContent

/** A standalone, bilingual history destination with source-aware map boundaries. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var language by remember { mutableStateOf(HistoryLanguage.Arabic) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.reference_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        language = if (language == HistoryLanguage.Arabic) {
                            HistoryLanguage.English
                        } else {
                            HistoryLanguage.Arabic
                        }
                    }) {
                        Text(
                            if (language == HistoryLanguage.Arabic) {
                                stringResource(R.string.history_language_english)
                            } else {
                                stringResource(R.string.history_language_arabic)
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            HistoryTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            when (selectedTab) {
                0 -> TimelineTab(language = language)
                1 -> AtlasTab(language = language)
                else -> PeopleTab(language = language)
            }
        }
    }
}

@Composable
private fun HistoryTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.history_timeline_tab),
        stringResource(R.string.history_atlas_tab),
        stringResource(R.string.history_people_tab),
    )
    PrimaryTabRow(selectedTabIndex = selectedTab) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelect(index) },
                text = { Text(label, maxLines = 2, textAlign = TextAlign.Center) },
            )
        }
    }
}

@Composable
private fun TimelineTab(language: HistoryLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HistoryNotice(stringResource(R.string.history_timeline_intro)) }
        items(IslamicHistoryContent.timeline, key = { it.id }) { era ->
            TimelineEraCard(era = era, language = language)
        }
        item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
    }
}

@Composable
private fun TimelineEraCard(era: HistoryEra, language: HistoryLanguage) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = era.title.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = eraRange(era),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = era.summary.resolve(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = stringResource(R.string.history_era_highlights),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
            era.highlights.forEach { highlight ->
                Text(
                    text = "• ${highlight.resolve(language)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun eraRange(era: HistoryEra): String = stringResource(
    R.string.history_era_range,
    era.startCe,
    era.endCe?.toString() ?: stringResource(R.string.history_present),
)

@Composable
private fun AtlasTab(language: HistoryLanguage) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val layers = IslamicHistoryContent.atlasLayers
    val layer = layers[selectedIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        AtlasSelector(
            layers = layers,
            selectedIndex = selectedIndex,
            language = language,
            onSelect = { selectedIndex = it },
        )
        AtlasList(
            layer = layer,
            language = language,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AtlasSelector(
    layers: List<HistoricalMapLayer>,
    selectedIndex: Int,
    language: HistoryLanguage,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = stringResource(R.string.history_atlas_layer),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            layers.forEachIndexed { index, layer ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    label = { Text(layer.title.resolve(language)) },
                )
            }
        }
    }
}

@Composable
private fun AtlasList(
    layer: HistoricalMapLayer,
    language: HistoryLanguage,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { HistoryNotice(layer.summary.resolve(language)) }
        if (layer.routes.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.history_atlas_routes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(layer.routes, key = { it.id }) { route ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(route.title.resolve(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            route.note.resolve(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.history_atlas_places),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        items(layer.places, key = { it.id }) { place ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(place.title.resolve(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        place.note.resolve(language),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleTab(language: HistoryLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HistoryNotice(stringResource(R.string.history_people_intro)) }
        items(IslamicHistoryContent.personalities, key = { it.id }) { person ->
            PersonCard(person = person, language = language)
        }
        item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
    }
}

@Composable
private fun PersonCard(person: HistoryPerson, language: HistoryLanguage) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = person.name.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = person.years,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = stringResource(R.string.history_people_field, person.field.resolve(language)),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
            Text(person.summary.resolve(language), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 10.dp))
            Text(
                text = stringResource(R.string.history_people_contribution),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(person.contribution.resolve(language), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun HistoryNotice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}
