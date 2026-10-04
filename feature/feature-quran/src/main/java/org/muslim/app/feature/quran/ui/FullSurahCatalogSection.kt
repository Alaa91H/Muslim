package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.FullSurahRecitation
import org.muslim.app.feature.quran.domain.FullSurahRecitationSearch

@Composable
internal fun FullSurahCatalogSection(
    recordings: List<FullSurahRecitation>,
    loading: Boolean,
    hasError: Boolean,
    onLoad: () -> Unit,
    onPlay: (FullSurahRecitation) -> Unit,
    onDownload: (FullSurahRecitation) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(recordings, query) {
        FullSurahRecitationSearch.filter(recordings, query)
    }

    Spacer(Modifier.height(IslamicSpacing.Compact))
    Text(
        text = stringResource(R.string.quran_reciter),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    TextButton(onClick = onLoad, enabled = !loading) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(IslamicSpacing.Small))
        }
        Text(stringResource(R.string.quran_reciter_picker_title))
    }
    if (hasError) {
        Text(
            text = stringResource(R.string.quran_download_status_failed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (recordings.isNotEmpty()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.quran_reciter)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (filtered.isEmpty()) {
            Text(
                text = stringResource(R.string.quran_reciter_no_results),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                items(filtered, key = FullSurahRecitation::id) { recording ->
                    FullSurahRecordingRow(recording, onPlay, onDownload)
                }
            }
        }
    }
}

@Composable
private fun FullSurahRecordingRow(
    recording: FullSurahRecitation,
    onPlay: (FullSurahRecitation) -> Unit,
    onDownload: (FullSurahRecitation) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onPlay(recording) }, modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(recording.reciterName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(recording.rewayaName, style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
        }
        TextButton(onClick = { onDownload(recording) }) {
            Text(stringResource(R.string.quran_download_start))
        }
    }
}
