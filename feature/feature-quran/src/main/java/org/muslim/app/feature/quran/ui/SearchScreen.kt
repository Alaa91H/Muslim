package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.QuranWordSearch

/**
 * Full-text search over the Quran (PROJECT_PROMPT.md §6: بحث نصي).
 *
 * - Two match modes: root-friendly PREFIX (matches the FTS `token*`
 *   behaviour) and precise EXACT (whole word only — "الله" does not include
 *   بالله or اللهم).
 * - Every result reports how many times the word occurs in that ayah, with
 *   the matched words highlighted.
 * - A "where it occurs" breakdown groups the matches by surah with per-surah
 *   occurrence counts.
 * - Tapping a result opens the reader scrolled to the matched ayah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenAyah: (surahNumber: Int, globalNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mode by viewModel.matchMode.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val history by viewModel.searchHistory.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val wordInfo by viewModel.wordInfo.collectAsStateWithLifecycle()
    val indexProgress by viewModel.indexProgress.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                DigitNormalizedOutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.query.value = it },
                    placeholder = { Text(stringResource(R.string.quran_search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.saveCurrentSearch() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.quran_back))
                }
            },
        )

        // Mode switch: prefix (root) vs exact word matching.
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = mode == QuranWordSearch.MatchMode.PREFIX,
                onClick = { viewModel.matchMode.value = QuranWordSearch.MatchMode.PREFIX },
                label = { Text(stringResource(R.string.quran_search_prefix)) },
            )
            Spacer(Modifier.padding(start = 8.dp))
            FilterChip(
                selected = mode == QuranWordSearch.MatchMode.EXACT,
                onClick = { viewModel.matchMode.value = QuranWordSearch.MatchMode.EXACT },
                label = { Text(stringResource(R.string.quran_search_exact)) },
            )
        }

        // Live autocomplete from the most frequent mushaf words.
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { word ->
                    SuggestionChip(text = word, onClick = { viewModel.applySuggestion(word) })
                }
            }
        }

        // Root + derived inflections of the picked suggestion (معجم القرآن).
        val selectedWordInfo = wordInfo
        if (selectedWordInfo != null) {
            WordInfoCard(
                info = selectedWordInfo,
                onPickWord = viewModel::applySuggestion,
            )
        }

        when {
            state.searching -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    if (state.indexBuilding && indexProgress < 100) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { indexProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(6.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.quran_search_index_progress,
                                indexProgress,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            state.idle -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = stringResource(R.string.quran_search_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                    if (history.isNotEmpty()) {
                        SearchHistorySection(
                            history = history,
                            onSearch = viewModel::searchFromHistory,
                            onClear = viewModel::clearHistory,
                        )
                    }
                }
            }
            state.matches.isEmpty() -> {
                Text(
                    text = stringResource(R.string.quran_search_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = stringResource(
                                    R.string.quran_search_summary,
                                    state.occurrences,
                                    state.matches.size,
                                    state.surahBreakdown.size,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.quran_search_metrics, state.elapsedMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            state.exactWordFrequency?.let { frequency ->
                                Spacer(Modifier.height(8.dp))
                                SearchWordFrequencyCard(query = query, frequency = frequency)
                            }
                        }
                    }
                    if (state.surahBreakdown.isNotEmpty()) {
                        item {
                            SurahBreakdownHeader(state.surahBreakdown)
                        }
                    }
                    items(state.matches, key = { it.ayah.globalNumber }) { match ->
                        SearchResultRow(
                            match = match,
                            highlightColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                viewModel.saveCurrentSearch()
                                onOpenAyah(match.ayah.surahNumber, match.ayah.globalNumber)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Makes whole-mushaf word frequency visible in the same result stream as
 * search. The number is intentionally shown only for one exact token; phrases
 * and root-prefix searches retain their result-specific occurrence total.
 */
@Composable
private fun SearchWordFrequencyCard(query: String, frequency: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.quran_frequency_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$query · ${stringResource(R.string.quran_search_occurrences, frequency)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** One live autocomplete suggestion (a frequent mushaf word). */
@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodyMedium) },
    )
}

/**
 * Word-lookup card for the picked suggestion: the estimated root plus the
 * derived inflections that share it (معجم القرآن). Tapping a derivation
 * searches it directly.
 */
@Composable
private fun WordInfoCard(
    info: SearchViewModel.WordInfo,
    onPickWord: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = if (info.root.isNotBlank()) {
                stringResource(R.string.quran_search_word_root, info.root)
            } else {
                stringResource(R.string.quran_search_no_root)
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (info.derivations.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.quran_search_derivations),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                info.derivations.forEach { derivation ->
                    SuggestionChip(text = derivation, onClick = { onPickWord(derivation) })
                }
            }
        }
    }
}

/** "أين ذُكرت" — expandable per-surah distribution of the matches. */
@Composable
private fun SurahBreakdownHeader(breakdown: List<QuranWordSearch.SurahOccurrence>) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.quran_search_where),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        if (expanded) {
            breakdown.forEachIndexed { index, surah ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.quran_search_surah_item, surah.surahNumber, surah.surahName),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.quran_search_occurrences, surah.occurrences),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (index < breakdown.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    match: SearchViewModel.Match,
    highlightColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = highlight(match.ayah.text, match.spans, highlightColor),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.padding(start = 8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(
                    R.string.quran_search_ref,
                    match.ayah.surahNumber.toString(),
                    match.ayah.numberInSurah.toString(),
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (match.occurrences > 1) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.quran_search_occurrences, match.occurrences),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Bold + colored the matched word spans in the raw Uthmani text. */
private fun highlight(text: String, spans: List<IntRange>, color: androidx.compose.ui.graphics.Color): AnnotatedString =
    buildAnnotatedString {
        if (spans.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }
        var cursor = 0
        for (span in spans) {
            if (span.first > cursor) append(text.substring(cursor, span.first))
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(text.substring(span.first, span.last + 1))
            }
            cursor = span.last + 1
        }
        if (cursor < text.length) append(text.substring(cursor))
    }


/** Quick re-run list for previously committed queries (shown while idle). */
@Composable
private fun SearchHistorySection(
    history: List<String>,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.quran_search_history),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.quran_search_history_clear))
            }
        }
        history.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearch(entry) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.padding(start = 12.dp))
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
