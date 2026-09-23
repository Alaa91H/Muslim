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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.muslim.app.feature.reference.data.IslamicHistorySearchRepository
import org.muslim.app.feature.reference.domain.HistoryLanguage
import org.muslim.app.feature.reference.domain.HistorySearchResult
import org.muslim.app.feature.reference.domain.HistorySearchType

@Composable
internal fun HistorySearchTab(
    language: HistoryLanguage,
    onOpen: (HistoryNavigationTarget) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<HistorySearchType?>(null) }
    var results by remember { mutableStateOf<List<HistorySearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val repository = remember(context) {
        IslamicHistorySearchRepository.get(context.applicationContext)
    }

    LaunchedEffect(query, selectedType, repository) {
        if (query.isBlank()) {
            results = emptyList()
            isLoading = false
        } else {
            isLoading = true
            delay(SEARCH_DEBOUNCE_MS)
            results = repository.search(query = query, type = selectedType)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = {
                Text(
                    if (language == HistoryLanguage.Arabic) {
                        "ابحث في التاريخ والحضارة"
                    } else {
                        "Search history and civilization"
                    },
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
        SearchTypeFilters(
            selectedType = selectedType,
            language = language,
            onSelect = { selectedType = it },
        )
        SearchResults(
            query = query,
            results = results,
            isLoading = isLoading,
            language = language,
            onOpen = onOpen,
        )
    }
}

@Composable
private fun SearchTypeFilters(
    selectedType: HistorySearchType?,
    language: HistoryLanguage,
    onSelect: (HistorySearchType?) -> Unit,
) {
    val types = listOf<HistorySearchType?>(null) + HistorySearchType.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        types.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onSelect(type) },
                label = { Text(searchTypeLabel(type, language)) },
            )
        }
    }
}

@Composable
private fun SearchResults(
    query: String,
    results: List<HistorySearchResult>,
    isLoading: Boolean,
    language: HistoryLanguage,
    onOpen: (HistoryNavigationTarget) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (query.isBlank()) {
            item {
                SearchNotice(
                    if (language == HistoryLanguage.Arabic) {
                        "ابحث باسم حقبة أو دولة أو حدث أو شخصية أو مدينة أو موضوع حضاري. يعمل البحث محلياً دون اتصال بالإنترنت."
                    } else {
                        "Search for an era, state, event, person, place, or civilization topic. Search works offline."
                    },
                )
            }
        } else if (isLoading) {
            item {
                SearchNotice(
                    if (language == HistoryLanguage.Arabic) {
                        "جارٍ فهرسة المحتوى والبحث…"
                    } else {
                        "Indexing content and searching…"
                    },
                )
            }
        } else if (results.isEmpty()) {
            item {
                SearchNotice(
                    if (language == HistoryLanguage.Arabic) {
                        "لا توجد نتائج مطابقة."
                    } else {
                        "No matching results."
                    },
                )
            }
        } else {
            item {
                Text(
                    text = if (language == HistoryLanguage.Arabic) {
                        "${results.size} نتيجة"
                    } else {
                        "${results.size} results"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(results, key = { "${it.type}:${it.id}" }) { result ->
                SearchResultCard(
                    result = result,
                    language = language,
                    onOpen = { onOpen(result.toNavigationTarget()) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: HistorySearchResult,
    language: HistoryLanguage,
    onOpen: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.title.resolve(language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = searchTypeLabel(result.type, language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = result.summary.resolve(language),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(
                onClick = onOpen,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    if (language == HistoryLanguage.Arabic) {
                        "فتح النتيجة"
                    } else {
                        "Open result"
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchNotice(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun searchTypeLabel(
    type: HistorySearchType?,
    language: HistoryLanguage,
): String = when (type) {
    null -> if (language == HistoryLanguage.Arabic) "الكل" else "All"
    HistorySearchType.Era -> if (language == HistoryLanguage.Arabic) "حقب" else "Eras"
    HistorySearchType.State -> if (language == HistoryLanguage.Arabic) "دول" else "States"
    HistorySearchType.Event -> if (language == HistoryLanguage.Arabic) "أحداث" else "Events"
    HistorySearchType.Person -> if (language == HistoryLanguage.Arabic) "أعلام" else "People"
    HistorySearchType.Place -> if (language == HistoryLanguage.Arabic) "أماكن" else "Places"
    HistorySearchType.CivilizationTopic ->
        if (language == HistoryLanguage.Arabic) "حضارة" else "Civilization"
}


private const val SEARCH_DEBOUNCE_MS = 180L
