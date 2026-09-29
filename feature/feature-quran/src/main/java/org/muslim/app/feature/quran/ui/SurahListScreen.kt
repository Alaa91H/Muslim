package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicReadingHeaderDecoration
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.core.ui.theme.MuslimGroup
import org.muslim.app.core.ui.theme.MuslimHero
import org.muslim.app.core.ui.theme.MuslimLoadingState
import org.muslim.app.core.ui.theme.MuslimProgressHeader
import org.muslim.app.core.ui.theme.MuslimSearchBar
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimSegmentedControl
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.Bookmark
import org.muslim.app.feature.quran.domain.Surah

private const val TAB_SURAHS = 0
private const val TAB_JUZ = 1
private const val TAB_BOOKMARKS = 2

/**
 * Quran home: continue reading, unified discovery and Surah/Juz/Bookmarks views.
 *
 * The dedicated Bookmarks route remains available for compatibility and deep
 * navigation, while the primary Quran destination now exposes bookmarks inline.
 */
@Composable
fun SurahListScreen(
    onOpenSurah: (Int) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onOpenBookmarks: () -> Unit,
    onResumeReading: (surahNumber: Int, globalNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SurahListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_SURAHS) }

    Column(modifier = modifier.fillMaxSize()) {
        QuranHomeHeader(
            query = query,
            onQueryChange = { query = it },
            selectedTab = selectedTab,
            onSelectedTabChange = { selectedTab = it },
        )
        QuranHomeContent(
            state = state,
            query = query,
            selectedTab = selectedTab,
            onOpenSurah = onOpenSurah,
            onPlaySurah = onPlaySurah,
            onOpenBookmarks = onOpenBookmarks,
            onResumeReading = onResumeReading,
        )
    }
}

@Composable
private fun QuranHomeHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit,
) {
    IslamicReadingHeaderDecoration(
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(top = IslamicSpacing.XXSmall),
    )
    MuslimSectionHeader(
        title = stringResource(R.string.quran_title),
        modifier = Modifier.padding(
            horizontal = IslamicSpacing.PageHorizontal,
            vertical = IslamicSpacing.Small,
        ),
    )
    IslamicDecorationDivider(
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(horizontal = IslamicSpacing.PageHorizontal),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Small,
            ),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        MuslimSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.quran_search_hint),
            clearContentDescription = stringResource(R.string.quran_search_clear),
        )
        MuslimSegmentedControl(
            options = listOf(
                stringResource(R.string.quran_tab_surahs),
                stringResource(R.string.quran_tab_juz),
                stringResource(R.string.quran_tab_bookmarks),
            ),
            selectedIndex = selectedTab,
            onSelectedIndexChange = onSelectedTabChange,
        )
    }
}

@Composable
private fun QuranHomeContent(
    state: SurahListViewModel.UiState,
    query: String,
    selectedTab: Int,
    onOpenSurah: (Int) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onOpenBookmarks: () -> Unit,
    onResumeReading: (surahNumber: Int, globalNumber: Int) -> Unit,
) {
    if (state.loading) {
        MuslimLoadingState(
            title = stringResource(R.string.quran_loading),
            modifier = Modifier
                .fillMaxWidth()
                .padding(IslamicSpacing.Large),
        )
        return
    }

    when (selectedTab) {
        TAB_JUZ -> JuzContent(
            state = state,
            query = query,
            onOpenJuz = { start ->
                onResumeReading(start.surahNumber, start.globalNumber)
            },
        )
        TAB_BOOKMARKS -> QuranHomeBookmarksContent(
            bookmarks = state.bookmarks,
            query = query,
            onOpenBookmarks = onOpenBookmarks,
            onOpenAyah = { bookmark ->
                onResumeReading(
                    bookmark.ayah.surahNumber,
                    bookmark.ayah.globalNumber,
                )
            },
        )
        else -> SurahContent(
            state = state,
            query = query,
            onOpenSurah = onOpenSurah,
            onPlaySurah = onPlaySurah,
            onResumeReading = onResumeReading,
        )
    }
}

@Composable
private fun SurahContent(
    state: SurahListViewModel.UiState,
    query: String,
    onOpenSurah: (Int) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onResumeReading: (surahNumber: Int, globalNumber: Int) -> Unit,
) {
    val normalizedQuery = query.trim()
    val filtered = state.surahs.filter { surah ->
        normalizedQuery.isBlank() ||
            surah.number.toString() == normalizedQuery ||
            surah.arabicName.contains(normalizedQuery, ignoreCase = true) ||
            surah.englishName.contains(normalizedQuery, ignoreCase = true) ||
            surah.translation.contains(normalizedQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = IslamicSpacing.PageHorizontal,
            end = IslamicSpacing.PageHorizontal,
            bottom = IslamicSpacing.Medium,
        ),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
    ) {
        if (normalizedQuery.isBlank()) {
            state.lastRead?.let { last ->
                item(key = "continue-reading") {
                    val surahName = state.surahs
                        .firstOrNull { it.number == last.surahNumber }
                        ?.arabicName
                        ?: stringResource(R.string.quran_surah_number_short, last.surahNumber)
                    MuslimHero(
                        title = stringResource(R.string.quran_continue_reading),
                        value = surahName,
                        supportingText = stringResource(
                            R.string.quran_resume,
                            last.surahNumber.toString(),
                            last.numberInSurah.toString(),
                        ),
                        icon = Icons.Filled.PlayArrow,
                        iconContentDescription = null,
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClick = {
                                onResumeReading(last.surahNumber, last.globalNumber)
                            },
                        ),
                    )
                }
            }
            item(key = "khatma") {
                MuslimGroup {
                    MuslimProgressHeader(
                        title = stringResource(R.string.quran_khatma_progress),
                        progress = state.progressFraction,
                        supportingText = stringResource(
                            R.string.quran_khatma_detail,
                            state.readThroughGlobal.toString(),
                            state.totalAyahs.toString(),
                        ),
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item(key = "empty-search") {
                MuslimEmptyState(
                    title = stringResource(R.string.quran_search_no_results),
                    modifier = Modifier.padding(vertical = IslamicSpacing.Large),
                )
            }
        } else {
            items(filtered, key = { it.number }) { surah ->
                SurahRow(
                    surah = surah,
                    onClick = { onOpenSurah(surah.number) },
                    onPlay = { onPlaySurah(surah.number) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun JuzContent(
    state: SurahListViewModel.UiState,
    query: String,
    onOpenJuz: (SurahListViewModel.JuzStart) -> Unit,
) {
    val normalizedQuery = query.trim()
    val filtered = state.juzStarts.filter { start ->
        normalizedQuery.isBlank() ||
            start.juz.toString().contains(normalizedQuery)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = IslamicSpacing.PageHorizontal,
            vertical = IslamicSpacing.XSmall,
        ),
    ) {
        if (filtered.isEmpty()) {
            item {
                MuslimEmptyState(
                    title = stringResource(R.string.quran_search_no_results),
                    modifier = Modifier.padding(vertical = IslamicSpacing.Large),
                )
            }
        } else {
            items(filtered, key = { it.juz }) { start ->
                JuzRow(start = start, onClick = { onOpenJuz(start) })
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun JuzRow(
    start: SurahListViewModel.JuzStart,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = IslamicSpacing.Compact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Medium),
    ) {
        Text(
            text = start.juz.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quran_juz_title, start.juz),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.quran_juz_start,
                    start.surahNumber,
                    start.ayahNumber,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuranHomeBookmarksContent(
    bookmarks: List<Bookmark>,
    query: String,
    onOpenBookmarks: () -> Unit,
    onOpenAyah: (Bookmark) -> Unit,
) {
    val normalizedQuery = query.trim()
    val filtered = bookmarks.filter { bookmark ->
        normalizedQuery.isBlank() ||
            bookmark.surahName.contains(normalizedQuery, ignoreCase = true) ||
            bookmark.ayah.numberInSurah.toString() == normalizedQuery ||
            bookmark.ayah.text.contains(normalizedQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = IslamicSpacing.PageHorizontal,
            vertical = IslamicSpacing.XSmall,
        ),
    ) {
        item(key = "bookmarks-header") {
            MuslimSectionHeader(
                title = stringResource(R.string.quran_bookmarks),
                action = {
                    IconButton(onClick = onOpenBookmarks) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = stringResource(R.string.quran_open_bookmarks),
                        )
                    }
                },
            )
        }
        if (filtered.isEmpty()) {
            item(key = "bookmarks-empty") {
                MuslimEmptyState(
                    title = if (normalizedQuery.isBlank()) {
                        stringResource(R.string.quran_bookmarks_empty)
                    } else {
                        stringResource(R.string.quran_search_no_results)
                    },
                    icon = Icons.Filled.Bookmark,
                    modifier = Modifier.padding(vertical = IslamicSpacing.Large),
                )
            }
        } else {
            items(filtered, key = { it.ayah.globalNumber }) { bookmark ->
                QuranHomeBookmarkRow(
                    bookmark = bookmark,
                    onClick = { onOpenAyah(bookmark) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun QuranHomeBookmarkRow(
    bookmark: Bookmark,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = IslamicSpacing.Compact),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
    ) {
        Text(
            text = stringResource(
                R.string.quran_bookmark_ref,
                bookmark.surahName,
                bookmark.ayah.numberInSurah.toString(),
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = bookmark.ayah.text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SurahRow(
    surah: Surah,
    onClick: () -> Unit,
    onPlay: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = IslamicSpacing.Compact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Medium),
    ) {
        Text(
            text = surah.number.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XXSmall),
        ) {
            Text(
                text = surah.arabicName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = buildString {
                    append(surah.englishName)
                    surah.translation.takeIf { it.isNotBlank() }?.let {
                        append(" — ")
                        append(it)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(stringResource(R.string.quran_surah_ayahs, surah.ayahCount))
                    append(" · ")
                    append(
                        stringResource(
                            if (surah.revelationType.equals("Meccan", ignoreCase = true)) {
                                R.string.quran_surah_meccan
                            } else {
                                R.string.quran_surah_medinan
                            },
                        ),
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        IconButton(onClick = onPlay) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.quran_range_whole_surah),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
        }
    }
}
