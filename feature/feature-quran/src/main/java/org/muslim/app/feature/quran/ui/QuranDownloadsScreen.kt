package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.DownloadScope
import org.muslim.app.feature.quran.data.DownloadStatus
import org.muslim.app.feature.quran.data.DownloadTaskUi

/** Downloads hub: choose scope + reciter, see sizes, and track background progress. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranDownloadsViewModel = hiltViewModel(),
) {
    val reciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val scope by viewModel.scope.collectAsStateWithLifecycle()
    val surahInput by viewModel.surahInput.collectAsStateWithLifecycle()
    val ayahInput by viewModel.ayahInput.collectAsStateWithLifecycle()
    val estimateBytes by viewModel.estimateBytes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quran_downloads_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.quran_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ReciterDropdown(
                selected = reciter,
                reciters = viewModel.reciters,
                onSelected = viewModel::selectReciter,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = scope == DownloadScope.Ayah,
                    onClick = { viewModel.setScope(DownloadScope.Ayah) },
                    label = { Text(stringResource(R.string.quran_download_scope_ayah)) },
                )
                FilterChip(
                    selected = scope == DownloadScope.Surah,
                    onClick = { viewModel.setScope(DownloadScope.Surah) },
                    label = { Text(stringResource(R.string.quran_download_scope_surah)) },
                )
                FilterChip(
                    selected = scope == DownloadScope.FullQuran,
                    onClick = { viewModel.setScope(DownloadScope.FullQuran) },
                    label = { Text(stringResource(R.string.quran_download_scope_full)) },
                )
            }

            Spacer(Modifier.height(12.dp))

            when (scope) {
                DownloadScope.Ayah -> {
                    Row {
                        OutlinedTextField(
                            value = surahInput,
                            onValueChange = viewModel::setSurahInput,
                            label = { Text(stringResource(R.string.quran_download_surah_number)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        OutlinedTextField(
                            value = ayahInput,
                            onValueChange = viewModel::setAyahInput,
                            label = { Text(stringResource(R.string.quran_download_ayah_number)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                DownloadScope.Surah -> {
                    OutlinedTextField(
                        value = surahInput,
                        onValueChange = viewModel::setSurahInput,
                        label = { Text(stringResource(R.string.quran_download_surah_number)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                DownloadScope.FullQuran -> {
                    Text(
                        text = stringResource(R.string.quran_download_full_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (estimateBytes != null) {
                Text(
                    text = stringResource(R.string.quran_download_size_estimate, formatBytes(estimateBytes!!)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = stringResource(R.string.quran_download_size_unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = viewModel::startDownload,
                enabled = estimateBytes != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.quran_download_start))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.quran_downloads_active_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.quran_downloads_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                tasks.forEach { task ->
                    TaskRow(task = task, onCancel = { viewModel.cancel(task.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReciterDropdown(
    selected: org.muslim.app.feature.quran.domain.Reciter,
    reciters: List<org.muslim.app.feature.quran.domain.Reciter>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.quran_reciter)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            reciters.forEach { option ->
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
                    onClick = { expanded = false; onSelected(option.id) },
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: DownloadTaskUi, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.label, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${task.reciterName} · ${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = statusLabel(task.status),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (task.status) {
                        DownloadStatus.Completed -> MaterialTheme.colorScheme.primary
                        DownloadStatus.Failed -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (task.status == DownloadStatus.Queued || task.status == DownloadStatus.Downloading) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.quran_download_cancel))
                    }
                }
            }
            if (task.status == DownloadStatus.Downloading || task.status == DownloadStatus.Queued) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun statusLabel(status: DownloadStatus): String = stringResource(
    when (status) {
        DownloadStatus.Queued -> R.string.quran_download_status_queued
        DownloadStatus.Downloading -> R.string.quran_download_status_downloading
        DownloadStatus.Completed -> R.string.quran_download_status_completed
        DownloadStatus.Failed -> R.string.quran_download_status_failed
    },
)

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return String.format(if (unit == 0) "%.0f %s" else "%.1f %s", value, units[unit])
}
