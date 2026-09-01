package org.muslim.app.feature.quran.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.ui.theme.IslamicOrnament
import org.muslim.app.core.ui.theme.IslamicOrnamentImage
import org.muslim.app.core.ui.theme.IslamicOrnamentOpacity
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.Surah

/**
 * Quran surah list (PROJECT_PROMPT.md §6 Phase 2).
 * Content loads from the bundled offline database on first run.
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

    Column(modifier = modifier.fillMaxSize()) {
        IslamicOrnamentImage(
            ornament = IslamicOrnament.SurahHeader,
            tint = MaterialTheme.colorScheme.tertiary,
            alpha = IslamicOrnamentOpacity.LightSection,
            modifier = Modifier
                .fillMaxWidth()
                .height(IslamicSpacing.Medium)
                .padding(top = IslamicSpacing.XXSmall),
        )
        MuslimSectionHeader(
            title = stringResource(R.string.quran_title),
            modifier = Modifier.padding(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Small,
            ),
            action = {
                IconButton(onClick = onOpenBookmarks) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = stringResource(R.string.quran_bookmarks),
                    )
                }
            },
        )

        when {
            state.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(IslamicSpacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = IslamicSpacing.Medium,
                    ),
                ) {
                    item(key = "khatma") {
                        KhatmaProgressCard(
                            readThrough = state.readThroughGlobal,
                            totalAyahs = state.totalAyahs,
                            fraction = state.progressFraction,
                        )
                    }
                    state.lastRead?.let { last ->
                        item(key = "resume") {
                            ResumeReadingCard(
                                surahNumber = last.surahNumber,
                                ayahNumber = last.numberInSurah,
                                onClick = { onResumeReading(last.surahNumber, last.globalNumber) },
                            )
                        }
                    }
                    items(state.surahs, key = { it.number }) { surah ->
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
    }
}

@Composable
private fun KhatmaProgressCard(readThrough: Int, totalAyahs: Int, fraction: Float) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Small,
            ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(IslamicSpacing.Medium),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.quran_khatma_progress),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(
                        R.string.quran_khatma_percent,
                        (fraction * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(IslamicSpacing.Small))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(IslamicSpacing.XSmall))
            Text(
                text = stringResource(
                    R.string.quran_khatma_detail,
                    readThrough.toString(),
                    totalAyahs.toString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResumeReadingCard(surahNumber: Int, ayahNumber: Int, onClick: () -> Unit) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Small,
            )
            .clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(IslamicSpacing.Medium),
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
            Spacer(Modifier.width(IslamicSpacing.XSmall))
            Text(
                text = stringResource(R.string.quran_resume, surahNumber.toString(), ayahNumber.toString()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SurahRow(surah: Surah, onClick: () -> Unit, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Compact,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = surah.number.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = IslamicSpacing.Medium),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.arabicName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = buildString {
                    append(surah.englishName)
                    surah.translation.takeIf { it.isNotBlank() }?.let { append(" — $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onPlay,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(
                    R.string.quran_range_whole_surah,
                ),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.quran_surah_ayahs, surah.ayahCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    if (surah.revelationType.equals("Meccan", ignoreCase = true)) {
                        R.string.quran_surah_meccan
                    } else {
                        R.string.quran_surah_medinan
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}
