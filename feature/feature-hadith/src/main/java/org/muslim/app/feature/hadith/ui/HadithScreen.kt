package org.muslim.app.feature.hadith.ui

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.IslamicDecorationCorners
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.core.ui.theme.MuslimErrorState
import org.muslim.app.core.ui.theme.MuslimLoadingState
import org.muslim.app.feature.hadith.R
import org.muslim.app.feature.hadith.data.HadithCorpusState
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithChapter
import org.muslim.app.feature.hadith.domain.HadithChapterArabicTitles
import org.muslim.app.feature.hadith.domain.HadithCollection

/** 30-minute increments across a full day, as minutes from midnight. */
private val hadithTimeOptions: List<Int> = (0 until 24 * 60 step 30).toList()

private val HadithLibraryBackground = Color(0xFF062F24)
private val HadithLibrarySurface = Color(0xFF0B3B2E)
private val HadithLibrarySurfaceRaised = Color(0xFF104536)
private val HadithGold = Color(0xFFE9C36D)
private val HadithIvory = Color(0xFFFFF5DA)

@Composable
private fun HadithLibraryTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = base.copy(
            primary = HadithGold,
            onPrimary = HadithLibraryBackground,
            surface = HadithLibraryBackground,
            onSurface = HadithIvory,
            surfaceVariant = HadithLibrarySurface,
            onSurfaceVariant = HadithIvory.copy(alpha = 0.72f),
            secondaryContainer = HadithLibrarySurfaceRaised,
            onSecondaryContainer = HadithIvory,
            outline = HadithGold.copy(alpha = 0.42f),
            outlineVariant = HadithGold.copy(alpha = 0.22f),
        ),
        content = content,
    )
}

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
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
        },
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HadithLibraryBackground,
            titleContentColor = HadithGold,
            navigationIconContentColor = HadithIvory,
            actionIconContentColor = HadithIvory,
        ),
    )
}

@Suppress("LongMethod")
@Composable
private fun HadithCatalogue(
    onOpenCollection: (HadithCollection) -> Unit,
    modifier: Modifier = Modifier,
) {
    HadithLibraryTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF07372A),
                            HadithLibraryBackground,
                            Color(0xFF041F18),
                        ),
                    ),
                ),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = IslamicSpacing.Compact,
                    end = IslamicSpacing.Compact,
                    top = IslamicSpacing.Small,
                    bottom = IslamicSpacing.Large,
                ),
                horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
                verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Comfortable),
            ) {
                item(
                    key = "catalogue-header",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = IslamicSpacing.PageHorizontal,
                                end = IslamicSpacing.PageHorizontal,
                                bottom = IslamicSpacing.Small,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IslamicDecorationBand(
                            modifier = Modifier.fillMaxWidth(),
                            tint = HadithGold,
                            compact = true,
                        )
                        Spacer(Modifier.height(IslamicSpacing.Small))
                        Text(
                            text = stringResource(R.string.hadith_catalog_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HadithIvory.copy(alpha = 0.86f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(IslamicSpacing.Small))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = HadithGold.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, HadithGold.copy(alpha = 0.24f)),
                        ) {
                            Text(
                                text = stringResource(R.string.hadith_catalog_loading_contract),
                                modifier = Modifier.padding(horizontal = IslamicSpacing.Medium, vertical = IslamicSpacing.Small),
                                style = MaterialTheme.typography.labelMedium,
                                color = HadithGold,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(Modifier.height(IslamicSpacing.Small))
                        IslamicDecorationDivider(
                            modifier = Modifier.fillMaxWidth(),
                            tint = HadithGold,
                        )
                    }
                }

                gridItems(
                    items = HadithCollection.browsableCollections,
                    key = { it.id },
                ) { collection ->
                    HadithCollectionCard(
                        collection = collection,
                        onClick = { onOpenCollection(collection) },
                    )
                }
            }
        }
    }
}

private data class HadithCoverPalette(
    val background: Color,
    val accent: Color,
    val foreground: Color,
)

private fun HadithCollection.coverPalette(): HadithCoverPalette = when (this) {
    HadithCollection.Bukhari -> HadithCoverPalette(Color(0xFF174C3B), Color(0xFFD5B568), Color(0xFFF5E8C8))
    HadithCollection.Muslim -> HadithCoverPalette(Color(0xFF30334B), Color(0xFFC9A96E), Color(0xFFF4E8CC))
    HadithCollection.AbuDawud -> HadithCoverPalette(Color(0xFF6B352E), Color(0xFFD5AD63), Color(0xFFF7E7C5))
    HadithCollection.Tirmidhi -> HadithCoverPalette(Color(0xFF3A4966), Color(0xFFCCAA5F), Color(0xFFF3E5C4))
    HadithCollection.Nasai -> HadithCoverPalette(Color(0xFF28505A), Color(0xFFC7A96B), Color(0xFFF0E4C9))
    HadithCollection.IbnMajah -> HadithCoverPalette(Color(0xFF583A60), Color(0xFFD0AA69), Color(0xFFF5E5C9))
    HadithCollection.Muwatta -> HadithCoverPalette(Color(0xFF31543A), Color(0xFFCBAE6B), Color(0xFFF3E6CA))
    HadithCollection.Riyad -> HadithCoverPalette(Color(0xFF4C482F), Color(0xFFD7B56B), Color(0xFFF5E7C7))
    HadithCollection.Nawawi40 -> HadithCoverPalette(Color(0xFF67373A), Color(0xFFD3AA6A), Color(0xFFF6E4C5))
    HadithCollection.Other -> HadithCoverPalette(Color(0xFF454545), Color(0xFFB8A77A), Color(0xFFF2ECDD))
}

@Suppress("LongMethod")
@Composable
private fun HadithBookCover(
    collection: HadithCollection,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = collection.coverPalette()
    val title = stringResource(collection.titleRes)
    val largeCover = width >= 108.dp
    val titleSize = if (largeCover) 18.sp else if (width >= 90.dp) 13.sp else 11.sp
    val coverShape = RoundedCornerShape(if (largeCover) 12.dp else 9.dp)

    Surface(
        modifier = modifier.size(width = width, height = height),
        shape = coverShape,
        color = palette.background,
        tonalElevation = 3.dp,
        shadowElevation = if (largeCover) 14.dp else 9.dp,
        border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.92f)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = (size.minDimension * 0.017f).coerceAtLeast(1f)
                val edge = stroke * 1.65f
                val corner = CornerRadius(size.minDimension * 0.06f)

                drawRoundRect(
                    color = palette.accent,
                    topLeft = Offset(edge, edge),
                    size = Size(size.width - edge * 2f, size.height - edge * 2f),
                    cornerRadius = corner,
                    style = Stroke(width = stroke),
                )
                drawRoundRect(
                    color = palette.accent.copy(alpha = 0.58f),
                    topLeft = Offset(edge * 2.5f, edge * 2.5f),
                    size = Size(size.width - edge * 5f, size.height - edge * 5f),
                    cornerRadius = CornerRadius(size.minDimension * 0.045f),
                    style = Stroke(width = stroke * 0.55f),
                )

                val center = Offset(size.width * 0.53f, size.height * 0.40f)
                val medallionRadius = size.width * 0.30f
                drawCircle(
                    color = palette.accent.copy(alpha = 0.90f),
                    radius = medallionRadius,
                    center = center,
                    style = Stroke(width = stroke * 0.9f),
                )
                drawCircle(
                    color = palette.accent.copy(alpha = 0.42f),
                    radius = medallionRadius * 0.83f,
                    center = center,
                    style = Stroke(width = stroke * 0.48f),
                )

                val points = listOf(
                    0f to -1f,
                    0.707f to -0.707f,
                    1f to 0f,
                    0.707f to 0.707f,
                    0f to 1f,
                    -0.707f to 0.707f,
                    -1f to 0f,
                    -0.707f to -0.707f,
                )
                points.forEach { (dx, dy) ->
                    drawCircle(
                        color = palette.accent,
                        radius = stroke * 0.72f,
                        center = Offset(
                            center.x + dx * medallionRadius,
                            center.y + dy * medallionRadius,
                        ),
                    )
                }

                // Small geometric crown mark.
                val crownY = size.height * 0.13f
                drawLine(
                    color = palette.accent,
                    start = Offset(size.width * 0.42f, crownY),
                    end = Offset(size.width * 0.53f, crownY - size.width * 0.055f),
                    strokeWidth = stroke * 0.8f,
                )
                drawLine(
                    color = palette.accent,
                    start = Offset(size.width * 0.53f, crownY - size.width * 0.055f),
                    end = Offset(size.width * 0.64f, crownY),
                    strokeWidth = stroke * 0.8f,
                )

                // Minimal mosque silhouette at the bottom, matching the approved visual direction.
                val baseY = size.height * 0.84f
                val mosqueCenter = size.width * 0.53f
                val domeRadius = size.width * 0.085f
                drawLine(
                    color = palette.accent.copy(alpha = 0.92f),
                    start = Offset(size.width * 0.27f, baseY),
                    end = Offset(size.width * 0.79f, baseY),
                    strokeWidth = stroke * 0.72f,
                )
                drawRect(
                    color = palette.accent.copy(alpha = 0.90f),
                    topLeft = Offset(mosqueCenter - domeRadius, baseY - domeRadius * 0.95f),
                    size = Size(domeRadius * 2f, domeRadius * 0.95f),
                    style = Stroke(width = stroke * 0.65f),
                )
                drawCircle(
                    color = palette.accent.copy(alpha = 0.92f),
                    radius = domeRadius,
                    center = Offset(mosqueCenter, baseY - domeRadius * 0.95f),
                    style = Stroke(width = stroke * 0.65f),
                )
                listOf(size.width * 0.34f, size.width * 0.72f).forEach { x ->
                    drawLine(
                        color = palette.accent.copy(alpha = 0.92f),
                        start = Offset(x, baseY),
                        end = Offset(x, baseY - size.height * 0.12f),
                        strokeWidth = stroke * 0.72f,
                    )
                    drawCircle(
                        color = palette.accent.copy(alpha = 0.92f),
                        radius = stroke * 1.15f,
                        center = Offset(x, baseY - size.height * 0.12f),
                    )
                }

                // Book-spine cue.
                drawLine(
                    color = palette.accent.copy(alpha = 0.50f),
                    start = Offset(size.width * 0.105f, size.height * 0.06f),
                    end = Offset(size.width * 0.105f, size.height * 0.94f),
                    strokeWidth = stroke * 0.86f,
                )
            }

            Text(
                text = title,
                color = palette.foreground,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = titleSize * 1.08f,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(
                        start = if (largeCover) 16.dp else 8.dp,
                        end = if (largeCover) 16.dp else 8.dp,
                        bottom = height * 0.08f,
                    ),
            )
        }
    }
}

@Composable
private fun HadithCollectionCard(
    collection: HadithCollection,
    onClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        val coverWidth = (maxWidth - 4.dp).coerceAtMost(112.dp)
        val coverHeight = coverWidth * 1.46f

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HadithBookCover(
                collection = collection,
                width = coverWidth,
                height = coverHeight,
            )
            Spacer(Modifier.height(IslamicSpacing.Small))
            Text(
                text = stringResource(collection.titleRes),
                color = HadithIvory,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(IslamicSpacing.XXSmall))
            Text(
                text = stringResource(R.string.hadith_grid_count, collection.hadithCount),
                color = HadithGold.copy(alpha = 0.92f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
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
    val title = if (importedCount == 0) {
        stringResource(R.string.hadith_loading_book, stringResource(collection.titleRes))
    } else {
        stringResource(
            R.string.hadith_loading_book_progress,
            stringResource(collection.titleRes),
            importedCount,
        )
    }
    MuslimLoadingState(
        title = title,
        modifier = modifier.padding(IslamicSpacing.Large),
    )
}

@Composable
private fun HadithBookFailure(
    collection: HadithCollection,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MuslimErrorState(
        title = stringResource(R.string.hadith_book_load_failed),
        supportingText = stringResource(
            R.string.hadith_book_load_failed_supporting,
            stringResource(collection.titleRes),
        ),
        modifier = modifier.padding(IslamicSpacing.Large),
        actionLabel = stringResource(R.string.hadith_retry),
        onAction = onRetry,
    )
}

@Suppress("LongMethod")
@Composable
private fun HadithBookIndexOrPages(
    collection: HadithCollection,
    state: HadithBookContentState,
    actions: HadithBookActions,
    modifier: Modifier = Modifier,
) {
    HadithLibraryTheme {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(HadithLibraryBackground),
            contentPadding = PaddingValues(bottom = IslamicSpacing.Large),
        ) {
            item(key = "book-header") { HadithBookHeader(collection) }
            item(key = "search") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = HadithLibrarySurfaceRaised,
                    border = BorderStroke(1.dp, HadithGold.copy(alpha = 0.32f)),
                ) {
                    DigitNormalizedOutlinedTextField(
                        value = state.query,
                        onValueChange = actions.onQueryChanged,
                        label = { Text(stringResource(R.string.hadith_search_hint)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
                        fontWeight = FontWeight.Bold,
                        color = HadithGold,
                        modifier = Modifier.padding(
                            horizontal = IslamicSpacing.PageHorizontal,
                            vertical = IslamicSpacing.Medium,
                        ),
                    )
                }
                itemsIndexed(
                    items = state.chapters,
                    key = { _, item -> item.title },
                ) { index, item ->
                    HadithChapterRow(
                        number = index + 1,
                        chapter = item,
                        onClick = { actions.onOpenChapter(item) },
                    )
                }
                item(key = "source-notice") { HadithSourceNotice() }
            } else {
                item(key = "results-label") {
                    Text(
                        text = if (state.query.isBlank()) state.chapter.orEmpty() else stringResource(R.string.hadith_search_hint),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HadithGold,
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
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = IslamicSpacing.PageHorizontal),
                color = HadithGold.copy(alpha = 0.14f),
            )
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
    val palette = collection.coverPalette()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.background.copy(alpha = 0.96f),
                        HadithLibraryBackground,
                    ),
                ),
            )
            .padding(
                start = IslamicSpacing.PageHorizontal,
                end = IslamicSpacing.PageHorizontal,
                top = IslamicSpacing.Compact,
                bottom = IslamicSpacing.Medium,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HadithBookCover(
                collection = collection,
                width = 116.dp,
                height = 174.dp,
            )
            Spacer(Modifier.width(IslamicSpacing.Comfortable))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(collection.titleRes),
                    color = HadithIvory,
                    fontSize = 26.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(IslamicSpacing.Compact))
                Text(
                    text = stringResource(collection.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HadithIvory.copy(alpha = 0.84f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(IslamicSpacing.Comfortable))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            HadithStatCard(
                value = collection.hadithCount.toString(),
                label = stringResource(R.string.hadith_stat_hadiths),
                modifier = Modifier.weight(1f),
            )
            HadithStatCard(
                value = collection.chapterCount.toString(),
                label = stringResource(R.string.hadith_stat_chapters),
                modifier = Modifier.weight(1f),
            )
            HadithStatCard(
                value = "✓",
                label = stringResource(R.string.hadith_stat_offline),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HadithStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = HadithLibrarySurface.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, HadithGold.copy(alpha = 0.26f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = IslamicSpacing.Small, vertical = IslamicSpacing.Compact),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                color = HadithGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(Modifier.height(IslamicSpacing.XXSmall))
            Text(
                text = label,
                color = HadithIvory.copy(alpha = 0.76f),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HadithChapterRow(
    number: Int,
    chapter: HadithChapter,
    onClick: () -> Unit,
) {
    val sourceTitle = chapter.title.ifBlank { stringResource(R.string.hadith_all_chapters) }
    val displayTitle = if (AppLanguage.isArabicUi()) {
        HadithChapterArabicTitles.displayTitle(chapter.collection, sourceTitle)
    } else {
        sourceTitle
    }
    val range = if (chapter.firstHadithNumber != null && chapter.lastHadithNumber != null) {
        "${chapter.firstHadithNumber}–${chapter.lastHadithNumber}"
    } else {
        stringResource(R.string.hadith_chapter_unknown_range)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.XSmall)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = HadithLibrarySurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, HadithGold.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = IslamicSpacing.Compact, vertical = IslamicSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = HadithGold,
                modifier = Modifier.size(36.dp),
                shadowElevation = 3.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        color = HadithLibraryBackground,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.width(IslamicSpacing.Compact))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HadithIvory,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(IslamicSpacing.XSmall))
                Text(
                    text = stringResource(R.string.hadith_chapter_summary, chapter.hadithCount, range),
                    style = MaterialTheme.typography.bodySmall,
                    color = HadithIvory.copy(alpha = 0.66f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = HadithGold.copy(alpha = 0.90f),
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
    IslamicCard(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = IslamicSpacing.PageHorizontal,
            vertical = IslamicSpacing.Compact,
        ),
        containerColor = HadithLibrarySurfaceRaised,
    ) {
        Box {
            IslamicDecorationCorners(
                tint = MaterialTheme.colorScheme.tertiary,
                compact = true,
            )
            Column {
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
                modifier = Modifier.padding(horizontal = IslamicSpacing.Small, vertical = IslamicSpacing.XXSmall),
            )
        }
        Text(
            hadith.source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = IslamicSpacing.Small).weight(1f),
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
    MuslimLoadingState(
        title = stringResource(R.string.hadith_preview_loading),
        modifier = Modifier.padding(IslamicSpacing.PageHorizontal),
    )
}

@Composable
private fun HadithPageFailure(message: String?, onRetry: () -> Unit) {
    MuslimErrorState(
        title = stringResource(R.string.hadith_load_failed),
        supportingText = message ?: stringResource(R.string.hadith_load_failed),
        modifier = Modifier.padding(IslamicSpacing.PageHorizontal),
        actionLabel = stringResource(R.string.hadith_retry),
        onAction = onRetry,
    )
}

@Composable
private fun HadithEmptyState() {
    MuslimEmptyState(
        title = stringResource(R.string.hadith_no_results),
        icon = Icons.Filled.Search,
        modifier = Modifier.padding(IslamicSpacing.PageHorizontal),
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
