package org.muslim.app.core.permissions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Result of a permission status check. */
enum class PermissionStatus {
    /** Fully granted (or auto-granted normal permission). */
    Granted,

    /** Runtime permission not yet granted; a system dialog can be shown. */
    Denied,

    /** Special app access not yet enabled; the user must visit system settings. */
    SpecialAccessRequired,

    /** Not applicable on this device / API level (never shown as missing). */
    NotApplicable,
}

/**
 * Single source of truth for every permission the app uses (PROJECT_PROMPT.md
 * §3.3). Feature code consults this manager instead of sprinkling
 * `ContextCompat.checkSelfPermission` calls, and the unified permissions
 * screen in Settings renders its status for the whole app.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Current status of [permission] on this device. */
    fun status(permission: AppPermission): PermissionStatus {
        if (Build.VERSION.SDK_INT < permission.minSdk) return PermissionStatus.NotApplicable
        return when (permission.kind) {
            AppPermission.Kind.Normal -> PermissionStatus.Granted
            AppPermission.Kind.Runtime -> runtimeStatus(permission)
            AppPermission.Kind.SpecialAccess -> specialAccessStatus(permission)
        }
    }

    /** True when [permission] is fully usable on this device. */
    fun isGranted(permission: AppPermission): Boolean =
        status(permission) == PermissionStatus.Granted

    /**
     * The system-settings deep link for [permission], when it is a special
     * app access that cannot be granted through a runtime dialog. Returns
     * null for runtime/normal permissions and for unsupported API levels.
     */
    // Battery exemption is core to an adhan / prayer-reminder app (an
    // alarm-clock-like use case that Play policy permits).
    @SuppressLint("InlinedApi", "BatteryLife")
    fun systemSettingsIntent(permission: AppPermission): Intent? {
        if (permission.kind != AppPermission.Kind.SpecialAccess) return null
        if (Build.VERSION.SDK_INT < permission.minSdk) return null
        val packageName = context.packageName
        return when (permission) {
            AppPermission.ExactAlarms -> Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                "package:$packageName".toUri(),
            )
            AppPermission.Overlay -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri(),
            )
            AppPermission.NotificationPolicy -> Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            )
            AppPermission.BatteryOptimization -> Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "package:$packageName".toUri(),
            )
            AppPermission.NotificationListener -> Intent(
                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS,
            )
            else -> null
        }
    }

    /** The runtime permission strings to request for [permission] (Runtime only). */
    fun runtimeRequestArray(permission: AppPermission): Array<String>? {
        if (permission.kind != AppPermission.Kind.Runtime) return null
        if (Build.VERSION.SDK_INT < permission.minSdk) return null
        val primary = permission.runtimePermission ?: return null
        return if (permission.companionRuntimePermission != null) {
            arrayOf(primary, permission.companionRuntimePermission)
        } else {
            arrayOf(primary)
        }
    }

    /** Counts granted vs applicable permissions (for the summary banner). */
    fun summary(): PermissionSummary {
        val applicable = AppPermission.entries
            .filter { status(it) != PermissionStatus.NotApplicable }
        val granted = applicable.count { status(it) == PermissionStatus.Granted }
        return PermissionSummary(granted, applicable.size)
    }

    /** True when the app can still show the runtime rationale (not "don't ask again"). */
    fun canRequest(permission: AppPermission): Boolean =
        status(permission) == PermissionStatus.Denied

    private fun runtimeStatus(permission: AppPermission): PermissionStatus {
        val target = permission.runtimePermission ?: return PermissionStatus.NotApplicable
        val granted = ContextCompat.checkSelfPermission(context, target) ==
            PackageManager.PERMISSION_GRANTED
        return if (granted) PermissionStatus.Granted else PermissionStatus.Denied
    }

    private fun specialAccessStatus(permission: AppPermission): PermissionStatus {
        val enabled = when (permission) {
            AppPermission.ExactAlarms -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                } else {
                    true
                }
            }
            AppPermission.Overlay -> Settings.canDrawOverlays(context)
            AppPermission.NotificationPolicy -> {
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.isNotificationPolicyAccessGranted
            }
            AppPermission.BatteryOptimization -> {
                val powerManager = context.getSystemService(PowerManager::class.java)
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
            AppPermission.NotificationListener -> {
                NotificationManagerCompat.getEnabledListenerPackages(context)
                    .contains(context.packageName)
            }
            else -> true
        }
        return if (enabled) PermissionStatus.Granted else PermissionStatus.SpecialAccessRequired
    }
}

/** Result of [PermissionManager.summary] — granted and applicable counts. */
data class PermissionSummary(
    val grantedCount: Int,
    val applicableCount: Int,
)
