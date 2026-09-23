package org.muslim.app.feature.settings.update

/**
 * Process-independent state for the current app-update download.
 *
 * DownloadManager owns the actual transfer while only its durable id/version/file
 * metadata is persisted by the app. This lets the UI recover after process death
 * instead of depending on an Activity/ViewModel receiver.
 */
sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data object Enqueuing : UpdateDownloadState

    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int?,
    ) : UpdateDownloadState

    data class Paused(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int?,
    ) : UpdateDownloadState

    data object Verifying : UpdateDownloadState

    data class ReadyToInstall(
        val version: String,
    ) : UpdateDownloadState

    data class Failed(
        val reason: UpdateDownloadFailure,
    ) : UpdateDownloadState
}

enum class UpdateDownloadFailure {
    DownloadFailed,
    MissingFile,
    InvalidPackage,
    WrongPackage,
    SignatureMismatch,
    VersionMismatch,
    NotNewer,
    Unknown,
}

internal fun downloadProgress(downloadedBytes: Long, totalBytes: Long): Int? {
    if (downloadedBytes < 0L || totalBytes <= 0L) return null
    return ((downloadedBytes * 100L) / totalBytes)
        .coerceIn(0L, 100L)
        .toInt()
}
