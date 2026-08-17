package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import android.app.Activity
import android.view.WindowManager
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
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.PlaybackState
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.ReaderTheme
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.SurahRevelationData

private const val MIN_FONT_SP = 18f
private const val MAX_FONT_SP = 40f
private const val DEFAULT_FONT_SP = 26f
private const val FONT_STEP_SP = 2f
private val REPEAT_OPTIONS = listOf(1, 3, 5, 10)

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
    val reciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val downloaded by viewModel.downloaded.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentAudioAyah by viewModel.currentAudioAyah.collectAsStateWithLifecycle()

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

    var fontSize by rememberSaveable { mutableStateOf(DEFAULT_FONT_SP) }
    var repeatCount by rememberSaveable { mutableStateOf(1) }
    var showDetails by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
                                onClick = { viewModel.currentAyah.value = pageAyahs.first() },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

            SupplementPanel(supplements = supplements, currentAyah = currentAyah)

            RecitationBar(
                reciter = reciter,
                downloaded = downloaded,
                downloading = downloading,
                progress = downloadProgress,
                playbackState = playbackState,
                isCurrentAyahAudio = currentAudioAyah != null && currentAudioAyah == currentAyah?.globalNumber,
                repeatCount = repeatCount,
                onRepeatChanged = { repeatCount = it },
                onSelectReciter = viewModel::selectReciter,
                onDownload = viewModel::downloadCurrentSurah,
                onPlay = { ayah -> viewModel.playAyah(ayah, repeatCount) },
                onTogglePlayback = {
                    when (playbackState) {
                        PlaybackState.Playing -> viewModel.pausePlayback()
                        PlaybackState.Paused -> viewModel.resumePlayback()
                        PlaybackState.Idle -> currentAyah?.let { viewModel.playAyah(it, repeatCount) }
                    }
                },
            )

        }

        if (showDetails) {
            state.surah?.let { surah ->
                SurahDetailsDialog(surah = surah, onDismiss = { showDetails = false })
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecitationBar(
    reciter: Reciter,
    downloaded: Boolean,
    downloading: Boolean,
    progress: Float?,
    playbackState: PlaybackState,
    isCurrentAyahAudio: Boolean,
    repeatCount: Int,
    onRepeatChanged: (Int) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onDownload: () -> Unit,
    onPlay: (Ayah) -> Unit,
    onTogglePlayback: () -> Unit,
) {
    var reciterMenu by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = reciterMenu, onExpandedChange = { reciterMenu = it }) {
                    OutlinedTextField(
                        value = reciter.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.quran_reciter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reciterMenu) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = reciterMenu, onDismissRequest = { reciterMenu = false }) {
                        Reciter.Bundled.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(option.name)
                                        Text(
                                            text = option.style,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    reciterMenu = false
                                    onSelectReciter(option)
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (downloading || progress != null) {
                    LinearProgressIndicator(
                        progress = { progress ?: 0f },
                        modifier = Modifier.size(40.dp),
                    )
                } else {
                    IconButton(onClick = onDownload, enabled = !downloaded) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = stringResource(R.string.quran_download_surah),
                            tint = if (downloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePlayback, enabled = playbackState != PlaybackState.Idle || isCurrentAyahAudio) {
                    Icon(
                        imageVector = when (playbackState) {
                            PlaybackState.Playing -> Icons.Filled.Pause
                            else -> Icons.Filled.PlayArrow
                        },
                        contentDescription = stringResource(R.string.quran_play_ayah),
                    )
                }
                Text(
                    text = stringResource(R.string.quran_repeat),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                REPEAT_OPTIONS.forEach { count ->
                    FilterChip(
                        selected = repeatCount == count,
                        onClick = { onRepeatChanged(count) },
                        label = { Text("×$count") },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
        }
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
    onClick: () -> Unit,
) {
    if (ayahs.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val annotated = buildAnnotatedString {
        ayahs.forEach { ayah ->
            append(ayah.text)
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
            Text(
                text = annotated,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.9f).sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
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
