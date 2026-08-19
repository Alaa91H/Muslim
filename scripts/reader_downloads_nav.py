# -*- coding: utf-8 -*-
"""Reader: 'Downloads' menu item navigates to the full Downloads screen;
remove the in-reader RecitationDownloadsDialog and its exclusive UI."""
import io

p = "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"
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

# 1) signature: add onOpenDownloads
ok &= rep(
    "fun QuranReaderScreen(\n    onBack: () -> Unit,\n    modifier: Modifier = Modifier,\n    viewModel: QuranReaderViewModel = hiltViewModel(),\n) {",
    "fun QuranReaderScreen(\n    onBack: () -> Unit,\n    onOpenDownloads: () -> Unit = {},\n    modifier: Modifier = Modifier,\n    viewModel: QuranReaderViewModel = hiltViewModel(),\n) {",
)

# 2) remove dialog-only collected states
ok &= rep(
    "    val reciter by viewModel.selectedReciter.collectAsStateWithLifecycle()\n    val reciterDownloadState by viewModel.reciterDownloadState.collectAsStateWithLifecycle()\n    val downloaded by viewModel.downloaded.collectAsStateWithLifecycle()\n    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()\n    val downloading by viewModel.downloading.collectAsStateWithLifecycle()\n",
    "",
)

# 3) remove showDownloads state
ok &= rep("    var showDownloads by remember { mutableStateOf(false) }\n", "")

# 4) menu item: navigate instead of opening the dialog
ok &= rep(
    "                                onClick = {\n                                    showMoreMenu = false\n                                    if (state.surah != null) showDownloads = true\n                                },",
    "                                onClick = {\n                                    showMoreMenu = false\n                                    onOpenDownloads()\n                                },",
)

# 5) remove the dialog invocation block
ok &= rep(
    """        if (showDownloads) {
            RecitationDownloadsDialog(
                reciter = reciter,
                reciterState = reciterDownloadState,
                downloaded = downloaded,
                downloading = downloading,
                progress = downloadProgress,
                surahNumber = state.surah?.number ?: 0,
                ayahCount = state.ayahs.size,
                onSelectReciter = viewModel::selectReciter,
                onDownloadAyah = viewModel::downloadCurrentAyah,
                onDownloadSurah = viewModel::downloadCurrentSurah,
                onDownloadWholeQuran = viewModel::downloadWholeQuran,
                onDeleteSurah = viewModel::deleteDownloadedSurah,
                onDismiss = { showDownloads = false },
            )
        }
""",
    "",
)

# 6) remove the RecitationDownloadsDialog composable (doc comment + body)
old_dialog_start = """/**
 * Download hub for the reader: reciter selection and per-scope download
 * actions (current ayah / current surah / whole Quran) with approximate sizes
 * and live progress. Opened from the download icon in the reader top bar, so
 * the recitation controls below stay uncluttered.
 */
@Composable
private fun RecitationDownloadsDialog(
    reciter: Reciter,
    reciterState: org.muslim.app.feature.quran.data.ReciterDownloadState?,
    downloaded: Boolean,
    downloading: Boolean,
    progress: Float?,
    surahNumber: Int,
    ayahCount: Int,
    onSelectReciter: (Reciter) -> Unit,
    onDownloadAyah: () -> Unit,
    onDownloadSurah: () -> Unit,
    onDownloadWholeQuran: () -> Unit,
    onDeleteSurah: (Int) -> Unit,
    onDismiss: () -> Unit,
) {"""
idx = s.find(old_dialog_start)
if idx < 0:
    print("MISS: dialog doc start")
    ok = False
else:
    # The dialog ends right before the next top-level comment for mirroredIfRtl.
    end_marker = "\n/**\n * Flips an icon horizontally in RTL layouts."
    end = s.find(end_marker, idx)
    if end < 0:
        print("MISS: dialog end marker")
        ok = False
    else:
        s = s[:idx] + s[end:]
        print("OK: removed RecitationDownloadsDialog")

# 7) remove DownloadOptionRow composable
old_row = """@Composable
private fun DownloadOptionRow(
    label: String,
    sizeBytes: Long,
    completed: Boolean,
    downloading: Boolean,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.quran_download_size_estimate, formatBytes(sizeBytes)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (completed) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.quran_downloaded),
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            IconButton(onClick = onDownload, enabled = !downloading) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = stringResource(R.string.quran_download_start),
                )
            }
        }
    }
}

"""
ok &= rep(old_row, "")

# 8) remove TOTAL_AYAHS const (dialog only)
ok &= rep("\nprivate const val TOTAL_AYAHS = 6236L\n", "\n")

# 9) imports
ok &= rep("import androidx.compose.material.icons.filled.CheckCircle\n", "")
ok &= rep("import androidx.compose.material.icons.filled.Delete\n", "")
ok &= rep("import androidx.compose.material3.LinearProgressIndicator\n", "")
ok &= rep("import org.muslim.app.feature.quran.domain.Reciter\n", "")

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("ALL_OK" if ok else "SOME_MISSED")
