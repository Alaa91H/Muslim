package org.muslim.app.feature.reference.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.muslim.app.feature.reference.domain.HistoricalPlaceProfile
import org.muslim.app.feature.reference.domain.HistoryArticleSection
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryPerson
import org.muslim.app.feature.reference.domain.HistoryPersonProfile
import org.muslim.app.feature.reference.domain.HistorySource
import org.muslim.app.feature.reference.domain.IslamicHistoricalEvents
import org.muslim.app.feature.reference.domain.IslamicHistoryContent
import org.muslim.app.feature.reference.domain.IslamicHistoryProfiles
import org.muslim.app.feature.reference.domain.IslamicHistorySources
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

@Composable
internal fun HistoryPeopleProfilesTab(language: HistoryLanguage) {
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    val profile = selectedPersonId?.let(IslamicHistoryProfiles::personById)

    if (profile != null) {
        PersonProfileView(
            profile = profile,
            language = language,
            onBack = { selectedPersonId = null },
        )
        return
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ProfileNotice(stringResource(R.string.history_people_intro))
        }
        items(IslamicHistoryContent.personalities, key = { it.id }) { person ->
            PersonProfileCard(
                person = person,
                language = language,
                onOpen = IslamicHistoryProfiles.personById(person.id)?.let {
                    { selectedPersonId = person.id }
                },
            )
        }
        item {
            ProfileNotice(stringResource(R.string.history_sources_notice))
        }
    }
}

@Composable
private fun PersonProfileCard(
    person: HistoryPerson,
    language: HistoryLanguage,
    onOpen: (() -> Unit)?,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
            Text(
                text = person.summary.resolve(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (onOpen != null) {
                TextButton(
                    onClick = onOpen,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Text(
                        if (language == HistoryLanguage.Arabic) {
                            "فتح الملف الكامل"
                        } else {
                            "Open full profile"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonProfileView(
    profile: HistoryPersonProfile,
    language: HistoryLanguage,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val person = IslamicHistoryContent.personalities.first { it.id == profile.personId }
    val sources = profile.sourceIds.mapNotNull(IslamicHistorySources::byId)

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text(
                    if (language == HistoryLanguage.Arabic) {
                        "العودة إلى الشخصيات"
                    } else {
                        "Back to people"
                    },
                )
            }
        }
        item {
            ProfileHeader(
                title = person.name.resolve(language),
                subtitle = person.years,
                summary = profile.overview.resolve(language),
            )
        }
        items(profile.sections, key = { it.id }) { section ->
            ProfileSectionCard(section = section, language = language)
        }
        item {
            PersonRelationsCard(profile = profile, language = language)
        }
        if (sources.isNotEmpty()) {
            item { ProfileSourcesHeading(language) }
            items(sources, key = { it.id }) { source ->
                ProfileSourceCard(source = source, language = language)
            }
        }
    }
}

@Composable
internal fun HistoryPlaceProfileView(
    placeId: String,
    language: HistoryLanguage,
    onBack: () -> Unit,
) {
    val profile = IslamicHistoryProfiles.placeById(placeId) ?: return
    val place = IslamicHistoryProfiles.atlasPlaceById(placeId) ?: return
    val sources = profile.sourceIds.mapNotNull(IslamicHistorySources::byId)
    BackHandler(onBack = onBack)

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text(
                    if (language == HistoryLanguage.Arabic) {
                        "العودة إلى الأطلس"
                    } else {
                        "Back to atlas"
                    },
                )
            }
        }
        item {
            ProfileHeader(
                title = place.title.resolve(language),
                subtitle = coordinateLabel(place.coordinate.latitude, place.coordinate.longitude),
                summary = profile.overview.resolve(language),
            )
        }
        items(profile.sections, key = { it.id }) { section ->
            ProfileSectionCard(section = section, language = language)
        }
        item {
            PlaceRelationsCard(profile = profile, language = language)
        }
        if (sources.isNotEmpty()) {
            item { ProfileSourcesHeading(language) }
            items(sources, key = { it.id }) { source ->
                ProfileSourceCard(source = source, language = language)
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    title: String,
    subtitle: String,
    summary: String,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun ProfileSectionCard(
    section: HistoryArticleSection,
    language: HistoryLanguage,
) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = section.title.resolve(language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            section.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph.resolve(language),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PersonRelationsCard(
    profile: HistoryPersonProfile,
    language: HistoryLanguage,
) {
    val states = profile.stateIds.mapNotNull { IslamicHistoryStates.byId(it)?.title?.resolve(language) }
    val places = profile.placeIds.mapNotNull {
        IslamicHistoryProfiles.atlasPlaceById(it)?.title?.resolve(language)
    }
    val events = profile.eventIds.mapNotNull {
        IslamicHistoricalEvents.byId(it)?.title?.resolve(language)
    }
    RelationsCard(
        rows = relationRows(
            language = language,
            states = states,
            places = places,
            events = events,
        ),
    )
}

@Composable
private fun PlaceRelationsCard(
    profile: HistoricalPlaceProfile,
    language: HistoryLanguage,
) {
    val states = profile.stateIds.mapNotNull { IslamicHistoryStates.byId(it)?.title?.resolve(language) }
    val events = profile.eventIds.mapNotNull {
        IslamicHistoricalEvents.byId(it)?.title?.resolve(language)
    }
    val people = profile.relatedPersonIds.mapNotNull { id ->
        IslamicHistoryContent.personalities.firstOrNull { it.id == id }?.name?.resolve(language)
    }
    RelationsCard(
        rows = relationRows(
            language = language,
            states = states,
            events = events,
            people = people,
        ),
    )
}

private fun relationRows(
    language: HistoryLanguage,
    states: List<String> = emptyList(),
    places: List<String> = emptyList(),
    events: List<String> = emptyList(),
    people: List<String> = emptyList(),
): List<Pair<String, List<String>>> = buildList {
    if (states.isNotEmpty()) {
        add((if (language == HistoryLanguage.Arabic) "الدول المرتبطة" else "Related states") to states)
    }
    if (places.isNotEmpty()) {
        add((if (language == HistoryLanguage.Arabic) "الأماكن المرتبطة" else "Related places") to places)
    }
    if (events.isNotEmpty()) {
        add((if (language == HistoryLanguage.Arabic) "الأحداث المرتبطة" else "Related events") to events)
    }
    if (people.isNotEmpty()) {
        add((if (language == HistoryLanguage.Arabic) "الشخصيات المرتبطة" else "Related people") to people)
    }
}

@Composable
private fun RelationsCard(rows: List<Pair<String, List<String>>>) {
    if (rows.isEmpty()) return
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            rows.forEachIndexed { index, (label, values) ->
                if (index > 0) {
                    Text(text = "", modifier = Modifier.padding(top = 2.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = values.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileSourcesHeading(language: HistoryLanguage) {
    Text(
        text = if (language == HistoryLanguage.Arabic) "المصادر والمراجع" else "Sources and references",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ProfileSourceCard(
    source: HistorySource,
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
private fun ProfileNotice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun coordinateLabel(latitude: Double, longitude: Double): String =
    "%.4f, %.4f".format(latitude, longitude)
