package org.muslim.app.feature.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionStatus
import org.muslim.app.feature.settings.R

/**
 * Unified permissions manager (PROJECT_PROMPT.md §3.3): every permission the
 * app uses with its live status, one-tap runtime requests, system-settings
 * deep links for special app access, and a summary banner. Status refreshes
 * whenever the screen resumes so grants made in system dialogs appear at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    // Re-read all statuses on every resume (system dialog results land here).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val multipleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.refresh() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.permissions_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                PermissionSummaryCard(
                    granted = summary.grantedCount,
                    applicable = summary.applicableCount,
                )
            }
            items(AppPermission.entries) { permission ->
                PermissionRow(
                    permission = permission,
                    status = statuses[permission] ?: PermissionStatus.NotApplicable,
                    onRequest = {
                        viewModel.runtimeRequest(permission)?.let { multipleLauncher.launch(it) }
                    },
                    onOpenSettings = { viewModel.openSystemSettings(permission) },
                    onRationale = activity?.let { activity ->
                        { viewModel.shouldShowRationale(activity, permission) }
                    },
                )
            }
        }
    }
}

@Composable
private fun PermissionSummaryCard(granted: Int, applicable: Int) {
    val allGranted = applicable > 0 && granted == applicable
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (allGranted) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (allGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = pluralStringResource(R.plurals.permissions_summary, granted, granted, applicable),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        if (allGranted) R.string.permissions_all_granted
                        else R.string.permissions_missing_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    permission: AppPermission,
    status: PermissionStatus,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onRationale: (() -> Boolean)?,
) {
    val statusColor = when (status) {
        PermissionStatus.Granted -> MaterialTheme.colorScheme.primary
        PermissionStatus.NotApplicable -> MaterialTheme.colorScheme.outline
        PermissionStatus.Denied -> MaterialTheme.colorScheme.error
        PermissionStatus.SpecialAccessRequired -> MaterialTheme.colorScheme.error
    }
    ListItem(
        headlineContent = { Text(stringResource(permission.labelRes)) },
        supportingContent = {
            Column {
                Text(stringResource(permission.descriptionRes))
                Spacer(Modifier.height(2.dp))
                Text(
                    text = statusLabel(status),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = permissionIcon(permission),
                contentDescription = null,
                tint = statusColor,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (status) {
                    PermissionStatus.Granted, PermissionStatus.NotApplicable -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    PermissionStatus.Denied -> {
                        val permanentlyDenied = onRationale?.invoke() == false
                        if (permanentlyDenied) {
                            TextButton(onClick = onOpenSettings) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.permissions_open_settings))
                            }
                        } else {
                            Button(onClick = onRequest) {
                                Text(stringResource(R.string.permissions_grant))
                            }
                        }
                    }
                    PermissionStatus.SpecialAccessRequired -> {
                        TextButton(onClick = onOpenSettings) {
                            Icon(
                                Icons.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.permissions_enable))
                        }
                    }
                }
            }
        },
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun statusLabel(status: PermissionStatus): String = when (status) {
    PermissionStatus.Granted -> stringResource(R.string.permissions_granted)
    PermissionStatus.Denied -> stringResource(R.string.permissions_denied)
    PermissionStatus.SpecialAccessRequired -> stringResource(R.string.permissions_required)
    PermissionStatus.NotApplicable -> stringResource(R.string.permissions_not_applicable)
}

@Composable
private fun permissionIcon(permission: AppPermission): ImageVector = when (permission) {
    AppPermission.Notifications -> Icons.Filled.NotificationsActive
    AppPermission.Location -> Icons.Filled.GpsFixed
    AppPermission.ExactAlarms -> Icons.Filled.Alarm
    AppPermission.Overlay -> Icons.Filled.Circle
    AppPermission.NotificationPolicy -> Icons.Filled.Lock
    AppPermission.BootCompleted -> Icons.Filled.Power
    AppPermission.Vibrate -> Icons.Filled.Vibration
}
