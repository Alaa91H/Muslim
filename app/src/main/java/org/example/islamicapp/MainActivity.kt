package org.example.islamicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.example.islamicapp.core.notifications.NotificationChannels
import org.example.islamicapp.feature.prayertimes.data.PrayerSettingsRepository
import org.example.islamicapp.feature.prayertimes.notifications.AdhanScheduler
import org.example.islamicapp.feature.prayertimes.widget.refreshPrayerTimesWidgets
import org.example.islamicapp.ui.ManaraApp
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
    lateinit var adhanScheduler: AdhanScheduler

    /** Tab requested by an App Shortcut (`manara://times` etc.), else home. */
    private val targetRoute = MutableStateFlow(ROUTE_HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationChannels.create(this)
        requestNotificationPermission()
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            adhanScheduler.schedule(settings)
        }
        // Every app open refreshes the widget (fresh countdown for the user).
        lifecycleScope.launch {
            refreshPrayerTimesWidgets(applicationContext)
        }

        targetRoute.value = routeFromIntent(intent)
        setContent {
            val route by targetRoute.collectAsStateWithLifecycle()
            ManaraApp(initialRoute = route)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetRoute.value = routeFromIntent(intent)
    }

    /** Maps a `manara://<tab>` deep link (App Shortcuts) to a navigation route. */
    private fun routeFromIntent(intent: Intent?): String {
        val data = intent?.data?.toString().orEmpty()
        return when {
            data.startsWith("manara://times") -> ROUTE_TIMES
            data.startsWith("manara://qibla") -> ROUTE_QIBLA
            data.startsWith("manara://settings") -> ROUTE_SETTINGS
            else -> ROUTE_HOME
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    private companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_TIMES = "times"
        const val ROUTE_QIBLA = "qibla"
        const val ROUTE_SETTINGS = "settings"
    }
}
