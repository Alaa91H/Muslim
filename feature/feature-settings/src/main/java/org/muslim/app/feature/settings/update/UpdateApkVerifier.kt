package org.muslim.app.feature.settings.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * Verifies a downloaded APK before the app ever hands it to Android's package
 * installer. The archive must:
 *  - parse as an APK,
 *  - target this exact package,
 *  - advertise the release version we downloaded,
 *  - have a higher versionCode than the installed build, and
 *  - be signed by the same signing identity (including certificate history).
 */
internal class UpdateApkVerifier(
    private val context: Context,
) {

    fun verify(file: File, expectedVersion: String): UpdateDownloadFailure? {
        if (!file.isFile || file.length() <= 0L) return UpdateDownloadFailure.MissingFile

        val packageManager = context.packageManager
        val archive = archivePackageInfo(packageManager, file)
            ?: return UpdateDownloadFailure.InvalidPackage
        val installed = installedPackageInfo(packageManager)
            ?: return UpdateDownloadFailure.InvalidPackage

        if (archive.packageName != context.packageName) {
            return UpdateDownloadFailure.WrongPackage
        }

        val expected = normalizeVersion(expectedVersion)
        val archiveVersion = normalizeVersion(archive.versionName.orEmpty())
        if (expected.isNotEmpty() && archiveVersion != expected) {
            return UpdateDownloadFailure.VersionMismatch
        }

        if (longVersionCode(archive) <= longVersionCode(installed)) {
            return UpdateDownloadFailure.NotNewer
        }

        val archiveSigners = signerDigests(archive)
        val installedSigners = signerDigests(installed)
        if (archiveSigners.isEmpty() || installedSigners.isEmpty()) {
            return UpdateDownloadFailure.SignatureMismatch
        }

        val signerMatches = if (hasMultipleSigners(archive) || hasMultipleSigners(installed)) {
            archiveSigners == installedSigners
        } else {
            archiveSigners.any(installedSigners::contains)
        }
        return if (signerMatches) null else UpdateDownloadFailure.SignatureMismatch
    }

    private fun archivePackageInfo(packageManager: PackageManager, file: File): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNATURES)
        }

    private fun installedPackageInfo(packageManager: PackageManager): PackageInfo? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }
        }.getOrNull()

    private fun signerDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return emptySet()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        } ?: return emptySet()

        return signatures.mapTo(linkedSetOf()) { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString(separator = "") { byte -> "%02x".format(byte) }
        }
    }

    private fun hasMultipleSigners(info: PackageInfo): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            info.signingInfo?.hasMultipleSigners() == true

    @Suppress("DEPRECATION")
    private fun longVersionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode
        else info.versionCode.toLong()

    private fun normalizeVersion(version: String): String =
        version.trim().trimStart('v')
}
