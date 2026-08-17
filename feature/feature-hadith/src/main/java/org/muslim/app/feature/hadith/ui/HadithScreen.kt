package org.muslim.app.feature.hadith.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.hadith.R
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithCollection

/**
 * Hadith library (PROJECT_PROMPT.md §6 Phase 3): daily hadith, collection
 * filter, full-text search, bookmarks and text sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = hiltViewModel(),
) {
    val hadiths by viewModel.hadiths.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val dailyNotificationEnabled by viewModel.dailyNotificationEnabled.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hadith_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.hadith_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text(stringResource(R.string.hadith_search_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.hadith_daily_notification),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.hadith_daily_notification_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = dailyNotificationEnabled,
                    onCheckedChange = viewModel::setDailyNotificationEnabled,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = collection == null,
                    onClick = { viewModel.setCollection(null) },
                    label = { Text(stringResource(R.string.hadith_all)) },
                    modifier = Modifier.padding(end = 8.dp),
                )
                HadithCollection.entries.forEach { option ->
                    FilterChip(
                        selected = collection == option,
                        onClick = { viewModel.setCollection(option) },
                        label = { Text(stringResource(option.titleRes)) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
            ) {
                if (query.isBlank() && daily != null && collection == null) {
                    item(key = "daily") {
                        DailyHadithCard(
                            hadith = daily!!,
                            bookmarked = daily!!.id in bookmarkedIds,
                            onToggleBookmark = { viewModel.toggleBookmark(daily!!.id) },
                        )
                    }
                }
                items(hadiths, key = { it.id }) { hadith ->
                    HadithCard(
                        hadith = hadith,
                        bookmarked = hadith.id in bookmarkedIds,
                        onToggleBookmark = { viewModel.toggleBookmark(hadith.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DailyHadithCard(
    hadith: Hadith,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hadith_of_the_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            HadithBody(hadith)
        }
    }
}

@Composable
private fun HadithCard(
    hadith: Hadith,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
) {
    var showTranslation by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTranslation = !showTranslation }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HadithBody(hadith, showTranslation = showTranslation)
        }
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(
                    if (bookmarked) R.string.hadith_bookmark_remove else R.string.hadith_bookmark_add
                ),
                tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HadithBody(hadith: Hadith, showTranslation: Boolean = true) {
    val context = LocalContext.current
    Text(
        text = hadith.arabicText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(end = 8.dp),
    )
    if (showTranslation && hadith.translation.isNotBlank()) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = hadith.translation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = hadith.grade,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(0.dp))
        Text(
            text = hadith.source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                val text = "${hadith.arabicText}\n\n${hadith.source}\n\n${hadith.translation}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                runCatching { context.startActivity(Intent.createChooser(intent, null)) }
            },
        ) {
            Icon(
                Icons.Filled.Share,
                contentDescription = stringResource(R.string.hadith_share),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
