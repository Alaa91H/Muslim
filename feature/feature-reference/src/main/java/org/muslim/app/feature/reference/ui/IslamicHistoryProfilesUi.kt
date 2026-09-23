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
import org.muslim.app.feature.reference.domain.HistoricalPlaceProfile
import org.muslim.app.feature.reference.domain.HistoryArticleSection
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistoryPerson
import org.muslim.app.feature.reference.domain.HistoryPersonProfile
import org.muslim.app.feature.reference.domain.HistorySource
import org.muslim.app.feature.reference.domain.IslamicCivilizationContent
import org.muslim.app.feature.reference.domain.IslamicHistoricalEvents
import org.muslim.app.feature.reference.domain.IslamicHistoryContent
import org.muslim.app.feature.reference.domain.IslamicHistoryProfiles
import org.muslim.app.feature.reference.domain.IslamicHistorySources
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

@Composable
internal fun HistoryPeopleProfilesTab(
    language: HistoryLanguage,
    target: HistoryNavigationTarget?,
    onTargetConsumed: () -> Unit,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(target) {
        if (target?.type == HistoryTargetType.Person) {
            selectedPersonId = target.id
            onTargetConsumed()
        }
    }
    val profile by rememberHistoryPersonProfile(selectedPersonId)

    if (selectedPersonId != null) {
        if (profile == null) {
            HistoryContentLoading()
            return
        }
        PersonProfileView(
            profile = requireNotNull(profile),
            language = language,
            onBack = { selectedPersonId = null },
            onNavigate = onNavigate,
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
                onOpen = { selectedPersonId = person.id },
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
    onNavigate: (HistoryNavigationTarget) -> Unit,
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
            PersonRelationsCard(
                profile = profile,
                language = language,
                onNavigate = onNavigate,
            )
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
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    val profile by rememberHistoricalPlaceProfile(placeId)
    val place = IslamicHistoryProfiles.atlasPlaceById(placeId) ?: return
    if (profile == null) {
        HistoryContentLoading()
        return
    }
    val loadedProfile = requireNotNull(profile)
    val sources = loadedProfile.sourceIds.mapNotNull(IslamicHistorySources::byId)
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
                summary = loadedProfile.overview.resolve(language),
            )
        }
        items(loadedProfile.sections, key = { it.id }) { section ->
            ProfileSectionCard(section = section, language = language)
        }
        item {
            PlaceRelationsCard(
                profile = loadedProfile,
                language = language,
                onNavigate = onNavigate,
            )
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
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    val links = buildList {
        profile.stateIds.forEach { id ->
            IslamicHistoryStates.byId(id)?.let { state ->
                add(
                    ProfileLink(
                        label = state.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "دولة" else "State",
                        target = HistoryNavigationTarget(HistoryTargetType.State, id),
                    ),
                )
            }
        }
        profile.placeIds.forEach { id ->
            IslamicHistoryProfiles.atlasPlaceById(id)?.let { place ->
                add(
                    ProfileLink(
                        label = place.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "مكان" else "Place",
                        target = HistoryNavigationTarget(HistoryTargetType.Place, id),
                    ),
                )
            }
        }
        profile.eventIds.forEach { id ->
            IslamicHistoricalEvents.byId(id)?.let { event ->
                add(
                    ProfileLink(
                        label = event.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "حدث" else "Event",
                        target = HistoryNavigationTarget(HistoryTargetType.Event, id),
                    ),
                )
            }
        }
        profile.relatedTopicIds.forEach { id ->
            IslamicCivilizationContent.byId(id)?.let { topic ->
                add(
                    ProfileLink(
                        label = topic.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "حضارة" else "Civilization",
                        target = HistoryNavigationTarget(HistoryTargetType.CivilizationTopic, id),
                    ),
                )
            }
        }
    }
    ProfileRelationsCard(links = links, language = language, onNavigate = onNavigate)
}

@Composable
private fun PlaceRelationsCard(
    profile: HistoricalPlaceProfile,
    language: HistoryLanguage,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    val links = buildList {
        profile.stateIds.forEach { id ->
            IslamicHistoryStates.byId(id)?.let { state ->
                add(
                    ProfileLink(
                        label = state.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "دولة" else "State",
                        target = HistoryNavigationTarget(HistoryTargetType.State, id),
                    ),
                )
            }
        }
        profile.eventIds.forEach { id ->
            IslamicHistoricalEvents.byId(id)?.let { event ->
                add(
                    ProfileLink(
                        label = event.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "حدث" else "Event",
                        target = HistoryNavigationTarget(HistoryTargetType.Event, id),
                    ),
                )
            }
        }
        profile.relatedPersonIds.forEach { id ->
            IslamicHistoryContent.personalities.firstOrNull { it.id == id }?.let { person ->
                add(
                    ProfileLink(
                        label = person.name.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "شخصية" else "Person",
                        target = HistoryNavigationTarget(HistoryTargetType.Person, id),
                    ),
                )
            }
        }
        profile.relatedTopicIds.forEach { id ->
            IslamicCivilizationContent.byId(id)?.let { topic ->
                add(
                    ProfileLink(
                        label = topic.title.resolve(language),
                        typeLabel = if (language == HistoryLanguage.Arabic) "حضارة" else "Civilization",
                        target = HistoryNavigationTarget(HistoryTargetType.CivilizationTopic, id),
                    ),
                )
            }
        }
    }
    ProfileRelationsCard(links = links, language = language, onNavigate = onNavigate)
}

private data class ProfileLink(
    val label: String,
    val typeLabel: String,
    val target: HistoryNavigationTarget,
)

@Composable
private fun ProfileRelationsCard(
    links: List<ProfileLink>,
    language: HistoryLanguage,
    onNavigate: (HistoryNavigationTarget) -> Unit,
) {
    if (links.isEmpty()) return
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (language == HistoryLanguage.Arabic) "روابط مرتبطة" else "Related entries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            links.forEach { link ->
                TextButton(onClick = { onNavigate(link.target) }) {
                    Text("${link.typeLabel}: ${link.label}")
                }
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
