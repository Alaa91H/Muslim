package org.example.islamicapp.feature.hadith.ui

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.hadith.R
import org.example.islamicapp.feature.hadith.domain.Hadith

/**
 * Hadith hub (Phase 3 + the complete-books request): tab 0 = curated
 * fully-sourced selections; tab 1 = the complete library — Nawawi/Qudsi
 * bundled offline, the six books downloaded once then cached forever.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val libraryState by viewModel.libraryState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.hadith_title)) })
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = state.tab) {
                Tab(
                    selected = state.tab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text(stringResource(R.string.hadith_tab_selection)) },
                )
                Tab(
                    selected = state.tab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text(stringResource(R.string.hadith_tab_library)) },
                )
            }
            if (state.tab == 0) {
                SelectionTab(state, viewModel)
            } else {
                LibraryTab(libraryState, viewModel)
            }
        }
    }
}

@Composable
private fun SelectionTab(state: HadithUiState, viewModel: HadithViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.hadith_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = state.bookmarksOnly,
                        onClick = { viewModel.setBookmarksOnly(!state.bookmarksOnly) },
                        label = { Text(stringResource(R.string.hadith_bookmarks)) },
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.hadith_daily_notification),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Switch(
                        checked = state.dailyNotificationEnabled,
                        onCheckedChange = viewModel::setDailyNotification,
                    )
                }
            }
            items(state.hadiths, key = { it.id }) { hadith ->
                HadithCard(
                    hadith = hadith,
                    expanded = state.expandedId == hadith.id,
                    bookmarked = hadith.id in state.bookmarks,
                    onToggleExpand = { viewModel.toggleExpanded(hadith.id) },
                    onToggleBookmark = { viewModel.toggleBookmark(hadith.id) },
                )
            }
    }
}

/**
 * The complete-books library: catalog of the collections; a book opens
 * into its full text with kitab headers and instant search.
 */
@Composable
private fun LibraryTab(state: HadithUiState, viewModel: HadithViewModel) {
    val openBook = state.openBook

    if (openBook == null) {
        LibraryCatalog(state, viewModel)
    } else {
        BookReader(openBook, state, viewModel)
    }
}

@Composable
private fun LibraryCatalog(state: HadithUiState, viewModel: HadithViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.hadith_library_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.loadingBook) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.padding(end = 12.dp).height(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(stringResource(R.string.hadith_downloading))
                }
            }
        }
        state.libraryError?.let { error ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.hadith_download_error, error),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        items(viewModel.library.catalog, key = { it.id }) { book ->
            Card(onClick = { viewModel.openBookById(book.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(book.titleAr, style = MaterialTheme.typography.titleMedium)
                        Text(
                            book.authorAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        stringResource(
                            if (viewModel.library.isDownloaded(book)) R.string.hadith_available
                            else R.string.hadith_needs_download,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookReader(
    book: org.example.islamicapp.feature.hadith.data.LoadedBook,
    state: HadithUiState,
    viewModel: HadithViewModel,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sections = remember(book) { book.bySection() }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                book.book.titleAr,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = false,
                onClick = viewModel::closeBook,
                label = { Text(stringResource(R.string.hadith_close_book)) },
            )
        }
        OutlinedTextField(
            value = state.bookQuery,
            onValueChange = viewModel::setBookQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.hadith_book_search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
        )
        if (state.bookQuery.isNotBlank()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.bookResults, key = { "q" + it.hadithnumber }) { h ->
                    LibraryHadithCard(h) { share(context, h, book) }
                }
                if (state.bookResults.isEmpty()) {
                    item { Text(stringResource(R.string.hadith_no_results)) }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEachIndexed { sectionIndex, (title, hadiths) ->
                    if (title.isNotBlank()) {
                        item(key = "s$sectionIndex") {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }
                    items(hadiths, key = { sectionIndex.toString() + ":" + it.hadithnumber }) { h ->
                        LibraryHadithCard(h) { share(context, h, book) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryHadithCard(
    hadith: org.example.islamicapp.feature.hadith.data.HadithDto,
    onShare: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.hadith_number, hadith.hadithnumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.hadith_share),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                hadith.text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
            )
            hadith.grades.firstOrNull()?.let { grade ->
                Spacer(Modifier.height(8.dp))
                Text(
                    listOfNotNull(grade.attr, grade.grade).joinToString(" — "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun share(
    context: android.content.Context,
    hadith: org.example.islamicapp.feature.hadith.data.HadithDto,
    book: org.example.islamicapp.feature.hadith.data.LoadedBook,
) {
    val text = "${book.book.titleAr} — ${context.getString(R.string.hadith_number, hadith.hadithnumber)}\n\n${hadith.text}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

@Composable
private fun HadithCard(
    hadith: Hadith,
    expanded: Boolean,
    bookmarked: Boolean,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val context = LocalContext.current

    Card(onClick = onToggleExpand, modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    hadith.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.hadith_bookmark),
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { share(context, hadith) }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.hadith_share),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                hadith.text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    "${hadith.reference} • ${hadith.gradeAr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    hadith.narrator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun share(context: android.content.Context, hadith: Hadith) {
    val text = "${hadith.titleAr}\n\n${hadith.text}\n\n${hadith.reference}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
