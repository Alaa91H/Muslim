package org.muslim.app.feature.settings.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.first
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import java.io.File

/**
 * Durable update-download coordinator backed by Android DownloadManager.
 *
 * The DownloadManager id + release metadata are persisted in DataStore, so
 * transfers and their UI state survive Activity recreation and process death.
 * APKs stay inside the app-specific external-files directory: DownloadManager
 * can write them without storage permission and FileProvider can expose only
 * the verified update to Android's package installer.
 */
internal class UpdateDownloadManager(
    private val context: Context,
    private val preferencesRepository: AppPreferencesRepository,
) {
    private val downloadManager: DownloadManager
        get() = context.getSystemService(DownloadManager::class.java)

    private val verifier = UpdateApkVerifier(context)

    suspend fun enqueue(
        release: ReleaseInfo,
        wifiOnly: Boolean,
    ): UpdateDownloadState {
        val url = release.apkUrl ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.DownloadFailed)
        val currentPrefs = preferencesRepository.preferences.first()

        if (
            currentPrefs.updateDownloadId > 0L &&
            currentPrefs.updateDownloadVersion == release.version
        ) {
            when (val current = state(currentPrefs)) {
                is UpdateDownloadState.Downloading,
                is UpdateDownloadState.Paused,
                is UpdateDownloadState.ReadyToInstall -> return current
                else -> Unit
            }
        }

        clearPrevious(currentPrefs)

        val directory = updatesDirectory()
            ?.apply { mkdirs() }
            ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.DownloadFailed)
        val fileName = "Muslim-${safeVersion(release.version)}.apk"
        val destination = File(directory, fileName).apply {
            if (exists()) delete()
        }

        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription(release.name.ifBlank { "Muslim ${release.version}" })
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(destination))
            .setMimeType(APK_MIME)
            .setAllowedOverMetered(!wifiOnly)
            .setAllowedOverRoaming(false)

        if (wifiOnly) {
            @Suppress("DEPRECATION")
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        }

        val id = runCatching { downloadManager.enqueue(request) }
            .getOrElse { return UpdateDownloadState.Failed(UpdateDownloadFailure.DownloadFailed) }

        preferencesRepository.setUpdateDownload(
            id = id,
            version = release.version,
            fileName = fileName,
            sha256 = release.apkSha256,
            versionCode = release.versionCode,
        )
        return UpdateDownloadState.Downloading(
            downloadedBytes = 0L,
            totalBytes = release.apkSizeBytes,
            progressPercent = downloadProgress(0L, release.apkSizeBytes),
        )
    }

    suspend fun currentState(): UpdateDownloadState =
        runCatching { state(preferencesRepository.preferences.first()) }
            .getOrElse { UpdateDownloadState.Failed(UpdateDownloadFailure.Unknown) }

    suspend fun currentFile(): File? {
        val prefs = preferencesRepository.preferences.first()
        if (prefs.updateDownloadFileName.isBlank()) return null
        val directory = updatesDirectory() ?: return null
        return File(directory, prefs.updateDownloadFileName)
    }

    suspend fun verifyCurrent(): UpdateDownloadState {
        val prefs = preferencesRepository.preferences.first()
        val directory = updatesDirectory()
            ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)
        val file = prefs.updateDownloadFileName
            .takeIf(String::isNotBlank)
            ?.let { File(directory, it) }
            ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)

        val failure = verify(file, prefs)
        return if (failure == null) {
            UpdateDownloadState.ReadyToInstall(prefs.updateDownloadVersion)
        } else {
            UpdateDownloadState.Failed(failure)
        }
    }

    private fun state(prefs: AppPreferences): UpdateDownloadState {
        if (prefs.updateDownloadId <= 0L || prefs.updateDownloadFileName.isBlank()) {
            return UpdateDownloadState.Idle
        }

        val directory = updatesDirectory()
            ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)
        val query = DownloadManager.Query().setFilterById(prefs.updateDownloadId)
        downloadManager.query(query).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                )
                val total = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                )
                val progress = downloadProgress(downloaded, total)

                return when (status) {
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING ->
                        UpdateDownloadState.Downloading(downloaded, total, progress)

                    DownloadManager.STATUS_PAUSED ->
                        UpdateDownloadState.Paused(downloaded, total, progress)

                    DownloadManager.STATUS_FAILED ->
                        UpdateDownloadState.Failed(UpdateDownloadFailure.DownloadFailed)

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val file = File(directory, prefs.updateDownloadFileName)
                        val failure = verify(file, prefs)
                        if (failure == null) {
                            UpdateDownloadState.ReadyToInstall(prefs.updateDownloadVersion)
                        } else {
                            UpdateDownloadState.Failed(failure)
                        }
                    }

                    else -> UpdateDownloadState.Failed(UpdateDownloadFailure.Unknown)
                }
            }
        }

        // DownloadManager records can be pruned independently. A completed file
        // is still usable only after the same package/signature verification.
        val file = File(directory, prefs.updateDownloadFileName)
        if (file.exists()) {
            val failure = verify(file, prefs)
            return if (failure == null) {
                UpdateDownloadState.ReadyToInstall(prefs.updateDownloadVersion)
            } else {
                UpdateDownloadState.Failed(failure)
            }
        }
        return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)
    }

    private fun verify(file: File, prefs: AppPreferences): UpdateDownloadFailure? =
        verifier.verify(
            file = file,
            expectedVersion = prefs.updateDownloadVersion,
            expectedSha256 = prefs.updateDownloadSha256.takeIf(String::isNotBlank),
            expectedVersionCode = prefs.updateDownloadVersionCode.takeIf { it > 0L },
        )

    private suspend fun clearPrevious(prefs: AppPreferences) {
        if (prefs.updateDownloadId > 0L) {
            runCatching { downloadManager.remove(prefs.updateDownloadId) }
        }
        if (prefs.updateDownloadFileName.isNotBlank()) {
            updatesDirectory()?.let { directory ->
                runCatching { File(directory, prefs.updateDownloadFileName).delete() }
            }
        }
        preferencesRepository.clearUpdateDownload()
    }

    private fun updatesDirectory(): File? =
        context.getExternalFilesDir(null)?.let { baseDirectory ->
            File(baseDirectory, UPDATES_DIR)
        }

    private fun safeVersion(version: String): String =
        version.trim().ifBlank { "update" }
            .map { char ->
                if (char.isLetterOrDigit() || char == '.' || char == '-' || char == '_') char else '_'
            }
            .joinToString(separator = "")

    private companion object {
        const val UPDATES_DIR = "updates"
        const val APK_MIME = "application/vnd.android.package-archive"
    }
}
