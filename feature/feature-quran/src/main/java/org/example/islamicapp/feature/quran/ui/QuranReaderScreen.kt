package org.example.islamicapp.feature.quran.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import org.example.islamicapp.core.common.text.toArabicIndic
import org.example.islamicapp.feature.quran.R
import org.example.islamicapp.feature.quran.data.RecitationRepository
import org.example.islamicapp.feature.quran.domain.Ayah
import org.example.islamicapp.feature.quran.domain.Reciter
import org.example.islamicapp.feature.quran.ui.QuranReaderViewModel.RepeatState

private const val MIN_FONT_SP = 18f
private const val MAX_FONT_SP = 40f
private const val DEFAULT_FONT_SP = 26f
private const val FONT_STEP_SP = 2f

/**
 * Quran reader (PROJECT_PROMPT.md §6 Phase 2): Uthmani ayahs in a calm,
 * focus-first layout with adjustable font size, per-ayah bookmarks, automatic
 * last-read tracking, and a repeat (memorization) mode that loops an ayah or
 * passage a chosen number of times with auto-scroll and progress.
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
    val repeatState by viewModel.repeatState.collectAsStateWithLifecycle()
    val recitationState by viewModel.recitationState.collectAsStateWithLifecycle()
    val playingAyahGlobal = recitationState.playingIndex
        .takeIf { it >= 0 }
        ?.let { state.ayahs.getOrNull(it)?.globalNumber }
    var fontSize by rememberSaveable { mutableFloatStateOf(DEFAULT_FONT_SP) }
    val listState = rememberLazyListState()

    // Scroll to the requested ayah (from search/bookmarks/resume) once loaded.
    var scrolledToInitial by remember { mutableStateOf(false) }
    LaunchedEffect(state.ayahs) {
        if (scrolledToInitial || state.ayahs.isEmpty()) return@LaunchedEffect
        val index = if (viewModel.initialAyahGlobal > 0) {
            state.ayahs.indexOfFirst { it.globalNumber == viewModel.initialAyahGlobal }
        } else {
            -1
        }
        listState.scrollToItem(if (index >= 0) index else 0)
        scrolledToInitial = true
    }

    // Track the visible ayah (bookmark target) and persist the resume position.
    LaunchedEffect(listState, state.ayahs) {
        if (state.ayahs.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { state.ayahs.getOrNull(it) }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { viewModel.currentAyah.value = it }
            .debounce(2_000)
            .collect { viewModel.saveLastRead() }
    }

    // Auto-scroll driven by the repeat session and the recitation playback
    // (the highlight follows whichever is active).
    LaunchedEffect(Unit) {
        viewModel.scrollRequests.collect { global ->
            val index = state.ayahs.indexOfFirst { it.globalNumber == global }
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

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
                FontSizeControls(
                    fontSize = fontSize,
                    onChanged = { fontSize = it },
                )
                IconButton(onClick = viewModel::toggleRecitationPanel) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = stringResource(R.string.quran_recitation),
                        tint = if (recitationState.panelOpen || recitationState.isPlaying) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(onClick = viewModel::toggleRepeatPanel) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = stringResource(R.string.quran_repeat),
                        tint = if (repeatState.session.panelOpen || repeatState.session.playing) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(
                    onClick = viewModel::toggleBookmark,
                    enabled = currentAyah != null,
                ) {
                    Icon(
                        imageVector = if (bookmarked) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = stringResource(
                            if (bookmarked) R.string.quran_bookmark_remove else R.string.quran_bookmark_add
                        ),
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    items(state.ayahs, key = { it.globalNumber }) { ayah ->
                        val repeatHighlight = repeatState.session.playing &&
                            ayah.globalNumber == repeatState.session.currentGlobal
                        val recitationHighlight = playingAyahGlobal != null &&
                            ayah.globalNumber == playingAyahGlobal
                        AyahRow(
                            ayah = ayah,
                            fontSizeSp = fontSize,
                            highlighted = repeatHighlight || recitationHighlight,
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }
            }
        }

        if (recitationState.panelOpen) {
            RecitationPanel(
                state = recitationState,
                onClose = viewModel::closeRecitationPanel,
                onSelectReciter = viewModel::selectReciter,
                onDownload = viewModel::downloadRecitation,
                onDelete = viewModel::deleteRecitation,
                onTogglePlayback = viewModel::togglePlayback,
            )
        }

        if (repeatState.session.panelOpen) {
            RepeatPanel(
                state = repeatState,
                ayahs = state.ayahs,
                onClose = viewModel::closeRepeatPanel,
                onRangeStart = viewModel::setRangeStart,
                onRangeEnd = viewModel::setRangeEnd,
                onCount = viewModel::stepCount,
                onPacing = viewModel::stepPacing,
                onPlayPause = { if (repeatState.session.playing) viewModel.pause() else viewModel.play() },
                onReset = viewModel::resetRepeat,
            )
        }
    }
}

@Composable
private fun AyahRow(ayah: Ayah, fontSizeSp: Float, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (highlighted) {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(vertical = 6.dp)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.Top,
    ) {
        // Verse number badge (Arabic-Indic numerals) at the start of the ayah.
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(top = 6.dp, end = 12.dp)
                .size(28.dp),
        ) {
            Text(
                text = ayah.numberInSurah.toArabicIndic(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = ayah.text,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.9f).sp,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---- Repeat (memorization) panel ----

@Composable
private fun RepeatPanel(
    state: RepeatState,
    ayahs: List<Ayah>,
    onClose: () -> Unit,
    onRangeStart: () -> Unit,
    onRangeEnd: () -> Unit,
    onCount: (Int) -> Unit,
    onPacing: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onReset: () -> Unit,
) {
    val config = state.config
    val session = state.session
    val rangeSet = config.rangeStart > 0

    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.quran_repeat),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onReset) {
                    Icon(
                        Icons.Filled.RestartAlt,
                        contentDescription = stringResource(R.string.quran_repeat_reset),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.quran_repeat_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Range: from / to the ayah currently in view.
            RepeatRangeRow(
                label = stringResource(R.string.quran_repeat_from),
                ref = ayahRef(ayahs, config.rangeStart),
                actionLabel = stringResource(R.string.quran_repeat_from_here),
                onAction = onRangeStart,
            )
            RepeatRangeRow(
                label = stringResource(R.string.quran_repeat_to),
                ref = ayahRef(ayahs, config.rangeEnd),
                actionLabel = stringResource(R.string.quran_repeat_to_here),
                onAction = onRangeEnd,
            )

            Spacer(Modifier.height(8.dp))

            StepRow(
                label = stringResource(R.string.quran_repeat_count),
                value = config.count.toArabicIndic(),
                onDecrease = { onCount(-1) },
                onIncrease = { onCount(+1) },
            )
            Spacer(Modifier.height(4.dp))
            StepRow(
                label = stringResource(R.string.quran_repeat_pacing),
                value = stringResource(R.string.quran_repeat_seconds, config.pacingSeconds.toArabicIndic()),
                onDecrease = { onPacing(-3) },
                onIncrease = { onPacing(+3) },
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPlayPause,
                    enabled = rangeSet,
                    modifier = Modifier.width(140.dp),
                ) {
                    Icon(
                        imageVector = if (session.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (session.playing) R.string.quran_repeat_pause else R.string.quran_repeat_play
                        )
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = repeatStatus(state),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RepeatRangeRow(label: String, ref: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = ref,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun StepRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDecrease) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.quran_repeat_decrease))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp),
        )
        IconButton(onClick = onIncrease) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quran_repeat_increase))
        }
    }
}

/** Formats "surah:ayah" (Arabic-Indic) for a global number, or "—" if unset. */
@Composable
private fun ayahRef(ayahs: List<Ayah>, globalNumber: Int): String {
    if (globalNumber <= 0) return stringResource(R.string.quran_repeat_not_set)
    val ayah = ayahs.firstOrNull { it.globalNumber == globalNumber }
        ?: return stringResource(R.string.quran_repeat_not_set)
    return stringResource(
        R.string.quran_search_ref,
        ayah.surahNumber.toArabicIndic(),
        ayah.numberInSurah.toArabicIndic(),
    )
}

@Composable
private fun repeatStatus(state: RepeatState): String {
    val session = state.session
    return when {
        session.finished -> stringResource(R.string.quran_repeat_done)
        session.playing -> stringResource(
            R.string.quran_repeat_progress,
            (session.completedPasses + 1).toArabicIndic(),
            state.config.count.toArabicIndic(),
        )
        state.config.rangeStart > 0 -> stringResource(R.string.quran_repeat_ready)
        else -> stringResource(R.string.quran_repeat_hint)
    }
}

// ---- Recitation (audio) panel ----

@Composable
private fun RecitationPanel(
    state: QuranReaderViewModel.RecitationUiState,
    onClose: () -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    val status = state.status
    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.quran_recitation),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.quran_repeat_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Playback + status.
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlayback,
                    enabled = status.state == RecitationRepository.DownloadState.Downloaded,
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (state.isPlaying) R.string.quran_repeat_pause else R.string.quran_repeat_play
                        ),
                        tint = if (status.state == RecitationRepository.DownloadState.Downloaded) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Text(
                    text = recitationStatusText(status, state.isPlaying),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                when (status.state) {
                    RecitationRepository.DownloadState.Downloaded -> {
                        TextButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.quran_recitation_delete),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.quran_recitation_delete))
                        }
                    }
                    RecitationRepository.DownloadState.Downloading -> {
                        Button(onClick = {}, enabled = false) {
                            Text(stringResource(R.string.quran_recitation_downloading))
                        }
                    }
                    else -> {
                        Button(onClick = onDownload) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.quran_recitation_download))
                        }
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Reciter selector.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                Reciter.entries.forEach { reciter ->
                    FilterChip(
                        selected = reciter == state.reciter,
                        onClick = { onSelectReciter(reciter) },
                        label = { Text(stringResource(reciter.displayNameRes)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun recitationStatusText(
    status: RecitationRepository.RecitationStatus,
    isPlaying: Boolean,
): String = when (status.state) {
    RecitationRepository.DownloadState.Downloaded -> stringResource(R.string.quran_recitation_ready)
    RecitationRepository.DownloadState.Downloading -> stringResource(
        R.string.quran_recitation_progress,
        status.downloaded.toArabicIndic(),
        status.total.toArabicIndic(),
    )
    RecitationRepository.DownloadState.Error -> stringResource(R.string.quran_recitation_error)
    RecitationRepository.DownloadState.None -> stringResource(R.string.quran_recitation_not_downloaded)
}.let { if (isPlaying) "▶ $it" else it }

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
