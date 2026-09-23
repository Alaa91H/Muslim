package org.muslim.app.feature.reference.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.HistoryArticle
import org.muslim.app.feature.reference.domain.HistoryDatePrecision
import org.muslim.app.feature.reference.domain.HistoryEra
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryPerson
import org.muslim.app.feature.reference.domain.HistoryRegion
import org.muslim.app.feature.reference.domain.HistoricalMapLayer
import org.muslim.app.feature.reference.domain.HistoricalState
import org.muslim.app.feature.reference.domain.IslamicHistoryArticles
import org.muslim.app.feature.reference.domain.IslamicHistoryContent
import org.muslim.app.feature.reference.domain.IslamicHistorySources
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

/** A standalone, bilingual history destination with source-aware map boundaries. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var language by remember { mutableStateOf(HistoryLanguage.Arabic) }
    var selectedTab by remember { mutableIntStateOf(0) }

    MuslimAppScaffold(
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
            IslamicDecorationDivider(
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            when (selectedTab) {
                0 -> TimelineTab(language = language)
                1 -> StatesTab(language = language)
                2 -> AtlasTab(language = language)
                else -> PeopleTab(language = language)
            }
        }
    }
}

@Composable
private fun HistoryTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.history_timeline_tab),
        stringResource(R.string.history_states_tab),
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
    var selectedEraId by remember { mutableStateOf<String?>(null) }
    val selectedArticle = selectedEraId?.let(IslamicHistoryArticles::articleForEra)

    if (selectedArticle != null) {
        HistoryArticleView(
            article = selectedArticle,
            language = language,
            onBack = { selectedEraId = null },
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HistoryNotice(stringResource(R.string.history_timeline_intro)) }
            items(IslamicHistoryContent.timeline, key = { it.id }) { era ->
                TimelineEraCard(
                    era = era,
                    language = language,
                    onOpenDetails = IslamicHistoryArticles.articleForEra(era.id)?.let {
                        { selectedEraId = era.id }
                    },
                )
            }
            item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
        }
    }
}

@Composable
private fun TimelineEraCard(
    era: HistoryEra,
    language: HistoryLanguage,
    onOpenDetails: (() -> Unit)? = null,
) {
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
            if (onOpenDetails != null) {
                TextButton(
                    onClick = onOpenDetails,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        if (language == HistoryLanguage.Arabic) {
                            "قراءة الموضوع بالتفصيل"
                        } else {
                            "Read full article"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryArticleView(
    article: HistoryArticle,
    language: HistoryLanguage,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val sources = article.sourceIds.mapNotNull(IslamicHistorySources::byId)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HistoryArticleBackButton(language = language, onBack = onBack) }
        item {
            Text(
                text = article.title.resolve(language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        item { HistoryNotice(article.lead.resolve(language)) }
        items(article.sections, key = { it.id }) { section ->
            HistoryArticleSectionCard(section = section, language = language)
        }
        if (sources.isNotEmpty()) {
            item { HistorySourcesHeading(language = language) }
            items(sources, key = { it.id }) { source ->
                HistorySourceCard(source = source, language = language)
            }
        }
    }
}

@Composable
private fun HistoryArticleBackButton(
    language: HistoryLanguage,
    onBack: () -> Unit,
) {
    TextButton(onClick = onBack) {
        Text(
            if (language == HistoryLanguage.Arabic) {
                "العودة إلى الخط الزمني"
            } else {
                "Back to timeline"
            },
        )
    }
}

@Composable
private fun HistoryArticleSectionCard(
    section: org.muslim.app.feature.reference.domain.HistoryArticleSection,
    language: HistoryLanguage,
) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = section.title.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            section.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph.resolve(language),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun HistorySourcesHeading(language: HistoryLanguage) {
    Text(
        text = if (language == HistoryLanguage.Arabic) "المصادر والمراجع" else "Sources and references",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun HistorySourceCard(
    source: org.muslim.app.feature.reference.domain.HistorySource,
    language: HistoryLanguage,
) {
    val uriHandler = LocalUriHandler.current

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = source.title.resolve(language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            source.note?.let { note ->
                Text(
                    text = note.resolve(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            source.url?.let { url ->
                TextButton(onClick = { uriHandler.openUri(url) }) {
                    Text(
                        if (language == HistoryLanguage.Arabic) {
                            "فتح المصدر"
                        } else {
                            "Open source"
                        },
                    )
                }
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
private fun StatesTab(language: HistoryLanguage) {
    var selectedRegion by remember { mutableStateOf<HistoryRegion?>(null) }
    val regions = listOf<HistoryRegion?>(
        null,
        HistoryRegion.MultiRegional,
        HistoryRegion.MaghrebAndAlAndalus,
        HistoryRegion.EgyptAndLevant,
        HistoryRegion.Anatolia,
        HistoryRegion.IranAndCentralAsia,
        HistoryRegion.SouthAsia,
    )
    val states = IslamicHistoryStates.byRegion(selectedRegion)

    Column(modifier = Modifier.fillMaxSize()) {
        HistoryRegionSelector(
            regions = regions,
            selectedRegion = selectedRegion,
            language = language,
            onSelect = { selectedRegion = it },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HistoryNotice(stringResource(R.string.history_states_intro)) }
            items(states, key = { it.id }) { state ->
                HistoricalStateCard(state = state, language = language)
            }
            item { HistoryNotice(stringResource(R.string.history_sources_notice)) }
        }
    }
}

@Composable
private fun HistoryRegionSelector(
    regions: List<HistoryRegion?>,
    selectedRegion: HistoryRegion?,
    language: HistoryLanguage,
    onSelect: (HistoryRegion?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        regions.forEach { region ->
            FilterChip(
                selected = selectedRegion == region,
                onClick = { onSelect(region) },
                label = { Text(historyRegionLabel(region, language)) },
            )
        }
    }
}

@Composable
private fun HistoricalStateCard(
    state: HistoricalState,
    language: HistoryLanguage,
) {
    val uriHandler = LocalUriHandler.current
    val source = state.sourceIds.firstNotNullOfOrNull(IslamicHistorySources::byId)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = state.title.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = statePeriodLabel(state, language),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = historyRegionLabel(state.region, language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = state.summary.resolve(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            source?.url?.let { url ->
                TextButton(
                    onClick = { uriHandler.openUri(url) },
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Text(
                        if (language == HistoryLanguage.Arabic) {
                            "فتح المصدر"
                        } else {
                            "Open source"
                        },
                    )
                }
            }
        }
    }
}

private fun statePeriodLabel(state: HistoricalState, language: HistoryLanguage): String {
    val start = state.period.startCe?.toString() ?: "?"
    val end = state.period.endCe?.toString() ?: "?"
    val prefix = if (state.period.precision == HistoryDatePrecision.Approximate) {
        if (language == HistoryLanguage.Arabic) "نحو " else "ca. "
    } else {
        ""
    }
    val suffix = if (language == HistoryLanguage.Arabic) " م" else " CE"
    return "$prefix$start–$end$suffix"
}

private fun historyRegionLabel(region: HistoryRegion?, language: HistoryLanguage): String =
    when (region) {
        null -> if (language == HistoryLanguage.Arabic) "الكل" else "All"
        HistoryRegion.MultiRegional ->
            if (language == HistoryLanguage.Arabic) "متعددة الأقاليم" else "Multi-regional"
        HistoryRegion.Arabia ->
            if (language == HistoryLanguage.Arabic) "الجزيرة العربية" else "Arabia"
        HistoryRegion.EgyptAndLevant ->
            if (language == HistoryLanguage.Arabic) "مصر والشام" else "Egypt & Levant"
        HistoryRegion.MaghrebAndAlAndalus ->
            if (language == HistoryLanguage.Arabic) "المغرب والأندلس" else "Maghreb & al-Andalus"
        HistoryRegion.Anatolia ->
            if (language == HistoryLanguage.Arabic) "الأناضول" else "Anatolia"
        HistoryRegion.IranAndCentralAsia ->
            if (language == HistoryLanguage.Arabic) "إيران وآسيا الوسطى" else "Iran & Central Asia"
        HistoryRegion.SouthAsia ->
            if (language == HistoryLanguage.Arabic) "جنوب آسيا" else "South Asia"
    }

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
