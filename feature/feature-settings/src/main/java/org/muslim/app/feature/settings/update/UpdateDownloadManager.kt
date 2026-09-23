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

        val directory = updatesDirectory().apply { mkdirs() }
        val fileName = "Muslim-${safeVersion(release.version)}.apk"
        val destination = File(directory, fileName).apply {
            if (exists()) delete()
        }

        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription(release.name.ifBlank { "Muslim ${release.version}" })
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
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

        preferencesRepository.setUpdateDownload(id, release.version, fileName)
        return UpdateDownloadState.Downloading(0L, release.apkSizeBytes, 0)
    }

    suspend fun currentState(): UpdateDownloadState =
        runCatching { state(preferencesRepository.preferences.first()) }
            .getOrElse { UpdateDownloadState.Failed(UpdateDownloadFailure.Unknown) }

    suspend fun currentFile(): File? {
        val prefs = preferencesRepository.preferences.first()
        if (prefs.updateDownloadFileName.isBlank()) return null
        return File(updatesDirectory(), prefs.updateDownloadFileName)
    }

    suspend fun verifyCurrent(): UpdateDownloadState {
        val prefs = preferencesRepository.preferences.first()
        val file = prefs.updateDownloadFileName
            .takeIf(String::isNotBlank)
            ?.let { File(updatesDirectory(), it) }
            ?: return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)

        val failure = verifier.verify(file, prefs.updateDownloadVersion)
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
                        val file = File(updatesDirectory(), prefs.updateDownloadFileName)
                        val failure = verifier.verify(file, prefs.updateDownloadVersion)
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
        val file = File(updatesDirectory(), prefs.updateDownloadFileName)
        if (file.exists()) {
            val failure = verifier.verify(file, prefs.updateDownloadVersion)
            return if (failure == null) {
                UpdateDownloadState.ReadyToInstall(prefs.updateDownloadVersion)
            } else {
                UpdateDownloadState.Failed(failure)
            }
        }
        return UpdateDownloadState.Failed(UpdateDownloadFailure.MissingFile)
    }

    private suspend fun clearPrevious(prefs: AppPreferences) {
        if (prefs.updateDownloadId > 0L) {
            runCatching { downloadManager.remove(prefs.updateDownloadId) }
        }
        if (prefs.updateDownloadFileName.isNotBlank()) {
            runCatching { File(updatesDirectory(), prefs.updateDownloadFileName).delete() }
        }
        preferencesRepository.clearUpdateDownload()
    }

    private fun updatesDirectory(): File {
        val baseDirectory = context.getExternalFilesDir(null) ?: context.filesDir
        return File(baseDirectory, UPDATES_DIR)
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
