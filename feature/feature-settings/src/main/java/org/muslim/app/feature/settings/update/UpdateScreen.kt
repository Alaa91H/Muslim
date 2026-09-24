package org.muslim.app.feature.settings.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimErrorState
import org.muslim.app.core.ui.theme.MuslimLoadingState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.settings.R
import java.util.Locale

/**
 * The "Updates" screen (reached from Settings or from the update-available
 * notification): shows the installed version, the latest published version
 * with its changelog and APK size. Download progress survives process death;
 * a completed APK is verified for package identity, versionCode and signer
 * before Android's system installer is allowed to open it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val lastCheckEpoch by viewModel.lastCheckEpoch.collectAsStateWithLifecycle()

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.update_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = IslamicSpacing.PageHorizontal, vertical = IslamicSpacing.Compact),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
        ) {
            when (val state = uiState) {
                UpdateUiState.Loading -> {
                    MuslimLoadingState(
                        title = stringResource(R.string.update_checking),
                    )
                }

                UpdateUiState.UpToDate -> {
                    IslamicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(IslamicIconSize.Prominent),
                            )
                            Spacer(Modifier.width(IslamicSpacing.Compact))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.update_up_to_date),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(IslamicSpacing.XSmall))
                                Text(
                                    text = stringResource(
                                        R.string.update_current_version,
                                        viewModel.installedVersion,
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                UpdateUiState.Unavailable -> {
                    MuslimErrorState(
                        title = stringResource(R.string.update_unavailable),
                        icon = Icons.Filled.ErrorOutline,
                        actionLabel = stringResource(R.string.update_retry),
                        onAction = viewModel::refresh,
                    )
                    if (downloadState is UpdateDownloadState.ReadyToInstall) {
                        IslamicCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = stringResource(R.string.update_ready_offline),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        IslamicPrimaryButton(
                            onClick = viewModel::installDownloadedUpdate,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Filled.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(IslamicIconSize.Supporting),
                            )
                            Spacer(Modifier.width(IslamicSpacing.Small))
                            Text(stringResource(R.string.update_install))
                        }
                    }
                }

                is UpdateUiState.Available -> {
                    IslamicCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(Modifier.width(IslamicSpacing.Compact))
                                Text(
                                    text = stringResource(R.string.update_new_version, state.release.version),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Spacer(Modifier.height(IslamicSpacing.Small))
                            Text(
                                text = stringResource(R.string.update_current_version, state.installedVersion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            if (state.release.isPrerelease) {
                                Spacer(Modifier.height(IslamicSpacing.Small))
                                Text(
                                    text = stringResource(R.string.update_beta_release),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            if (state.release.hasVerifiedMetadata) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.update_integrity_metadata_available),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.update_changelog),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IslamicCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.release.body.ifBlank { stringResource(R.string.update_no_changelog) },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    when (val transfer = downloadState) {
                        UpdateDownloadState.Idle -> {
                            IslamicPrimaryButton(
                                onClick = viewModel::startDownload,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(
                                        R.string.update_download,
                                        formatSize(state.release.apkSizeBytes),
                                    ),
                                )
                            }
                        }

                        UpdateDownloadState.Enqueuing -> {
                            IslamicPrimaryButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(IslamicIconSize.Supporting),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.update_preparing_download))
                            }
                        }

                        is UpdateDownloadState.Downloading -> {
                            IslamicCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
                                ) {
                                    Text(
                                        text = downloadProgressText(transfer),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    transfer.progressPercent?.let { percent ->
                                        LinearProgressIndicator(
                                            progress = { percent / 100f },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    } ?: LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                            IslamicSecondaryButton(
                                onClick = viewModel::cancelDownload,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.update_cancel_download))
                            }
                        }

                        is UpdateDownloadState.Paused -> {
                            IslamicCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.update_download_paused),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = pausedProgressText(transfer),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    transfer.progressPercent?.let { percent ->
                                        LinearProgressIndicator(
                                            progress = { percent / 100f },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                            IslamicSecondaryButton(
                                onClick = viewModel::cancelDownload,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.update_cancel_download))
                            }
                        }

                        UpdateDownloadState.Verifying -> {
                            IslamicPrimaryButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.update_verifying))
                            }
                        }

                        is UpdateDownloadState.ReadyToInstall -> {
                            IslamicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = stringResource(R.string.update_ready_to_install),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                            IslamicPrimaryButton(
                                onClick = viewModel::installDownloadedUpdate,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Filled.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.update_install))
                            }
                        }

                        is UpdateDownloadState.Failed -> {
                            MuslimErrorState(
                                title = stringResource(downloadFailureMessage(transfer.reason)),
                                icon = Icons.Filled.ErrorOutline,
                                actionLabel = stringResource(R.string.update_retry_download),
                                onAction = viewModel::startDownload,
                            )
                        }
                    }
                }
            }

            if (lastCheckEpoch > 0L) {
                Text(
                    text = stringResource(
                        R.string.update_last_check,
                        formatCheckDate(lastCheckEpoch),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IslamicSecondaryButton(
                onClick = viewModel::openReleasesPage,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Filled.Link,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.update_open_releases))
            }
        }
    }
}

@Composable
private fun downloadProgressText(state: UpdateDownloadState.Downloading): String {
    val percent = state.progressPercent
    return if (percent != null && state.totalBytes > 0L) {
        stringResource(
            R.string.update_downloading_progress,
            percent,
            formatSize(state.downloadedBytes),
            formatSize(state.totalBytes),
        )
    } else {
        stringResource(R.string.update_downloading_bytes, formatSize(state.downloadedBytes))
    }
}

@Composable
private fun pausedProgressText(state: UpdateDownloadState.Paused): String {
    val percent = state.progressPercent
    return if (percent != null && state.totalBytes > 0L) {
        stringResource(
            R.string.update_downloading_progress,
            percent,
            formatSize(state.downloadedBytes),
            formatSize(state.totalBytes),
        )
    } else {
        stringResource(R.string.update_downloading_bytes, formatSize(state.downloadedBytes))
    }
}

private fun downloadFailureMessage(reason: UpdateDownloadFailure): Int = when (reason) {
    UpdateDownloadFailure.DownloadFailed -> R.string.update_failed_download
    UpdateDownloadFailure.MissingFile -> R.string.update_failed_missing_file
    UpdateDownloadFailure.InvalidPackage -> R.string.update_failed_invalid_package
    UpdateDownloadFailure.WrongPackage -> R.string.update_failed_wrong_package
    UpdateDownloadFailure.SignatureMismatch -> R.string.update_failed_signature
    UpdateDownloadFailure.ChecksumMismatch -> R.string.update_failed_checksum
    UpdateDownloadFailure.VersionMismatch -> R.string.update_failed_version
    UpdateDownloadFailure.VersionCodeMismatch -> R.string.update_failed_version_code
    UpdateDownloadFailure.NotNewer -> R.string.update_failed_not_newer
    UpdateDownloadFailure.Unknown -> R.string.update_failed_generic
}

/** Formats an epoch timestamp as "dd MMM yyyy, HH:mm" (locale-aware). */
internal fun formatCheckDate(epochMillis: Long): String {
    val date = java.util.Date(epochMillis)
    val fmt = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return fmt.format(date)
}

/** Formats a byte count as "12.3 MB" (Western digits, locale-independent). */
internal fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "?"
    val mb = bytes / (1024.0 * 1024.0)
    return String.format(Locale.ROOT, "%.1f MB", mb)
}
