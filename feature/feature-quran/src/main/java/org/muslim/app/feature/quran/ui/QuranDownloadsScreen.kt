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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.DownloadScope
import org.muslim.app.feature.quran.data.DownloadStatus
import org.muslim.app.feature.quran.data.DownloadTaskUi
import java.util.Locale

/** 30-minute increments across a full day, as minutes from midnight. */
private val nightTimeOptions: List<Int> = (0 until 24 * 60 step 30).toList()

/** Downloads hub: choose scope + reciter, see verified sizes, and track background progress. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranDownloadsViewModel = hiltViewModel(),
) {
    val scope by viewModel.scope.collectAsStateWithLifecycle()
    val surahInput by viewModel.surahInput.collectAsStateWithLifecycle()
    val ayahInput by viewModel.ayahInput.collectAsStateWithLifecycle()
    val estimateBytes by viewModel.estimateBytes.collectAsStateWithLifecycle()
    val verifiedBytes by viewModel.verifiedBytes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val nightOnly by viewModel.nightOnly.collectAsStateWithLifecycle()
    val nightWindowStart by viewModel.nightWindowStart.collectAsStateWithLifecycle()
    val nightWindowEnd by viewModel.nightWindowEnd.collectAsStateWithLifecycle()
    val reciterState by viewModel.reciterState.collectAsStateWithLifecycle()
    var confirmDeleteSurah by remember { mutableStateOf<Int?>(null) }
    var confirmDeleteReciter by remember { mutableStateOf(false) }
    val totalSummary by viewModel.totalSummary.collectAsStateWithLifecycle()
    val selectedReciterId by viewModel.selectedReciterId.collectAsStateWithLifecycle()
    val reciters = viewModel.reciters

    // One page per reciter: swipe between reciters or tap a tab. The active
    // page becomes the selected (persisted) download target.
    val pagerState = rememberPagerState(pageCount = { reciters.size })
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(selectedReciterId) {
        val initial = reciters.indexOfFirst { it.id == selectedReciterId }
        if (initial >= 0 && initial != pagerState.currentPage) pagerState.scrollToPage(initial)
    }
    LaunchedEffect(pagerState.currentPage) {
        val pageReciter = reciters.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (pageReciter.id != selectedReciterId) viewModel.selectReciter(pageReciter.id)
    }

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
                .padding(innerPadding),
        ) {
            // Everything downloaded across all reciters, at a glance.
            TotalSummaryCard(summary = totalSummary)

            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage.coerceIn(0, reciters.size - 1),
                edgePadding = 8.dp,
            ) {
                reciters.forEachIndexed { index, option ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = option.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) { pageIndex ->
                val pageReciter = reciters[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(12.dp))

                    // Immediate per-reciter summary in the page header: how
                    // many ayahs are on disk for this reciter and their size.
                    ReciterHeaderSummary(
                        state = reciterState,
                        reciterName = pageReciter.name,
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

                    when {
                        verifiedBytes != null -> {
                            Text(
                                text = stringResource(R.string.quran_download_size_verified, formatBytes(verifiedBytes!!)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        estimateBytes != null -> {
                            Text(
                                text = stringResource(R.string.quran_download_size_estimate, formatBytes(estimateBytes!!)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.quran_download_size_unknown),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Night-only downloads (التحميل الليلي): defer the transfer
                    // to the configured window to save data and battery.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.quran_download_night_only),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(
                                    R.string.quran_download_night_hint,
                                    formatWindow(nightWindowStart, nightWindowEnd),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = nightOnly,
                            onCheckedChange = viewModel::setNightOnly,
                        )
                    }

                    if (nightOnly) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.quran_download_night_window_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row {
                            TimeDropdown(
                                label = stringResource(R.string.quran_download_night_start),
                                selectedMinutes = nightWindowStart,
                                options = nightTimeOptions,
                                onSelected = viewModel::setNightWindowStart,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(12.dp))
                            TimeDropdown(
                                label = stringResource(R.string.quran_download_night_end),
                                selectedMinutes = nightWindowEnd,
                                options = nightTimeOptions,
                                onSelected = viewModel::setNightWindowEnd,
                                modifier = Modifier.weight(1f),
                            )
                        }
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

                    // What is already downloaded for this reciter's page.
                    ReciterStateSection(
                        state = reciterState,
                        onDeleteSurah = { confirmDeleteSurah = it },
                        onDeleteReciter = { confirmDeleteReciter = true },
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.quran_downloads_active_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))

                    val pageTasks = tasks.filter { it.reciterId == pageReciter.id }
                    if (pageTasks.isEmpty()) {
                        Text(
                            text = stringResource(R.string.quran_downloads_none),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        pageTasks.forEach { task ->
                            TaskRow(
                                task = task,
                                onPause = { viewModel.pause(task.id) },
                                onResume = { viewModel.resume(task.id) },
                                onCancel = { viewModel.cancel(task.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    confirmDeleteSurah?.let { surahNumber ->
        AlertDialog(
            onDismissRequest = { confirmDeleteSurah = null },
            title = { Text(stringResource(R.string.quran_download_delete_confirm)) },
            text = { Text(stringResource(R.string.quran_download_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSurah(surahNumber)
                    confirmDeleteSurah = null
                }) {
                    Text(stringResource(R.string.quran_download_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteSurah = null }) {
                    Text(stringResource(R.string.quran_download_keep))
                }
            },
        )
    }
    if (confirmDeleteReciter) {
        AlertDialog(
            onDismissRequest = { confirmDeleteReciter = false },
            title = { Text(stringResource(R.string.quran_download_delete_confirm)) },
            text = { Text(stringResource(R.string.quran_download_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReciter()
                    confirmDeleteReciter = false
                }) {
                    Text(stringResource(R.string.quran_download_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteReciter = false }) {
                    Text(stringResource(R.string.quran_download_keep))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDropdown(
    label: String,
    selectedMinutes: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = formatMinutes(selectedMinutes),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(formatMinutes(minutes)) },
                    onClick = {
                        expanded = false
                        onSelected(minutes)
                    },
                )
            }
        }
    }
}

/** Summary of all downloaded recitation audio across every reciter. */
@Composable
private fun TotalSummaryCard(summary: TotalDownloadSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.quran_downloads_summary_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.quran_downloads_summary,
                        summary.downloadedSurahs,
                        summary.downloadedAyahs,
                        formatBytes(summary.totalBytes),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: DownloadTaskUi,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
) {
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
                when (task.status) {
                    DownloadStatus.Downloading,
                    DownloadStatus.Queued -> {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Filled.Pause, contentDescription = stringResource(R.string.quran_download_pause))
                        }
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.quran_download_cancel))
                        }
                    }
                    DownloadStatus.Paused -> {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.quran_download_resume))
                        }
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.quran_download_cancel))
                        }
                    }
                    DownloadStatus.WaitingNight -> {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.quran_download_cancel))
                        }
                    }
                    else -> Unit
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
        DownloadStatus.Paused -> R.string.quran_download_status_paused
        DownloadStatus.WaitingNight -> R.string.quran_download_night_status
        DownloadStatus.Completed -> R.string.quran_download_status_completed
        DownloadStatus.Failed -> R.string.quran_download_status_failed
    },
)

internal fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return String.format(Locale.ROOT, if (unit == 0) "%.0f %s" else "%.1f %s", value, units[unit])
}

/**
 * Compact header summary shown at the top of each reciter's page: the number
 * of ayahs already downloaded for that reciter and their total size, so the
 * user sees the state immediately without scrolling to the bottom.
 */
@Composable
private fun ReciterHeaderSummary(
    state: org.muslim.app.feature.quran.data.ReciterDownloadState?,
    reciterName: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = reciterName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.quran_download_reciter_header,
                        state?.downloadedAyahs ?: 0,
                        formatBytes(state?.totalBytes ?: 0L),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * What is downloaded for the selected reciter: a summary line plus one row per
 * surah with its size and a delete button. Deletes are confirmed by the caller
 * (dialog), so the section itself stays simple.
 */
@Composable
private fun ReciterStateSection(
    state: org.muslim.app.feature.quran.data.ReciterDownloadState?,
    onDeleteSurah: (Int) -> Unit,
    onDeleteReciter: () -> Unit,
) {
    Text(
        text = stringResource(R.string.quran_downloads_title) + " — " + stringResource(R.string.quran_reciter),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(8.dp))
    val current = state
    if (current == null || current.downloadedAyahs == 0) {
        Text(
            text = stringResource(R.string.quran_download_reciter_state, "—"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        text = stringResource(
            R.string.quran_download_reciter_state_detail,
            current.downloadedSurahs,
            current.downloadedAyahs,
            formatBytes(current.totalBytes),
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(8.dp))
    current.surahCounts.forEach { (surahNumber, ayahs) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.quran_surah_number_short, surahNumber) + " · $ayahs",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onDeleteSurah(surahNumber) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.quran_download_delete_surah),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    TextButton(onClick = onDeleteReciter) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.quran_download_delete_reciter))
    }
}

/** Formats minutes-from-midnight as "HH:MM". */
private fun formatMinutes(minutes: Int): String =
    String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

/** Formats a window as "HH:MM – HH:MM". */
private fun formatWindow(startMinutes: Int, endMinutes: Int): String =
    "${formatMinutes(startMinutes)} – ${formatMinutes(endMinutes)}"
