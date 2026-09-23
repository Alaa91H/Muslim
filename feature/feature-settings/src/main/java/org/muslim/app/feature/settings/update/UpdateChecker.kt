package org.muslim.app.feature.settings.update

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.notificationAllowed
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single update-check entry point shared by the background worker and UI.
 *
 * Dependencies are constructor-injected so update checks no longer perform
 * service-locator lookups through Hilt EntryPoints.
 */
@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val releasesClient: GithubReleasesClient,
    private val appPreferencesRepository: AppPreferencesRepository,
) {

    sealed interface Result {
        data class UpdateAvailable(val release: ReleaseInfo) : Result
        data object UpToDate : Result
        data object Unavailable : Result
    }

    /** Fetches the latest release for the selected channel and compares builds. */
    suspend fun check(): Result {
        val preferences = appPreferencesRepository.preferences.first()
        val release = releasesClient.latestRelease(preferences.updateChannel)
            ?: return Result.Unavailable
        val newer = ReleaseVersionPolicy.isNewer(
            release = release,
            installedVersionCode = installedVersionCode(),
            installedVersion = installedVersion(),
        )
        return if (newer) Result.UpdateAvailable(release) else Result.UpToDate
    }

    /**
     * Runs [check] and posts at most one notification for each release version.
     * The version is persisted only after Android accepted the notification, so
     * temporarily disabled notifications do not permanently lose the alert.
     */
    suspend fun checkAndNotify(): Result {
        val result = check()
        if (result !is Result.UpdateAvailable || !categoryAllowed()) return result

        val lastNotifiedVersion =
            appPreferencesRepository.preferences.first().lastNotifiedUpdateVersion
        if (!UpdateNotificationPolicy.shouldNotify(result.release.version, lastNotifiedVersion)) {
            return result
        }

        val posted = runCatching {
            UpdateCheckNotifier(context).show(result.release)
        }.getOrDefault(false)
        if (posted) {
            appPreferencesRepository.setLastNotifiedUpdateVersion(result.release.version)
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

    /** Whether the unified notification manager allows the app-update category. */
    suspend fun categoryAllowed(): Boolean =
        context.notificationAllowed(NotificationCategory.AppUpdate)
}
