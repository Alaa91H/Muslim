package org.muslim.app.feature.adhkar.ui

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory

/** Main routes owned by the adhkar feature. */
private enum class AdhkarRoute { Library, Reader, Settings, Customize }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarViewModel = hiltViewModel(),
) {
    var route by remember { mutableStateOf(AdhkarRoute.Library) }

    when (route) {
        AdhkarRoute.Settings -> AdhkarSettingsScreen(
            onBack = { route = AdhkarRoute.Library },
            onOpenCustomize = { route = AdhkarRoute.Customize },
        )
        AdhkarRoute.Customize -> AdhkarCustomizeScreen(
            onBack = { route = AdhkarRoute.Settings },
        )
        AdhkarRoute.Reader -> AdhkarReaderContent(
            onBack = { route = AdhkarRoute.Library },
            viewModel = viewModel,
        )
        AdhkarRoute.Library -> AdhkarLibraryContent(
            onBack = onBack,
            onOpenSettings = { route = AdhkarRoute.Settings },
            onOpenReader = { route = AdhkarRoute.Reader },
            modifier = modifier,
            viewModel = viewModel,
        )
    }
}

private data class AdhkarLibrarySnapshot(
    val adhkar: List<Dhikr>,
    val favorites: List<Dhikr>,
    val visibleCount: Int,
    val favoriteIds: Set<Long>,
    val selectedCategory: DhikrCategory?,
    val searchQuery: String,
    val favoritesOnly: Boolean,
    val morningEveningReminderEnabled: Boolean,
    val speechEnabled: Boolean,
    val speechReady: Boolean,
    val speakingDhikrId: Long?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhkarLibraryContent(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenReader: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarViewModel,
) {
    val snapshot = AdhkarLibrarySnapshot(
        adhkar = viewModel.adhkar.collectAsStateWithLifecycle().value,
        favorites = viewModel.favorites.collectAsStateWithLifecycle().value,
        visibleCount = viewModel.resultCount.collectAsStateWithLifecycle().value,
        favoriteIds = viewModel.favoriteIds.collectAsStateWithLifecycle().value,
        selectedCategory = viewModel.selectedCategory.collectAsStateWithLifecycle().value,
        searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle().value,
        favoritesOnly = viewModel.favoritesOnly.collectAsStateWithLifecycle().value,
        morningEveningReminderEnabled = viewModel.morningEveningReminderEnabled.collectAsStateWithLifecycle().value,
        speechEnabled = viewModel.speechEnabled.collectAsStateWithLifecycle().value,
        speechReady = viewModel.speechReady.collectAsStateWithLifecycle().value,
        speakingDhikrId = viewModel.speakingDhikrId.collectAsStateWithLifecycle().value,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.adhkar_copied)
    val onCopied: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(copiedMessage) } }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhkar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.adhkar_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.adhkar_settings_title),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AdhkarLibraryBody(
            snapshot = snapshot,
            innerPadding = innerPadding,
            categories = viewModel.categories,
            viewModel = viewModel,
            onOpenReader = onOpenReader,
            onCopied = onCopied,
        )
    }
}

@Composable
private fun AdhkarLibraryBody(
    snapshot: AdhkarLibrarySnapshot,
    innerPadding: PaddingValues,
    categories: List<DhikrCategory>,
    viewModel: AdhkarViewModel,
    onOpenReader: () -> Unit,
    onCopied: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        AdhkarCategoryFilters(
            selectedCategory = snapshot.selectedCategory,
            categories = categories,
            onSelected = viewModel::selectCategory,
        )
        Spacer(Modifier.height(8.dp))
        AdhkarLibraryFilters(
            searchQuery = snapshot.searchQuery,
            favoritesOnly = snapshot.favoritesOnly,
            resultCount = snapshot.visibleCount,
            readerEnabled = snapshot.visibleCount > 0,
            onSearchQueryChanged = viewModel::setSearchQuery,
            onFavoritesOnlyChanged = viewModel::setFavoritesOnly,
            onOpenReader = onOpenReader,
        )
        Spacer(Modifier.height(8.dp))
        ReminderMasterSwitch(
            enabled = snapshot.morningEveningReminderEnabled,
            onEnabledChanged = viewModel::setMorningEveningReminderEnabled,
        )
        IslamicDecorationDivider(
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(8.dp))
        AdhkarList(
            snapshot = snapshot,
            viewModel = viewModel,
            onCopied = onCopied,
        )
    }
}

@Composable
private fun AdhkarList(
    snapshot: AdhkarLibrarySnapshot,
    viewModel: AdhkarViewModel,
    onCopied: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        if (snapshot.visibleCount == 0) {
            item(key = "empty") {
                Text(
                    text = stringResource(R.string.adhkar_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }

        if (snapshot.favorites.isNotEmpty()) {
            item(key = "favorites-header") {
                MuslimSectionHeader(
                    title = stringResource(R.string.adhkar_favorites),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(snapshot.favorites, key = { "fav-${it.id}" }) { dhikr ->
                LibraryDhikrItem(dhikr, snapshot, viewModel, onCopied)
            }
        }

        items(snapshot.adhkar, key = { it.id }) { dhikr ->
            LibraryDhikrItem(dhikr, snapshot, viewModel, onCopied)
        }
    }
}

@Composable
private fun LibraryDhikrItem(
    dhikr: Dhikr,
    snapshot: AdhkarLibrarySnapshot,
    viewModel: AdhkarViewModel,
    onCopied: () -> Unit,
) {
    DhikrCard(
        dhikr = dhikr,
        count = viewModel.count(dhikr.id).collectAsStateWithLifecycle(),
        isFavorite = dhikr.id in snapshot.favoriteIds,
        actions = cardActions(
            dhikr = dhikr,
            viewModel = viewModel,
            speechEnabled = snapshot.speechEnabled && snapshot.speechReady,
            isSpeaking = snapshot.speakingDhikrId == dhikr.id,
            onCopied = onCopied,
        ),
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun AdhkarCategoryFilters(
    selectedCategory: DhikrCategory?,
    categories: List<DhikrCategory>,
    onSelected: (DhikrCategory?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelected(null) },
            label = { Text(stringResource(R.string.adhkar_all)) },
            modifier = Modifier.padding(end = 8.dp),
        )
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelected(category) },
                label = { Text(stringResource(category.titleRes)) },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun AdhkarLibraryFilters(
    searchQuery: String,
    favoritesOnly: Boolean,
    resultCount: Int,
    readerEnabled: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onFavoritesOnlyChanged: (Boolean) -> Unit,
    onOpenReader: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.adhkar_search_clear),
                        )
                    }
                }
            } else {
                null
            },
            placeholder = { Text(stringResource(R.string.adhkar_search_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = favoritesOnly,
                onClick = { onFavoritesOnlyChanged(!favoritesOnly) },
                label = { Text(stringResource(R.string.adhkar_filter_favorites_only)) },
            )
            Text(
                text = stringResource(R.string.adhkar_results_count, resultCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onOpenReader,
                enabled = readerEnabled,
            ) {
                Text(stringResource(R.string.adhkar_reader_start))
            }
        }
    }
}

@Composable
private fun ReminderMasterSwitch(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.adhkar_morning_evening_notification),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.adhkar_morning_evening_notification_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChanged,
        )
    }
}

private data class AdhkarReaderSnapshot(
    val queue: List<Dhikr>,
    val favoriteIds: Set<Long>,
    val speechEnabled: Boolean,
    val speechReady: Boolean,
    val speakingDhikrId: Long?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhkarReaderContent(
    onBack: () -> Unit,
    viewModel: AdhkarViewModel,
) {
    val snapshot = AdhkarReaderSnapshot(
        queue = viewModel.visibleAdhkar.collectAsStateWithLifecycle().value,
        favoriteIds = viewModel.favoriteIds.collectAsStateWithLifecycle().value,
        speechEnabled = viewModel.speechEnabled.collectAsStateWithLifecycle().value,
        speechReady = viewModel.speechReady.collectAsStateWithLifecycle().value,
        speakingDhikrId = viewModel.speakingDhikrId.collectAsStateWithLifecycle().value,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.adhkar_copied)
    val onCopied: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(copiedMessage) } }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(snapshot.queue.size) {
        currentIndex = if (snapshot.queue.isEmpty()) {
            0
        } else {
            currentIndex.coerceIn(0, snapshot.queue.lastIndex)
        }
    }

    MuslimAppScaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhkar_reader_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.adhkar_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AdhkarReaderBody(
            snapshot = snapshot,
            currentIndex = currentIndex,
            innerPadding = innerPadding,
            viewModel = viewModel,
            onCopied = onCopied,
            onIndexChanged = { currentIndex = it },
            onFinish = onBack,
        )
    }
}

@Composable
private fun AdhkarReaderBody(
    snapshot: AdhkarReaderSnapshot,
    currentIndex: Int,
    innerPadding: PaddingValues,
    viewModel: AdhkarViewModel,
    onCopied: () -> Unit,
    onIndexChanged: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(vertical = 8.dp),
    ) {
        if (snapshot.queue.isEmpty()) {
            Text(
                text = stringResource(R.string.adhkar_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
            return@Column
        }

        val safeIndex = currentIndex.coerceIn(0, snapshot.queue.lastIndex)
        val dhikr = snapshot.queue[safeIndex]
        ReaderProgress(safeIndex = safeIndex, total = snapshot.queue.size)

        DhikrCard(
            dhikr = dhikr,
            count = viewModel.count(dhikr.id).collectAsStateWithLifecycle(),
            isFavorite = dhikr.id in snapshot.favoriteIds,
            actions = cardActions(
                dhikr = dhikr,
                viewModel = viewModel,
                speechEnabled = snapshot.speechEnabled && snapshot.speechReady,
                isSpeaking = snapshot.speakingDhikrId == dhikr.id,
                onCopied = onCopied,
            ),
        )

        Spacer(Modifier.weight(1f))
        ReaderNavigation(
            currentIndex = safeIndex,
            lastIndex = snapshot.queue.lastIndex,
            onIndexChanged = onIndexChanged,
            onFinish = onFinish,
        )
    }
}

@Composable
private fun ReaderProgress(
    safeIndex: Int,
    total: Int,
) {
    val progress = (safeIndex + 1).toFloat() / total.toFloat()
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
    Text(
        text = stringResource(R.string.adhkar_reader_progress, safeIndex + 1, total),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ReaderNavigation(
    currentIndex: Int,
    lastIndex: Int,
    onIndexChanged: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { onIndexChanged(currentIndex - 1) },
            enabled = currentIndex > 0,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.adhkar_reader_previous))
        }
        Button(
            onClick = {
                if (currentIndex < lastIndex) {
                    onIndexChanged(currentIndex + 1)
                } else {
                    onFinish()
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(
                stringResource(
                    if (currentIndex < lastIndex) {
                        R.string.adhkar_reader_next
                    } else {
                        R.string.adhkar_reader_finish
                    },
                ),
            )
        }
    }
}

private fun cardActions(
    dhikr: Dhikr,
    viewModel: AdhkarViewModel,
    speechEnabled: Boolean,
    isSpeaking: Boolean,
    onCopied: () -> Unit,
): DhikrCardActions = DhikrCardActions(
    onToggleFavorite = { viewModel.toggleFavorite(dhikr.id) },
    onIncrement = { viewModel.increment(dhikr.id) },
    onReset = { viewModel.reset(dhikr.id) },
    onCopied = onCopied,
    speech = DhikrSpeechControls(
        enabled = speechEnabled,
        isSpeaking = isSpeaking,
        onToggle = { viewModel.toggleSpeech(dhikr) },
    ),
)

private data class DhikrSpeechControls(
    val enabled: Boolean,
    val isSpeaking: Boolean,
    val onToggle: () -> Unit,
)

private data class DhikrCardActions(
    val onToggleFavorite: () -> Unit,
    val onIncrement: () -> Unit,
    val onReset: () -> Unit,
    val onCopied: () -> Unit,
    val speech: DhikrSpeechControls,
)

@Composable
private fun DhikrCard(
    dhikr: Dhikr,
    count: androidx.compose.runtime.State<Int>,
    isFavorite: Boolean,
    actions: DhikrCardActions,
) {
    val currentCount by count
    val complete = currentCount >= dhikr.repetition
    val showEnglishFallback = AppLanguage.showEnglishFallback()

    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = if (complete) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column {
            DhikrHeader(dhikr)

            dhikr.virtue?.let { virtue ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = virtue,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (showEnglishFallback) {
                Spacer(Modifier.height(8.dp))
                TranslationToggle(dhikr.translation)
            }

            Spacer(Modifier.height(12.dp))
            DhikrMetadata(dhikr)
            Spacer(Modifier.height(14.dp))

            DhikrBottomBar(
                dhikr = dhikr,
                currentCount = currentCount,
                complete = complete,
                isFavorite = isFavorite,
                actions = actions,
            )
        }
    }
}

@Composable
private fun DhikrHeader(dhikr: Dhikr) {
    val accessibilityVisuals = LocalAccessibilityVisuals.current

    Text(
        text = dhikr.arabic,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 22.sp,
            lineHeight = (22f * accessibilityVisuals.arabicLineHeightMultiplier).sp,
            fontFamily = accessibilityVisuals.arabicReadingFont,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DhikrMetadata(dhikr: Dhikr) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = dhikr.source,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.adhkar_repetition_label, dhikr.repetition.toString()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DhikrBottomBar(
    dhikr: Dhikr,
    currentCount: Int,
    complete: Boolean,
    isFavorite: Boolean,
    actions: DhikrCardActions,
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    val showEnglishFallback = AppLanguage.showEnglishFallback()
    val shareText = remember(dhikr, showEnglishFallback) {
        dhikrShareText(dhikr, showEnglishFallback)
    }
    val copyDhikr = {
        clipboard?.setPrimaryClip(ClipData.newPlainText("dhikr", shareText))
        actions.onCopied()
    }
    val shareDhikr = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, null)) }
        Unit
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DhikrPrimaryControls(
            dhikr = dhikr,
            currentCount = currentCount,
            complete = complete,
            actions = actions,
        )
        Spacer(Modifier.weight(1f))
        DhikrSecondaryActions(
            isFavorite = isFavorite,
            onToggleFavorite = actions.onToggleFavorite,
            onCopy = copyDhikr,
            onShare = shareDhikr,
        )
    }

    if (complete) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.adhkar_complete),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun dhikrShareText(dhikr: Dhikr, showEnglishFallback: Boolean): String = buildString {
    append(dhikr.arabic)
    if (showEnglishFallback && dhikr.translation.isNotBlank()) {
        append("\n\n").append(dhikr.translation)
    }
    if (dhikr.source.isNotBlank()) {
        append("\n\n").append(dhikr.source)
    }
}

@Composable
private fun DhikrPrimaryControls(
    dhikr: Dhikr,
    currentCount: Int,
    complete: Boolean,
    actions: DhikrCardActions,
) {
    val haptics = LocalHapticFeedback.current

    if (actions.speech.enabled) {
        OutlinedIconButton(
            onClick = actions.speech.onToggle,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = if (actions.speech.isSpeaking) {
                    Icons.Filled.StopCircle
                } else {
                    Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = stringResource(
                    if (actions.speech.isSpeaking) {
                        R.string.adhkar_speech_stop
                    } else {
                        R.string.adhkar_speech_play
                    },
                ),
            )
        }
        Spacer(Modifier.size(8.dp))
    }

    FilledIconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            actions.onIncrement()
        },
        enabled = !complete,
        modifier = Modifier.size(68.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (complete) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = "${currentCount}/${dhikr.repetition}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (currentCount > 0) {
        IconButton(
            onClick = actions.onReset,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.adhkar_reset),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DhikrSecondaryActions(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.adhkar_remove_favorite else R.string.adhkar_add_favorite,
            ),
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
    IconButton(
        onClick = onCopy,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = stringResource(R.string.adhkar_copy),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    IconButton(
        onClick = onShare,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(R.string.adhkar_share),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TranslationToggle(translation: String) {
    var showTranslation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = if (showTranslation) translation else stringResource(R.string.adhkar_show_translation),
            style = if (showTranslation) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.labelMedium
            },
            color = if (showTranslation) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.clickable { showTranslation = !showTranslation },
        )
    }
}
