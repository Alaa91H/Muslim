package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import java.util.Locale
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.PlaybackState
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.ReaderTheme
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.SurahRevelationData

private const val MIN_FONT_SP = 18f
private const val MAX_FONT_SP = 40f
private const val DEFAULT_FONT_SP = 26f
private const val FONT_STEP_SP = 2f
private val REPEAT_OPTIONS = listOf(1, 3, 5, 10, -1) // -1 = continuous ("بدون توقف")

/**
 * The opening Basmala, rendered standalone at the top of every surah — except
 * At-Tawbah (9), which omits it, and Al-Fatiha (1), where it is ayah 1 and
 * stays numbered inline.
 */
internal val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
/**
 * Decorative mushaf ornament (Rub el Hizb ۞) framing the Basmala header.
 */
internal const val MUSHAF_ORNAMENT = "۞"

/**
 * Removes a leading Basmala from [text]. Matching is diacritic-insensitive and
 * also covers the `بِّسْمِ` shadda variant used in a couple of surahs; returns
 * the text unchanged when there is no Basmala prefix.
 */
internal fun stripLeadingBasmala(text: String): String {
    val normalizedBasmala = ArabicText.normalize(BASMALA)
    val consumed = StringBuilder()
    var index = 0
    for (c in text) {
        val kept = when {
            c.code in 0x064B..0x065F || c.code == 0x0670 -> ""
            c == '\u0671' -> "\u0627"
            else -> c.toString()
        }
        consumed.append(kept)
        index++
        if (consumed.length >= normalizedBasmala.length) {
            if (consumed.toString() != normalizedBasmala) return text
            // The Basmala's trailing diacritics (e.g. the kasra on its final
            // letter) are dropped by normalization but still occupy characters
            // in the source — skip them along with any following whitespace.
            var cut = index
            while (cut < text.length && isSkippableAfterBasmala(text[cut])) cut++
            return text.substring(cut)
        }
    }
    return text
}

private fun isSkippableAfterBasmala(c: Char): Boolean =
    c.isWhitespace() || c.code in 0x064B..0x065F || c.code == 0x0670 || c == '\u0640'

/** Sepia palette — warm paper, high contrast, comfortable long reading. */
private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF7A4E00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEAC),
    onPrimaryContainer = Color(0xFF2A1800),
    background = Color(0xFFF6EAD3),
    onBackground = Color(0xFF2A2118),
    surface = Color(0xFFF6EAD3),
    onSurface = Color(0xFF2A2118),
    surfaceVariant = Color(0xFFEBDCC0),
    onSurfaceVariant = Color(0xFF4E4434),
    secondaryContainer = Color(0xFFE5D2B0),
    onSecondaryContainer = Color(0xFF3A2C18),
)

/** Warm dark palette for night reading (reduces blue light). */
private val NightColorScheme = darkColorScheme(
    primary = Color(0xFFD4A017),
    onPrimary = Color(0xFF2A1A00),
    background = Color(0xFF141210),
    onBackground = Color(0xFFE8E0D4),
    surface = Color(0xFF141210),
    onSurface = Color(0xFFE8E0D4),
    surfaceVariant = Color(0xFF2A2622),
    onSurfaceVariant = Color(0xFFB8AE9F),
    secondaryContainer = Color(0xFF3A332A),
    onSecondaryContainer = Color(0xFFE8E0D4),
)

/**
 * Quran reader (PROJECT_PROMPT.md §6 Phase 2): Uthmani ayahs in a calm,
 * focus-first layout with adjustable font size, per-ayah bookmarks, automatic
 * last-read + khatma tracking, a reader colour theme (light/sepia/night), and
 * on-demand recitation playback with repetition for memorisation.
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun QuranReaderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDownloads: () -> Unit = {},
    viewModel: QuranReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val currentAyah by viewModel.currentAyah.collectAsStateWithLifecycle()
    val theme by viewModel.readerTheme.collectAsStateWithLifecycle()
    val persistedFont by viewModel.readerFontSize.collectAsStateWithLifecycle()
    val supplements by viewModel.supplements.collectAsStateWithLifecycle()
    val supplementEnabled by viewModel.supplementEnabled.collectAsStateWithLifecycle()
    val supplementLanguage by viewModel.supplementLanguage.collectAsStateWithLifecycle()
    val availableSupplementLanguages by viewModel.availableSupplementLanguages.collectAsStateWithLifecycle()
    val continuousStopAtEnd by viewModel.continuousStopAtEnd.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentAudioAyah by viewModel.currentAudioAyah.collectAsStateWithLifecycle()
    val hasNextAyah by viewModel.hasNextAyah.collectAsStateWithLifecycle()
    val hasPreviousAyah by viewModel.hasPreviousAyah.collectAsStateWithLifecycle()
    val playbackErrorCount by viewModel.playbackErrorCount.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    // Keep the screen lit while the mushaf reader is open (and therefore
    // during recitation) when the user enabled the keep-screen-on option;
    // restore the normal screen timeout on leave.
    val window = (LocalContext.current as? Activity)?.window
    DisposableEffect(keepScreenOn) {
        val activityWindow = window ?: return@DisposableEffect onDispose {}
        if (keepScreenOn) {
            activityWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Surface playback failures (e.g. a bad download or no connectivity) so a
    // silent "nothing happened" never confuses the user.
    val context = LocalContext.current
    val playbackErrorText = stringResource(R.string.quran_playback_error)
    var lastShownError by remember { mutableIntStateOf(0) }
    LaunchedEffect(playbackErrorCount) {
        if (playbackErrorCount > lastShownError) {
            lastShownError = playbackErrorCount
            Toast.makeText(context, playbackErrorText, Toast.LENGTH_SHORT).show()
        }
    }

    var fontSize by rememberSaveable { mutableFloatStateOf(DEFAULT_FONT_SP) }
    var repeatCount by rememberSaveable { mutableIntStateOf(1) }
    var playRange by rememberSaveable { mutableStateOf(RecitationRange.FromAyahToEnd) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showSupplementControls by remember { mutableStateOf(false) }
    // A short-lived highlight flashed on the ayah the user just tapped, so the
    // selection is unmistakable before playback starts.
    var tappedAyahGlobal by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(tappedAyahGlobal) {
        if (tappedAyahGlobal != null) {
            kotlinx.coroutines.delay(1_500)
            tappedAyahGlobal = null
        }
    }

    // The ayah the user explicitly tapped (تحديد بالضغط). Playback starts here
    // when set; otherwise pressing play starts from the surah's FIRST ayah,
    // regardless of which page is currently in view.
    var userSelectedAyah by remember { mutableStateOf<Int?>(null) }

    // Shared play/pause/resume toggle used by both the mini now-playing bar
    // and the full recitation bar.
    val togglePlayback: () -> Unit = {
        when (playbackState) {
            PlaybackState.Playing -> viewModel.pausePlayback()
            PlaybackState.Paused -> viewModel.resumePlayback()
            PlaybackState.Idle -> {
                // No explicit tap → start from the surah's first ayah, so
                // entering a surah and pressing play always begins correctly.
                val start = userSelectedAyah?.let { global ->
                    state.ayahs.firstOrNull { it.globalNumber == global }
                } ?: state.ayahs.firstOrNull()
                if (start != null) viewModel.playAyahWithRange(start, repeatCount, playRange)
            }
        }
    }

    // The ayah currently playing (if any) — drives the mini now-playing bar.
    val playingAyah = currentAudioAyah?.let { global -> state.ayahs.firstOrNull { it.globalNumber == global } }

    // Auto-scroll state so the selected / recited ayah stays fully visible.
    var scrollTargetAyah by remember { mutableStateOf<Int?>(null) }
    var targetAyahRootTopPx by remember { mutableStateOf<Float?>(null) }
    var viewportTopPx by remember { mutableFloatStateOf(0f) }
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    val reportAyahTop = remember { { _: Int, top: Float -> targetAyahRootTopPx = top } }

    // Group the surah's ayahs into mushaf pages (flowing text per page).
    val pageEntries = remember(state.ayahs) {
        state.ayahs.groupBy { it.page }.toSortedMap().entries.toList()
    }

    // Wide screens (tablets, landscape phones) show two mushaf pages side by
    // side per row — a real printed spread. Pairings are global mushaf pairs:
    // odd pages sit on the RIGHT, even pages on the LEFT, exactly as in a
    // printed mushaf (page 1 right + page 2 left, then 3+4, 5+6, …).
    val isWide = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp() >= 600.dp
    }
    val spreads = remember(state.ayahs) {
        state.ayahs.groupBy { it.page }.toSortedMap().entries
            .groupBy { (page, _) -> (page - 1) / 2 }
            .toSortedMap()
            .values
            .map { spread -> spread.sortedBy { it.key } }
    }
    val firstSpreadKey = spreads.firstOrNull()?.let { (it.first().key - 1) / 2 } ?: 0
    val spreadIndexOfPage: (Int) -> Int = { page -> ((page - 1) / 2) - firstSpreadKey }

    // Horizontal paging between mushaf pages (swipe left/right like a printed
    // mushaf). Each pager page keeps its own vertical scroll for content that
    // is taller than the screen.
    val pagerState = rememberPagerState(pageCount = { if (isWide) spreads.size else pageEntries.size })
    val pageScrollStates = remember { mutableStateMapOf<Int, ScrollState>() }

    // Persisted font size wins after the async read arrives.
    LaunchedEffect(persistedFont) {
        if (persistedFont > 0f && persistedFont != DEFAULT_FONT_SP) fontSize = persistedFont
    }

    // Scroll to the requested ayah (from search/bookmarks/resume) once loaded.
    var scrolledToInitial by remember { mutableStateOf(false) }
    LaunchedEffect(pageEntries, isWide) {
        if (scrolledToInitial || pageEntries.isEmpty()) return@LaunchedEffect
        val pageIndex = if (viewModel.initialAyahGlobal > 0) {
            pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == viewModel.initialAyahGlobal } }
        } else {
            -1
        }
        val targetItem = if (pageIndex >= 0 && isWide) {
            spreadIndexOfPage(pageEntries[pageIndex].key)
        } else {
            if (pageIndex >= 0) pageIndex else 0
        }
        pagerState.animateScrollToPage(targetItem)
        scrolledToInitial = true
    }

    // Follow-along: keep the recited ayah fully in view. When the playing
    // ayah changes it becomes the scroll target; the page-level jump below
    // brings a far page into composition, and the fine adjustment keeps the
    // ayah inside a comfortable band while reading.
    LaunchedEffect(currentAudioAyah) {
        scrollTargetAyah = currentAudioAyah
        targetAyahRootTopPx = null
    }

    // Phase 1: jump to the page/spread holding the target ayah only when it is
    // not currently visible (otherwise the fine adjustment does the work).
    LaunchedEffect(scrollTargetAyah, pageEntries, isWide) {
        val target = scrollTargetAyah ?: return@LaunchedEffect
        val pageIndex = pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == target } }
        if (pageIndex < 0) return@LaunchedEffect
        val targetItem = if (isWide) spreadIndexOfPage(pageEntries[pageIndex].key) else pageIndex
        // Smooth glide to a far page instead of an instant teleport; the fine
        // ayah alignment below stays immediate (scrollBy, not animated).
        if (pagerState.currentPage != targetItem) pagerState.animateScrollToPage(targetItem)
    }

    // Phase 2: once the target ayah's on-screen position is measured, scroll
    // just enough to keep it inside the top/bottom band of the viewport.
    LaunchedEffect(targetAyahRootTopPx, viewportTopPx, viewportHeightPx) {
        val ayahTop = targetAyahRootTopPx ?: return@LaunchedEffect
        if (viewportHeightPx <= 0) return@LaunchedEffect
        val pad = viewportHeightPx * 0.12f
        val topBound = viewportTopPx + pad
        val bottomBound = viewportTopPx + viewportHeightPx - pad
        val delta = when {
            ayahTop < topBound -> ayahTop - topBound
            ayahTop > bottomBound -> ayahTop - bottomBound
            else -> 0f
        }
        if (delta < -2f || delta > 2f) pageScrollStates[pagerState.currentPage]?.scrollBy(delta)
    }

    // Track the visible page (bookmark/play target) and persist resume + khatma.
    LaunchedEffect(pagerState, pageEntries, isWide) {
        if (pageEntries.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .map { itemIndex ->
                if (isWide) {
                    // A spread item covers two pages; the reading side is the
                    // right (odd) page, falling back to the only page present.
                    val spread = spreads.getOrNull(itemIndex) ?: return@map null
                    val rightPage = spread.firstOrNull { it.key % 2 == 1 } ?: spread.first()
                    rightPage.value.firstOrNull()
                } else {
                    pageEntries.getOrNull(itemIndex)?.value?.firstOrNull()
                }
            }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { viewModel.currentAyah.value = it }
            .debounce(2_000)
            .collect { viewModel.saveLastRead() }
    }

    val scheme = when (theme) {
        ReaderTheme.Light -> MaterialTheme.colorScheme
        ReaderTheme.Sepia -> SepiaColorScheme
        ReaderTheme.Dark -> NightColorScheme
    }

    MaterialTheme(colorScheme = scheme) {
        Column(modifier = modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.surah?.arabicName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        state.surah?.let {
                            Text(
                                text = it.englishName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.quran_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setReaderTheme(theme.next) }) {
                        Icon(
                            imageVector = when (theme) {
                                ReaderTheme.Light -> Icons.Filled.LightMode
                                ReaderTheme.Sepia -> Icons.Filled.Nightlight
                                ReaderTheme.Dark -> Icons.Filled.DarkMode
                            },
                            contentDescription = stringResource(R.string.quran_reader_theme),
                        )
                    }
                    IconButton(
                        onClick = viewModel::toggleBookmark,
                        enabled = currentAyah != null,
                    ) {
                        Icon(
                            imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(
                                if (bookmarked) R.string.quran_bookmark_remove else R.string.quran_bookmark_add
                            ),
                            tint = if (bookmarked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.quran_more_actions),
                            )
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            // Font size: − 26 +, compact so the row stays slim.
                            // Rendered directly (not as a disabled item) so the
                            // − / + buttons stay interactive.
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.quran_font_size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                FontSizeControls(
                                    fontSize = fontSize,
                                    onChanged = { newSize ->
                                        fontSize = newSize
                                        viewModel.setReaderFontSize(newSize)
                                    },
                                )
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.quran_keep_screen_on),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(
                                    checked = keepScreenOn,
                                    onCheckedChange = viewModel::setKeepScreenOn,
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.quran_details)) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Info, contentDescription = null)
                                },
                                onClick = {
                                    showMoreMenu = false
                                    if (state.surah != null) showDetails = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.quran_supplement_controls)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Translate,
                                        contentDescription = null,
                                        tint = if (supplementEnabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    if (currentAyah != null) showSupplementControls = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.quran_downloads_title)) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Download, contentDescription = null)
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenDownloads()
                                },
                            )
                        }
                    }
                },
            )

            when {
                state.loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                viewportTopPx = coords.positionInRoot().y
                                viewportHeightPx = coords.size.height
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                        pageSpacing = 12.dp,
                    ) { pageIndex ->
                        // Each pager page keeps its own vertical scroll for
                        // content taller than the screen; the follow-along
                        // adjustment scrolls this state.
                        val pageScroll = remember { ScrollState(0) }
                        pageScrollStates[pageIndex] = pageScroll
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(pageScroll),
                        ) {
                            if (isWide) {
                                // Two mushaf pages per pager page on wide screens.
                                val spread = spreads.getOrNull(pageIndex)
                                if (spread != null) {
                                    MushafSpreadRow(
                                        spread = spread,
                                        surahName = state.surah?.arabicName.orEmpty(),
                                        fontSizeSp = fontSize,
                                        playingAyahGlobal = currentAudioAyah,
                                        selectedAyahGlobal = currentAyah?.globalNumber,
                                        tappedAyahGlobal = tappedAyahGlobal,
                                        scrollTargetAyahGlobal = scrollTargetAyah,
                                        onAyahRootTopPx = reportAyahTop,
                                        onClickPage = { pageAyahs ->
                                            viewModel.currentAyah.value = pageAyahs.first()
                                        },
                                        onAyahClick = { ayah ->
                                            // Tap only selects the ayah; recitation
                                            // starts from the play button in the
                                            // recitation bar below.
                                            viewModel.currentAyah.value = ayah
                                            userSelectedAyah = ayah.globalNumber
                                            tappedAyahGlobal = ayah.globalNumber
                                        },
                                    )
                                }
                            } else {
                                val (pageNumber, pageAyahs) = pageEntries[pageIndex]
                                MushafPageCard(
                                    pageNumber = pageNumber,
                                    ayahs = pageAyahs,
                                    surahName = state.surah?.arabicName.orEmpty(),
                                    fontSizeSp = fontSize,
                                    playingAyahGlobal = currentAudioAyah,
                                    selectedAyahGlobal = currentAyah?.globalNumber,
                                    tappedAyahGlobal = tappedAyahGlobal,
                                    scrollTargetAyahGlobal = scrollTargetAyah,
                                    onAyahRootTopPx = reportAyahTop,
                                    onClick = { viewModel.currentAyah.value = pageAyahs.first() },
                                    onAyahClick = { ayah ->
                                        // Tap only selects the ayah; recitation
                                        // starts from the play button in the
                                        // recitation bar below.
                                        viewModel.currentAyah.value = ayah
                                        userSelectedAyah = ayah.globalNumber
                                        tappedAyahGlobal = ayah.globalNumber
                                    },
                                )
                            }
                        }
                    }
                }
            }

            SupplementPanel(supplements = supplements, currentAyah = currentAyah)

            RecitationBar(
                playbackState = playbackState,
                currentAyah = currentAyah,
                hasNext = hasNextAyah,
                hasPrevious = hasPreviousAyah,
                repeatCount = repeatCount,
                onRepeatChanged = { repeatCount = it },
                stopAtEnd = continuousStopAtEnd,
                onStopAtEndChanged = viewModel::setContinuousStopAtEnd,
                onPrevious = viewModel::previousAyah,
                onNext = viewModel::nextAyah,
                onTogglePlayback = togglePlayback,
                onStop = viewModel::stopPlayback,
                reciter = selectedReciter,
                onReciterSelected = viewModel::selectReciter,
                reciters = Reciter.Bundled,
                playingSurahName = state.surah?.arabicName.orEmpty(),
                playingSurahNumber = state.surah?.number ?: 0,
                playingAyahNumber = playingAyah?.numberInSurah,
                positionMs = positionMs,
                durationMs = durationMs,
                range = playRange,
                onRangeChanged = { playRange = it },
            )

        }

        if (showDetails) {
            state.surah?.let { surah ->
                SurahDetailsDialog(surah = surah, onDismiss = { showDetails = false })
            }
        }
        if (showSupplementControls) {
            SupplementControlsDialog(
                enabled = supplementEnabled,
                language = supplementLanguage,
                availableLanguages = availableSupplementLanguages,
                onEnabledChanged = viewModel::setSupplementEnabled,
                onLanguageChanged = viewModel::setSupplementLanguage,
                onDismiss = { showSupplementControls = false },
            )
        }
    }
}

@Composable
private fun SupplementPanel(
    supplements: QuranReaderViewModel.SupplementUi,
    currentAyah: Ayah?,
) {
    val hasContent = supplements.translations.isNotEmpty() || supplements.tafsir.isNotEmpty()
    if (!hasContent || currentAyah == null) return

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.quran_supplement_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            supplements.translations.forEach { translation ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = translation.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            supplements.tafsir.forEach { entry ->
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.quran_tafsir_source, entry.source),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


/**
 * Flips an icon horizontally in RTL layouts. Used for SkipPrevious/SkipNext,
 * which are not in the AutoMirrored icon set in the pinned Compose version.
 */
@Composable
private fun Modifier.mirroredIfRtl(): Modifier {
    val direction = androidx.compose.ui.platform.LocalLayoutDirection.current
    return if (direction == androidx.compose.ui.unit.LayoutDirection.Rtl) {
        this.then(
            Modifier.scale(
                scaleX = -1f,
                scaleY = 1f,
            )
        )
    } else {
        this
    }
}

@Composable
private fun RecitationBar(
    playbackState: PlaybackState,
    currentAyah: Ayah?,
    hasNext: Boolean,
    hasPrevious: Boolean,
    repeatCount: Int,
    onRepeatChanged: (Int) -> Unit,
    stopAtEnd: Boolean,
    onStopAtEndChanged: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTogglePlayback: () -> Unit,
    onStop: () -> Unit,
    reciter: Reciter,
    onReciterSelected: (Reciter) -> Unit,
    reciters: List<Reciter>,
    playingSurahName: String,
    playingSurahNumber: Int,
    playingAyahNumber: Int?,
    positionMs: Long,
    durationMs: Long,
    range: RecitationRange,
    onRangeChanged: (RecitationRange) -> Unit,
) {
    var repeatMenu by remember { mutableStateOf(false) }
    var rangeMenu by remember { mutableStateOf(false) }
    var reciterMenu by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        // Explicit content color keeps every label readable in the reader's
        // light / sepia / night themes (dark-mode contrast fix).
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column {
            // One slim now-playing line: reciter chip + surah/ayah + time, so
            // there is exactly ONE control bar. The reciter name is shown
            // next to the surah/ayah and is tappable to pick another reciter.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reciter chip: tap to open the full reciter picker.
                Box {
                    TextButton(
                        onClick = { reciterMenu = true },
                        modifier = Modifier.widthIn(max = 150.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = stringResource(R.string.quran_reciter),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = reciter.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    DropdownMenu(expanded = reciterMenu, onDismissRequest = { reciterMenu = false }) {
                        reciters.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = option.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            text = option.style,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (option.id == reciter.id) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                },
                                onClick = {
                                    reciterMenu = false
                                    onReciterSelected(option)
                                },
                            )
                        }
                    }
                }
                if (playingAyahNumber != null && playbackState != PlaybackState.Idle) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            R.string.quran_mini_surah_ayah,
                            playingSurahName,
                            playingSurahNumber,
                            playingAyahNumber,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${formatTime(positionMs)} / ${formatTime(durationMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
            IconButton(onClick = onPrevious, enabled = hasPrevious) {
                // SkipPrevious is not in the AutoMirrored set; flip it manually
                // so the "previous" arrow points forward in RTL layouts.
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(R.string.quran_previous_ayah),
                    modifier = Modifier.mirroredIfRtl(),
                )
            }
            IconButton(onClick = onTogglePlayback, enabled = playbackState != PlaybackState.Idle || currentAyah != null) {
                Icon(
                    imageVector = when (playbackState) {
                        PlaybackState.Playing -> Icons.Filled.Pause
                        else -> Icons.Filled.PlayArrow
                    },
                    contentDescription = stringResource(R.string.quran_play_ayah),
                )
            }
            IconButton(onClick = onNext, enabled = hasNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = stringResource(R.string.quran_next_ayah),
                    modifier = Modifier.mirroredIfRtl(),
                )
            }

            // One repeat button: opens a popup with all repeat options; a
            // single tap selects one (incl. "بدون توقف" continuous playback).
            Box {
                TextButton(onClick = { repeatMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = stringResource(R.string.quran_repeat),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(repeatButtonLabel(repeatCount))
                }
                DropdownMenu(expanded = repeatMenu, onDismissRequest = { repeatMenu = false }) {
                    REPEAT_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(repeatOptionLabel(option)) },
                            trailingIcon = {
                                if (repeatCount == option) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                            onClick = {
                                repeatMenu = false
                                onRepeatChanged(option)
                            },
                        )
                    }
                    // Only meaningful for continuous ("بدون توقف") playback.
                    if (repeatCount <= 0) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStopAtEndChanged(!stopAtEnd) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.quran_stop_at_end_of_mushaf),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Checkbox(
                                checked = stopAtEnd,
                                onCheckedChange = onStopAtEndChanged,
                            )
                        }
                    }
                }
            }

            // One range button: opens a popup with the playback-range options.
            Box {
                TextButton(onClick = { rangeMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = stringResource(R.string.quran_play_range),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(rangeButtonLabel(range))
                }
                DropdownMenu(expanded = rangeMenu, onDismissRequest = { rangeMenu = false }) {
                    RecitationRange.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(rangeLabel(option)) },
                            trailingIcon = {
                                if (range == option) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                            onClick = {
                                rangeMenu = false
                                onRangeChanged(option)
                            },
                        )
                    }
                }
            }

            // Stop button, only meaningful while something is playing. Kept
            // inside the single bar so users never see a second control row.
            if (playbackState != PlaybackState.Idle) {
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.quran_stop_playback),
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun repeatButtonLabel(repeatCount: Int): String =
    if (repeatCount <= 0) "∞" else "×$repeatCount"

@Composable
private fun repeatOptionLabel(repeatCount: Int): String =
    if (repeatCount <= 0) stringResource(R.string.quran_repeat_continuous) else "×$repeatCount"

@Composable
private fun rangeLabel(range: RecitationRange): String = when (range) {
    RecitationRange.SingleAyah -> stringResource(R.string.quran_range_single_ayah)
    RecitationRange.FromAyahToEnd -> stringResource(R.string.quran_range_to_end)
    RecitationRange.WholeSurah -> stringResource(R.string.quran_range_whole_surah)
}

@Composable
private fun rangeButtonLabel(range: RecitationRange): String = when (range) {
    RecitationRange.SingleAyah -> stringResource(R.string.quran_range_single_ayah_short)
    RecitationRange.FromAyahToEnd -> stringResource(R.string.quran_range_to_end_short)
    RecitationRange.WholeSurah -> stringResource(R.string.quran_range_whole_surah_short)
}
/**
 * Formats milliseconds as m:ss or mm:ss (h:mm:ss from one hour up), always
 * with Western digits regardless of the device locale.
 */

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}

/**
 * Two facing mushaf pages (a printed spread) on wide screens: the odd page on
 * the right, the even page on the left — matching the layout of a real mushaf
 * regardless of the app's language direction. A spread with a single page
 * keeps it on its correct side (page 2 alone sits on the left).
 */
@Composable
private fun MushafSpreadRow(
    spread: List<Map.Entry<Int, List<Ayah>>>,
    surahName: String,
    fontSizeSp: Float,
    playingAyahGlobal: Int?,
    selectedAyahGlobal: Int?,
    tappedAyahGlobal: Int?,
    scrollTargetAyahGlobal: Int?,
    onClickPage: (List<Ayah>) -> Unit,
    onAyahClick: (Ayah) -> Unit,
    onAyahRootTopPx: (Int, Float) -> Unit,
) {
    val rightPage = spread.firstOrNull { it.key % 2 == 1 }
    val leftPage = spread.firstOrNull { it.key % 2 == 0 }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (rightPage != null) {
                MushafPageCard(
                    pageNumber = rightPage.key,
                    ayahs = rightPage.value,
                    surahName = surahName,
                    fontSizeSp = fontSizeSp,
                    playingAyahGlobal = playingAyahGlobal,
                    selectedAyahGlobal = selectedAyahGlobal,
                    tappedAyahGlobal = tappedAyahGlobal,
                    scrollTargetAyahGlobal = scrollTargetAyahGlobal,
                    onAyahRootTopPx = onAyahRootTopPx,
                    onClick = { onClickPage(rightPage.value) },
                    onAyahClick = onAyahClick,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (leftPage != null) {
                MushafPageCard(
                    pageNumber = leftPage.key,
                    ayahs = leftPage.value,
                    surahName = surahName,
                    fontSizeSp = fontSizeSp,
                    playingAyahGlobal = playingAyahGlobal,
                    selectedAyahGlobal = selectedAyahGlobal,
                    tappedAyahGlobal = tappedAyahGlobal,
                    scrollTargetAyahGlobal = scrollTargetAyahGlobal,
                    onAyahRootTopPx = onAyahRootTopPx,
                    onClick = { onClickPage(leftPage.value) },
                    onAyahClick = onAyahClick,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MushafPageCard(
    pageNumber: Int,
    ayahs: List<Ayah>,
    surahName: String,
    fontSizeSp: Float,
    playingAyahGlobal: Int?,
    selectedAyahGlobal: Int?,
    tappedAyahGlobal: Int?,
    scrollTargetAyahGlobal: Int?,
    onClick: () -> Unit,
    onAyahClick: (Ayah) -> Unit,
    onAyahRootTopPx: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (ayahs.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    var textRootTopPx by remember { mutableFloatStateOf(0f) }
    var targetCharOffset by remember { mutableIntStateOf(-1) }
    var targetLineTopPx by remember { mutableFloatStateOf(-1f) }

    // Report the target ayah's absolute on-screen top once it is laid out in
    // this page so the screen can scroll it into view.
    LaunchedEffect(textRootTopPx, targetLineTopPx, scrollTargetAyahGlobal) {
        val target = scrollTargetAyahGlobal ?: return@LaunchedEffect
        if (targetLineTopPx < 0f) return@LaunchedEffect
        onAyahRootTopPx(target, textRootTopPx + targetLineTopPx)
    }
    // The Basmala embedded at the start of ayah 1 (every surah except 9) is
    // pulled out into its own line above the surah, like printed mushafs.
    val firstAyah = ayahs.first()
    val isSurahOpeningPage = firstAyah.numberInSurah == 1
    val firstAyahText = if (isSurahOpeningPage) stripLeadingBasmala(firstAyah.text) else firstAyah.text
    val showBasmala = isSurahOpeningPage && firstAyah.surahNumber != 1 && firstAyahText != firstAyah.text
    val annotated = buildAnnotatedString {
        ayahs.forEach { ayah ->
            if (ayah.globalNumber == scrollTargetAyahGlobal) targetCharOffset = length
            // Visual hierarchy: the tapped ayah flashes strongest (temporary),
            // the ayah being recited glows while playing (follow-along), and the
            // currently selected ayah keeps a soft tint. All work on the light,
            // sepia and night themes.
            val highlight = when {
                ayah.globalNumber == tappedAyahGlobal ->
                    SpanStyle(background = scheme.primary.copy(alpha = 0.35f))
                ayah.globalNumber == playingAyahGlobal ->
                    SpanStyle(background = scheme.primary.copy(alpha = 0.22f))
                // Suppress the soft selection tint while recitation is playing so
                // a stale highlight never lingers on the originally-tapped ayah
                // once the reciter advances to the next ayah.
                playingAyahGlobal == null && ayah.globalNumber == selectedAyahGlobal ->
                    SpanStyle(background = scheme.primary.copy(alpha = 0.12f))
                else -> SpanStyle()
            }
            // Every ayah is individually tappable: tapping selects it (and
            // flashes the highlight); recitation starts from the play button.
            withLink(
                LinkAnnotation.Clickable(tag = "ayah-${ayah.globalNumber}") {
                    onAyahClick(ayah)
                },
            ) {
                withStyle(highlight) {
                    append(if (ayah === firstAyah) firstAyahText else ayah.text)
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = scheme.primary,
                            fontSize = (fontSizeSp * 0.6f).sp,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.35f),
                        ),
                    ) {
                        append("\uFD3F${ayah.numberInSurah.toString()}\uFD3E")
                    }
                    append(" ")
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = surahName,
                    style = MaterialTheme.typography.labelMedium.copy(textDirection = TextDirection.Rtl),
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.quran_page_header, pageNumber.toString(), ayahs.first().juz.toString()),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = scheme.surfaceVariant)
            Spacer(Modifier.height(14.dp))
            if (showBasmala) {
                // Mushaf-style Basmala header: a decorative ornament above, the
                // Basmala centered, and an ornamented divider below.
                Text(
                    text = MUSHAF_ORNAMENT,
                    fontSize = (fontSizeSp * 0.9f).sp,
                    textAlign = TextAlign.Center,
                    color = scheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = BASMALA,
                    fontSize = (fontSizeSp * 1.1f).sp,
                    lineHeight = (fontSizeSp * 1.9f).sp,
                    textAlign = TextAlign.Center,
                    color = scheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                )
                Spacer(Modifier.height(10.dp))
                OrnamentedDivider(
                    color = scheme.surfaceVariant,
                    accentColor = scheme.primary,
                )
                Spacer(Modifier.height(14.dp))
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            BasicText(
                text = annotated,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords -> textRootTopPx = coords.positionInRoot().y }
                    .pointerInput(annotated) {
                        detectTapGestures { position ->
                            val result = layoutResult ?: return@detectTapGestures
                            val offset = result.getOffsetForPosition(position)
                            val link = annotated.getLinkAnnotations(offset, offset + 1).firstOrNull()
                            link?.item?.linkInteractionListener?.onClick(link.item)
                        }
                    },
                style = MaterialTheme.typography.bodyLarge.copy(
                    // Explicit onSurface color: the ayah text must stay readable
                    // on the night (dark) mushaf page regardless of the theme.
                    color = scheme.onSurface,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.9f).sp,
                    textAlign = TextAlign.Center,
                    // The mushaf is always right-to-left, even when the app UI
                    // language is LTR (English).
                    textDirection = TextDirection.Rtl,
                ),
                onTextLayout = { result ->
                    layoutResult = result
                    if (targetCharOffset >= 0 && result.layoutInput.text.isNotEmpty()) {
                        val offset = targetCharOffset.coerceIn(0, result.layoutInput.text.length - 1)
                        val line = result.getLineForOffset(offset)
                        targetLineTopPx = result.getLineTop(line)
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * A divider with the Rub el Hizb (۞) ornament in the middle, echoing the
 * decorative bands between a mushaf header and its body text.
 */
@Composable
private fun OrnamentedDivider(color: Color, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = color)
        Text(
            text = MUSHAF_ORNAMENT,
            fontSize = 13.sp,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = color)
    }
}

@Composable
private fun FontSizeControls(fontSize: Float, onChanged: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { onChanged((fontSize - FONT_STEP_SP).coerceAtLeast(MIN_FONT_SP)) },
            enabled = fontSize > MIN_FONT_SP,
        ) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.quran_font_smaller))
        }
        Text(
            text = "${fontSize.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = { onChanged((fontSize + FONT_STEP_SP).coerceAtMost(MAX_FONT_SP)) },
            enabled = fontSize < MAX_FONT_SP,
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quran_font_larger))
        }
    }
}

private val ReaderTheme.next: ReaderTheme
    get() = when (this) {
        ReaderTheme.Light -> ReaderTheme.Sepia
        ReaderTheme.Sepia -> ReaderTheme.Dark
        ReaderTheme.Dark -> ReaderTheme.Light
    }

/**
 * Details dialog for a surah: type (Meccan/Medinan), chronological order of
 * revelation, ayah count, and the reason for revelation when known.
 */
@Composable
private fun SurahDetailsDialog(surah: Surah, onDismiss: () -> Unit) {
    val isEnglish = LocalConfiguration.current.locales[0].language.startsWith("en")
    val reason = SurahRevelationData.reasonOf(surah.number)
    val order = SurahRevelationData.orderOf(surah.number)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(surah.arabicName) },
        text = {
            Column {
                Text(
                    text = "${surah.englishName} — ${surah.translation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                DetailRow(
                    label = stringResource(R.string.quran_details_type),
                    value = stringResource(
                        if (surah.revelationType.equals("Meccan", ignoreCase = true)) {
                            R.string.quran_details_meccan
                        } else {
                            R.string.quran_details_medinan
                        }
                    ),
                )
                order?.let {
                    DetailRow(
                        label = stringResource(R.string.quran_details_order),
                        value = stringResource(R.string.quran_details_order_value, it),
                    )
                }
                DetailRow(
                    label = stringResource(R.string.quran_details_ayahs),
                    value = surah.ayahCount.toString(),
                )
                reason?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.quran_details_reason_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) it.second else it.first,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.quran_details_close))
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Meanings/tafsir controls: a master switch (show/hide the panel under the
 * mushaf page) plus the translation language, defaulting to the app language
 * ("auto"). The reader applies the choice immediately via the ViewModel.
 */
@Composable
private fun SupplementControlsDialog(
    enabled: Boolean,
    language: String,
    availableLanguages: List<String>,
    onEnabledChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quran_supplement_controls)) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.quran_supplement_show),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChanged,
                    )
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.quran_supplement_language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                val options = buildList {
                    add(QuranPrefsRepository.AUTO_LANGUAGE)
                    addAll(availableLanguages)
                }.distinct()
                options.forEach { option ->
                    val label = if (option == QuranPrefsRepository.AUTO_LANGUAGE) {
                        stringResource(R.string.quran_supplement_language_auto, appLanguageName())
                    } else {
                        displayLanguageName(option)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onLanguageChanged(option) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (language == option) {
                                Icons.Filled.RadioButtonChecked
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (language == option) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.quran_details_close))
            }
        },
    )
}

/** The current app language name for the "auto" option (e.g. "العربية"). */
@Composable
private fun appLanguageName(): String {
    val config = LocalConfiguration.current
    val language = config.locales[0].language
    return displayLanguageName(language)
}

/** Localized display name of a BCP-47 language tag, falling back to the tag. */
@Composable
private fun displayLanguageName(tag: String): String {
    val locale = LocalConfiguration.current.locales[0]
    return runCatching {
        Locale.forLanguageTag(tag).getDisplayName(locale)
    }.getOrElse { tag }
}
