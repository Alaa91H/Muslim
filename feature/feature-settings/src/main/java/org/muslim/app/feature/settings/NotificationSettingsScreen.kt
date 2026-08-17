package org.muslim.app.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationCategoryPrefs
import org.muslim.app.core.notifications.NotificationImportance
import org.muslim.app.core.notifications.QuietHours
import org.muslim.app.feature.settings.R

/**
 * Master notification manager (PROJECT_PROMPT.md §3.3): a single switch per
 * [NotificationCategory] controls every notifier in the app — prayers &
 * adhan, adhkar, Quran ayah, Ramadan, and the daily hadith — plus per-category
 * presentation (sound, vibration, importance, badge), a global quiet-hours
 * window, test notifications and direct access to the system settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val quietHours by viewModel.quietHours.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(viewModel.notificationPermissionGranted()) }

    // Refresh the permission flag whenever the screen is resumed so a grant
    // made in the system dialog is reflected immediately.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted.value = viewModel.notificationPermissionGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permissionGranted.value = viewModel.notificationPermissionGranted() }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted.value) {
                item {
                    PermissionBanner(
                        onAllow = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onOpenSystemSettings = viewModel::openSystemNotificationSettings,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.notifications_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item {
                QuietHoursCard(
                    quietHours = quietHours,
                    onToggle = viewModel::setQuietHoursEnabled,
                    onStartClick = { showStartPicker = true },
                    onEndClick = { showEndPicker = true },
                )
            }
            items(NotificationCategory.entries) { category ->
                NotificationCategoryCard(
                    category = category,
                    prefs = preferences[category] ?: category.defaultPrefs(),
                    onToggleEnabled = { viewModel.setEnabled(category, it) },
                    onToggleSound = { viewModel.setSoundEnabled(category, it) },
                    onToggleVibrate = { viewModel.setVibrateEnabled(category, it) },
                    onSelectImportance = { viewModel.setImportance(category, it) },
                    onToggleBadge = { viewModel.setBadgeEnabled(category, it) },
                    onTest = { viewModel.testNotification(category) },
                    onSystemSettings = viewModel::openSystemNotificationSettings,
                )
            }
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            title = stringResource(R.string.notif_quiet_start),
            initialMinutes = quietHours.startMinutes,
            onConfirm = {
                showStartPicker = false
                viewModel.setQuietStartMinutes(it)
            },
            onDismiss = { showStartPicker = false },
        )
    }
    if (showEndPicker) {
        TimePickerDialog(
            title = stringResource(R.string.notif_quiet_end),
            initialMinutes = quietHours.endMinutes,
            onConfirm = {
                showEndPicker = false
                viewModel.setQuietEndMinutes(it)
            },
            onDismiss = { showEndPicker = false },
        )
    }
}

@Composable
private fun PermissionBanner(
    onAllow: () -> Unit,
    onOpenSystemSettings: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.notif_permission_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.notif_permission_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onAllow) {
                    Text(stringResource(R.string.notif_permission_allow))
                }
                Spacer(Modifier.width(12.dp))
                TextButton(onClick = onOpenSystemSettings) {
                    Text(stringResource(R.string.notif_open_system_settings))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuietHoursCard(
    quietHours: QuietHours,
    onToggle: (Boolean) -> Unit,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column {
            ListItem(
                headlineContent = { Text(stringResource(R.string.notif_quiet_title)) },
                supportingContent = { Text(stringResource(R.string.notif_quiet_desc)) },
                leadingContent = { Icon(Icons.Filled.Nightlight, contentDescription = null) },
                trailingContent = {
                    Switch(checked = quietHours.enabled, onCheckedChange = onToggle)
                },
            )
            AnimatedVisibility(visible = quietHours.enabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onStartClick, modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.notif_quiet_start) + "  " +
                                timeLabel(quietHours.startMinutes),
                        )
                    }
                    OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.notif_quiet_end) + "  " +
                                timeLabel(quietHours.endMinutes),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCategoryCard(
    category: NotificationCategory,
    prefs: NotificationCategoryPrefs,
    onToggleEnabled: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onSelectImportance: (NotificationImportance) -> Unit,
    onToggleBadge: (Boolean) -> Unit,
    onTest: () -> Unit,
    onSystemSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column {
            ListItem(
                headlineContent = { Text(stringResource(categoryLabelRes(category))) },
                supportingContent = { Text(stringResource(categoryDescriptionRes(category))) },
                leadingContent = {
                    Icon(categoryIcon(category), contentDescription = null)
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = stringResource(R.string.notif_details),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = prefs.enabled, onCheckedChange = onToggleEnabled)
                    }
                },
                modifier = Modifier.clickable { expanded = !expanded },
            )
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                ) {
                    SettingRow(
                        label = stringResource(R.string.notif_sound),
                        checked = prefs.soundEnabled,
                        onCheckedChange = onToggleSound,
                    )
                    SettingRow(
                        label = stringResource(R.string.notif_vibration),
                        checked = prefs.vibrateEnabled,
                        onCheckedChange = onToggleVibrate,
                    )
                    SettingRow(
                        label = stringResource(R.string.notif_badge),
                        checked = prefs.badgeEnabled,
                        onCheckedChange = onToggleBadge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.notif_importance),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationImportance.entries.forEach { importance ->
                            FilterChip(
                                selected = prefs.importance == importance,
                                onClick = { onSelectImportance(importance) },
                                label = { Text(stringResource(importanceLabelRes(importance))) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onTest) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.notif_test))
                        }
                        TextButton(onClick = onSystemSettings) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.notif_open_system_settings))
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text(stringResource(R.string.notif_time_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notif_time_picker_cancel))
            }
        },
    )
}

private fun timeLabel(minutes: Int): String {
    val safe = ((minutes % 1440) + 1440) % 1440
    return String.format(java.util.Locale.US, "%02d:%02d", safe / 60, safe % 60)
}

@Composable
private fun categoryIcon(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.Adhan -> Icons.Filled.NotificationsActive
    NotificationCategory.PrayerReminder -> Icons.Filled.Alarm
    NotificationCategory.QuranDaily -> Icons.Filled.MenuBook
    NotificationCategory.Ramadan -> Icons.Filled.Nightlight
    NotificationCategory.Adhkar -> Icons.Filled.AutoAwesome
    NotificationCategory.HadithDaily -> Icons.Filled.Book
}

@Composable
private fun categoryLabelRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> R.string.notif_category_adhan
    NotificationCategory.PrayerReminder -> R.string.notif_category_reminder
    NotificationCategory.QuranDaily -> R.string.notif_category_quran
    NotificationCategory.Ramadan -> R.string.notif_category_ramadan
    NotificationCategory.Adhkar -> R.string.notif_category_adhkar
    NotificationCategory.HadithDaily -> R.string.notif_category_hadith
}

@Composable
private fun categoryDescriptionRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> R.string.notif_category_adhan_desc
    NotificationCategory.PrayerReminder -> R.string.notif_category_reminder_desc
    NotificationCategory.QuranDaily -> R.string.notif_category_quran_desc
    NotificationCategory.Ramadan -> R.string.notif_category_ramadan_desc
    NotificationCategory.Adhkar -> R.string.notif_category_adhkar_desc
    NotificationCategory.HadithDaily -> R.string.notif_category_hadith_desc
}

@Composable
private fun importanceLabelRes(importance: NotificationImportance): Int = when (importance) {
    NotificationImportance.Low -> R.string.notif_importance_low
    NotificationImportance.Default -> R.string.notif_importance_default
    NotificationImportance.High -> R.string.notif_importance_high
}
