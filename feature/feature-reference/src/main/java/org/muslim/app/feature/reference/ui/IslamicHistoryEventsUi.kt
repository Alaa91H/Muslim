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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.reference.R
import org.muslim.app.feature.reference.domain.HistoricalEvent
import org.muslim.app.feature.reference.domain.HistoricalEventCategory
import org.muslim.app.feature.reference.domain.HistoryDatePrecision
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryText
import org.muslim.app.feature.reference.domain.IslamicHistoricalEvents
import org.muslim.app.feature.reference.domain.IslamicHistoryContent
import org.muslim.app.feature.reference.domain.IslamicHistorySources
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

@Composable
internal fun EventsTab(
    language: HistoryLanguage,
    target: HistoryNavigationTarget?,
    onTargetConsumed: () -> Unit,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<HistoricalEventCategory?>(null) }
    var selectedEraId by remember { mutableStateOf<String?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(target) {
        if (target?.type == HistoryTargetType.Event) {
            selectedEventId = target.id
            onTargetConsumed()
        }
    }
    val selectedEvent = selectedEventId?.let(IslamicHistoricalEvents::byId)

    if (selectedEvent != null) {
        HistoricalEventView(
            event = selectedEvent,
            language = language,
            onBack = { selectedEventId = null },
            onNavigate = onNavigate,
        )
        return
    }

    val events = IslamicHistoricalEvents.events.filter { event ->
        (selectedCategory == null || event.category == selectedCategory) &&
            (selectedEraId == null || selectedEraId in event.eraIds)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EventFilters(
            selectedCategory = selectedCategory,
            selectedEraId = selectedEraId,
            language = language,
            onCategorySelect = { selectedCategory = it },
            onEraSelect = { selectedEraId = it },
        )
        EventList(
            events = events,
            language = language,
            onOpen = { selectedEventId = it },
        )
    }
}

@Composable
private fun EventFilters(
    selectedCategory: HistoricalEventCategory?,
    selectedEraId: String?,
    language: HistoryLanguage,
    onCategorySelect: (HistoricalEventCategory?) -> Unit,
    onEraSelect: (String?) -> Unit,
) {
    Column {
        EventCategoryFilterRow(
            selectedCategory = selectedCategory,
            language = language,
            onSelect = onCategorySelect,
        )
        EventEraFilterRow(
            selectedEraId = selectedEraId,
            language = language,
            onSelect = onEraSelect,
        )
    }
}

@Composable
private fun EventCategoryFilterRow(
    selectedCategory: HistoricalEventCategory?,
    language: HistoryLanguage,
    onSelect: (HistoricalEventCategory?) -> Unit,
) {
    val categories = listOf<HistoricalEventCategory?>(null) + HistoricalEventCategory.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelect(category) },
                label = { Text(eventCategoryLabel(category, language)) },
            )
        }
    }
}

@Composable
private fun EventEraFilterRow(
    selectedEraId: String?,
    language: HistoryLanguage,
    onSelect: (String?) -> Unit,
) {
    val eras = listOf<String?>(null) + IslamicHistoryContent.timeline.map { it.id }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        eras.forEach { eraId ->
            FilterChip(
                selected = selectedEraId == eraId,
                onClick = { onSelect(eraId) },
                label = { Text(eventEraLabel(eraId, language)) },
            )
        }
    }
}

@Composable
private fun EventList(
    events: List<HistoricalEvent>,
    language: HistoryLanguage,
    onOpen: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            EventNotice(stringResource(R.string.history_events_intro))
        }
        items(events, key = { it.id }) { event ->
            HistoricalEventCard(
                event = event,
                language = language,
                onOpen = { onOpen(event.id) },
            )
        }
        if (events.isEmpty()) {
            item {
                EventNotice(
                    if (language == HistoryLanguage.Arabic) {
                        "لا توجد أحداث مطابقة لهذه المرشحات."
                    } else {
                        "No events match these filters."
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoricalEventCard(
    event: HistoricalEvent,
    language: HistoryLanguage,
    onOpen: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = event.title.resolve(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = eventDateLabel(event, language),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = eventCategoryLabel(event.category, language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = event.summary.resolve(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            TextButton(
                onClick = onOpen,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(
                    if (language == HistoryLanguage.Arabic) {
                        "عرض السياق والتفاصيل"
                    } else {
                        "View context and details"
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoricalEventView(
    event: HistoricalEvent,
    language: HistoryLanguage,
    onBack: () -> Unit,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    BackHandler(onBack = onBack)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { EventBackButton(language = language, onBack = onBack) }
        item { EventHeader(event = event, language = language) }
        item {
            EventDetailCard(
                title = if (language == HistoryLanguage.Arabic) "السياق" else "Context",
                text = event.context,
                language = language,
            )
        }
        item {
            EventDetailCard(
                title = if (language == HistoryLanguage.Arabic) "الأهمية التاريخية" else "Historical significance",
                text = event.significance,
                language = language,
            )
        }
        item {
            EventRelations(
                event = event,
                language = language,
                onNavigate = onNavigate,
            )
        }
        item { EventSources(event = event, language = language) }
    }
}

@Composable
private fun EventBackButton(
    language: HistoryLanguage,
    onBack: () -> Unit,
) {
    TextButton(onClick = onBack) {
        Text(
            if (language == HistoryLanguage.Arabic) {
                "العودة إلى الأحداث"
            } else {
                "Back to events"
            },
        )
    }
}

@Composable
private fun EventHeader(
    event: HistoricalEvent,
    language: HistoryLanguage,
) {
    Column {
        Text(
            text = event.title.resolve(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = eventDateLabel(event, language),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = event.summary.resolve(language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun EventDetailCard(
    title: String,
    text: HistoryText,
    language: HistoryLanguage,
) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text.resolve(language),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun EventRelations(
    event: HistoricalEvent,
    language: HistoryLanguage,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    val links = buildList {
        event.stateIds.forEach { id ->
            IslamicHistoryStates.byId(id)?.let { state ->
                add(
                    Triple(
                        if (language == HistoryLanguage.Arabic) "دولة" else "State",
                        state.title.resolve(language),
                        HistoryNavigationTarget(HistoryTargetType.State, id),
                    ),
                )
            }
        }
        event.personIds.forEach { id ->
            IslamicHistoryContent.personalities.firstOrNull { it.id == id }?.let { person ->
                add(
                    Triple(
                        if (language == HistoryLanguage.Arabic) "شخصية" else "Person",
                        person.name.resolve(language),
                        HistoryNavigationTarget(HistoryTargetType.Person, id),
                    ),
                )
            }
        }
        event.placeIds.forEach { id ->
            IslamicHistoryContent.atlasLayers
                .asSequence()
                .flatMap { it.places.asSequence() }
                .firstOrNull { it.id == id }
                ?.let { place ->
                    add(
                        Triple(
                            if (language == HistoryLanguage.Arabic) "مكان" else "Place",
                            place.title.resolve(language),
                            HistoryNavigationTarget(HistoryTargetType.Place, id),
                        ),
                    )
                }
        }
        event.relatedTopicIds.forEach { id ->
            org.muslim.app.feature.reference.domain.IslamicCivilizationContent.byId(id)?.let { topic ->
                add(
                    Triple(
                        if (language == HistoryLanguage.Arabic) "موضوع حضاري" else "Civilization",
                        topic.title.resolve(language),
                        HistoryNavigationTarget(HistoryTargetType.CivilizationTopic, id),
                    ),
                )
            }
        }
    }
    if (links.isEmpty()) return

    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (language == HistoryLanguage.Arabic) "روابط مرتبطة" else "Related entries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            links.forEach { (kind, label, target) ->
                TextButton(onClick = { onNavigate(target) }) {
                    Text("$kind: $label")
                }
            }
        }
    }
}

@Composable
private fun EventSources(
    event: HistoricalEvent,
    language: HistoryLanguage,
) {
    val uriHandler = LocalUriHandler.current
    val sources = event.sourceIds.mapNotNull(IslamicHistorySources::byId)
    if (sources.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (language == HistoryLanguage.Arabic) "المصادر والمراجع" else "Sources and references",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        sources.forEach { source ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = source.title.resolve(language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
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
    }
}

@Composable
private fun EventNotice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun eventDateLabel(
    event: HistoricalEvent,
    language: HistoryLanguage,
): String {
    val start = event.date.startCe?.toString() ?: "?"
    val end = event.date.endCe?.toString()
    val range = if (end == null || end == start) start else "$start–$end"
    val prefix = when (event.date.precision) {
        HistoryDatePrecision.Exact -> ""
        HistoryDatePrecision.Approximate ->
            if (language == HistoryLanguage.Arabic) "نحو " else "ca. "
        HistoryDatePrecision.Range ->
            if (language == HistoryLanguage.Arabic) "بين " else "between "
        HistoryDatePrecision.Disputed ->
            if (language == HistoryLanguage.Arabic) "تاريخ مختلف فيه: " else "disputed date: "
    }
    val suffix = if (language == HistoryLanguage.Arabic) " م" else " CE"
    return "$prefix$range$suffix"
}

private fun eventCategoryLabel(
    category: HistoricalEventCategory?,
    language: HistoryLanguage,
): String =
    when (category) {
        null -> if (language == HistoryLanguage.Arabic) "كل الأنواع" else "All types"
        HistoricalEventCategory.ReligiousAndCommunity ->
            if (language == HistoryLanguage.Arabic) "ديني ومجتمعي" else "Religion & community"
        HistoricalEventCategory.PoliticalTransition ->
            if (language == HistoryLanguage.Arabic) "تحول سياسي" else "Political transition"
        HistoricalEventCategory.Conflict ->
            if (language == HistoryLanguage.Arabic) "نزاع ومعركة" else "Conflict"
        HistoricalEventCategory.FoundationAndUrbanism ->
            if (language == HistoryLanguage.Arabic) "تأسيس وعمران" else "Foundation & urbanism"
        HistoricalEventCategory.KnowledgeAndCulture ->
            if (language == HistoryLanguage.Arabic) "علم وثقافة" else "Knowledge & culture"
        HistoricalEventCategory.InstitutionalChange ->
            if (language == HistoryLanguage.Arabic) "تحول مؤسسي" else "Institutional change"
    }

private fun eventEraLabel(
    eraId: String?,
    language: HistoryLanguage,
): String {
    if (eraId == null) {
        return if (language == HistoryLanguage.Arabic) "كل الحقب" else "All eras"
    }
    return IslamicHistoryContent.eraById(eraId)?.title?.resolve(language) ?: eraId
}
