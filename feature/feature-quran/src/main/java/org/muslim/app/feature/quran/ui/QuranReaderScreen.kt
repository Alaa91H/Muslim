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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
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
    val reciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val reciterDownloadState by viewModel.reciterDownloadState.collectAsStateWithLifecycle()
    val downloaded by viewModel.downloaded.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val continuousStopAtEnd by viewModel.continuousStopAtEnd.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentAudioAyah by viewModel.currentAudioAyah.collectAsStateWithLifecycle()
    val hasNextAyah by viewModel.hasNextAyah.collectAsStateWithLifecycle()
    val hasPreviousAyah by viewModel.hasPreviousAyah.collectAsStateWithLifecycle()
    val playbackErrorCount by viewModel.playbackErrorCount.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()

    // Keep the screen lit while recitation is playing (تلاوة) so the ayah
    // stays readable without touching the device; restore on pause/stop.
    val window = (LocalContext.current as? Activity)?.window
    DisposableEffect(playbackState) {
        val activityWindow = window ?: return@DisposableEffect onDispose {}
        if (playbackState == PlaybackState.Playing) {
            activityWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Surface playback failures (e.g. a bad download or no connectivity) so a
    // silent "nothing happened" never confuses the user.
    val context = LocalContext.current
    val playbackErrorText = stringResource(R.string.quran_playback_error)
    var lastShownError by remember { mutableStateOf(0) }
    LaunchedEffect(playbackErrorCount) {
        if (playbackErrorCount > lastShownError) {
            lastShownError = playbackErrorCount
            Toast.makeText(context, playbackErrorText, Toast.LENGTH_SHORT).show()
        }
    }

    var fontSize by rememberSaveable { mutableStateOf(DEFAULT_FONT_SP) }
    var repeatCount by rememberSaveable { mutableStateOf(1) }
    var playRange by rememberSaveable { mutableStateOf(RecitationRange.FromAyahToEnd) }
    var showDetails by remember { mutableStateOf(false) }
    var showDownloads by remember { mutableStateOf(false) }
    var showSupplementControls by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // A short-lived highlight flashed on the ayah the user just tapped, so the
    // selection is unmistakable before playback starts.
    var tappedAyahGlobal by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(tappedAyahGlobal) {
        if (tappedAyahGlobal != null) {
            kotlinx.coroutines.delay(1_500)
            tappedAyahGlobal = null
        }
    }

    // Shared play/pause/resume toggle used by both the mini now-playing bar
    // and the full recitation bar.
    val togglePlayback: () -> Unit = {
        when (playbackState) {
            PlaybackState.Playing -> viewModel.pausePlayback()
            PlaybackState.Paused -> viewModel.resumePlayback()
            PlaybackState.Idle -> currentAyah?.let { viewModel.playAyahWithRange(it, repeatCount, playRange) }
        }
    }

    // The ayah currently playing (if any) — drives the mini now-playing bar.
    val playingAyah = currentAudioAyah?.let { global -> state.ayahs.firstOrNull { it.globalNumber == global } }

    // Group the surah's ayahs into mushaf pages (flowing text per page).
    val pageEntries = remember(state.ayahs) {
        state.ayahs.groupBy { it.page }.toSortedMap().entries.toList()
    }

    // Persisted font size wins after the async read arrives.
    LaunchedEffect(persistedFont) {
        if (persistedFont > 0f && persistedFont != DEFAULT_FONT_SP) fontSize = persistedFont
    }

    // Scroll to the requested ayah (from search/bookmarks/resume) once loaded.
    var scrolledToInitial by remember { mutableStateOf(false) }
    LaunchedEffect(pageEntries) {
        if (scrolledToInitial || pageEntries.isEmpty()) return@LaunchedEffect
        val pageIndex = if (viewModel.initialAyahGlobal > 0) {
            pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == viewModel.initialAyahGlobal } }
        } else {
            -1
        }
        listState.scrollToItem(if (pageIndex >= 0) pageIndex else 0)
        scrolledToInitial = true
    }

    // Follow-along: keep the page containing the currently-playing ayah in
    // view so the highlighted ayah is always visible during recitation.
    LaunchedEffect(currentAudioAyah) {
        val target = currentAudioAyah ?: return@LaunchedEffect
        val pageIndex = pageEntries.indexOfFirst { (_, ayahs) -> ayahs.any { it.globalNumber == target } }
        if (pageIndex >= 0) listState.animateScrollToItem(pageIndex)
    }

    // Track the visible page (bookmark/play target) and persist resume + khatma.
    LaunchedEffect(listState, pageEntries) {
        if (pageEntries.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { pageEntries.getOrNull(it)?.value?.firstOrNull() }
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
                        onClick = { showDetails = true },
                        enabled = state.surah != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.quran_details),
                        )
                    }
                    IconButton(
                        onClick = { showSupplementControls = true },
                        enabled = currentAyah != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = stringResource(R.string.quran_supplement_controls),
                            tint = if (supplementEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(
                        onClick = { showDownloads = true },
                        enabled = state.surah != null,
                    ) {
                        // Download icon with a live progress ring while a surah
                        // download is running; tinted once the surah is stored.
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.quran_downloads_title),
                                tint = if (downloaded) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            if (downloading) {
                                CircularProgressIndicator(
                                    progress = { downloadProgress ?: 0f },
                                    modifier = Modifier.size(30.dp),
                                    strokeWidth = 2.5.dp,
                                )
                            }
                        }
                    }
                    FontSizeControls(
                        fontSize = fontSize,
                        onChanged = { newSize ->
                            fontSize = newSize
                            viewModel.setReaderFontSize(newSize)
                        },
                    )
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    ) {
                        items(pageEntries.size, key = { pageEntries[it].key }) { index ->
                            val (pageNumber, pageAyahs) = pageEntries[index]
                            MushafPageCard(
                                pageNumber = pageNumber,
                                ayahs = pageAyahs,
                                surahName = state.surah?.arabicName.orEmpty(),
                                fontSizeSp = fontSize,
                                playingAyahGlobal = currentAudioAyah,
                                selectedAyahGlobal = currentAyah?.globalNumber,
                                tappedAyahGlobal = tappedAyahGlobal,
                                onClick = { viewModel.currentAyah.value = pageAyahs.first() },
                                onAyahClick = { ayah ->
                                    // Tap only selects the ayah; recitation
                                    // starts from the play button in the
                                    // recitation bar below.
                                    viewModel.currentAyah.value = ayah
                                    tappedAyahGlobal = ayah.globalNumber
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

            SupplementPanel(supplements = supplements, currentAyah = currentAyah)

            if (playingAyah != null) {
                MiniPlayerBar(
                    surahName = state.surah?.arabicName.orEmpty(),
                    surahNumber = state.surah?.number ?: 0,
                    ayahNumber = playingAyah.numberInSurah,
                    playbackState = playbackState,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    hasNext = hasNextAyah,
                    hasPrevious = hasPreviousAyah,
                    onTogglePlayback = togglePlayback,
                    onPrevious = viewModel::previousAyah,
                    onNext = viewModel::nextAyah,
                    onStop = viewModel::stopPlayback,
                )
            }

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
                range = playRange,
                onRangeChanged = { playRange = it },
            )

        }

        if (showDetails) {
            state.surah?.let { surah ->
                SurahDetailsDialog(surah = surah, onDismiss = { showDetails = false })
            }
        }
        if (showDownloads) {
            RecitationDownloadsDialog(
                reciter = reciter,
                reciterState = reciterDownloadState,
                downloaded = downloaded,
                downloading = downloading,
                progress = downloadProgress,
                surahNumber = state.surah?.number ?: 0,
                ayahCount = state.ayahs.size,
                onSelectReciter = viewModel::selectReciter,
                onDownloadAyah = viewModel::downloadCurrentAyah,
                onDownloadSurah = viewModel::downloadCurrentSurah,
                onDownloadWholeQuran = viewModel::downloadWholeQuran,
                onDeleteSurah = viewModel::deleteDownloadedSurah,
                onDismiss = { showDownloads = false },
            )
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
 * Download hub for the reader: reciter selection and per-scope download
 * actions (current ayah / current surah / whole Quran) with approximate sizes
 * and live progress. Opened from the download icon in the reader top bar, so
 * the recitation controls below stay uncluttered.
 */
@Composable
private fun RecitationDownloadsDialog(
    reciter: Reciter,
    reciterState: org.muslim.app.feature.quran.data.ReciterDownloadState?,
    downloaded: Boolean,
    downloading: Boolean,
    progress: Float?,
    surahNumber: Int,
    ayahCount: Int,
    onSelectReciter: (Reciter) -> Unit,
    onDownloadAyah: () -> Unit,
    onDownloadSurah: () -> Unit,
    onDownloadWholeQuran: () -> Unit,
    onDeleteSurah: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(stringResource(R.string.quran_download_reciter_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Reciter.Bundled.forEach { option ->
                    val selected = option.id == reciter.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectReciter(option) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (selected) {
                                Icons.Filled.RadioButtonChecked
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(option.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = option.style,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (option.id == reciter.id && reciterState != null && reciterState.downloadedAyahs > 0) {
                                Text(
                                    text = stringResource(
                                        R.string.quran_download_reciter_state_detail,
                                        reciterState.downloadedSurahs,
                                        reciterState.downloadedAyahs,
                                        formatBytes(reciterState.totalBytes),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.quran_download_options_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                DownloadOptionRow(
                    label = stringResource(R.string.quran_download_current_ayah),
                    sizeBytes = reciter.estimatedBytesPerAyah(),
                    completed = downloaded,
                    downloading = downloading,
                    onDownload = onDownloadAyah,
                )
                DownloadOptionRow(
                    label = stringResource(R.string.quran_download_current_surah),
                    sizeBytes = reciter.estimatedBytesPerAyah() * ayahCount.coerceAtLeast(1),
                    completed = downloaded,
                    downloading = downloading,
                    onDownload = onDownloadSurah,
                )
                DownloadOptionRow(
                    label = stringResource(R.string.quran_download_scope_full),
                    sizeBytes = reciter.estimatedBytesPerAyah() * TOTAL_AYAHS,
                    completed = false,
                    downloading = false,
                    onDownload = onDownloadWholeQuran,
                )
                if (downloading) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress ?: 0f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.quran_download_status_downloading),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (downloaded) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = { onDeleteSurah(surahNumber) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.quran_download_delete_surah),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.quran_download_cancel))
            }
        },
    )
}

@Composable
private fun DownloadOptionRow(
    label: String,
    sizeBytes: Long,
    completed: Boolean,
    downloading: Boolean,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.quran_download_size_estimate, formatBytes(sizeBytes)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (completed) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.quran_downloaded),
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            IconButton(onClick = onDownload, enabled = !downloading) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = stringResource(R.string.quran_download_start),
                )
            }
        }
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
    range: RecitationRange,
    onRangeChanged: (RecitationRange) -> Unit,
) {
    var repeatMenu by remember { mutableStateOf(false) }
    var rangeMenu by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        // Explicit content color keeps every label readable in the reader's
        // light / sepia / night themes (dark-mode contrast fix).
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = onPrevious, enabled = hasPrevious) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(R.string.quran_previous_ayah),
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
// A slim "now playing" bar shown only while recitation is active (or paused).
// It keeps the essential controls (previous / play-pause / next / close), the
// current ayah number and a live progress bar within easy reach without the
// full recitation bar.
@Composable
private fun MiniPlayerBar(
    surahName: String,
    surahNumber: Int,
    ayahNumber: Int,
    playbackState: PlaybackState,
    positionMs: Long,
    durationMs: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onTogglePlayback: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 3.dp,
    ) {
        Column {
            // Elapsed / total time of the current ayah.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTime(positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.quran_mini_surah_ayah,
                        surahName,
                        surahNumber,
                        ayahNumber,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onPrevious, enabled = hasPrevious) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = stringResource(R.string.quran_previous_ayah),
                    )
                }
                IconButton(onClick = onTogglePlayback) {
                    Icon(
                        imageVector = if (playbackState == PlaybackState.Playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.quran_play_ayah),
                    )
                }
                IconButton(onClick = onNext, enabled = hasNext) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.quran_next_ayah),
                    )
                }
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

/**
 * Formats milliseconds as m:ss or mm:ss (h:mm:ss from one hour up), always
 * with Western digits regardless of the device locale.
 */
private const val TOTAL_AYAHS = 6236L

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

// One mushaf-style page: a framed card with flowing ayahs and inline ayah
// markers (﴿1﴾), like a printed Quran page rather than a vertical list.
@Composable
private fun MushafPageCard(
    pageNumber: Int,
    ayahs: List<Ayah>,
    surahName: String,
    fontSizeSp: Float,
    playingAyahGlobal: Int?,
    selectedAyahGlobal: Int?,
    tappedAyahGlobal: Int?,
    onClick: () -> Unit,
    onAyahClick: (Ayah) -> Unit,
) {
    if (ayahs.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    // The Basmala embedded at the start of ayah 1 (every surah except 9) is
    // pulled out into its own line above the surah, like printed mushafs.
    val firstAyah = ayahs.first()
    val isSurahOpeningPage = firstAyah.numberInSurah == 1
    val firstAyahText = if (isSurahOpeningPage) stripLeadingBasmala(firstAyah.text) else firstAyah.text
    val showBasmala = isSurahOpeningPage && firstAyah.surahNumber != 1 && firstAyahText != firstAyah.text
    val annotated = buildAnnotatedString {
        ayahs.forEach { ayah ->
            // Visual hierarchy: the tapped ayah flashes strongest (temporary),
            // the ayah being recited glows while playing (follow-along), and the
            // currently selected ayah keeps a soft tint. All work on the light,
            // sepia and night themes.
            val highlight = when {
                ayah.globalNumber == tappedAyahGlobal ->
                    SpanStyle(background = scheme.primary.copy(alpha = 0.35f))
                ayah.globalNumber == playingAyahGlobal ->
                    SpanStyle(background = scheme.primary.copy(alpha = 0.22f))
                ayah.globalNumber == selectedAyahGlobal ->
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
        modifier = Modifier
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
                    style = MaterialTheme.typography.labelMedium,
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
                // Standalone Basmala line, separated from the ayah text below.
                Text(
                    text = BASMALA,
                    fontSize = (fontSizeSp * 1.1f).sp,
                    lineHeight = (fontSizeSp * 1.9f).sp,
                    textAlign = TextAlign.Center,
                    color = scheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = scheme.surfaceVariant)
                Spacer(Modifier.height(14.dp))
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            BasicText(
                text = annotated,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(annotated) {
                        detectTapGestures { position ->
                            val result = layoutResult ?: return@detectTapGestures
                            val offset = result.getOffsetForPosition(position)
                            val link = annotated.getLinkAnnotations(offset, offset + 1).firstOrNull()
                            link?.item?.linkInteractionListener?.onClick(link.item)
                        }
                    },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.9f).sp,
                    textAlign = TextAlign.Center,
                ),
                onTextLayout = { layoutResult = it },
            )
            Spacer(Modifier.height(8.dp))
        }
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
