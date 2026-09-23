package org.muslim.app.feature.settings.update

import android.content.Context
import android.os.Build
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.muslim.app.core.datastore.AppPreferencesRepository
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

    /** Fetches the latest release and compares it with the installed build. */
    suspend fun check(): Result {
        val release = client().latestRelease() ?: return Result.Unavailable
        val installedName = installedVersion()
        val installedCode = installedVersionCode()
        val newer = release.versionCode
            ?.takeIf { it > 0L }
            ?.let { it > installedCode }
            ?: VersionCompare.isNewer(release.version, installedName)

        return if (newer) Result.UpdateAvailable(release) else Result.UpToDate
    }

    /**
     * Runs [check] and posts at most one notification for each release version.
     * The version is persisted only after Android accepted the notification, so
     * a temporarily disabled notification permission does not permanently lose
     * the update alert.
     */
    suspend fun checkAndNotify(): Result {
        val result = check()
        if (result !is Result.UpdateAvailable || !categoryAllowed()) return result

        val preferences = prefs()
        val lastNotifiedVersion = preferences.preferences.first().lastNotifiedUpdateVersion
        if (!UpdateNotificationPolicy.shouldNotify(result.release.version, lastNotifiedVersion)) {
            return result
        }

        val posted = runCatching { notifier().show(result.release) }.getOrDefault(false)
        if (posted) {
            preferences.setLastNotifiedUpdateVersion(result.release.version)
        }
        return result
    }

    fun installedVersion(): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")

    fun installedVersionCode(): Long =
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode
            else info.versionCode.toLong()
        }.getOrDefault(0L)

    private fun client(): GithubReleasesClient =
        EntryPointAccessors.fromApplication(context, UpdateEntryPoint::class.java).releasesClient()

    private fun notifier(): UpdateCheckNotifier = UpdateCheckNotifier(context)

    private fun prefs(): AppPreferencesRepository =
        EntryPointAccessors.fromApplication(context, UpdateEntryPoint::class.java).prefs()

    /** Whether the unified notification manager allows the app-update category. */
    suspend fun categoryAllowed(): Boolean =
        context.notificationAllowed(NotificationCategory.AppUpdate)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateEntryPoint {
        fun releasesClient(): GithubReleasesClient
        fun prefs(): AppPreferencesRepository
    }
}
