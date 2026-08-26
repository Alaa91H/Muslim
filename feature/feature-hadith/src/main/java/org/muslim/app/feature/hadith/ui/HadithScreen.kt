package org.muslim.app.feature.hadith.ui

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.feature.hadith.R
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimCenteredStatus
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.data.HadithCorpusState
import org.muslim.app.feature.hadith.domain.HadithCollection
import kotlinx.coroutines.launch

/** 30-minute increments across a full day, as minutes from midnight. */
private val hadithTimeOptions: List<Int> = (0 until 24 * 60 step 30).toList()

private data class HadithControlsState(
    val query: String,
    val collection: HadithCollection?,
)

private data class HadithControlsActions(
    val onQueryChanged: (String) -> Unit,
    val onCollectionSelected: (HadithCollection?) -> Unit,
)

private data class HadithListState(
    val corpusState: HadithCorpusState,
    val pagedHadiths: androidx.paging.compose.LazyPagingItems<Hadith>,
    val query: String,
    val collection: HadithCollection?,
    val daily: Hadith?,
    val bookmarkedIds: Set<Long>,
)

private data class HadithListActions(
    val onRetryCorpus: () -> Unit,
    val onToggleBookmark: (Long) -> Unit,
    val onCopied: () -> Unit,
)

/**
 * Hadith library (PROJECT_PROMPT.md §6 Phase 3): daily hadith, collection
 * filter, full-text search, bookmarks and text sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = hiltViewModel(),
) {
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val dailyNotificationEnabled by viewModel.dailyNotificationEnabled.collectAsStateWithLifecycle()
    val dailyNotificationTimeMinutes by viewModel.dailyNotificationTimeMinutes.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    val corpusState by viewModel.corpusState.collectAsStateWithLifecycle()
    val pagedHadiths = viewModel.pagedHadiths.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.hadith_copied)
    val onCopied: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(copiedMessage) } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { HadithTopBar(onBack = onBack, onOpenSettings = { showNotificationSettings = true }) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HadithLibraryControls(
                state = HadithControlsState(
                    query = query,
                    collection = collection,
                ),
                actions = HadithControlsActions(
                    onQueryChanged = viewModel::setQuery,
                    onCollectionSelected = viewModel::setCollection,
                ),
            )
            HadithPagedList(
                state = HadithListState(
                    corpusState = corpusState,
                    pagedHadiths = pagedHadiths,
                    query = query,
                    collection = collection,
                    daily = daily,
                    bookmarkedIds = bookmarkedIds,
                ),
                actions = HadithListActions(
                    onRetryCorpus = viewModel::retryCorpusPreparation,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onCopied = onCopied,
                ),
            )
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
                HadithNotificationControls(
                    daily = daily,
                    enabled = enabled,
                    timeMinutes = timeMinutes,
                    use24h = use24h,
                    onEnabledChanged = onEnabledChanged,
                    onTimeSelected = onTimeSelected,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.hadith_done)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithTopBar(onBack: () -> Unit, onOpenSettings: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.hadith_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.hadith_back))
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
private fun HadithLibraryControls(
    state: HadithControlsState,
    actions: HadithControlsActions,
) {
    DigitNormalizedOutlinedTextField(
        value = state.query,
        onValueChange = actions.onQueryChanged,
        label = { Text(stringResource(R.string.hadith_search_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HadithCollectionFilters(state.collection, actions.onCollectionSelected)
}

@Composable
private fun HadithNotificationControls(
    daily: Hadith?,
    enabled: Boolean,
    timeMinutes: Int,
    use24h: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onTimeSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
    HadithNotificationPreview(
        hadith = daily,
        timeMinutes = timeMinutes,
        enabled = enabled,
        use24h = use24h,
    )
    if (enabled) {
        HadithTimeDropdown(
            use24h = use24h,
            selectedMinutes = timeMinutes,
            options = hadithTimeOptions,
            onSelected = onTimeSelected,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun HadithCollectionFilters(
    collection: HadithCollection?,
    onCollectionSelected: (HadithCollection?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = collection == null,
            onClick = { onCollectionSelected(null) },
            label = { Text(stringResource(R.string.hadith_all)) },
            modifier = Modifier.padding(end = 8.dp),
        )
        HadithCollection.entries.forEach { option ->
            FilterChip(
                selected = collection == option,
                onClick = { onCollectionSelected(option) },
                label = { Text(stringResource(option.titleRes)) },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun HadithPagedList(
    state: HadithListState,
    actions: HadithListActions,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            bottom = IslamicSpacing.Medium,
        ),
    ) {
        when (state.corpusState) {
            HadithCorpusState.NotStarted -> item { HadithCorpusProgress(0) }
            is HadithCorpusState.Importing -> item { HadithCorpusProgress(state.corpusState.importedCount) }
            is HadithCorpusState.Failed -> item {
                HadithCorpusFailure(state.corpusState.message, actions.onRetryCorpus)
            }
            HadithCorpusState.Ready -> {
                if (state.query.isBlank() && state.daily != null && state.collection == null) {
                    item(key = "daily") {
                        DailyHadithCard(
                            hadith = state.daily,
                            bookmarked = state.daily.id in state.bookmarkedIds,
                            onToggleBookmark = { actions.onToggleBookmark(state.daily.id) },
                            onCopied = actions.onCopied,
                        )
                    }
                }
                items(
                    count = state.pagedHadiths.itemCount,
                    key = { index -> state.pagedHadiths[index]?.id ?: "hadith-placeholder-$index" },
                ) { index ->
                    state.pagedHadiths[index]?.let { hadith ->
                        HadithCard(
                            hadith = hadith,
                            bookmarked = hadith.id in state.bookmarkedIds,
                            onToggleBookmark = { actions.onToggleBookmark(hadith.id) },
                            onCopied = actions.onCopied,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
                when (val refresh = state.pagedHadiths.loadState.refresh) {
                    is LoadState.Loading -> item { HadithPageLoading() }
                    is LoadState.Error -> item { HadithPageFailure(refresh.error.message, state.pagedHadiths::retry) }
                    is LoadState.NotLoading -> if (state.pagedHadiths.itemCount == 0) {
                        item { HadithEmptyState() }
                    }
                }
                when (val append = state.pagedHadiths.loadState.append) {
                    is LoadState.Loading -> item { HadithPageLoading() }
                    is LoadState.Error -> item { HadithPageFailure(append.error.message, state.pagedHadiths::retry) }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun HadithCorpusProgress(importedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            if (importedCount == 0) stringResource(R.string.hadith_preparing) else stringResource(
                R.string.hadith_preparing_progress,
                importedCount,
            ),
        )
    }
}

@Composable
private fun HadithCorpusFailure(message: String, onRetry: () -> Unit) {
    MuslimStateSurface(
        title = stringResource(R.string.hadith_preparing_failed),
        supportingText = message,
        modifier = Modifier.padding(IslamicSpacing.Large),
        tone = MuslimStateTone.Critical,
        actionLabel = stringResource(R.string.hadith_retry),
        onAction = onRetry,
    )
}

@Composable
private fun HadithPageLoading() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
}

@Composable
private fun HadithPageFailure(message: String?, onRetry: () -> Unit) {
    MuslimStateSurface(
        title = stringResource(R.string.hadith_load_failed),
        supportingText = message ?: stringResource(R.string.hadith_load_failed),
        modifier = Modifier.padding(
            horizontal = IslamicSpacing.PageHorizontal,
            vertical = IslamicSpacing.Compact,
        ),
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

/**
 * Live preview of the daily-hadith notification, mirroring
 * [org.muslim.app.feature.hadith.data.HadithOfTheDayNotifier]: agenda icon,
 * "hadith of the day" title, the hadith body and the scheduled time. Dims when
 * the daily notification is disabled.
 */
@Composable
private fun HadithNotificationPreview(
    hadith: Hadith?,
    timeMinutes: Int,
    enabled: Boolean,
    use24h: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.hadith_preview_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.hadith_of_the_day),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = hadith?.arabicText?.take(160)
                            ?: stringResource(R.string.hadith_preview_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatHadithTime(timeMinutes, use24h),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.hadith_preview_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithTimeDropdown(
    selectedMinutes: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit,
    use24h: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = formatHadithTime(selectedMinutes, use24h),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.hadith_daily_notification_time)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(formatHadithTime(minutes, use24h)) },
                    onClick = {
                        expanded = false
                        onSelected(minutes)
                    },
                )
            }
        }
    }
}

/** Formats minutes-from-midnight as "HH:MM". */
private fun formatHadithTime(minutes: Int, use24h: Boolean): String =
    org.muslim.app.core.common.time.TimeFormats.formatMinutes(minutes, use24h)

@Composable
private fun DailyHadithCard(
    hadith: Hadith,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onCopied: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hadith_of_the_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            HadithBody(hadith, onCopied = onCopied)
        }
    }
}

@Composable
private fun HadithCard(
    hadith: Hadith,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onCopied: () -> Unit,
) {
    var showTranslation by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTranslation = !showTranslation }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HadithBody(hadith, showTranslation = showTranslation, onCopied = onCopied)
        }
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(
                    if (bookmarked) R.string.hadith_bookmark_remove else R.string.hadith_bookmark_add
                ),
                tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HadithBody(
    hadith: Hadith,
    showTranslation: Boolean = true,
    onCopied: () -> Unit = {},
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    // English fallback is hidden when the UI language is Arabic (each language
    // shows its own texts — an Arabic reader reads the Arabic original only).
    val showEnglishFallback = AppLanguage.showEnglishFallback()
    val shareText = buildString {
        append(hadith.arabicText)
        if (showEnglishFallback) append("\n\n").append(hadith.translation)
        append("\n\n").append(hadith.source)
    }
    Text(
        text = hadith.arabicText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(end = 8.dp),
    )
    if (showEnglishFallback && showTranslation && hadith.translation.isNotBlank()) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = hadith.translation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = hadith.grade,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(0.dp))
        Text(
            text = hadith.source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {
            clipboard?.setPrimaryClip(ClipData.newPlainText("hadith", shareText))
            onCopied()
        }) {
            Icon(
                Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.hadith_copy),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                runCatching { context.startActivity(Intent.createChooser(intent, null)) }
            },
        ) {
            Icon(
                Icons.Filled.Share,
                contentDescription = stringResource(R.string.hadith_share),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
