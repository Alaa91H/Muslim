package org.muslim.app.feature.scholarlibrary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.feature.scholarlibrary.R
import org.muslim.app.feature.scholarlibrary.domain.FlashcardWithCitation
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewEvent
import org.muslim.app.feature.scholarlibrary.domain.ScholarReviewRating
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyAnalytics
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPath

private data class ReviewCenterSelection(
    val categoryName: String?,
    val pathId: String?,
    val bookId: String?,
    val revealedCardId: Long?,
)

private data class ReviewCenterActions(
    val onBack: () -> Unit,
    val onCategory: (String?) -> Unit,
    val onPath: (String?) -> Unit,
    val onBook: (String?) -> Unit,
    val onClear: () -> Unit,
    val onReveal: (Long) -> Unit,
    val onRate: (Long, ScholarReviewRating) -> Unit,
)

private data class ReviewCenterDerivedState(
    val queue: List<FlashcardWithCitation>,
    val filterBooks: List<ScholarBook>,
    val relevantBookCount: Int,
)

@Composable
fun ScholarReviewCenterScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var pathId by rememberSaveable { mutableStateOf<String?>(null) }
    var bookId by rememberSaveable { mutableStateOf<String?>(null) }
    var revealedCardId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }

    ReviewCenterLayout(
        state = state,
        selection = ReviewCenterSelection(categoryName, pathId, bookId, revealedCardId),
        actions = ReviewCenterActions(
            onBack = onBack,
            onCategory = {
                categoryName = it
                bookId = null
                revealedCardId = null
            },
            onPath = {
                pathId = it
                bookId = null
                revealedCardId = null
            },
            onBook = {
                bookId = it
                revealedCardId = null
            },
            onClear = {
                categoryName = null
                pathId = null
                bookId = null
                revealedCardId = null
            },
            onReveal = { revealedCardId = it },
            onRate = { cardId, rating ->
                revealedCardId = null
                viewModel.reviewFlashcard(cardId, rating)
            },
        ),
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewCenterLayout(
    state: ScholarLibraryUiState,
    selection: ReviewCenterSelection,
    actions: ReviewCenterActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
) {
    val derived = deriveReviewCenterState(state, selection)
    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scholar_library_review_center)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scholar_library_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            reviewCenterItems(state, selection, actions, derived)
        }
    }
}

private fun deriveReviewCenterState(
    state: ScholarLibraryUiState,
    selection: ReviewCenterSelection,
): ReviewCenterDerivedState {
    val category = selection.categoryName?.let { ScholarCategory.fromId(it) }
    val path = state.studyPaths.firstOrNull { it.id == selection.pathId }
    val queue = ScholarStudyAnalytics.dueCards(
        cards = state.flashcards,
        nowEpochMillis = System.currentTimeMillis(),
        category = category,
        path = path,
        bookId = selection.bookId,
    )
    val filterBooks = state.books.filter { book ->
        state.flashcards.any { it.bookId == book.id } &&
            (selection.categoryName == null || book.category.name == selection.categoryName) &&
            (path == null || path.containsBook(book.id))
    }
    return ReviewCenterDerivedState(
        queue = queue,
        filterBooks = filterBooks,
        relevantBookCount = queue.map { it.bookId }.distinct().size,
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.reviewCenterItems(
    state: ScholarLibraryUiState,
    selection: ReviewCenterSelection,
    actions: ReviewCenterActions,
    derived: ReviewCenterDerivedState,
) {
    item { ReviewActivityCard(state) }
    item {
        ReviewFilters(
            state = state,
            filterState = ReviewFilterState(
                selectedCategoryName = selection.categoryName,
                selectedPathId = selection.pathId,
                selectedBookId = selection.bookId,
                filterBooks = derived.filterBooks,
            ),
            actions = ReviewFilterActions(
                onCategory = actions.onCategory,
                onPath = actions.onPath,
                onBook = actions.onBook,
                onClear = actions.onClear,
            ),
        )
    }
    item {
        FocusedReviewQueue(
            queue = derived.queue,
            revealedCardId = selection.revealedCardId,
            onReveal = actions.onReveal,
            onRate = actions.onRate,
        )
    }
    item {
        Text(
            stringResource(
                R.string.scholar_library_review_filter_match,
                derived.queue.size,
                derived.relevantBookCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    item {
        Text(
            stringResource(R.string.scholar_library_recent_review_activity),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    items(state.reviewEvents.take(10), key = { "review_event_${it.id}" }) { event ->
        ReviewHistoryCard(event = event, state = state)
    }
    if (state.reviewEvents.isEmpty()) {
        item {
            Text(
                stringResource(R.string.scholar_library_no_review_activity),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReviewActivityCard(state: ScholarLibraryUiState) {
    val summary = state.studyActivitySummary
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                stringResource(R.string.scholar_library_activity_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(
                    R.string.scholar_library_activity_reviews,
                    summary.review.reviewsToday,
                    summary.review.reviewsLast7Days,
                    summary.review.cardsReviewedLast7Days,
                ),
            )
            Text(
                stringResource(
                    R.string.scholar_library_activity_sessions,
                    summary.study.completedSessionsLast7Days,
                    summary.study.studiedPassagesLast7Days,
                    summary.study.dueCards,
                ),
            )
            Text(
                stringResource(
                    R.string.scholar_library_activity_ratings,
                    summary.review.againLast7Days,
                    summary.review.hardLast7Days,
                    summary.review.goodLast7Days,
                    summary.review.easyLast7Days,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class ReviewFilterState(
    val selectedCategoryName: String?,
    val selectedPathId: String?,
    val selectedBookId: String?,
    val filterBooks: List<ScholarBook>,
)

private data class ReviewFilterActions(
    val onCategory: (String?) -> Unit,
    val onPath: (String?) -> Unit,
    val onBook: (String?) -> Unit,
    val onClear: () -> Unit,
)

@Composable
private fun ReviewFilters(
    state: ScholarLibraryUiState,
    filterState: ReviewFilterState,
    actions: ReviewFilterActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.scholar_library_review_filters),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (
                filterState.selectedCategoryName != null ||
                filterState.selectedPathId != null ||
                filterState.selectedBookId != null
            ) {
                OutlinedButton(onClick = actions.onClear) {
                    Text(stringResource(R.string.scholar_library_clear_filters))
                }
            }
        }
        FilterRow(
            title = stringResource(R.string.scholar_library_filter_by_category),
            allLabel = stringResource(R.string.scholar_library_all_categories),
            selectedId = filterState.selectedCategoryName,
            options = state.flashcards.map { it.category }.distinct().map { it.name to it.label },
            onSelected = actions.onCategory,
        )
        FilterRow(
            title = stringResource(R.string.scholar_library_filter_by_path),
            allLabel = stringResource(R.string.scholar_library_all_paths),
            selectedId = filterState.selectedPathId,
            options = state.studyPaths
                .filter { path -> state.flashcards.any { path.containsBook(it.bookId) } }
                .map { it.id to it.title },
            onSelected = actions.onPath,
        )
        FilterRow(
            title = stringResource(R.string.scholar_library_filter_by_book),
            allLabel = stringResource(R.string.scholar_library_all_books),
            selectedId = filterState.selectedBookId,
            options = filterState.filterBooks.map { it.id to it.title },
            onSelected = actions.onBook,
        )
    }
}

@Composable
private fun FilterRow(
    title: String,
    allLabel: String,
    selectedId: String?,
    options: List<Pair<String, String>>,
    onSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedId == null,
                    onClick = { onSelected(null) },
                    label = { Text(allLabel) },
                )
            }
            items(options, key = { it.first }) { option ->
                FilterChip(
                    selected = selectedId == option.first,
                    onClick = { onSelected(if (selectedId == option.first) null else option.first) },
                    label = { Text(option.second) },
                )
            }
        }
    }
}

@Composable
private fun FocusedReviewQueue(
    queue: List<FlashcardWithCitation>,
    revealedCardId: Long?,
    onReveal: (Long) -> Unit,
    onRate: (Long, ScholarReviewRating) -> Unit,
) {
    val card = queue.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.scholar_library_focused_review),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (card == null) {
                Text(stringResource(R.string.scholar_library_review_queue_empty))
                return@Column
            }
            Text(
                stringResource(R.string.scholar_library_review_queue_remaining, queue.size),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(card.card.front, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (revealedCardId == card.card.id) {
                Text(card.card.back, style = MaterialTheme.typography.bodyLarge)
                Text(
                    card.citation.compactLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReviewRatingButton(
                        label = stringResource(R.string.scholar_library_rating_again),
                        rating = ScholarReviewRating.Again,
                        onRate = { onRate(card.card.id, it) },
                    )
                    ReviewRatingButton(
                        label = stringResource(R.string.scholar_library_rating_hard),
                        rating = ScholarReviewRating.Hard,
                        onRate = { onRate(card.card.id, it) },
                    )
                    Button(onClick = { onRate(card.card.id, ScholarReviewRating.Good) }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.scholar_library_rating_good))
                    }
                    ReviewRatingButton(
                        label = stringResource(R.string.scholar_library_rating_easy),
                        rating = ScholarReviewRating.Easy,
                        onRate = { onRate(card.card.id, it) },
                    )
                }
            } else {
                Button(onClick = { onReveal(card.card.id) }) {
                    Text(stringResource(R.string.scholar_library_show_answer))
                }
            }
        }
    }
}

@Composable
private fun ReviewRatingButton(
    label: String,
    rating: ScholarReviewRating,
    onRate: (ScholarReviewRating) -> Unit,
) {
    OutlinedButton(onClick = { onRate(rating) }) {
        Text(label)
    }
}

@Composable
private fun ReviewHistoryCard(
    event: ScholarReviewEvent,
    state: ScholarLibraryUiState,
) {
    val bookTitle = state.books.firstOrNull { it.id == event.bookId }?.title ?: event.bookId
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(bookTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(event.category.label, style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(
                    R.string.scholar_library_review_history_item,
                    reviewRatingLabel(event.outcome.rating),
                    event.outcome.scheduledIntervalDays,
                    event.outcome.lapseCountAfterReview,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun reviewRatingLabel(rating: ScholarReviewRating): String = when (rating) {
    ScholarReviewRating.Again -> stringResource(R.string.scholar_library_rating_again)
    ScholarReviewRating.Hard -> stringResource(R.string.scholar_library_rating_hard)
    ScholarReviewRating.Good -> stringResource(R.string.scholar_library_rating_good)
    ScholarReviewRating.Easy -> stringResource(R.string.scholar_library_rating_easy)
}

private fun ScholarStudyPath.containsBook(bookId: String): Boolean =
    stages.any { stage -> bookId in stage.bookIds }
