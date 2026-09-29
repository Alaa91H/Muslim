package org.muslim.app.feature.quran.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import java.util.Locale
import androidx.compose.material3.TopAppBar

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.muslim.app.core.common.text.ArabicText
import org.muslim.app.core.designsystem.IslamicElevation
import org.muslim.app.core.designsystem.IslamicMotion
import org.muslim.app.core.designsystem.IslamicRadius
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.designsystem.MuslimSepiaColors
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicSelectableCard
import org.muslim.app.core.ui.theme.MuslimActionItem
import org.muslim.app.core.ui.theme.MuslimActionSheet
import org.muslim.app.core.ui.theme.MuslimBottomSheet
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.core.ui.theme.IslamicDecorationCorners
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicReadingBasmalaAccent
import org.muslim.app.core.ui.theme.IslamicReadingDivider
import org.muslim.app.core.ui.theme.IslamicReadingHeaderDecoration
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.TajweedMarkup
import org.muslim.app.feature.quran.data.PlaybackState
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.ReaderTheme
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.SurahRevelationData

private const val DEFAULT_FONT_SP = 26f
private val REPEAT_OPTIONS = listOf(1, 3, 5, 10, -1) // -1 = continuous ("بدون توقف")

/**
 * The opening Basmala, rendered standalone at the top of every surah — except
 * At-Tawbah (9), which omits it, and Al-Fatiha (1), where it is ayah 1 and
 * stays numbered inline.
 */
internal val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
/**
 * Removes a leading Basmala from [text]. Matching is diacritic-insensitive and
 * also covers the `بِّسْمِ` shadda variant used in a couple of surahs; returns
 * the text unchanged when there is no Basmala prefix.
 */
private fun leadingBasmalaEndIndex(text: String): Int? {
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
            if (consumed.toString() != normalizedBasmala) return null
            // Include the Basmala's trailing diacritics and consume only the
            // separator whitespace before the first ayah text.
            var cut = index
            while (cut < text.length && isSkippableAfterBasmala(text[cut])) cut++
            return cut
        }
    }
    return null
}

/**
 * Returns the exact Uthmani Basmala prefix from the bundled Quran text.
 * Keeping the source spelling is important for Medina-Mushaf variants such as
 * the shadda on the opening baa in surahs 95 and 97.
 */
internal fun extractLeadingBasmala(text: String): String? =
    leadingBasmalaEndIndex(text)?.let { end -> text.substring(0, end).trimEnd() }

internal fun stripLeadingBasmala(text: String): String =
    leadingBasmalaEndIndex(text)?.let { end -> text.substring(end) } ?: text

internal data class SurahOpeningPresentation(
    val ayahText: String,
    val standaloneBasmala: String?,
)

/**
 * Medina-Mushaf opening rule:
 * - Al-Fatiha keeps the Basmala inline as numbered ayah 1.
 * - Surahs 2..114 display their encoded opening Basmala separately, when one
 *   exists in the source text.
 * - At-Tawbah (9) has no opening Basmala, so nothing is synthesized.
 */
internal fun surahOpeningPresentation(
    surahNumber: Int,
    ayahNumber: Int,
    text: String,
): SurahOpeningPresentation {
    if (ayahNumber != 1 || surahNumber == 1) {
        return SurahOpeningPresentation(ayahText = text, standaloneBasmala = null)
    }
    val basmala = extractLeadingBasmala(text)
        ?: return SurahOpeningPresentation(ayahText = text, standaloneBasmala = null)
    return SurahOpeningPresentation(
        ayahText = stripLeadingBasmala(text),
        standaloneBasmala = basmala,
    )
}

private fun isSkippableAfterBasmala(c: Char): Boolean =
    c.isWhitespace() || c.code in 0x064B..0x065F || c.code == 0x0670 || c == '\u0640'

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
    val tajweedEnabled by viewModel.tajweedEnabled.collectAsStateWithLifecycle()
    val tajweedAnnotations by viewModel.tajweedAnnotations.collectAsStateWithLifecycle()
    val installedTafsirSources by viewModel.installedTafsirSources.collectAsStateWithLifecycle()
    val selectedTafsirSource by viewModel.selectedTafsirSource.collectAsStateWithLifecycle()
    val tafsirDownloadState by viewModel.tafsirDownloadState.collectAsStateWithLifecycle()
    val supplementLanguage by viewModel.supplementLanguage.collectAsStateWithLifecycle()
    val availableSupplementLanguages by viewModel.availableSupplementLanguages.collectAsStateWithLifecycle()
    val continuousStopAtEnd by viewModel.continuousStopAtEnd.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentAudioAyah by viewModel.currentAudioAyah.collectAsStateWithLifecycle()
    val hasNextAyah by viewModel.hasNextAyah.collectAsStateWithLifecycle()
    val hasPreviousAyah by viewModel.hasPreviousAyah.collectAsStateWithLifecycle()
    val recitationFailure by viewModel.recitationFailure.collectAsStateWithLifecycle()
    val restorableSession by viewModel.restorableSession.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    // Keep the screen lit while the mushaf reader is open (and therefore
    // during recitation) when the user enabled the keep-screen-on option;
    // restore the normal screen timeout on leave.
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    DisposableEffect(keepScreenOn) {
        val activityWindow = window ?: return@DisposableEffect onDispose {}
        if (keepScreenOn) {
            activityWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Playback/download failures are actionable and retain the last queue
    // request, so retry resumes the same recitation intent instead of silently
    // restarting from ayah one. Reuse the already-localized playback message;
    // the typed failure reason remains available to the state layer.
    val playbackFailureText = stringResource(R.string.quran_playback_error)

    var fontSize by rememberSaveable { mutableFloatStateOf(DEFAULT_FONT_SP) }
    var repeatCount by rememberSaveable { mutableIntStateOf(1) }
    // The reader defaults to a continuous recitation from the selected ayah
    // through the end of the mushaf. Other ranges remain explicit choices.
    var playRange by rememberSaveable { mutableStateOf(DEFAULT_RECITATION_RANGE) }
    var showReaderSettings by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showSupplementControls by remember { mutableStateOf(false) }
    var showAyahActions by remember { mutableStateOf(false) }
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

    // The explicit play action in the surah list opens this reader with a
    // one-shot request. Wait until the ayahs are loaded, then start from ayah
    // one of this surah; do not replay on recomposition or configuration UI.
    LaunchedEffect(state.ayahs) {
        if (state.ayahs.isNotEmpty() && viewModel.consumeAutoplayWholeSurah()) {
            userSelectedAyah = null
            playRange = DEFAULT_RECITATION_RANGE
            viewModel.playFromAyah(state.ayahs.first(), repeatCount)
        }
    }

    // Preserve the deliberately tapped ayah independently of the scrolling
    // cursor, so both play controls can start exactly from that selection.
    val selectedStart = userSelectedAyah?.let { global ->
        state.ayahs.firstOrNull { it.globalNumber == global }
    }

    // Shared play/pause/resume toggle used by both the mini now-playing bar
    // and the full recitation bar.
    val togglePlayback: () -> Unit = {
        when (playbackState) {
            PlaybackState.Playing -> viewModel.pausePlayback()
            PlaybackState.Paused -> {
                // A newly selected ayah is an explicit new start point; never
                // resume a previously paused ayah when the user chose another.
                if (selectedStart != null && selectedStart.globalNumber != currentAudioAyah) {
                    viewModel.playFromSelectedAyahToSurahEnd(selectedStart, repeatCount)
                } else {
                    viewModel.resumePlayback()
                }
            }
            PlaybackState.Idle -> {
                // Without a deliberate selection, playback starts at ayah one.
                // A selected ayah always wins and continues only to this
                // surah's end, rather than restarting from ayah one.
                if (selectedStart != null) {
                    viewModel.playFromSelectedAyahToSurahEnd(selectedStart, repeatCount)
                } else {
                    state.ayahs.firstOrNull()?.let { start ->
                        viewModel.playAyahWithRange(start, repeatCount, playRange)
                    }
                }
            }
        }
    }

    // The ayah currently playing (if any) — drives the mini now-playing bar.
    val playingAyah = currentAudioAyah?.let { global -> state.ayahs.firstOrNull { it.globalNumber == global } }

    // Auto-scroll state so the selected / recited ayah stays fully visible.
    var scrollTargetAyah by remember { mutableStateOf<Int?>(null) }
    var targetAyahRootBoundsPx by remember { mutableStateOf<AyahViewportBounds?>(null) }
    var viewportTopPx by remember { mutableFloatStateOf(0f) }
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    // Only accept position reports for the ayah we are currently tracking — a
    // neighbouring page's measurement for an older target must never clobber
    // the live follow-along position.
    val reportAyahBounds = remember {
        { ayahGlobal: Int, top: Float, bottom: Float ->
            if (ayahGlobal == scrollTargetAyah) {
                targetAyahRootBoundsPx = AyahViewportBounds(topPx = top, bottomPx = bottom)
            }
        }
    }

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
    // Reserve one virtual page at each edge. Swiping onto either edge opens
    // the adjacent surah, so manual reading continues like a physical Mushaf
    // rather than stopping at a surah boundary.
    val realPageCount = if (isWide) spreads.size else pageEntries.size
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { realPageCount + 2 },
    )
    val pageScrollStates = remember { mutableStateMapOf<Int, ScrollState>() }
    var pendingAdjacentSurah by remember { mutableStateOf<Int?>(null) }
    var openAdjacentAtEnd by remember { mutableStateOf(false) }
    // Each mushaf page reports the top of every ayah. This keeps the selected
    // ayah and the saved reading position in step with manual up/down reading,
    // while the existing audio follow-along remains authoritative during play.
    val ayahPositionsByPage = remember { mutableStateMapOf<Int, List<AyahViewportPosition>>() }

    // Persisted font size wins after the async read arrives.
    LaunchedEffect(persistedFont) {
        if (persistedFont > 0f && persistedFont != DEFAULT_FONT_SP) fontSize = persistedFont
    }

    // Scroll to the requested ayah (from search/bookmarks/resume) once loaded.
    var scrolledToInitial by remember { mutableStateOf(false) }
    // When opening with a target ayah, highlight it and center it vertically
    // in the viewport (not just bring its page into view). Cleared after the
    // one-shot centering so normal follow-along takes over.
    var centerInitialAyah by remember { mutableStateOf(viewModel.initialAyahGlobal > 0) }
    // The ayah to tint on open (search/bookmark/resume). Separate from
    // currentAyah because the pager-tracking collector owns that value.
    var openedTargetGlobal by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(pageEntries, isWide) {
        if (scrolledToInitial || pageEntries.isEmpty()) return@LaunchedEffect
        val targetGlobal = viewModel.initialAyahGlobal
        val pageIndex = if (targetGlobal > 0) {
            pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == targetGlobal } }
        } else {
            -1
        }
        val targetItem = if (pageIndex >= 0 && isWide) {
            contentIndexToReaderPagerPage(spreadIndexOfPage(pageEntries[pageIndex].key))
        } else {
            if (pageIndex >= 0) contentIndexToReaderPagerPage(pageIndex) else 1
        }
        // Wait for the page animation to settle before measuring/centering
        // the ayah, so the one-shot centering sees a stable layout.
        pagerState.animateScrollToPage(targetItem)
        if (targetGlobal > 0) {
            if (state.ayahs.any { it.globalNumber == targetGlobal }) {
                // Highlight the target ayah and make it the scroll target so
                // the centering pass below can align it in the viewport.
                openedTargetGlobal = targetGlobal
                scrollTargetAyah = targetGlobal
                centerInitialAyah = true
                targetAyahRootBoundsPx = null
            }
        }
        scrolledToInitial = true
    }

    // Follow-along: keep the recited ayah fully in view. When the playing
    // ayah changes it becomes the scroll target; the page-level jump below
    // brings a far page into composition, and the fine adjustment keeps the
    // ayah inside a comfortable band while reading. While the initial
    // open-target is being centered it must not be clobbered by this.
    LaunchedEffect(currentAudioAyah) {
        if (currentAudioAyah == null && centerInitialAyah) return@LaunchedEffect
        scrollTargetAyah = currentAudioAyah
        targetAyahRootBoundsPx = null
    }

    // Phase 1: jump to the page/spread holding the target ayah only when it is
    // not currently visible (otherwise the fine adjustment does the work).
    LaunchedEffect(scrollTargetAyah, pageEntries, isWide) {
        val target = scrollTargetAyah ?: return@LaunchedEffect
        val pageIndex = pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == target } }
        if (pageIndex < 0) return@LaunchedEffect
        val targetItem = if (isWide) {
            contentIndexToReaderPagerPage(spreadIndexOfPage(pageEntries[pageIndex].key))
        } else {
            contentIndexToReaderPagerPage(pageIndex)
        }
        // Smooth glide to a far page instead of an instant teleport; the fine
        // ayah alignment below stays immediate (scrollBy, not animated).
        if (pagerState.currentPage != targetItem) pagerState.animateScrollToPage(targetItem)
    }

    // Phase 2: once the target ayah's on-screen position is measured, scroll
    // just enough to keep it inside the top/bottom band of the viewport.
    // On the one-shot initial open (search/bookmark/resume) the ayah is
    // centered vertically instead, then normal band-following resumes.
    LaunchedEffect(targetAyahRootBoundsPx, viewportTopPx, viewportHeightPx) {
        val ayahBounds = targetAyahRootBoundsPx ?: return@LaunchedEffect
        if (viewportHeightPx <= 0) return@LaunchedEffect
        // Only align when the measurement comes from the CURRENT pager page.
        // While a page-slide animation runs, measurements from the incoming
        // page are transient — scrolling the old page's scroll state would be
        // wrong — and the settled measurement that follows triggers this again.
        val target = scrollTargetAyah ?: return@LaunchedEffect
        val targetPageIndex = pageEntries.indexOfFirst { (_, ayahs) ->
            ayahs.any { it.globalNumber == target }
        }
        if (targetPageIndex < 0) return@LaunchedEffect
        val targetItem = if (isWide) {
            contentIndexToReaderPagerPage(spreadIndexOfPage(pageEntries[targetPageIndex].key))
        } else {
            contentIndexToReaderPagerPage(targetPageIndex)
        }
        if (pagerState.currentPage != targetItem) return@LaunchedEffect
        // ScrollState.scrollBy uses the conventional scroll offset: a
        // positive value moves the content toward the end (up on screen),
        // while a negative value moves it back toward the start (down).
        // Follow the COMPLETE ayah bounds, not only its first line, so a
        // multi-line recited ayah is never left partially hidden behind the
        // recitation controls when the whole ayah can fit in the viewport.
        val delta = calculateAyahFollowScrollDelta(
            ayahTopPx = ayahBounds.topPx,
            ayahBottomPx = ayahBounds.bottomPx,
            viewportTopPx = viewportTopPx,
            viewportHeightPx = viewportHeightPx,
            center = centerInitialAyah,
        )
        if (delta < -2f || delta > 2f) pageScrollStates[pagerState.currentPage]?.scrollBy(delta)
        if (centerInitialAyah) {
            // The one-shot centering is done; hand control back to the
            // follow-along logic (and stop tracking the open target).
            centerInitialAyah = false
            scrollTargetAyah = null
            openedTargetGlobal = null
        }
    }

    // Manual vertical reading: update the active ayah to the first ayah at the
    // reader's top edge. This deliberately does not set scrollTargetAyah, so a
    // user can scroll in either direction without being pulled back unless an
    // actual recitation is playing.
    LaunchedEffect(pagerState, pageEntries, spreads, isWide, currentAudioAyah) {
        snapshotFlow {
            val itemIndex = pagerState.currentPage - 1
            // Reading this state makes the flow react to deliberate vertical
            // scrolls as well as horizontal page changes.
            pageScrollStates[pagerState.currentPage]?.value
            val mushafPages = if (isWide) {
                spreads.getOrNull(itemIndex).orEmpty().map { it.key }
            } else {
                listOfNotNull(pageEntries.getOrNull(itemIndex)?.key)
            }
            val positions = mushafPages.flatMap { ayahPositionsByPage[it].orEmpty() }
            if (currentAudioAyah != null) null else ayahAtReaderTop(positions, viewportTopPx)
        }
            .filterNotNull()
            .distinctUntilChanged()
            .debounce(350)
            .collect { globalNumber ->
                val ayah = state.ayahs.firstOrNull { it.globalNumber == globalNumber } ?: return@collect
                if (viewModel.currentAyah.value?.globalNumber != globalNumber) {
                    viewModel.currentAyah.value = ayah
                    viewModel.saveLastRead()
                }
            }
    }

    // Track the visible page (bookmark/play target) and persist resume + khatma.
    LaunchedEffect(pagerState, pageEntries, isWide) {
        if (pageEntries.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .map { virtualIndex ->
                val itemIndex = virtualIndex - 1
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
        ReaderTheme.Sepia -> MuslimSepiaColors
        // Respect the application palette, dark-mode choice, dynamic colours,
        // and accessibility contrast instead of forcing a fixed black page.
        ReaderTheme.Dark -> MaterialTheme.colorScheme
    }

    MaterialTheme(colorScheme = scheme) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                    // While recitation audio is being downloaded (before
                    // playback starts) show a compact download icon + the
                    // live percentage in the top bar; it disappears on its
                    // own once the download finishes.
                    if (downloading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.quran_download_status_downloading),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(IslamicSpacing.XSmall))
                            val percent = downloadProgress
                            if (percent != null) {
                                Text(
                                    text = "${(percent * 100).toInt().coerceIn(0, 100)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(Modifier.width(IslamicSpacing.XSmall))
                    }
                    IconButton(onClick = { showReaderSettings = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.quran_more_actions),
                        )
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
                    val mushafPresentation = MushafPagePresentation(
                        surahName = state.surah?.arabicName.orEmpty(),
                        fontSizeSp = fontSize,
                        playingAyahGlobal = currentAudioAyah,
                        selectedAyahGlobal = currentAyah?.globalNumber,
                        openedAyahGlobal = openedTargetGlobal,
                        tappedAyahGlobal = tappedAyahGlobal,
                        scrollTargetAyahGlobal = scrollTargetAyah,
                        tajweedEnabled = tajweedEnabled,
                        tajweedByAyah = tajweedAnnotations,
                    )
                    val mushafCallbacks = MushafPageCallbacks(
                        onPageClick = { pageAyahs ->
                            viewModel.currentAyah.value = pageAyahs.first()
                        },
                        onAyahClick = { ayah ->
                            // Tapping selects an ayah and keeps it visible;
                            // the user retains control over when to start audio.
                            viewModel.currentAyah.value = ayah
                            userSelectedAyah = ayah.globalNumber
                            tappedAyahGlobal = ayah.globalNumber
                            scrollTargetAyah = ayah.globalNumber
                            targetAyahRootBoundsPx = null
                            showAyahActions = true
                        },
                        onAyahRootBoundsPx = reportAyahBounds,
                        onAyahPositionsChanged = { page, positions ->
                            ayahPositionsByPage[page] = positions
                        },
                    )
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
                        val contentIndex = pageIndex - 1
                        if (contentIndex in 0 until realPageCount) {
                            val pageScroll = remember(pageIndex) { ScrollState(0) }
                            pageScrollStates[pageIndex] = pageScroll
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(pageScroll),
                            ) {
                                if (isWide) {
                                    // Two mushaf pages per pager page on wide screens.
                                    val spread = spreads.getOrNull(contentIndex)
                                    if (spread != null) {
                                        MushafSpreadRow(
                                            spread = spread,
                                            presentation = mushafPresentation,
                                            callbacks = mushafCallbacks,
                                        )
                                    }
                                } else {
                                    val (pageNumber, pageAyahs) = pageEntries[contentIndex]
                                    MushafPageCard(
                                        pageNumber = pageNumber,
                                        ayahs = pageAyahs,
                                        presentation = mushafPresentation,
                                        callbacks = mushafCallbacks,
                                    )
                                }
                            }
                        }
                    }

                    LaunchedEffect(pagerState.currentPage, realPageCount, state.surah?.number) {
                        val currentSurah = state.surah?.number ?: return@LaunchedEffect
                        val destination = when (pagerState.currentPage) {
                            0 -> (currentSurah - 1).takeIf { it >= 1 }
                            realPageCount + 1 -> (currentSurah + 1).takeIf { it <= 114 }
                            else -> null
                        }
                        if (destination != null && pendingAdjacentSurah != destination) {
                            pendingAdjacentSurah = destination
                            openAdjacentAtEnd = pagerState.currentPage == 0
                            viewModel.openSurah(destination)
                        } else if (destination == null &&
                            (pagerState.currentPage == 0 || pagerState.currentPage == realPageCount + 1)
                        ) {
                            pagerState.scrollToPage(
                                if (pagerState.currentPage == 0) 1 else realPageCount,
                            )
                        }
                    }

                    LaunchedEffect(state.surah?.number, realPageCount, pendingAdjacentSurah) {
                        val pending = pendingAdjacentSurah ?: return@LaunchedEffect
                        if (state.surah?.number == pending && realPageCount > 0) {
                            pagerState.scrollToPage(if (openAdjacentAtEnd) realPageCount else 1)
                            pendingAdjacentSurah = null
                        }
                    }
                    }
                }

            SupplementPanel(supplements = supplements, currentAyah = currentAyah)

            RecitationBar(
                state = RecitationBarState(
                    playbackState = playbackState,
                    currentAyah = currentAyah,
                    navigation = RecitationNavigationState(
                        hasNext = hasNextAyah,
                        hasPrevious = hasPreviousAyah,
                    ),
                    nowPlaying = RecitationNowPlayingState(
                        surahName = state.surah?.arabicName.orEmpty(),
                        surahNumber = state.surah?.number ?: 0,
                        ayahNumber = playingAyah?.numberInSurah,
                        positionMs = positionMs,
                        durationMs = durationMs,
                    ),
                    settings = RecitationSettingsState(
                        repeatCount = repeatCount,
                        stopAtEnd = continuousStopAtEnd,
                        reciter = selectedReciter,
                        reciters = Reciter.Bundled,
                        range = playRange,
                    ),
                    selectedAyahNumber = selectedStart?.numberInSurah,
                ),
                actions = RecitationBarActions(
                    onPrevious = viewModel::previousAyah,
                    onNext = viewModel::nextAyah,
                    onTogglePlayback = togglePlayback,
                    onStop = viewModel::stopPlayback,
                    onPlaySelectedAyah = selectedStart?.let { selected ->
                        { viewModel.playFromSelectedAyahToSurahEnd(selected, repeatCount) }
                    },
                    settings = RecitationSettingsActions(
                        onRepeatChanged = { repeatCount = it },
                        onStopAtEndChanged = viewModel::setContinuousStopAtEnd,
                        onReciterSelected = viewModel::selectReciter,
                        onRangeChanged = { playRange = it },
                    ),
                ),
            )

            }

            if (recitationFailure != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(IslamicSpacing.Medium),
                    action = {
                        IconButton(onClick = viewModel::retryPlaybackAfterFailure) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = playbackFailureText,
                            )
                        }
                    },
                ) {
                    Text(playbackFailureText)
                }
            } else if (
                restorableSession != null &&
                playbackState == PlaybackState.Idle
            ) {
                val resumeText = stringResource(R.string.quran_recitation_notif_paused)
                val playText = stringResource(R.string.quran_recitation_notif_play)
                val dismissText = stringResource(R.string.quran_stop_playback)
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(IslamicSpacing.Medium),
                    action = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    restorableSession?.let { session ->
                                        repeatCount = session.intent.repeatCount
                                    }
                                    viewModel.resumeRestorableSession()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = playText,
                                )
                            }
                            IconButton(onClick = viewModel::discardRestorableSession) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = dismissText,
                                )
                            }
                        }
                    },
                ) {
                    Text(resumeText)
                }
            }

            if (showReaderSettings) {
                ReaderSettingsSheet(
                    state = ReaderSettingsState(
                        theme = theme,
                        fontSize = fontSize,
                        keepScreenOn = keepScreenOn,
                        tajweedEnabled = tajweedEnabled,
                        supplementEnabled = supplementEnabled,
                        canOpenSupplement = currentAyah != null,
                        canOpenDetails = state.surah != null,
                    ),
                    actions = ReaderSettingsActions(
                        onDismiss = { showReaderSettings = false },
                        onThemeChange = viewModel::setReaderTheme,
                        onFontSizeChanged = { newSize ->
                            fontSize = newSize
                            viewModel.setReaderFontSize(newSize)
                        },
                        onKeepScreenOnChanged = viewModel::setKeepScreenOn,
                        onTajweedChanged = viewModel::setTajweedEnabled,
                        onOpenSupplement = {
                            showReaderSettings = false
                            showSupplementControls = true
                        },
                        onOpenDetails = {
                            showReaderSettings = false
                            showDetails = true
                        },
                        onOpenDownloads = {
                            showReaderSettings = false
                            onOpenDownloads()
                        },
                    ),
                )
            }

            if (showAyahActions) {
                val selectedAyah = selectedStart ?: currentAyah
                if (selectedAyah != null) {
                    MuslimActionSheet(
                        title = stringResource(R.string.quran_ayah_actions),
                        onDismiss = { showAyahActions = false },
                        actions = listOf(
                            MuslimActionItem(
                                id = "play",
                                label = stringResource(
                                    R.string.quran_play_from_selected_ayah,
                                    selectedAyah.numberInSurah,
                                ),
                                icon = Icons.Filled.PlayArrow,
                                onClick = {
                                    viewModel.playFromSelectedAyahToSurahEnd(
                                        selectedAyah,
                                        repeatCount,
                                    )
                                },
                            ),
                            MuslimActionItem(
                                id = "bookmark",
                                label = stringResource(
                                    if (bookmarked) {
                                        R.string.quran_bookmark_remove
                                    } else {
                                        R.string.quran_bookmark_add
                                    },
                                ),
                                icon = if (bookmarked) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                onClick = viewModel::toggleBookmark,
                            ),
                            MuslimActionItem(
                                id = "supplement",
                                label = stringResource(R.string.quran_supplement_controls),
                                icon = Icons.Filled.Translate,
                                onClick = { showSupplementControls = true },
                            ),
                            MuslimActionItem(
                                id = "share",
                                label = stringResource(R.string.quran_share_ayah),
                                icon = Icons.Filled.Share,
                                onClick = {
                                    shareAyah(
                                        context = context,
                                        ayah = selectedAyah,
                                        surahName = state.surah?.arabicName.orEmpty(),
                                    )
                                },
                            ),
                            MuslimActionItem(
                                id = "copy",
                                label = stringResource(R.string.quran_copy_ayah),
                                icon = Icons.Filled.ContentCopy,
                                onClick = {
                                    copyAyah(
                                        context = context,
                                        ayah = selectedAyah,
                                        surahName = state.surah?.arabicName.orEmpty(),
                                    )
                                },
                            ),
                        ),
                    )
                } else {
                    showAyahActions = false
                }
            }

            if (showDetails) {
            state.surah?.let { surah ->
                SurahDetailsDialog(surah = surah, onDismiss = { showDetails = false })
            }
        }
            if (showSupplementControls) {
                SupplementControlsDialog(
                    state = SupplementControlsState(
                        enabled = supplementEnabled,
                        installedTafsirSources = installedTafsirSources,
                        selectedTafsirSource = selectedTafsirSource,
                        tafsirDownloadState = tafsirDownloadState,
                        language = supplementLanguage,
                        availableLanguages = availableSupplementLanguages,
                    ),
                    actions = SupplementControlsActions(
                        onEnabledChanged = viewModel::setSupplementEnabled,
                        onTafsirSourceSelected = viewModel::setSelectedTafsirSource,
                        onDownloadOfficialTafsir = viewModel::downloadOfficialTafsir,
                        onLanguageChanged = viewModel::setSupplementLanguage,
                        onDismiss = { showSupplementControls = false },
                    ),
                )
            }
        }
    }
}

@Composable
private fun SupplementPanel(
    supplements: QuranReaderSupplementUi,
    currentAyah: Ayah?,
) {
    val hasContent = supplements.translations.isNotEmpty() || supplements.tafsir.isNotEmpty()
    if (!hasContent || currentAyah == null) return

    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IslamicSpacing.Compact, vertical = IslamicSpacing.XSmall),
        contentPadding = PaddingValues(IslamicSpacing.Compact),
    ) {
        Text(
            text = stringResource(R.string.quran_supplement_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        supplements.translations.forEach { translation ->
            Spacer(Modifier.height(IslamicSpacing.Small))
            Text(
                text = translation.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        supplements.tafsir.forEach { entry ->
            Spacer(Modifier.height(IslamicSpacing.Small))
            HorizontalDivider()
            Spacer(Modifier.height(IslamicSpacing.Small))
            Text(
                text = stringResource(R.string.quran_tafsir_source, entry.source),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(IslamicSpacing.XSmall))
            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

private data class RecitationNavigationState(
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

private data class RecitationNowPlayingState(
    val surahName: String,
    val surahNumber: Int,
    val ayahNumber: Int?,
    val positionMs: Long,
    val durationMs: Long,
)

private data class RecitationSettingsState(
    val repeatCount: Int,
    val stopAtEnd: Boolean,
    val reciter: Reciter,
    val reciters: List<Reciter>,
    val range: RecitationRange,
)

private data class RecitationBarState(
    val playbackState: PlaybackState,
    val currentAyah: Ayah?,
    val navigation: RecitationNavigationState,
    val nowPlaying: RecitationNowPlayingState,
    val settings: RecitationSettingsState,
    val selectedAyahNumber: Int?,
)

private data class RecitationSettingsActions(
    val onRepeatChanged: (Int) -> Unit,
    val onStopAtEndChanged: (Boolean) -> Unit,
    val onReciterSelected: (Reciter) -> Unit,
    val onRangeChanged: (RecitationRange) -> Unit,
)

private data class RecitationBarActions(
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onTogglePlayback: () -> Unit,
    val onStop: () -> Unit,
    val onPlaySelectedAyah: (() -> Unit)?,
    val settings: RecitationSettingsActions,
)

@Composable
private fun RecitationBar(
    state: RecitationBarState,
    actions: RecitationBarActions,
) {
    var showSettings by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = IslamicElevation.Resting,
        shadowElevation = IslamicElevation.Raised,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = IslamicSpacing.Small,
                vertical = IslamicSpacing.XSmall,
            ),
        ) {
            SelectedAyahPlaybackAction(
                selectedAyahNumber = state.selectedAyahNumber,
                onPlaySelectedAyah = actions.onPlaySelectedAyah,
            )
            RecitationNowPlayingRow(
                state = state,
                onOpenSettings = { showSettings = true },
            )
            RecitationPrimaryControls(
                state = state,
                actions = actions,
                onOpenSettings = { showSettings = true },
            )
        }
    }

    if (showSettings) {
        RecitationSettingsSheet(
            state = state.settings,
            actions = actions.settings,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun SelectedAyahPlaybackAction(
    selectedAyahNumber: Int?,
    onPlaySelectedAyah: (() -> Unit)?,
) {
    if (selectedAyahNumber == null || onPlaySelectedAyah == null) return

    TextButton(
        onClick = onPlaySelectedAyah,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(IslamicSpacing.Small))
        Text(
            text = stringResource(
                R.string.quran_play_from_selected_ayah,
                selectedAyahNumber,
            ),
        )
    }
    IslamicDecorationDivider(tint = MaterialTheme.colorScheme.tertiary)
}

@Composable
private fun RecitationNowPlayingRow(
    state: RecitationBarState,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
    ) {
        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.widthIn(max = 190.dp),
        ) {
            ReciterPortrait(reciter = state.settings.reciter, size = 26.dp)
            Spacer(Modifier.width(IslamicSpacing.Small))
            Text(
                text = state.settings.reciter.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val nowPlaying = state.nowPlaying
        if (nowPlaying.ayahNumber != null && state.playbackState != PlaybackState.Idle) {
            Text(
                text = stringResource(
                    R.string.quran_mini_surah_ayah,
                    nowPlaying.surahName,
                    nowPlaying.surahNumber,
                    nowPlaying.ayahNumber,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatTime(nowPlaying.positionMs)} / ${formatTime(nowPlaying.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecitationPrimaryControls(
    state: RecitationBarState,
    actions: RecitationBarActions,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(
            onClick = actions.onPrevious,
            enabled = state.navigation.hasPrevious,
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.quran_previous_ayah),
                modifier = Modifier.mirroredIfRtl(),
            )
        }
        IconButton(
            onClick = actions.onTogglePlayback,
            enabled = state.playbackState != PlaybackState.Idle || state.currentAyah != null,
        ) {
            Icon(
                imageVector = when (state.playbackState) {
                    PlaybackState.Playing -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                contentDescription = stringResource(R.string.quran_play_ayah),
            )
        }
        IconButton(
            onClick = actions.onNext,
            enabled = state.navigation.hasNext,
        ) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.quran_next_ayah),
                modifier = Modifier.mirroredIfRtl(),
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = stringResource(R.string.quran_playback_settings),
            )
        }
        if (state.playbackState != PlaybackState.Idle) {
            IconButton(onClick = actions.onStop) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.quran_stop_playback),
                )
            }
        }
    }
}

@Composable
private fun RecitationSettingsSheet(
    state: RecitationSettingsState,
    actions: RecitationSettingsActions,
    onDismiss: () -> Unit,
) {
    MuslimBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.quran_playback_settings),
    ) {
        ReciterSelectionSection(
            selectedReciter = state.reciter,
            reciters = state.reciters,
            onReciterSelected = actions.onReciterSelected,
        )
        RepeatSelectionSection(
            repeatCount = state.repeatCount,
            stopAtEnd = state.stopAtEnd,
            onRepeatChanged = actions.onRepeatChanged,
            onStopAtEndChanged = actions.onStopAtEndChanged,
        )
        RangeSelectionSection(
            range = state.range,
            onRangeChanged = actions.onRangeChanged,
        )
    }
}

@Composable
private fun ReciterSelectionSection(
    selectedReciter: Reciter,
    reciters: List<Reciter>,
    onReciterSelected: (Reciter) -> Unit,
) {
    Text(
        text = stringResource(R.string.quran_reciter),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    reciters.forEach { option ->
        val selected = option.id == selectedReciter.id
        IslamicSelectableCard(
            selected = selected,
            onClick = { onReciterSelected(option) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(IslamicRadius.AyahMarker),
            contentPadding = PaddingValues(IslamicSpacing.Compact),
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        ) {
            ReciterSelectionRow(option = option, selected = selected)
        }
    }
}

@Composable
private fun ReciterSelectionRow(
    option: Reciter,
    selected: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReciterPortrait(reciter = option, size = 40.dp)
        Spacer(Modifier.width(IslamicSpacing.Compact))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = option.style,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RepeatSelectionSection(
    repeatCount: Int,
    stopAtEnd: Boolean,
    onRepeatChanged: (Int) -> Unit,
    onStopAtEndChanged: (Boolean) -> Unit,
) {
    Text(
        text = stringResource(R.string.quran_repeat),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    REPEAT_OPTIONS.forEach { option ->
        IslamicSelectableCard(
            selected = repeatCount == option,
            onClick = { onRepeatChanged(option) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(IslamicSpacing.Compact),
        ) {
            Text(
                text = repeatOptionLabel(option),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
    if (repeatCount <= 0) {
        MuslimSettingsItem(
            title = stringResource(R.string.quran_stop_at_end_of_mushaf),
            onClick = { onStopAtEndChanged(!stopAtEnd) },
            trailing = {
                Switch(
                    checked = stopAtEnd,
                    onCheckedChange = onStopAtEndChanged,
                )
            },
        )
    }
}

@Composable
private fun RangeSelectionSection(
    range: RecitationRange,
    onRangeChanged: (RecitationRange) -> Unit,
) {
    Text(
        text = stringResource(R.string.quran_play_range),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    RecitationRange.entries.forEach { option ->
        IslamicSelectableCard(
            selected = range == option,
            onClick = { onRangeChanged(option) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(IslamicSpacing.Compact),
        ) {
            Text(
                text = rangeLabel(option),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Circular reciter portrait shared by the compact player chip and every picker
 * card. The person icon is rendered underneath the network image, so readers
 * without a portrait (or a temporarily unavailable image) still look complete.
 */
@Composable
private fun ReciterPortrait(
    reciter: Reciter,
    size: Dp,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            )
            reciter.portraitUrl?.let { portraitUrl ->
                AsyncImage(
                    model = portraitUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            }
        }
    }
}

@Composable
private fun repeatOptionLabel(repeatCount: Int): String =
    if (repeatCount <= 0) stringResource(R.string.quran_repeat_continuous) else "×$repeatCount"

@Composable
private fun rangeLabel(range: RecitationRange): String = when (range) {
    RecitationRange.SingleAyah -> stringResource(R.string.quran_range_single_ayah)
    RecitationRange.FromAyahToEnd -> stringResource(R.string.quran_range_to_end)
    RecitationRange.WholeSurah -> stringResource(R.string.quran_range_whole_surah)
}

/** One ayah's top coordinate inside the reader's root layout. */
internal data class AyahViewportPosition(val globalNumber: Int, val topPx: Float)

/** Full vertical bounds of one rendered ayah inside the reader root. */
internal data class AyahViewportBounds(val topPx: Float, val bottomPx: Float)

/**
 * Calculates the minimum vertical scroll needed to keep a complete ayah inside
 * the reader's comfortable viewport band. If an ayah is taller than the band,
 * its first line is aligned to the top band so reading starts predictably.
 */
internal fun calculateAyahFollowScrollDelta(
    ayahTopPx: Float,
    ayahBottomPx: Float,
    viewportTopPx: Float,
    viewportHeightPx: Int,
    paddingFraction: Float = 0.12f,
    center: Boolean = false,
): Float {
    if (viewportHeightPx <= 0) return 0f
    val safePaddingFraction = paddingFraction.coerceIn(0f, 0.45f)
    val pad = viewportHeightPx * safePaddingFraction
    val topBound = viewportTopPx + pad
    val bottomBound = viewportTopPx + viewportHeightPx - pad
    val top = minOf(ayahTopPx, ayahBottomPx)
    val bottom = maxOf(ayahTopPx, ayahBottomPx)
    val ayahHeight = bottom - top
    val availableHeight = bottomBound - topBound

    if (center) {
        val ayahCenter = (top + bottom) / 2f
        val viewportCenter = viewportTopPx + viewportHeightPx / 2f
        return ayahCenter - viewportCenter
    }

    if (ayahHeight > availableHeight) {
        return top - topBound
    }

    return when {
        bottom > bottomBound -> bottom - bottomBound
        top < topBound -> top - topBound
        else -> 0f
    }
}

/** Maps a real mushaf content index to the pager index after the leading edge page. */
internal fun contentIndexToReaderPagerPage(contentIndex: Int): Int = contentIndex + 1

/** Shared visual state for one or two rendered mushaf pages. */
private data class MushafPagePresentation(
    val surahName: String,
    val fontSizeSp: Float,
    val playingAyahGlobal: Int?,
    val selectedAyahGlobal: Int?,
    val openedAyahGlobal: Int?,
    val tappedAyahGlobal: Int?,
    val scrollTargetAyahGlobal: Int?,
    val tajweedEnabled: Boolean,
    val tajweedByAyah: Map<Int, List<org.muslim.app.feature.quran.domain.TajweedAnnotation>>,
)

/** Events emitted by a rendered mushaf page. */
private data class MushafPageCallbacks(
    val onPageClick: (List<Ayah>) -> Unit,
    val onAyahClick: (Ayah) -> Unit,
    val onAyahRootBoundsPx: (Int, Float, Float) -> Unit,
    val onAyahPositionsChanged: (Int, List<AyahViewportPosition>) -> Unit,
)

/**
 * Chooses the first ayah beginning at or below the reader's top edge; when the
 * viewport ends after the final line, it keeps the last available ayah active.
 */
internal fun ayahAtReaderTop(
    positions: List<AyahViewportPosition>,
    viewportTopPx: Float,
): Int? = positions
    .filter { it.topPx >= viewportTopPx - 1f }
    .minByOrNull { it.topPx }
    ?.globalNumber
    ?: positions.maxByOrNull { it.topPx }?.globalNumber
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
    presentation: MushafPagePresentation,
    callbacks: MushafPageCallbacks,
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
                    presentation = presentation,
                    callbacks = callbacks.copy(onPageClick = { callbacks.onPageClick(rightPage.value) }),
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (leftPage != null) {
                MushafPageCard(
                    pageNumber = leftPage.key,
                    ayahs = leftPage.value,
                    presentation = presentation,
                    callbacks = callbacks.copy(onPageClick = { callbacks.onPageClick(leftPage.value) }),
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun MushafPageCard(
    pageNumber: Int,
    ayahs: List<Ayah>,
    presentation: MushafPagePresentation,
    callbacks: MushafPageCallbacks,
    modifier: Modifier = Modifier,
) {
    if (ayahs.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val accessibilityVisuals = LocalAccessibilityVisuals.current
    var textRootTopPx by remember { mutableFloatStateOf(0f) }
    var targetCharOffset by remember { mutableIntStateOf(-1) }
    var targetCharEndExclusive by remember { mutableIntStateOf(-1) }
    var targetLineTopPx by remember { mutableFloatStateOf(-1f) }
    var targetLineBottomPx by remember { mutableFloatStateOf(-1f) }
    var ayahLineTops by remember { mutableStateOf<List<AyahViewportPosition>>(emptyList()) }

    // The currently recited ayah needs more separation than the passive
    // selection tint. Adapt the fill to the actual reader surface instead of
    // assuming a fixed light/dark palette so Material You, every curated app
    // palette, sepia, AMOLED black and high-contrast themes remain legible.
    val darkReaderSurface = scheme.surface.luminance() < 0.35f
    val playingHighlightAlpha by animateFloatAsState(
        targetValue = when {
            presentation.playingAyahGlobal == null -> 0f
            darkReaderSurface -> 0.34f
            else -> 0.20f
        },
        animationSpec = tween(IslamicMotion.StandardMillis),
        label = "ayah_playback_highlight",
    )
    val playingHighlightBorderAlpha = if (darkReaderSurface) 0.68f else 0.48f

    // Report the target ayah's complete absolute on-screen bounds once it is
    // laid out. Measuring both first and last lines lets the reader keep the
    // whole highlighted ayah visible instead of tracking only its first line.
    LaunchedEffect(
        textRootTopPx,
        targetLineTopPx,
        targetLineBottomPx,
        presentation.scrollTargetAyahGlobal,
        ayahs,
    ) {
        val target = presentation.scrollTargetAyahGlobal ?: return@LaunchedEffect
        if (ayahs.none { it.globalNumber == target }) return@LaunchedEffect
        if (targetLineTopPx < 0f || targetLineBottomPx < targetLineTopPx) return@LaunchedEffect
        callbacks.onAyahRootBoundsPx(
            target,
            textRootTopPx + targetLineTopPx,
            textRootTopPx + targetLineBottomPx,
        )
    }
    LaunchedEffect(pageNumber, textRootTopPx, ayahLineTops) {
        if (ayahLineTops.isNotEmpty()) {
            callbacks.onAyahPositionsChanged(
                pageNumber,
                ayahLineTops.map { position ->
                    position.copy(topPx = textRootTopPx + position.topPx)
                },
            )
        }
    }
    // Follow the Medina-Mushaf numbering/display convention exactly: Al-Fatiha
    // keeps the Basmala as numbered ayah 1; other surahs separate the encoded
    // Basmala from ayah 1; At-Tawbah has no opening Basmala.
    val firstAyah = ayahs.first()
    val openingPresentation = surahOpeningPresentation(
        surahNumber = firstAyah.surahNumber,
        ayahNumber = firstAyah.numberInSurah,
        text = firstAyah.text,
    )
    val firstAyahText = openingPresentation.ayahText
    val standaloneBasmala = openingPresentation.standaloneBasmala
    val ayahCharOffsets = ArrayList<AyahViewportPosition>(ayahs.size)
    val highlightRanges = ArrayList<QuranTextHighlightRange>(4)
    val annotated = buildAnnotatedString {
        // Reset before scanning so a page whose target moved away (or a
        // follow-along advance within this page) never reports stale bounds.
        targetCharOffset = -1
        targetCharEndExclusive = -1
        ayahs.forEach { ayah ->
            ayahCharOffsets += AyahViewportPosition(ayah.globalNumber, length.toFloat())
            val ayahStartOffset = length
            if (ayah.globalNumber == presentation.scrollTargetAyahGlobal) targetCharOffset = length
            // Every ayah is individually tappable: tapping selects it (and
            // flashes the highlight); recitation starts from the play button.
            withLink(
                LinkAnnotation.Clickable(tag = "ayah-${ayah.globalNumber}") {
                    callbacks.onAyahClick(ayah)
                },
            ) {
                val ayahText = if (ayah === firstAyah) firstAyahText else ayah.text
                    val rawAnnotations = if (presentation.tajweedEnabled) {
                        presentation.tajweedByAyah[ayah.numberInSurah].orEmpty()
                    } else {
                        emptyList()
                    }
                    // The opening Basmala is visually removed on most surahs.
                    // Shift source offsets so only annotations in displayed text
                    // remain; out-of-range annotations are never rendered.
                    val removedPrefix = ayah.text.length - ayahText.length
                    val visibleAnnotations = rawAnnotations.mapNotNull { annotation ->
                        val start = annotation.start - removedPrefix
                        val end = annotation.endExclusive - removedPrefix
                        if (end <= 0 || start >= ayahText.length) null else annotation.copy(
                            start = start.coerceAtLeast(0),
                            endExclusive = end.coerceAtMost(ayahText.length),
                        )
                    }
                    TajweedMarkup.segment(ayahText, visibleAnnotations).forEach { segment ->
                        val color = when (segment.rule) {
                            org.muslim.app.feature.quran.domain.TajweedRule.Ghunnah -> scheme.tertiary
                            org.muslim.app.feature.quran.domain.TajweedRule.Idghaam -> scheme.secondary
                            org.muslim.app.feature.quran.domain.TajweedRule.Ikhfa -> scheme.error
                            org.muslim.app.feature.quran.domain.TajweedRule.Iqlab -> scheme.tertiary
                            org.muslim.app.feature.quran.domain.TajweedRule.Madd -> scheme.primary
                            org.muslim.app.feature.quran.domain.TajweedRule.Qalqalah -> scheme.error
                            org.muslim.app.feature.quran.domain.TajweedRule.HamzatWasl,
                            org.muslim.app.feature.quran.domain.TajweedRule.Silent -> scheme.onSurfaceVariant
                            org.muslim.app.feature.quran.domain.TajweedRule.LamShamsiyyah -> scheme.secondary
                            null -> null
                        }
                        if (color == null) append(segment.text) else withStyle(SpanStyle(color = color)) { append(segment.text) }
                    }
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = when {
                                ayah.globalNumber == presentation.playingAyahGlobal -> scheme.primary
                                ayah.globalNumber == presentation.selectedAyahGlobal -> scheme.primary
                                else -> scheme.tertiary
                            },
                            fontSize = (presentation.fontSizeSp * 0.6f).sp,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.35f),
                        ),
                    ) {
                        append("\uFD3F${ayah.numberInSurah.toString()}\uFD3E")
                    }
                    append(" ")
            }
            val ayahEndExclusive = (length - 1).coerceAtLeast(ayahStartOffset + 1)
            val highlightKind = when {
                ayah.globalNumber == presentation.tappedAyahGlobal -> QuranHighlightKind.Tapped
                ayah.globalNumber == presentation.playingAyahGlobal -> QuranHighlightKind.Playback
                ayah.globalNumber == presentation.openedAyahGlobal -> QuranHighlightKind.Opened
                presentation.playingAyahGlobal == null &&
                    ayah.globalNumber == presentation.selectedAyahGlobal -> QuranHighlightKind.Selected
                else -> null
            }
            if (highlightKind != null) {
                highlightRanges += QuranTextHighlightRange(
                    start = ayahStartOffset,
                    endExclusive = ayahEndExclusive,
                    kind = highlightKind,
                )
            }
            if (ayah.globalNumber == presentation.scrollTargetAyahGlobal) {
                // Exclude the separator space after the ayah marker from the
                // measured bounds so a wrapped trailing blank cannot create a
                // phantom extra line at the bottom.
                targetCharEndExclusive = ayahEndExclusive
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(IslamicRadius.Card),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { callbacks.onPageClick(ayahs) }),
    ) {
        Box {
            // Edge ornaments follow the selected app style but stay outside Quran text.
            IslamicDecorationCorners(
                tint = scheme.tertiary,
                compact = true,
            )
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            IslamicReadingHeaderDecoration(
                tint = scheme.tertiary,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = presentation.surahName,
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
            HorizontalDivider(color = scheme.outlineVariant)
            Spacer(Modifier.height(14.dp))
            if (standaloneBasmala != null) {
                // Mushaf-style Basmala header: a decorative ornament above, the
                // Basmala centered, and an ornamented divider below.
                IslamicReadingBasmalaAccent(
                    tint = scheme.tertiary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = standaloneBasmala,
                    fontSize = (presentation.fontSizeSp * 1.1f).sp,
                    lineHeight = (presentation.fontSizeSp * accessibilityVisuals.arabicLineHeightMultiplier).sp,
                    textAlign = TextAlign.Center,
                    color = scheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Rtl,
                        fontFamily = accessibilityVisuals.arabicReadingFont,
                    ),
                )
                Spacer(Modifier.height(10.dp))
                OrnamentedDivider(tint = scheme.tertiary)
                Spacer(Modifier.height(14.dp))
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            BasicText(
                text = annotated,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val result = layoutResult ?: return@drawBehind
                        drawQuranTextHighlights(
                            layoutResult = result,
                            ranges = highlightRanges,
                            primaryColor = scheme.primary,
                            playbackFillAlpha = playingHighlightAlpha,
                            playbackBorderAlpha = playingHighlightBorderAlpha,
                        )
                    }
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
                    fontSize = presentation.fontSizeSp.sp,
                    lineHeight = (presentation.fontSizeSp * accessibilityVisuals.arabicLineHeightMultiplier).sp,
                    fontFamily = accessibilityVisuals.arabicReadingFont,
                    textAlign = TextAlign.Center,
                    // The mushaf is always right-to-left, even when the app UI
                    // language is LTR (English).
                    textDirection = TextDirection.Rtl,
                ),
                onTextLayout = { result ->
                    layoutResult = result
                    ayahLineTops = ayahCharOffsets.mapNotNull { (globalNumber, charOffset) ->
                        if (result.layoutInput.text.isEmpty()) return@mapNotNull null
                        val offset = charOffset.toInt().coerceIn(0, result.layoutInput.text.length - 1)
                        val line = result.getLineForOffset(offset)
                        AyahViewportPosition(globalNumber, result.getLineTop(line))
                    }
                    if (
                        targetCharOffset >= 0 &&
                        targetCharEndExclusive > targetCharOffset &&
                        result.layoutInput.text.isNotEmpty()
                    ) {
                        val startOffset = targetCharOffset.coerceIn(0, result.layoutInput.text.length - 1)
                        val endOffset = (targetCharEndExclusive - 1)
                            .coerceIn(startOffset, result.layoutInput.text.length - 1)
                        val startLine = result.getLineForOffset(startOffset)
                        val endLine = result.getLineForOffset(endOffset)
                        targetLineTopPx = result.getLineTop(startLine)
                        targetLineBottomPx = result.getLineBottom(endLine)
                    } else {
                        // The target ayah is not on this page: reset both bounds
                        // so the report above stays silent instead of reusing a
                        // stale measurement from an earlier target.
                        targetLineTopPx = -1f
                        targetLineBottomPx = -1f
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Vector divider for the Basmala and compact section transitions. */
@Composable
private fun OrnamentedDivider(tint: Color) {
    IslamicReadingDivider(tint = tint)
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

/** State rendered by the reader's meanings and tafsir controls. */
private data class SupplementControlsState(
    val enabled: Boolean,
    val installedTafsirSources: List<String>,
    val selectedTafsirSource: String?,
    val tafsirDownloadState: QuranReaderTafsirDownloadState,
    val language: String,
    val availableLanguages: List<String>,
)

/** Reader-owned actions invoked by the stateless supplement controls. */
private data class SupplementControlsActions(
    val onEnabledChanged: (Boolean) -> Unit,
    val onTafsirSourceSelected: (String?) -> Unit,
    val onDownloadOfficialTafsir: (org.muslim.app.feature.quran.data.OfficialTafsirSource) -> Unit,
    val onLanguageChanged: (String) -> Unit,
    val onDismiss: () -> Unit,
)

@Composable
private fun SupplementControlsDialog(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    AlertDialog(
        onDismissRequest = actions.onDismiss,
        title = { Text(stringResource(R.string.quran_supplement_controls)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SupplementVisibilityControls(state, actions)
                SupplementTafsirSources(state, actions)
                SupplementLanguageOptions(state, actions)
            }
        },
        confirmButton = {
            TextButton(onClick = actions.onDismiss) {
                Text(stringResource(R.string.quran_details_close))
            }
        },
    )
}

@Composable
private fun SupplementVisibilityControls(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.quran_supplement_show),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = state.enabled, onCheckedChange = actions.onEnabledChanged)
    }
    Spacer(Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SupplementTafsirSources(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    Text(
        stringResource(R.string.quran_tafsir_sources_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(6.dp))
    if (state.installedTafsirSources.isEmpty()) {
        Text(
            stringResource(R.string.quran_tafsir_sources_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        state.installedTafsirSources.forEach { source ->
            TafsirSourceChoice(
                source = source,
                selected = state.selectedTafsirSource == source,
                onSelected = { actions.onTafsirSourceSelected(source) },
            )
        }
    }
    org.muslim.app.feature.quran.data.OfficialTafsirSource.entries.forEach { source ->
        OfficialTafsirDownloadRow(source, state, actions.onDownloadOfficialTafsir)
    }
    state.tafsirDownloadState.error?.let { error ->
        Text(
            stringResource(R.string.quran_tafsir_download_failed, error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    Text(
        stringResource(R.string.quran_tafsir_attribution),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun TafsirSourceChoice(source: String, selected: Boolean, onSelected: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
            .clickable(onClick = onSelected).padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(source, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun OfficialTafsirDownloadRow(
    source: org.muslim.app.feature.quran.data.OfficialTafsirSource,
    state: SupplementControlsState,
    onDownload: (org.muslim.app.feature.quran.data.OfficialTafsirSource) -> Unit,
) {
    val installed = source.storageKey in state.installedTafsirSources
    val downloading = state.tafsirDownloadState.downloading == source
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = when (source) {
                org.muslim.app.feature.quran.data.OfficialTafsirSource.AlMuyassar ->
                    stringResource(R.string.quran_tafsir_muyassar)
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            enabled = !installed && !downloading && state.tafsirDownloadState.downloading == null,
            onClick = { onDownload(source) },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (downloading) stringResource(R.string.quran_tafsir_downloading)
                    else if (installed) stringResource(R.string.quran_tafsir_installed)
                    else stringResource(R.string.quran_tafsir_download),
                )
                if (downloading) {
                    Text(
                        stringResource(
                            R.string.quran_tafsir_download_progress,
                            state.tafsirDownloadState.completedSurahs,
                            114,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplementLanguageOptions(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    Text(
        stringResource(R.string.quran_supplement_language),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(8.dp))
    buildList {
        add(QuranPrefsRepository.AUTO_LANGUAGE)
        addAll(state.availableLanguages)
    }.distinct().forEach { option ->
        val label = if (option == QuranPrefsRepository.AUTO_LANGUAGE) {
            stringResource(R.string.quran_supplement_language_auto, appLanguageName())
        } else {
            displayLanguageName(option)
        }
        TafsirSourceChoice(
            source = label,
            selected = state.language == option,
            onSelected = { actions.onLanguageChanged(option) },
        )
    }
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
