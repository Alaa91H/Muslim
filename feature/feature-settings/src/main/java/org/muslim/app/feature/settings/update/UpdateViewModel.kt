package org.muslim.app.feature.settings.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.PendingIntent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

/** UI state of the update screen. */
sealed interface UpdateUiState {
    data object Loading : UpdateUiState
    data class Available(val release: ReleaseInfo, val installedVersion: String) : UpdateUiState
    data object UpToDate : UpdateUiState
    data object Unavailable : UpdateUiState
}

/** State of the release-APK download + install flow. */
sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data object Downloading : UpdateDownloadState
    data object Downloaded : UpdateDownloadState
    data object Failed : UpdateDownloadState
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
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
    val installedVersion: String = UpdateChecker(context).installedVersion()

    private var downloadId: Long = -1L

    /** Snapshot of the auto-update flag taken when a release is found (the
     *  install path runs from a BroadcastReceiver, which cannot suspend). */
    private var autoUpdateMode = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != downloadId) return
            _downloadState.value = UpdateDownloadState.Downloaded
            installApk()
        }
    }

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Loading
            val checker = UpdateChecker(context)
            when (val result = checker.check()) {
                is UpdateChecker.Result.UpdateAvailable -> {
                    _uiState.value = UpdateUiState.Available(result.release, checker.installedVersion())
                    // Fully automatic mode (Session API install, user already
                    // confirmed once in Settings): download + install silently.
                    autoUpdateMode = appPreferencesRepository.preferences.first().autoUpdateEnabled
                    if (autoUpdateMode) {
                        startDownload()
                    }
                }
                UpdateChecker.Result.UpToDate -> _uiState.value = UpdateUiState.UpToDate
                UpdateChecker.Result.Unavailable -> _uiState.value = UpdateUiState.Unavailable
            }
            appPreferencesRepository.setLastUpdateCheck(System.currentTimeMillis())
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

    private companion object {
        const val RELEASES_URL = "https://github.com/Alaa91H/Muslim/releases"
    }

    /** Enqueues the release APK into Android's DownloadManager. */
    fun startDownload() {
        val state = _uiState.value as? UpdateUiState.Available ?: return
        val url = state.release.apkUrl ?: run {
            _downloadState.value = UpdateDownloadState.Failed
            return
        }
        val manager = context.getSystemService(DownloadManager::class.java)
        val fileName = "Muslim-${state.release.version}.apk"
        val dir = File(context.getExternalFilesDir(null), "updates")
        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription(state.release.name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(File(dir, fileName)))
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        downloadId = manager.enqueue(request)
        _downloadState.value = UpdateDownloadState.Downloading
    }

    /**
     * Installs the downloaded APK. In fully-automatic mode (the user enabled
     * auto-update and confirmed once) this goes through the PackageInstaller
     * Session API — no system UI, no further prompts. Otherwise the APK is
     * opened with the system package installer as before.
     */
    private fun installApk() {
        val state = _uiState.value as? UpdateUiState.Available ?: return
        val dir = File(context.getExternalFilesDir(null), "updates")
        val file = File(dir, "Muslim-${state.release.version}.apk")
        if (!file.exists()) {
            _downloadState.value = UpdateDownloadState.Failed
            return
        }
        if (autoUpdateMode && installViaSession(file)) {
            _downloadState.value = UpdateDownloadState.Downloaded
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                _downloadState.value = UpdateDownloadState.Failed
                openInstallSettings()
            }
    }

    /**
     * Commits the APK through the PackageInstaller Session API (no installer
     * UI, no extra prompt). Returns true when the session was committed.
     */
    private fun installViaSession(file: File): Boolean = runCatching {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            .apply {
                setAppPackageName(context.packageName)
                setAppLabel(context.getString(org.muslim.app.feature.settings.R.string.app_name))
            }
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)
        FileInputStream(file).use { input ->
            session.openWrite("update", 0, file.length()).use { output ->
                input.copyTo(output)
                session.fsync(output)
            }
        }
        val statusReceiver = PendingIntent.getActivity(
            context,
            0,
            Intent(),
            PendingIntent.FLAG_IMMUTABLE,
        )
        session.commit(statusReceiver.intentSender)
        true
    }.getOrDefault(false)

    /** Fallback when the installer cannot be opened directly: system settings. */
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

    override fun onCleared() {
        runCatching { context.unregisterReceiver(receiver) }
    }
}
