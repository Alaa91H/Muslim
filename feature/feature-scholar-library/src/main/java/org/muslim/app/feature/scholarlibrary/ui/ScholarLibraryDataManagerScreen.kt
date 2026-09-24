package org.muslim.app.feature.scholarlibrary.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.feature.scholarlibrary.R
import org.muslim.app.feature.scholarlibrary.domain.ScholarContentPack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarLibraryDataManagerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingBackup by remember { mutableStateOf<String?>(null) }

    val packLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        readScholarTextFile(context, uri)?.let { selected ->
            viewModel.importPack(selected.text, selected.displayName)
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        readScholarTextFile(context, uri)?.let { selected ->
            viewModel.restoreStudyBackup(selected.text)
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        val success = writeScholarTextFile(context, uri, pendingBackup)
        pendingBackup = null
        viewModel.reportBackupSaved(success)
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scholar_library_data_manager)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scholar_library_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        DataManagerContent(
            packs = state.contentPacks,
            padding = padding,
            onImportPack = { packLauncher.launch(arrayOf("application/json", "text/plain")) },
            onRestoreBackup = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) },
            onExportBackup = {
                scope.launch {
                    val raw = viewModel.createStudyBackup() ?: return@launch
                    pendingBackup = raw
                    exportLauncher.launch(BACKUP_FILENAME)
                }
            },
        )
    }
}

@Composable
private fun DataManagerContent(
    packs: List<ScholarContentPack>,
    padding: PaddingValues,
    onImportPack: () -> Unit,
    onRestoreBackup: () -> Unit,
    onExportBackup: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PackageActionsCard(
                onImportPack = onImportPack,
                onRestoreBackup = onRestoreBackup,
                onExportBackup = onExportBackup,
            )
        }
        item {
            Text(
                stringResource(R.string.scholar_library_installed_packs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (packs.isEmpty()) {
            item {
                MuslimEmptyState(
                    title = stringResource(R.string.scholar_library_no_registered_packs),
                    icon = Icons.Filled.Download,
                )
            }
        } else {
            items(packs, key = { it.id }) { pack ->
                ContentPackCard(pack)
            }
        }
    }
}

@Composable
private fun PackageActionsCard(
    onImportPack: () -> Unit,
    onRestoreBackup: () -> Unit,
    onExportBackup: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.scholar_library_content_and_backup),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.scholar_library_backup_scope_notice),
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onImportPack, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Text(
                    text = stringResource(R.string.scholar_library_import_or_update_pack),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExportBackup, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Text(
                        text = stringResource(R.string.scholar_library_export_study_backup),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                OutlinedButton(onClick = onRestoreBackup, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Text(
                        text = stringResource(R.string.scholar_library_restore_study_backup),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentPackCard(pack: ScholarContentPack) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(pack.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(
                    R.string.scholar_library_pack_version_summary,
                    pack.version,
                    pack.schemaVersion,
                    pack.installation.bookCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                if (pack.installation.managed) {
                    stringResource(R.string.scholar_library_pack_managed)
                } else {
                    stringResource(R.string.scholar_library_pack_legacy)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.scholar_library_pack_source, pack.source.name),
                style = MaterialTheme.typography.bodySmall,
            )
            pack.source.originName?.let { origin ->
                Text(
                    stringResource(R.string.scholar_library_pack_origin, origin),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                stringResource(R.string.scholar_library_pack_license),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(pack.source.licenseNotice, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private const val BACKUP_FILENAME = "scholar-library-study-backup.json"
