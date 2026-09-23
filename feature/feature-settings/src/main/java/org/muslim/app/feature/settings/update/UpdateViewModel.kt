package org.muslim.app.feature.settings.update

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import javax.inject.Inject

/** UI state of the update screen. */
sealed interface UpdateUiState {
    data object Loading : UpdateUiState
    data class Available(val release: ReleaseInfo, val installedVersion: String) : UpdateUiState
    data object UpToDate : UpdateUiState
    data object Unavailable : UpdateUiState
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val updateChecker: UpdateChecker,
    private val updateDownloads: UpdateDownloadManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Loading)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    /** Epoch millis of the last successful check (0 = never checked). */
    val lastCheckEpoch: StateFlow<Long> = appPreferencesRepository.preferences
        .map { it.lastUpdateCheckEpoch }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** The version currently installed on the device. */
    val installedVersion: String = updateChecker.installedVersion()

    init {
        refresh()
        observeDownloadState()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Loading
            when (val result = updateChecker.check()) {
                is UpdateChecker.Result.UpdateAvailable -> {
                    _uiState.value = UpdateUiState.Available(
                        result.release,
                        updateChecker.installedVersion(),
                    )
                    appPreferencesRepository.setLastUpdateCheck(System.currentTimeMillis())
                    maybeStartAutomaticDownload(result.release)
                }
                UpdateChecker.Result.UpToDate -> {
                    _uiState.value = UpdateUiState.UpToDate
                    appPreferencesRepository.setLastUpdateCheck(System.currentTimeMillis())
                }
                UpdateChecker.Result.Unavailable -> _uiState.value = UpdateUiState.Unavailable
            }
        }
    }

    /** Opens the public GitHub releases page in the browser. */
    fun openReleasesPage() {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, RELEASES_URL.toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    /** Starts a user-requested download on any connected network. */
    fun startDownload() {
        val release = (_uiState.value as? UpdateUiState.Available)?.release ?: return
        viewModelScope.launch {
            _downloadState.value = UpdateDownloadState.Enqueuing
            _downloadState.value = updateDownloads.enqueue(release, wifiOnly = false)
        }
    }

    /**
     * Re-verifies the persisted APK immediately before handing it to Android's
     * package installer. Installation itself always remains system-confirmed.
     */
    fun installDownloadedUpdate() {
        viewModelScope.launch {
            _downloadState.value = UpdateDownloadState.Verifying
            when (val verified = updateDownloads.verifyCurrent()) {
                is UpdateDownloadState.ReadyToInstall -> {
                    val file = updateDownloads.currentFile()
                    if (file == null || !file.exists()) {
                        _downloadState.value =
                            UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)
                        return@launch
                    }

                    if (!context.packageManager.canRequestPackageInstalls()) {
                        _downloadState.value = verified
                        openInstallSettings()
                        return@launch
                    }

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file,
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, APK_MIME)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching { context.startActivity(intent) }
                        .onSuccess { _downloadState.value = verified }
                        .onFailure {
                            _downloadState.value =
                                UpdateDownloadState.Failed(UpdateDownloadFailure.Unknown)
                        }
                }
                else -> _downloadState.value = verified
            }
        }
    }

    private suspend fun maybeStartAutomaticDownload(release: ReleaseInfo) {
        val prefs = appPreferencesRepository.preferences.first()
        if (!prefs.updateCheckEnabled || !prefs.autoUpdateEnabled) return

        when (updateDownloads.currentState()) {
            UpdateDownloadState.Idle,
            is UpdateDownloadState.Failed -> {
                _downloadState.value = UpdateDownloadState.Enqueuing
                _downloadState.value = updateDownloads.enqueue(
                    release = release,
                    wifiOnly = prefs.autoUpdateWifiOnly,
                )
            }
            else -> Unit
        }
    }

    /**
     * Polls DownloadManager while this screen's ViewModel exists. The transfer
     * itself is not tied to this loop; the manifest receiver handles completion
     * even when the UI/process is gone.
     */
    private fun observeDownloadState() {
        viewModelScope.launch {
            while (isActive) {
                if (_downloadState.value !is UpdateDownloadState.Enqueuing) {
                    _downloadState.value = runCatching { updateDownloads.currentState() }
                        .getOrElse { UpdateDownloadState.Failed(UpdateDownloadFailure.Unknown) }
                }
                val delayMillis = when (_downloadState.value) {
                    is UpdateDownloadState.Downloading,
                    is UpdateDownloadState.Paused,
                    UpdateDownloadState.Enqueuing,
                    UpdateDownloadState.Verifying -> 750L
                    else -> 3_000L
                }
                delay(delayMillis)
            }
        }
    }

    /** Opens Android's per-app "install unknown apps" permission when required. */
    private fun openInstallSettings() {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:${context.packageName}".toUri(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private companion object {
        const val RELEASES_URL = "https://github.com/Alaa91H/Muslim/releases"
        const val APK_MIME = "application/vnd.android.package-archive"
    }
}
