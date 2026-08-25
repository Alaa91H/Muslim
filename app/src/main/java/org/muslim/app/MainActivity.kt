package org.muslim.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.crash.AppError
import org.muslim.app.crash.AppErrorBus
import org.muslim.app.crash.CrashLogStore
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionManager
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler
import org.muslim.app.feature.prayertimes.notifications.NextAdhanService
import org.muslim.app.feature.prayertimes.widget.refreshPrayerTimesWidgets
import org.muslim.app.feature.settings.locale.withAppLocale
import org.muslim.app.feature.settings.update.UpdateCheckScheduler
import org.muslim.app.ui.MuslimApp
import java.util.ArrayDeque
import javax.inject.Inject

/**
 * Single-activity entry point hosting the main navigation graph.
 *
 * Also wires app-startup concerns: notification channels, the first-install
 * permission flow, and (re)scheduling the Adhan alarms from persisted settings.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: PrayerSettingsRepository

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var adhanScheduler: AdhanScheduler

    @Inject
    lateinit var permissionManager: PermissionManager

    /** Tab requested by an App Shortcut (`muslim://times` etc.), else home. */
    private val targetRoute = MutableStateFlow(ROUTE_HOME)

    /** Special-access pages must be opened one at a time by Android. */
    private val initialSpecialPermissionQueue = ArrayDeque<AppPermission>()

    private val initialRuntimePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            launchNextInitialSpecialAccess()
        }

    private val initialSpecialAccessLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            launchNextInitialSpecialAccess()
        }

    /**
     * Applies the user-chosen UI language before any resource is inflated.
     * The value comes from a synchronous mirror updated on every change.
     * Hilt fields are not injected yet at this stage, so the repository is
     * resolved through an [EntryPoint] on the application graph.
     */
    override fun attachBaseContext(newBase: Context) {
        val repository = EntryPointAccessors
            .fromApplication(newBase.applicationContext, LocaleEntryPoint::class.java)
            .appPreferencesRepository()
        super.attachBaseContext(newBase.withAppLocale(repository.readLanguageSync()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Surface the persisted fatal crash (from the auto-relaunch) once.
        showPreviousCrashIfAny()

        NotificationChannels.create(this)
        requestInitialPermissionsOnFreshInstall()
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            adhanScheduler.schedule(settings)
            // Keep the permanent next-adhan countdown notification fresh.
            NextAdhanService.start(applicationContext)
            // Re-anchor the optional update check to the persisted cadence
            // (daily/weekly/monthly) — or cancel it when the user turned it off.
            val prefs = appPreferencesRepository.preferences.first()
            if (prefs.updateCheckEnabled) {
                UpdateCheckScheduler.schedule(applicationContext, prefs.updateCheckFrequency)
            } else {
                UpdateCheckScheduler.cancel(applicationContext)
            }
        }
        // Every app open refreshes the widget (fresh countdown for the user).
        lifecycleScope.launch {
            refreshPrayerTimesWidgets(applicationContext)
        }
        targetRoute.value = routeFromIntent(intent)
        setContent {
            val route by targetRoute.collectAsStateWithLifecycle()
            MuslimApp(
                initialRoute = route,
                // Read once per process: changing the start tab in Settings only
                // persists the choice; it takes effect on the next cold start.
                initialStartTab = appPreferencesRepository.readStartTabSync(),
                onLanguageChanged = ::recreate,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Exact-alarm access is granted in a system settings screen. The first
        // launch may have scheduled a degraded inexact alarm before the user
        // returned from that screen, so always replace it with the correct
        // schedule when the activity resumes.
        lifecycleScope.launch {
            adhanScheduler.schedule(settingsRepository.settings.first())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetRoute.value = routeFromIntent(intent)
    }

    /** Maps a `muslim://<tab>` deep link (App Shortcuts) to a navigation route. */
    private fun routeFromIntent(intent: Intent?): String {
        val explicitRoute = intent?.getStringExtra(EXTRA_ROUTE)
        if (explicitRoute != null) return explicitRoute
        val data = intent?.data?.toString().orEmpty()
        return when {
            // The standalone times page was merged into the home tab ("أوقات
            // الصلاة"), so the old times shortcut now opens the home tab.
            data.startsWith("muslim://times") -> ROUTE_HOME
            data.startsWith("muslim://qibla") -> ROUTE_QIBLA
            // The update-available notification opens the in-app update screen.
            data.startsWith("muslim://settings/update") -> ROUTE_UPDATE
            data.startsWith("muslim://accessibility") -> ROUTE_ACCESSIBILITY
            data.startsWith("muslim://settings") -> ROUTE_SETTINGS
            data.startsWith("muslim://hadith") -> ROUTE_HADITH
            data.startsWith("muslim://finance") -> ROUTE_FINANCE
            data.startsWith("muslim://noorani") -> ROUTE_NOORANI
            data.startsWith("muslim://traveler") -> ROUTE_TRAVELER
            data.startsWith("muslim://history") -> ROUTE_HISTORY
            data.startsWith("muslim://learn") -> ROUTE_LEARN
            else -> ROUTE_HOME
        }
    }

    /** Re-reads the persisted fatal crash and shows the elegant error dialog once. */
    private fun showPreviousCrashIfAny() {
        val report = CrashLogStore.readLatest(this) ?: return
        CrashLogStore.clear(this)
        AppErrorBus.show(AppError(detail = report, fatal = true))
    }

    /**
     * Requests the app's runtime permissions on the first open after a fresh
     * install, then opens each applicable special-access setting in turn. Android
     * does not permit special access (exact alarms, DND, overlay, battery, and
     * notification listener) to be granted from one runtime dialog.
     */
    private fun requestInitialPermissionsOnFreshInstall() {
        lifecycleScope.launch {
            if (!isFreshInstall() || !appPreferencesRepository.isInitialPermissionSetupPending()) return@launch
            // Record before showing system UI. A denial remains manageable from
            // Settings, but the app must not repeatedly interrupt later launches.
            appPreferencesRepository.markInitialPermissionSetupHandled()

            initialSpecialPermissionQueue.clear()
            AppPermission.entries
                .filter { permission ->
                    permission.kind == AppPermission.Kind.SpecialAccess && !permissionManager.isGranted(permission)
                }
                .forEach(initialSpecialPermissionQueue::addLast)

            val runtimePermissions = AppPermission.entries
                .filter { permission ->
                    permission.kind == AppPermission.Kind.Runtime && permissionManager.canRequest(permission)
                }
                .flatMap { permission -> permissionManager.runtimeRequestArray(permission)?.asList().orEmpty() }
                .distinct()

            if (runtimePermissions.isEmpty()) {
                launchNextInitialSpecialAccess()
            } else {
                initialRuntimePermissionsLauncher.launch(runtimePermissions.toTypedArray())
            }
        }
    }

    /** Opens the next system-only access page after the previous one returns. */
    private fun launchNextInitialSpecialAccess() {
        while (initialSpecialPermissionQueue.isNotEmpty()) {
            val permission = initialSpecialPermissionQueue.removeFirst()
            if (permissionManager.isGranted(permission)) continue
            val intent = permissionManager.systemSettingsIntent(permission) ?: continue
            initialSpecialAccessLauncher.launch(intent)
            return
        }
    }

    @Suppress("DEPRECATION")
    private fun isFreshInstall(): Boolean = runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    }.getOrDefault(false)

    /** Exposes [AppPreferencesRepository] before the activity is injected. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleEntryPoint {
        fun appPreferencesRepository(): AppPreferencesRepository
    }

    private companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_QIBLA = "qibla"
        const val ROUTE_SETTINGS = "settings"
        const val ROUTE_ACCESSIBILITY = "accessibility"
        const val ROUTE_HADITH = "hadith"
        const val ROUTE_FINANCE = "finance"
        const val ROUTE_NOORANI = "learn/noorani-new-muslim"
        const val ROUTE_TRAVELER = "learn/traveler-expat"
        const val ROUTE_HISTORY = "history"
        const val ROUTE_LEARN = "learn"
        const val ROUTE_UPDATE = "settings/update"
        const val EXTRA_ROUTE = "org.muslim.app.extra.ROUTE"
    }
}
