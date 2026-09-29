package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimTopBar
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.Bookmark

/** Focused list of the user's saved ayahs. */
@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onOpenAyah: (surahNumber: Int, globalNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    MuslimScreen(
        modifier = modifier,
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.quran_bookmarks),
                onNavigateBack = onBack,
                navigationContentDescription = stringResource(R.string.quran_back),
            )
        },
    ) {
        if (bookmarks.isEmpty()) {
            MuslimEmptyState(
                title = stringResource(R.string.quran_bookmarks_empty),
                icon = Icons.Filled.Bookmark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(IslamicSpacing.Large),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = IslamicSpacing.Small,
                    bottom = IslamicSpacing.Large,
                ),
                verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
            ) {
                items(
                    items = bookmarks,
                    key = { it.ayah.globalNumber },
                ) { bookmark ->
                    BookmarkCard(
                        bookmark = bookmark,
                        onClick = {
                            onOpenAyah(
                                bookmark.ayah.surahNumber,
                                bookmark.ayah.globalNumber,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: Bookmark,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
    ) {
        Column(
            modifier = Modifier.padding(IslamicSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
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
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
