package org.muslim.app.feature.scholarlibrary.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.scholarlibrary.R
import org.muslim.app.feature.scholarlibrary.domain.Citation
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarLibraryScreen(
    onBack: () -> Unit,
    onOpenBook: (String) -> Unit,
    onOpenStudyDesk: () -> Unit,
    onOpenStudyPath: (String) -> Unit,
    onOpenAuthors: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        viewModel.importPack(readSelectedPack(context, uri) ?: "{}")
    }
    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ScholarLibraryTopBar(onBack, onOpenStudyDesk, onOpenAuthors) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScholarLibraryContent(
            state = state,
            padding = padding,
            onImport = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
            onOpenStudyDesk = onOpenStudyDesk,
            onOpenBook = onOpenBook,
            onOpenStudyPath = onOpenStudyPath,
            viewModel = viewModel,
        )
    }
}

private fun readSelectedPack(context: android.content.Context, uri: Uri?): String? = uri?.let {
    runCatching {
        context.contentResolver.openInputStream(it)?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText() }
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScholarLibraryTopBar(
    onBack: () -> Unit,
    onOpenStudyDesk: () -> Unit,
    onOpenAuthors: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.scholar_library_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.scholar_library_back))
            }
        },
        actions = {
            IconButton(onClick = onOpenAuthors) {
                Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.scholar_library_authors))
            }
            IconButton(onClick = onOpenStudyDesk) {
                Icon(Icons.Filled.Bookmarks, contentDescription = stringResource(R.string.scholar_library_study_desk))
            }
        },
    )
}

@Composable
private fun ScholarLibraryContent(
    state: ScholarLibraryUiState,
    padding: PaddingValues,
    onImport: () -> Unit,
    onOpenStudyDesk: () -> Unit,
    onOpenBook: (String) -> Unit,
    onOpenStudyPath: (String) -> Unit,
    viewModel: ScholarLibraryViewModel,
) {
    if (state.loading) {
        LibraryLoading(padding)
    } else {
        ScholarLibraryCatalog(
            state = state,
            padding = padding,
            onImport = onImport,
            onOpenStudyDesk = onOpenStudyDesk,
            onOpenBook = onOpenBook,
            onOpenStudyPath = onOpenStudyPath,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun LibraryLoading(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.scholar_library_loading))
    }
}

@Composable
private fun ScholarLibraryCatalog(
    state: ScholarLibraryUiState,
    padding: PaddingValues,
    onImport: () -> Unit,
    onOpenStudyDesk: () -> Unit,
    onOpenBook: (String) -> Unit,
    onOpenStudyPath: (String) -> Unit,
    viewModel: ScholarLibraryViewModel,
) {
    val filteredBooks = remember(
        state.books,
        state.selectedCategory,
        state.selectedDifficulty,
        state.selectedAuthorName,
    ) {
        state.books.filter { book ->
            (state.selectedCategory == null || book.category == state.selectedCategory) &&
                (state.selectedDifficulty == null || book.difficulty == state.selectedDifficulty) &&
                (state.selectedAuthorName.isNullOrBlank() || book.author == state.selectedAuthorName)
        }
    }
    val progressByBook = remember(state.readingProgress) {
        state.readingProgress.associateBy { it.bookId }
    }
    val continueReading = remember(state.books, state.readingProgress) {
        val progress = state.readingProgress
            .filter { it.status == ScholarReadingStatus.InProgress }
            .sortedByDescending { it.updatedAtEpochMillis }
            .associateBy { it.bookId }
        state.books.filter { it.id in progress }.sortedByDescending { progress[it.id]?.updatedAtEpochMillis ?: 0L }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { LibraryIntroCard(state.books.size, onImport, onOpenStudyDesk) }
        item { LibrarySearchInput(state.query, viewModel::updateQuery) }
        item { LibraryCategoryFilter(state.selectedCategory, viewModel::selectCategory) }
        item {
            LibraryAdvancedFilters(
                selectedCategory = state.selectedCategory,
                selectedDifficulty = state.selectedDifficulty,
                selectedAuthorName = state.selectedAuthorName,
                authors = state.authors.map { it.name },
                onDifficultyChange = viewModel::selectDifficulty,
                onAuthorChange = viewModel::selectAuthor,
                onClear = viewModel::clearSearchFilters,
            )
        }
        if (state.query.isNotBlank()) {
            searchResultItems(state, onOpenBook)
        } else {
            if (state.studyPaths.isNotEmpty()) {
                studyPathItems(
                    paths = state.studyPaths,
                    progressByPath = state.pathProgress.associateBy { it.pathId },
                    onOpenPath = onOpenStudyPath,
                )
            }
            if (continueReading.isNotEmpty()) {
                continueReadingItems(continueReading, progressByBook, onOpenBook)
            }
            catalogItems(filteredBooks, progressByBook, onOpenBook)
        }
    }
}

@Composable
private fun LibrarySearchInput(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        label = { Text(stringResource(R.string.scholar_library_search_label)) },
        placeholder = { Text(stringResource(R.string.scholar_library_search_hint)) },
    )
}

@Composable
private fun LibraryCategoryFilter(selected: ScholarCategory?, onChange: (ScholarCategory?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onChange(null) },
                label = { Text(stringResource(R.string.scholar_library_all_categories)) },
            )
        }
        items(ScholarCategory.entries.toList(), key = { it.name }) { category ->
            FilterChip(selected == category, { onChange(category) }, label = { Text(category.label) })
        }
    }
}

@Composable
private fun LibraryAdvancedFilters(
    selectedCategory: ScholarCategory?,
    selectedDifficulty: ScholarDifficulty?,
    selectedAuthorName: String?,
    authors: List<String>,
    onDifficultyChange: (ScholarDifficulty?) -> Unit,
    onAuthorChange: (String?) -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.scholar_library_filters),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultyChange(null) },
                    label = { Text(stringResource(R.string.scholar_library_all_levels)) },
                )
            }
            items(
                listOf(
                    ScholarDifficulty.Foundation,
                    ScholarDifficulty.Intermediate,
                    ScholarDifficulty.Advanced,
                    ScholarDifficulty.Unspecified,
                ),
                key = { it.name },
            ) { level ->
                FilterChip(
                    selected = selectedDifficulty == level,
                    onClick = { onDifficultyChange(level) },
                    label = { Text(difficultyLabel(level)) },
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedAuthorName == null,
                    onClick = { onAuthorChange(null) },
                    label = { Text(stringResource(R.string.scholar_library_all_authors)) },
                )
            }
            items(authors, key = { it }) { author ->
                FilterChip(
                    selected = selectedAuthorName == author,
                    onClick = { onAuthorChange(author) },
                    label = { Text(author) },
                )
            }
        }
        if (selectedCategory != null || selectedDifficulty != null || selectedAuthorName != null) {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.scholar_library_clear_filters))
            }
        }
    }
}

private fun LazyListScope.searchResultItems(state: ScholarLibraryUiState, onOpenBook: (String) -> Unit) {
    item { SectionLabel(stringResource(R.string.scholar_library_search_results)) }
    if (state.searchResults.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_search_results)) }
    } else {
        items(state.searchResults, key = { it.passage.id }) { hit ->
            PassageCard(hit.passage, hit.citation, {}, {}, { onOpenBook(hit.passage.bookId) }, compact = true)
        }
    }
}

private fun LazyListScope.continueReadingItems(
    books: List<ScholarBook>,
    progressByBook: Map<String, ScholarReadingProgress>,
    onOpenBook: (String) -> Unit,
) {
    item { SectionLabel(stringResource(R.string.scholar_library_continue_reading)) }
    items(books, key = { "continue_${it.id}" }) { book ->
        BookCard(book, progressByBook[book.id]) { onOpenBook(book.id) }
    }
}

private fun LazyListScope.catalogItems(
    books: List<ScholarBook>,
    progressByBook: Map<String, ScholarReadingProgress>,
    onOpenBook: (String) -> Unit,
) {
    item { SectionLabel(stringResource(R.string.scholar_library_catalog)) }
    items(books, key = { it.id }) { book ->
        BookCard(book, progressByBook[book.id]) { onOpenBook(book.id) }
    }
    if (books.isEmpty()) item { EmptyState(stringResource(R.string.scholar_library_no_books)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarBookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var notePassage by remember { mutableStateOf<ScholarPassage?>(null) }
    var cardPassage by remember { mutableStateOf<ScholarPassage?>(null) }

    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }
    val book = state.selectedBook

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(book?.title ?: stringResource(R.string.scholar_library_book)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.scholar_library_back))
                    }
                },
            )
        },
    ) { padding ->
        ScholarBookDetailBody(
            book = book,
            state = state,
            padding = padding,
            viewModel = viewModel,
            onAddNote = { notePassage = it },
            onAddFlashcard = { cardPassage = it },
        )
    }

    ScholarBookDialogs(
        notePassage = notePassage,
        cardPassage = cardPassage,
        onDismissNote = { notePassage = null },
        onDismissCard = { cardPassage = null },
        viewModel = viewModel,
    )
}

@Composable
private fun ScholarBookDetailBody(
    book: ScholarBook?,
    state: ScholarLibraryUiState,
    padding: PaddingValues,
    viewModel: ScholarLibraryViewModel,
    onAddNote: (ScholarPassage) -> Unit,
    onAddFlashcard: (ScholarPassage) -> Unit,
) {
    if (book == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { BookMetadataCard(book) }
        item { BookHierarchyCard(state.selectedBookHierarchy) }
        item { SectionLabel(stringResource(R.string.scholar_library_passages)) }
        itemsIndexed(state.selectedBookPassages, key = { _, item -> item.id }) { index, passage ->
            val bookmark = state.bookmarks.any { it.bookmark.passageId == passage.id }
            val highlight = state.highlights.firstOrNull { it.highlight.passageId == passage.id }
            val reachedPercent = ((index + 1) * 100 / state.selectedBookPassages.size.coerceAtLeast(1)).coerceIn(1, 100)
            PassageCard(
                passage = passage,
                citation = Citation(
                    book.title,
                    book.author,
                    passage.chapter,
                    passage.volume,
                    passage.page,
                    book.edition,
                    book.publisher,
                    book.publicationYear,
                    passage.section,
                ),
                onAddNote = { onAddNote(passage) },
                onAddFlashcard = { onAddFlashcard(passage) },
                onOpenBook = null,
                isBookmarked = bookmark,
                isHighlighted = highlight != null,
                onToggleBookmark = { viewModel.toggleBookmark(passage.id) },
                onToggleHighlight = { viewModel.togglePassageHighlight(passage) },
                onMarkStudied = { viewModel.markStudied(book.id, passage.id, reachedPercent) },
            )
        }
    }
}

@Composable
private fun ScholarBookDialogs(
    notePassage: ScholarPassage?,
    cardPassage: ScholarPassage?,
    onDismissNote: () -> Unit,
    onDismissCard: () -> Unit,
    viewModel: ScholarLibraryViewModel,
) {
    notePassage?.let { passage ->
        TextEntryDialog(
            title = stringResource(R.string.scholar_library_add_note),
            label = stringResource(R.string.scholar_library_note_label),
            confirm = stringResource(R.string.scholar_library_save),
            onDismiss = onDismissNote,
            onConfirm = { text ->
                viewModel.addNote(passage.id, text)
                onDismissNote()
            },
        )
    }
    cardPassage?.let { passage ->
        FlashcardDialog(
            onDismiss = onDismissCard,
            onConfirm = { front, back ->
                viewModel.addFlashcard(passage.id, front, back)
                onDismissCard()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarStudyDeskScreen(
    onBack: () -> Unit,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showingAnswerFor by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scholar_library_study_desk)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.scholar_library_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            studySessionItems(state, onOpenSession)
            studyReviewItems(
                state = state,
                showingAnswerFor = showingAnswerFor,
                onShowingAnswerChange = { showingAnswerFor = it },
                viewModel = viewModel,
            )
            studyBookmarkItems(state, viewModel)
            studyHighlightItems(state, viewModel)
            studyNoteItems(state, viewModel)
        }
    }
}

private fun LazyListScope.studySessionItems(
    state: ScholarLibraryUiState,
    onOpenSession: (String) -> Unit,
) {
    val sessions = state.studySessions.take(10)
    item { SectionLabel(stringResource(R.string.scholar_library_recent_study_sessions)) }
    if (sessions.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_study_sessions)) }
        return
    }
    items(sessions, key = { "session_${it.id}" }) { session ->
        val pathTitle = state.studyPaths.firstOrNull { it.id == session.pathId }?.title ?: session.pathId
        val bookTitle = state.books.firstOrNull { it.id == session.bookId }?.title ?: session.bookId
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(pathTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(bookTitle, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(
                        R.string.scholar_library_session_history_progress,
                        session.completedPassageIds.size,
                        session.targetPassageIds.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
                when (session.status) {
                    ScholarStudySessionStatus.InProgress -> {
                        Button(onClick = { onOpenSession(session.pathId) }) {
                            Text(stringResource(R.string.scholar_library_resume_session))
                        }
                    }
                    ScholarStudySessionStatus.Completed -> {
                        Text(
                            stringResource(R.string.scholar_library_session_history_completed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    ScholarStudySessionStatus.Abandoned -> {
                        Text(
                            stringResource(R.string.scholar_library_session_history_abandoned),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.studyReviewItems(
    state: ScholarLibraryUiState,
    showingAnswerFor: Long?,
    onShowingAnswerChange: (Long?) -> Unit,
    viewModel: ScholarLibraryViewModel,
) {
    val dueCards = state.flashcards.filter { it.card.dueAtEpochMillis <= System.currentTimeMillis() }
    item {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.scholar_library_review_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.scholar_library_review_summary, dueCards.size, state.flashcards.size),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
    item { SectionLabel(stringResource(R.string.scholar_library_due_cards)) }
    if (dueCards.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_due_cards)) }
    } else {
        items(dueCards, key = { it.card.id }) { card ->
            FlashcardCard(
                card = card,
                showAnswer = showingAnswerFor == card.card.id,
                onReveal = { onShowingAnswerChange(card.card.id) },
                onRemembered = {
                    onShowingAnswerChange(null)
                    viewModel.reviewFlashcard(card.card.id, remembered = true)
                },
                onAgain = {
                    onShowingAnswerChange(null)
                    viewModel.reviewFlashcard(card.card.id, remembered = false)
                },
                onDelete = { viewModel.deleteFlashcard(card.card.id) },
            )
        }
    }
}

private fun LazyListScope.studyBookmarkItems(
    state: ScholarLibraryUiState,
    viewModel: ScholarLibraryViewModel,
) {
    item { SectionLabel(stringResource(R.string.scholar_library_bookmarks)) }
    if (state.bookmarks.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_bookmarks)) }
        return
    }
    items(state.bookmarks, key = { it.bookmark.passageId }) { bookmark ->
        Card {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CitationLabel(bookmark.citation)
                }
                IconButton(onClick = { viewModel.toggleBookmark(bookmark.bookmark.passageId) }) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = stringResource(R.string.scholar_library_remove_bookmark),
                    )
                }
            }
        }
    }
}

private fun LazyListScope.studyHighlightItems(
    state: ScholarLibraryUiState,
    viewModel: ScholarLibraryViewModel,
) {
    item { SectionLabel(stringResource(R.string.scholar_library_highlights)) }
    if (state.highlights.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_highlights)) }
        return
    }
    items(state.highlights, key = { it.highlight.id }) { highlight ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    highlight.highlight.quote,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
                CitationLabel(highlight.citation)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { viewModel.deleteHighlight(highlight.highlight.id) }) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.scholar_library_delete_highlight),
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.studyNoteItems(
    state: ScholarLibraryUiState,
    viewModel: ScholarLibraryViewModel,
) {
    item { SectionLabel(stringResource(R.string.scholar_library_notes)) }
    if (state.notes.isEmpty()) {
        item { EmptyState(stringResource(R.string.scholar_library_no_notes)) }
        return
    }
    items(state.notes, key = { it.note.id }) { note ->
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(note.note.text, style = MaterialTheme.typography.bodyLarge)
                CitationLabel(note.citation)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { viewModel.deleteNote(note.note.id) }) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.scholar_library_delete_note),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryIntroCard(
    bookCount: Int,
    onImport: () -> Unit,
    onOpenStudyDesk: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.scholar_library_intro_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.scholar_library_intro_summary, bookCount), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(stringResource(R.string.scholar_library_license_boundary), style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onImport) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.scholar_library_import_pack))
                }
                Button(onClick = onOpenStudyDesk) {
                    Icon(Icons.Filled.FlashOn, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.scholar_library_study_desk))
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: ScholarBook,
    progress: ScholarReadingProgress? = null,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    book.subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(book.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (book.imported) {
                    AssistChip(onClick = onClick, label = { Text(stringResource(R.string.scholar_library_imported)) })
                }
            }
            Text(book.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(book.category.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                progress?.let {
                    Text(
                        text = if (it.status == ScholarReadingStatus.Completed) {
                            stringResource(R.string.scholar_library_completed)
                        } else {
                            stringResource(R.string.scholar_library_progress_percent, it.progressPercent)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookMetadataCard(book: ScholarBook) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(book.author, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            book.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            book.authorDeathYearHijri?.let { Text(stringResource(R.string.scholar_library_death_year, it)) }
            Text(book.description)
            HorizontalDivider()
            Text(
                stringResource(R.string.scholar_library_level, difficultyLabel(book.difficulty)),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(stringResource(R.string.scholar_library_language, book.language), style = MaterialTheme.typography.bodySmall)
            book.publisher?.let { Text(stringResource(R.string.scholar_library_publisher, it), style = MaterialTheme.typography.bodySmall) }
            book.edition?.let { Text(stringResource(R.string.scholar_library_edition, it), style = MaterialTheme.typography.bodySmall) }
            book.editor?.let { Text(stringResource(R.string.scholar_library_editor, it), style = MaterialTheme.typography.bodySmall) }
            book.publicationYear?.let {
                Text(stringResource(R.string.scholar_library_publication_year, it), style = MaterialTheme.typography.bodySmall)
            }
            book.volumeCount?.let {
                Text(stringResource(R.string.scholar_library_volume_count, it), style = MaterialTheme.typography.bodySmall)
            }
            Text(stringResource(R.string.scholar_library_source, book.sourceName), style = MaterialTheme.typography.bodySmall)
            Text(book.licenseSummary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun difficultyLabel(difficulty: ScholarDifficulty): String = when (difficulty) {
    ScholarDifficulty.Unspecified -> stringResource(R.string.scholar_library_level_unspecified)
    ScholarDifficulty.Foundation -> stringResource(R.string.scholar_library_level_foundation)
    ScholarDifficulty.Intermediate -> stringResource(R.string.scholar_library_level_intermediate)
    ScholarDifficulty.Advanced -> stringResource(R.string.scholar_library_level_advanced)
}

@Composable
private fun PassageCard(
    passage: ScholarPassage,
    citation: Citation,
    onAddNote: () -> Unit,
    onAddFlashcard: () -> Unit,
    onOpenBook: (() -> Unit)?,
    compact: Boolean = false,
    isBookmarked: Boolean = false,
    isHighlighted: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onToggleHighlight: () -> Unit = {},
    onMarkStudied: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isHighlighted) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(passage.chapter, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            SelectionContainer { Text(passage.text, style = MaterialTheme.typography.bodyLarge) }
            CitationLabel(citation, modifier = if (onOpenBook == null) Modifier else Modifier.clickable(onClick = onOpenBook))
            if (!compact) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = onAddNote) {
                        Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.scholar_library_add_note))
                    }
                    OutlinedButton(onClick = onAddFlashcard) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.scholar_library_add_flashcard))
                    }
                    OutlinedButton(onClick = onToggleBookmark) {
                        Icon(
                            if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(
                                if (isBookmarked) R.string.scholar_library_remove_bookmark
                                else R.string.scholar_library_add_bookmark,
                            ),
                        )
                    }
                    OutlinedButton(onClick = onToggleHighlight) {
                        Icon(Icons.Filled.Highlight, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(
                                if (isHighlighted) R.string.scholar_library_remove_highlight
                                else R.string.scholar_library_add_highlight,
                            ),
                        )
                    }
                    OutlinedButton(onClick = onMarkStudied) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.scholar_library_mark_studied))
                    }
                }
            }
        }
    }
}

@Composable
private fun CitationLabel(citation: Citation, modifier: Modifier = Modifier) {
    Text(
        citation.compactLabel(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun FlashcardCard(
    card: org.muslim.app.feature.scholarlibrary.domain.FlashcardWithCitation,
    showAnswer: Boolean,
    onReveal: () -> Unit,
    onRemembered: () -> Unit,
    onAgain: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(card.card.front, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (showAnswer) {
                Text(card.card.back, style = MaterialTheme.typography.bodyLarge)
                CitationLabel(card.citation)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRemembered) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.scholar_library_remembered))
                    }
                    OutlinedButton(onClick = onAgain) {
                        Text(stringResource(R.string.scholar_library_again))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.scholar_library_delete_flashcard))
                    }
                }
            } else {
                Button(onClick = onReveal) { Text(stringResource(R.string.scholar_library_show_answer)) }
            }
        }
    }
}

@Composable
private fun TextEntryDialog(
    title: String,
    label: String,
    confirm: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.scholar_library_cancel)) } },
    )
}

@Composable
private fun FlashcardDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scholar_library_add_flashcard)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(front, { front = it }, label = { Text(stringResource(R.string.scholar_library_flashcard_front)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(back, { back = it }, label = { Text(stringResource(R.string.scholar_library_flashcard_back)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(front, back) }, enabled = front.isNotBlank() && back.isNotBlank()) {
                Text(stringResource(R.string.scholar_library_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.scholar_library_cancel)) } },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun EmptyState(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
