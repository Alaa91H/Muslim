package org.muslim.app.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionManager
import org.muslim.app.core.permissions.PermissionStatus
import org.muslim.app.core.permissions.PermissionSummary
import javax.inject.Inject

/**
 * Backs the unified permissions screen (PROJECT_PROMPT.md §3.3): shows the
 * current status of every [AppPermission], re-reads it on every screen resume
 * (so a grant made in a system dialog is reflected immediately), and exposes
 * the actions the UI needs — runtime requests, system-settings deep links and
 * the summary banner.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _statuses = MutableStateFlow<Map<AppPermission, PermissionStatus>>(emptyMap())
    val statuses: StateFlow<Map<AppPermission, PermissionStatus>> = _statuses.asStateFlow()

    private val _summary = MutableStateFlow(PermissionSummary(0, 0))
    val summary: StateFlow<PermissionSummary> = _summary.asStateFlow()

    init {
        refresh()
    }

    /** Re-reads every permission's status (call on resume / after a dialog). */
    fun refresh() {
        _statuses.value = AppPermission.entries.associateWith { permissionManager.status(it) }
        _summary.value = permissionManager.summary()
    }

    /** The runtime permission array to hand to a request launcher, if applicable. */
    fun runtimeRequest(permission: AppPermission): Array<String>? =
        permissionManager.runtimeRequestArray(permission)

    /** Whether the user can still be shown the runtime dialog (not "don't ask again"). */
    fun canRequest(permission: AppPermission): Boolean =
        permissionManager.canRequest(permission)

    /** Deep link to the system screen for special app access (alarms/overlay/DND). */
    fun systemSettingsIntent(permission: AppPermission): Intent? =
        permissionManager.systemSettingsIntent(permission)

    /** Opens the deep link; used for special-access permissions. */
    fun openSystemSettings(permission: AppPermission) {
        val intent = permissionManager.systemSettingsIntent(permission) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /** Returns true on API 26+ when the runtime rationale should be shown. */
    fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
        val target = permission.runtimePermission ?: return false
        return activity.shouldShowRequestPermissionRationale(target)
    }
}
