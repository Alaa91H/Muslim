package org.muslim.app.feature.settings.update

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed

/**
 * Single entry point the update worker and the in-app "check now" button both
 * use: reads the installed version, fetches the latest release, and — when a
 * newer version exists — posts the update-available notification (respecting
 * the unified notification manager's AppUpdate category).
 */
class UpdateChecker(private val context: Context) {

    sealed interface Result {
        data class UpdateAvailable(val release: ReleaseInfo) : Result
        data object UpToDate : Result
        data object Unavailable : Result
    }

    /** Fetches the latest release and compares it with the installed version. */
    suspend fun check(): Result {
        val release = client().latestRelease() ?: return Result.Unavailable
        val installed = installedVersion()
        return if (VersionCompare.isNewer(release.version, installed)) {
            Result.UpdateAvailable(release)
        } else {
            Result.UpToDate
        }
    }

    /** Runs [check] and posts the notification when an update is available. */
    suspend fun checkAndNotify(): Result {
        val result = check()
        if (result is Result.UpdateAvailable) {
            notifier().show(result.release)
        }
        return result
    }

    fun installedVersion(): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")

    private fun client(): GithubReleasesClient =
        EntryPointAccessors.fromApplication(context, UpdateEntryPoint::class.java).releasesClient()

    private fun notifier(): UpdateCheckNotifier = UpdateCheckNotifier(context)

    /** Whether the unified notification manager allows the app-update category. */
    suspend fun categoryAllowed(): Boolean =
        context.notificationAllowed(NotificationCategory.AppUpdate)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateEntryPoint {
        fun releasesClient(): GithubReleasesClient
    }
}
