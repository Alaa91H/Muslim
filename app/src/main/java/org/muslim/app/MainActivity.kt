package org.muslim.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import javax.inject.Inject

/**
 * Single-activity entry point hosting the main navigation graph.
 *
 * Also wires app-startup concerns: notification channels, the notification
 * permission, and (re)scheduling the Adhan alarms from the persisted settings.
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
        requestNotificationPermission()
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
            data.startsWith("muslim://settings") -> ROUTE_SETTINGS
            data.startsWith("muslim://hadith") -> ROUTE_HADITH
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

    private fun requestNotificationPermission() {
        if (permissionManager.canRequest(AppPermission.Notifications)) {
            requestPermissions(
                arrayOf(AppPermission.Notifications.runtimePermission!!),
                REQUEST_NOTIFICATIONS,
            )
        }
    }

    /** Exposes [AppPreferencesRepository] before the activity is injected. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleEntryPoint {
        fun appPreferencesRepository(): AppPreferencesRepository
    }

    private companion object {
        const val REQUEST_NOTIFICATIONS = 100
        const val ROUTE_HOME = "home"
        const val ROUTE_QIBLA = "qibla"
        const val ROUTE_SETTINGS = "settings"
        const val ROUTE_HADITH = "hadith"
        const val ROUTE_LEARN = "learn"
        const val ROUTE_UPDATE = "settings/update"
        const val EXTRA_ROUTE = "org.muslim.app.extra.ROUTE"
    }
}
