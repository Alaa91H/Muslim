package org.muslim.app.feature.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.family.R
import org.muslim.app.feature.family.domain.FamilyGlobalSearch
import org.muslim.app.feature.family.domain.FamilyGlobalSearchHit
import org.muslim.app.feature.family.domain.FamilySearchKind
import org.muslim.app.feature.family.domain.LocalizedFamilyText

internal object FamilyUiTags {
    const val GLOBAL_SEARCH_FIELD = "family_global_search_field"
    const val GUIDE_SEARCH_FIELD = "family_guide_search_field"
    const val ARTICLE_READER = "family_article_reader"
    const val ARTICLE_ACTIONS = "family_article_actions"
}

@Composable
internal fun FamilyGlobalSearchContent(
    isArabic: Boolean,
    onOpenArticle: (String) -> Unit,
    onOpenNames: () -> Unit,
    onOpenRuqyah: () -> Unit,
    onOpenChecklist: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedKindName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedKind = selectedKindName?.let { name ->
        FamilySearchKind.entries.firstOrNull { it.name == name }
    }
    val results = remember(query, selectedKindName) {
        FamilyGlobalSearch.search(query = query, kind = selectedKind)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FamilyGlobalSearchControls(
                query = query,
                selectedKind = selectedKind,
                isArabic = isArabic,
                onQueryChange = { query = it },
                onKindChange = { selectedKindName = it?.name },
            )
        }
        familyGlobalSearchResults(
            query = query,
            results = results,
            isArabic = isArabic,
            onOpenArticle = onOpenArticle,
            onOpenNames = onOpenNames,
            onOpenRuqyah = onOpenRuqyah,
            onOpenChecklist = onOpenChecklist,
        )
    }
}

@Composable
private fun FamilyGlobalSearchControls(
    query: String,
    selectedKind: FamilySearchKind?,
    isArabic: Boolean,
    onQueryChange: (String) -> Unit,
    onKindChange: (FamilySearchKind?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MuslimStateSurface(
            title = stringResource(R.string.family_global_search_title),
            supportingText = stringResource(R.string.family_global_search_intro),
            tone = MuslimStateTone.Information,
            icon = Icons.Filled.Search,
        )
        DigitNormalizedOutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FamilyUiTags.GLOBAL_SEARCH_FIELD),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.family_global_search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        )
        FamilySearchKindFilters(
            selectedKind = selectedKind,
            isArabic = isArabic,
            onSelected = onKindChange,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.familyGlobalSearchResults(
    query: String,
    results: List<FamilyGlobalSearchHit>,
    isArabic: Boolean,
    onOpenArticle: (String) -> Unit,
    onOpenNames: () -> Unit,
    onOpenRuqyah: () -> Unit,
    onOpenChecklist: (String) -> Unit,
) {
    if (query.isBlank()) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_global_search_start_title),
                supportingText = stringResource(R.string.family_global_search_start_text),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Search,
            )
        }
        return
    }
    item {
        Text(
            text = stringResource(R.string.family_global_search_count, results.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    items(results, key = { it.kind.name + ":" + it.id }) { hit ->
        FamilyGlobalSearchResultCard(
            hit = hit,
            isArabic = isArabic,
            onClick = {
                when (hit.kind) {
                    FamilySearchKind.Article -> onOpenArticle(hit.id)
                    FamilySearchKind.BabyName -> onOpenNames()
                    FamilySearchKind.Ruqyah -> onOpenRuqyah()
                    FamilySearchKind.Checklist -> onOpenChecklist(hit.id)
                }
            },
        )
    }
    if (results.isEmpty()) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_global_search_empty),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Search,
            )
        }
    }
}

@Composable
private fun FamilySearchKindFilters(
    selectedKind: FamilySearchKind?,
    isArabic: Boolean,
    onSelected: (FamilySearchKind?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedKind == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.family_global_search_all)) },
            )
        }
        items(FamilySearchKind.entries, key = { it.name }) { kind ->
            FilterChip(
                selected = selectedKind == kind,
                onClick = { onSelected(kind) },
                label = { Text(kind.label(isArabic)) },
            )
        }
    }
}

@Composable
private fun FamilyGlobalSearchResultCard(
    hit: FamilyGlobalSearchHit,
    isArabic: Boolean,
    onClick: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (hit.kind) {
                    FamilySearchKind.Article -> Icons.AutoMirrored.Filled.MenuBook
                    FamilySearchKind.BabyName -> Icons.Filled.ChildCare
                    FamilySearchKind.Ruqyah -> Icons.Filled.HealthAndSafety
                    FamilySearchKind.Checklist -> Icons.Filled.Checklist
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = hit.title.pick(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = hit.kind.label(isArabic),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = hit.summary.pick(isArabic),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

private fun FamilySearchKind.label(isArabic: Boolean): String = when (this) {
    FamilySearchKind.Article -> if (isArabic) "مقالات" else "Articles"
    FamilySearchKind.BabyName -> if (isArabic) "أسماء" else "Names"
    FamilySearchKind.Ruqyah -> if (isArabic) "رقية" else "Ruqyah"
    FamilySearchKind.Checklist -> if (isArabic) "قوائم" else "Checklists"
}

private fun LocalizedFamilyText.pick(isArabic: Boolean): String =
    if (isArabic) arabic else english
