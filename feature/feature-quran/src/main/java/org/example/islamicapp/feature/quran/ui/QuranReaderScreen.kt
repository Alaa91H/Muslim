package org.example.islamicapp.feature.quran.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.example.islamicapp.core.common.text.toArabicIndic
import org.example.islamicapp.feature.quran.R
import org.example.islamicapp.feature.quran.domain.Ayah

private const val MIN_FONT_SP = 18f
private const val MAX_FONT_SP = 40f
private const val DEFAULT_FONT_SP = 26f
private const val FONT_STEP_SP = 2f

/**
 * Quran reader (PROJECT_PROMPT.md §6 Phase 2): Uthmani ayahs in a calm,
 * focus-first layout with adjustable font size, per-ayah bookmarks, and
 * automatic last-read tracking.
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
                        AyahRow(ayah = ayah, fontSizeSp = fontSize)
                        Spacer(Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AyahRow(ayah: Ayah, fontSizeSp: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
