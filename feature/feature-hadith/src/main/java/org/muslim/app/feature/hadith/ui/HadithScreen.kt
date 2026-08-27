package org.muslim.app.feature.hadith.ui

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimCenteredStatus
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.hadith.R
import org.muslim.app.feature.hadith.data.HadithCorpusState
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithChapter
import org.muslim.app.feature.hadith.domain.HadithCollection

/** 30-minute increments across a full day, as minutes from midnight. */
private val hadithTimeOptions: List<Int> = (0 until 24 * 60 step 30).toList()

/**
 * Collection-first offline Hadith library. The catalogue has no corpus query.
 * Opening one card explicitly starts the bounded streaming import for that book;
 * a chapter subsequently exposes Room Paging rather than an in-memory list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = hiltViewModel(),
) {
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val chapter by viewModel.chapter.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val corpusState by viewModel.corpusState.collectAsStateWithLifecycle()
    val pagedHadiths = viewModel.pagedHadiths.collectAsLazyPagingItems()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val dailyNotificationEnabled by viewModel.dailyNotificationEnabled.collectAsStateWithLifecycle()
    val dailyNotificationTimeMinutes by viewModel.dailyNotificationTimeMinutes.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.hadith_copied)
    var showNotificationSettings by remember { mutableStateOf(false) }

    val closeBookOrScreen = {
        when {
            chapter != null -> viewModel.returnToIndex()
            collection != null -> viewModel.returnToCatalogue()
            else -> onBack()
        }
    }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HadithTopBar(
                title = collection?.let { stringResource(it.titleRes) } ?: stringResource(R.string.hadith_title),
                onBack = closeBookOrScreen,
                onOpenSettings = { showNotificationSettings = true },
                backDescription = if (chapter != null) {
                    stringResource(R.string.hadith_chapter_back)
                } else {
                    stringResource(R.string.hadith_back)
                },
            )
        },
    ) { innerPadding ->
        MuslimContentFrame(modifier = Modifier.padding(innerPadding)) {
            when (val selected = collection) {
                null -> HadithCatalogue(
                    modifier = Modifier.fillMaxSize(),
                    onOpenCollection = viewModel::openCollection,
                )
                else -> HadithBookContent(
                    collection = selected,
                    state = HadithBookContentState(
                        chapter = chapter,
                        chapters = chapters,
                        query = query,
                        corpusState = corpusState,
                        pagedHadiths = pagedHadiths,
                        daily = daily,
                        bookmarkedIds = bookmarkedIds,
                    ),
                    actions = HadithBookActions(
                        onQueryChanged = viewModel::setQuery,
                        onOpenChapter = viewModel::openChapter,
                        onRetry = viewModel::retryCollectionLoad,
                        onToggleBookmark = viewModel::toggleBookmark,
                        onCopied = { scope.launch { snackbarHostState.showSnackbar(copiedMessage) } },
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (showNotificationSettings) {
            HadithNotificationSettingsDialog(
                daily = daily,
                enabled = dailyNotificationEnabled,
                timeMinutes = dailyNotificationTimeMinutes,
                use24h = use24h,
                onEnabledChanged = viewModel::setDailyNotificationEnabled,
                onTimeSelected = viewModel::setDailyNotificationTimeMinutes,
                onDismiss = { showNotificationSettings = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    backDescription: String,
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backDescription)
            }
        },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.hadith_notification_settings))
            }
        },
    )
}

@Composable
private fun HadithCatalogue(
    onOpenCollection: (HadithCollection) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = IslamicSpacing.Large),
    ) {
        item(key = "catalogue-header") {
            Column(modifier = Modifier.padding(IslamicSpacing.PageHorizontal)) {
                Text(
                    text = stringResource(R.string.hadith_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(IslamicSpacing.Compact))
                Text(
                    text = stringResource(R.string.hadith_catalog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(IslamicSpacing.Compact))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.hadith_catalog_loading_contract),
                        modifier = Modifier.padding(horizontal = IslamicSpacing.Medium, vertical = IslamicSpacing.Compact),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
        items(HadithCollection.browsableCollections, key = { it.id }) { collection ->
            HadithCollectionCard(collection = collection, onClick = { onOpenCollection(collection) })
        }
    }
}

@Composable
private fun HadithCollectionCard(
    collection: HadithCollection,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.Compact)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(IslamicSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(collection.coverRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 84.dp, height = 126.dp),
            )
            Spacer(Modifier.width(IslamicSpacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(collection.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(collection.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(IslamicSpacing.Medium))
                Text(
                    text = stringResource(
                        R.string.hadith_book_summary,
                        collection.hadithCount,
                        collection.chapterCount,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(IslamicSpacing.Compact))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.hadith_open_index),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private data class HadithBookContentState(
    val chapter: String?,
    val chapters: List<HadithChapter>,
    val query: String,
    val corpusState: HadithCorpusState,
    val pagedHadiths: LazyPagingItems<Hadith>,
    val daily: Hadith?,
    val bookmarkedIds: Set<Long>,
)

private data class HadithBookActions(
    val onQueryChanged: (String) -> Unit,
    val onOpenChapter: (HadithChapter) -> Unit,
    val onRetry: () -> Unit,
    val onToggleBookmark: (Long) -> Unit,
    val onCopied: () -> Unit,
)

@Composable
private fun HadithBookContent(
    collection: HadithCollection,
    state: HadithBookContentState,
    actions: HadithBookActions,
    modifier: Modifier = Modifier,
) {
    when (val corpusState = state.corpusState) {
        is HadithCorpusState.Importing -> HadithBookProgress(
            collection = corpusState.collection,
            importedCount = corpusState.importedCount,
            modifier = modifier,
        )
        is HadithCorpusState.Failed -> HadithBookFailure(
            collection = corpusState.collection,
            onRetry = actions.onRetry,
            modifier = modifier,
        )
        HadithCorpusState.Catalogue -> Unit
        is HadithCorpusState.Ready -> {
            // A stale asynchronous state can only render when it belongs to the selected card.
            if (corpusState.collection == collection) {
                HadithBookIndexOrPages(
                    collection = collection,
                    state = state,
                    actions = actions,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun HadithBookProgress(
    collection: HadithCollection,
    importedCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(IslamicSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        Spacer(Modifier.height(IslamicSpacing.Medium))
        Text(
            text = if (importedCount == 0) {
                stringResource(R.string.hadith_loading_book, stringResource(collection.titleRes))
            } else {
                stringResource(
                    R.string.hadith_loading_book_progress,
                    stringResource(collection.titleRes),
                    importedCount,
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HadithBookFailure(
    collection: HadithCollection,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MuslimStateSurface(
        title = stringResource(R.string.hadith_book_load_failed),
        supportingText = stringResource(
            R.string.hadith_book_load_failed_supporting,
            stringResource(collection.titleRes),
        ),
        modifier = modifier.padding(IslamicSpacing.Large),
        tone = MuslimStateTone.Critical,
        actionLabel = stringResource(R.string.hadith_retry),
        onAction = onRetry,
    )
}

@Composable
private fun HadithBookIndexOrPages(
    collection: HadithCollection,
    state: HadithBookContentState,
    actions: HadithBookActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = IslamicSpacing.Large),
    ) {
        item(key = "book-header") { HadithBookHeader(collection) }
        item(key = "search") {
            DigitNormalizedOutlinedTextField(
                value = state.query,
                onValueChange = actions.onQueryChanged,
                label = { Text(stringResource(R.string.hadith_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = IslamicSpacing.PageHorizontal, vertical = 8.dp),
            )
        }

        if (state.query.isBlank() && state.chapter == null) {
            state.daily?.let { hadith ->
                item(key = "daily") {
                    DailyHadithCard(
                        hadith = hadith,
                        bookmarked = hadith.id in state.bookmarkedIds,
                        onToggleBookmark = { actions.onToggleBookmark(hadith.id) },
                        onCopied = actions.onCopied,
                    )
                }
            }
            item(key = "index-label") {
                Text(
                    text = stringResource(R.string.hadith_book_index),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = IslamicSpacing.PageHorizontal,
                        vertical = IslamicSpacing.Medium,
                    ),
                )
            }
            items(state.chapters, key = { it.title }) { item ->
                HadithChapterRow(item, onClick = { actions.onOpenChapter(item) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = IslamicSpacing.PageHorizontal))
            }
            item(key = "source-notice") { HadithSourceNotice() }
        } else {
            item(key = "results-label") {
                Text(
                    text = if (state.query.isBlank()) state.chapter.orEmpty() else stringResource(R.string.hadith_search_hint),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = IslamicSpacing.PageHorizontal,
                        vertical = IslamicSpacing.Medium,
                    ),
                )
            }
            pagedHadithRows(
                state.pagedHadiths,
                state.bookmarkedIds,
                actions.onToggleBookmark,
                actions.onCopied,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.pagedHadithRows(
    pagedHadiths: LazyPagingItems<Hadith>,
    bookmarkedIds: Set<Long>,
    onToggleBookmark: (Long) -> Unit,
    onCopied: () -> Unit,
) {
    items(
        count = pagedHadiths.itemCount,
        key = { index -> pagedHadiths[index]?.id ?: "hadith-placeholder-$index" },
    ) { index ->
        pagedHadiths[index]?.let { hadith ->
            HadithCard(
                hadith = hadith,
                bookmarked = hadith.id in bookmarkedIds,
                onToggleBookmark = { onToggleBookmark(hadith.id) },
                onCopied = onCopied,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = IslamicSpacing.PageHorizontal))
        }
    }
    when (val refresh = pagedHadiths.loadState.refresh) {
        is LoadState.Loading -> item { HadithPageLoading() }
        is LoadState.Error -> item { HadithPageFailure(refresh.error.message, pagedHadiths::retry) }
        is LoadState.NotLoading -> if (pagedHadiths.itemCount == 0) item { HadithEmptyState() }
    }
    when (val append = pagedHadiths.loadState.append) {
        is LoadState.Loading -> item { HadithPageLoading() }
        is LoadState.Error -> item { HadithPageFailure(append.error.message, pagedHadiths::retry) }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun HadithBookHeader(collection: HadithCollection) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(IslamicSpacing.PageHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(collection.coverRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 64.dp, height = 96.dp),
        )
        Spacer(Modifier.width(IslamicSpacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(collection.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.hadith_book_index_subtitle,
                    collection.hadithCount,
                    collection.chapterCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HadithChapterRow(chapter: HadithChapter, onClick: () -> Unit) {
    val range = if (chapter.firstHadithNumber != null && chapter.lastHadithNumber != null) {
        "${chapter.firstHadithNumber}–${chapter.lastHadithNumber}"
    } else {
        stringResource(R.string.hadith_chapter_unknown_range)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(8.dp),
            )
        }
        Spacer(Modifier.width(IslamicSpacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.title.ifBlank { stringResource(R.string.hadith_all_chapters) },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.hadith_chapter_summary, chapter.hadithCount, range),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HadithNotificationSettingsDialog(
    daily: Hadith?,
    enabled: Boolean,
    timeMinutes: Int,
    use24h: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.hadith_notification_settings)) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.hadith_daily_notification), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.hadith_daily_notification_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = onEnabledChanged)
                }
                HadithNotificationPreview(daily, timeMinutes, enabled, use24h)
                if (enabled) HadithTimeDropdown(timeMinutes, hadithTimeOptions, onTimeSelected, use24h)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.hadith_done)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithTimeDropdown(
    selectedMinutes: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit,
    use24h: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(top = IslamicSpacing.Compact),
    ) {
        OutlinedTextField(
            value = formatHadithTime(selectedMinutes, use24h),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.hadith_daily_notification_time)) },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(
                androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable,
            ),
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { minutes ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(formatHadithTime(minutes, use24h)) },
                    onClick = { expanded = false; onSelected(minutes) },
                )
            }
        }
    }
}

@Composable
private fun HadithNotificationPreview(hadith: Hadith?, timeMinutes: Int, enabled: Boolean, use24h: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth().padding(vertical = IslamicSpacing.Compact).alpha(if (enabled) 1f else 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(IslamicSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(IslamicSpacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.hadith_of_the_day), style = MaterialTheme.typography.labelLarge)
                Text(
                    hadith?.arabicText?.take(120) ?: stringResource(R.string.hadith_select_book_for_search),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(formatHadithTime(timeMinutes, use24h), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DailyHadithCard(hadith: Hadith, bookmarked: Boolean, onToggleBookmark: () -> Unit, onCopied: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.Compact),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(modifier = Modifier.padding(IslamicSpacing.Medium)) {
            Text(
                text = stringResource(R.string.hadith_of_the_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(IslamicSpacing.Compact))
            HadithBody(hadith, bookmarked, onToggleBookmark, onCopied)
        }
    }
}

@Composable
private fun HadithCard(hadith: Hadith, bookmarked: Boolean, onToggleBookmark: () -> Unit, onCopied: () -> Unit) {
    var showTranslation by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { showTranslation = !showTranslation }
            .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.Medium),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HadithBody(hadith, bookmarked, onToggleBookmark, onCopied, showTranslation)
        }
    }
}

@Composable
private fun HadithBody(
    hadith: Hadith,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onCopied: () -> Unit,
    showTranslation: Boolean = true,
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    val showEnglishFallback = AppLanguage.showEnglishFallback()
    val shareText = buildString {
        append(hadith.arabicText)
        if (showEnglishFallback && hadith.translation.isNotBlank()) append("\n\n").append(hadith.translation)
        append("\n\n").append(hadith.source)
    }
    Text(text = hadith.arabicText, style = MaterialTheme.typography.bodyLarge)
    if (showEnglishFallback && showTranslation && hadith.translation.isNotBlank()) {
        Spacer(Modifier.height(IslamicSpacing.Compact))
        Text(hadith.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(IslamicSpacing.Compact))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                hadith.grade,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Text(
            hadith.source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(if (bookmarked) R.string.hadith_bookmark_remove else R.string.hadith_bookmark_add),
            )
        }
        IconButton(onClick = {
            clipboard?.setPrimaryClip(ClipData.newPlainText("hadith", shareText))
            onCopied()
        }) { Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.hadith_copy)) }
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, null)) }
        }) { Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.hadith_share)) }
    }
}

@Composable
private fun HadithPageLoading() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(IslamicSpacing.Large),
        horizontalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
}

@Composable
private fun HadithPageFailure(message: String?, onRetry: () -> Unit) {
    MuslimStateSurface(
        title = stringResource(R.string.hadith_load_failed),
        supportingText = message ?: stringResource(R.string.hadith_load_failed),
        modifier = Modifier.padding(IslamicSpacing.PageHorizontal),
        tone = MuslimStateTone.Critical,
        actionLabel = stringResource(R.string.hadith_retry),
        onAction = onRetry,
    )
}

@Composable
private fun HadithEmptyState() {
    MuslimCenteredStatus(
        text = stringResource(R.string.hadith_no_results),
        modifier = Modifier.padding(IslamicSpacing.Large),
    )
}

@Composable
private fun HadithSourceNotice() {
    Text(
        text = stringResource(R.string.hadith_source_notice),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(IslamicSpacing.PageHorizontal),
    )
}

private fun formatHadithTime(minutes: Int, use24h: Boolean): String =
    org.muslim.app.core.common.time.TimeFormats.formatMinutes(minutes, use24h)
