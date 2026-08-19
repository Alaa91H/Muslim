# -*- coding: utf-8 -*-
"""QuranDownloadsScreen: one page per reciter (tabs + HorizontalPager),
a global downloaded-summary card, and per-reciter task filtering."""
import io

p = "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadsScreen.kt"
s = io.open(p, encoding="utf-8").read()


def rep(old, new, count=1):
    global s
    n = s.count(old)
    if n < count:
        print("MISS (%d/%d): %r" % (n, count, old[:80]))
        return False
    s = s.replace(old, new, count)
    print("OK: %r" % old[:70])
    return True


ok = True

# ---- 1) imports ----
ok &= rep(
    "import androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\n",
    "import androidx.compose.foundation.pager.HorizontalPager\nimport androidx.compose.foundation.pager.rememberPagerState\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\n",
)
ok &= rep(
    "import androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.Scaffold\n",
    "import androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.PrimaryScrollableTabRow\nimport androidx.compose.material3.Scaffold\nimport androidx.compose.material3.Tab\n",
)
ok &= rep(
    "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\n",
    "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.rememberCoroutineScope\n",
)
ok &= rep(
    "import androidx.compose.ui.unit.dp\n",
    "import androidx.compose.ui.text.style.TextOverflow\nimport androidx.compose.ui.unit.dp\n",
)
ok &= rep(
    "import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel\n",
    "import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel\nimport kotlinx.coroutines.launch\n",
)

# ---- 2) replace the screen body (keeps the two confirm dialogs at the end) ----
start_marker = "    var confirmDeleteReciter by remember { mutableStateOf(false) }\n"
end_marker = "\n    confirmDeleteSurah?.let { surahNumber ->"
i = s.find(start_marker)
j = s.find(end_marker, i)
if i < 0 or j < 0:
    print("MISS: body markers (%d, %d)" % (i, j))
    ok = False
else:
    new_body = '''    var confirmDeleteReciter by remember { mutableStateOf(false) }
    val totalSummary by viewModel.totalSummary.collectAsStateWithLifecycle()
    val reciters = viewModel.reciters

    // One page per reciter: swipe between reciters or tap a tab. The active
    // page becomes the selected (persisted) download target.
    val pagerState = rememberPagerState(pageCount = { reciters.size })
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(viewModel.selectedReciterId.value) {
        val initial = reciters.indexOfFirst { it.id == viewModel.selectedReciterId.value }
        if (initial >= 0 && initial != pagerState.currentPage) pagerState.scrollToPage(initial)
    }
    LaunchedEffect(pagerState.currentPage) {
        val pageReciter = reciters.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (pageReciter.id != viewModel.selectedReciterId.value) viewModel.selectReciter(pageReciter.id)
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
'''
    s = s[:i] + new_body + s[j:]
    print("OK: body replaced")

# ---- 3) remove ReciterDropdown composable ----
old_dropdown = """@OptIn(ExperimentalMaterial3Api::class)
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

"""
ok &= rep(old_dropdown, "")

# ---- 4) add TotalSummaryCard before TaskRow ----
old_taskrow = """@Composable
private fun TaskRow("""
new_taskrow = """/** Summary of all downloaded recitation audio across every reciter. */
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
private fun TaskRow("""
ok &= rep(old_taskrow, new_taskrow)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("ALL_OK" if ok else "SOME_MISSED")
