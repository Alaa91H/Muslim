package org.muslim.app.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationCategoryPrefs
import org.muslim.app.core.notifications.NotificationImportance
import org.muslim.app.core.notifications.QuietHours
import org.muslim.app.feature.settings.R
import java.time.LocalTime

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
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    val quietHours by viewModel.quietHours.collectAsStateWithLifecycle()
    val showMissedAdhan by viewModel.showMissedAdhan.collectAsStateWithLifecycle()
    val dailyHadithTimeMinutes by viewModel.dailyHadithTimeMinutes.collectAsStateWithLifecycle()
    val countdownPreview by viewModel.countdownPreview.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(viewModel.notificationPermissionGranted()) }
    var channelStatuses by remember { mutableStateOf(viewModel.channelStatuses()) }

    // Refresh the permission flag and every channel's system status whenever
    // the screen is resumed, so a change made in the system settings is
    // reflected immediately.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted.value = viewModel.notificationPermissionGranted()
                channelStatuses = viewModel.channelStatuses()
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
    var showHadithTimePicker by remember { mutableStateOf(false) }

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
                    use24h = use24h,
                    quietHours = quietHours,
                    onToggle = viewModel::setQuietHoursEnabled,
                    onStartClick = { showStartPicker = true },
                    onEndClick = { showEndPicker = true },
                )
            }
            items(NotificationCategory.entries) { category ->
                NotificationCategoryCard(
                    use24h = use24h,
                    category = category,
                    prefs = preferences[category] ?: category.defaultPrefs(),
                    actions = NotificationCategoryActions(
                        onToggleEnabled = { viewModel.setEnabled(category, it) },
                        onToggleSound = { viewModel.setSoundEnabled(category, it) },
                        onToggleVibrate = { viewModel.setVibrateEnabled(category, it) },
                        onSelectImportance = { viewModel.setImportance(category, it) },
                        onToggleBadge = { viewModel.setBadgeEnabled(category, it) },
                        onTest = { viewModel.testNotification(category) },
                        onSystemSettings = viewModel::openSystemNotificationSettings,
                        onToggleShowMissedAdhan = viewModel::setShowMissedAdhan,
                        onOpenChannelSettings = { viewModel.openSystemChannelSettings(category) },
                    ),
                    details = NotificationCategoryDetails(
                        showMissedAdhan = showMissedAdhan,
                        dailyHadithTime = dailyHadithTimeMinutes,
                        onDailyHadithTimeClick = { showHadithTimePicker = true },
                        channelStatus = channelStatuses[category] ?: SystemChannelStatus.NotCreated,
                        countdownPreview = countdownPreview,
                    ),
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
    if (showHadithTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.notif_hadith_time),
            initialMinutes = dailyHadithTimeMinutes,
            onConfirm = {
                showHadithTimePicker = false
                viewModel.setDailyHadithTimeMinutes(it)
            },
            onDismiss = { showHadithTimePicker = false },
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
    use24h: Boolean,
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
                                timeLabel(quietHours.startMinutes, use24h),
                        )
                    }
                    OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.notif_quiet_end) + "  " +
                                timeLabel(quietHours.endMinutes, use24h),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/** Formats a countdown in HH:MM:SS (Western digits). */
private fun formatCountdown(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val sec = totalSeconds % 60
    return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", h, m, sec)
}

/** Formats a prayer time as HH:MM (Western digits, locale-independent). */
private fun formatPreviewTime(time: LocalTime, use24h: Boolean): String =
    org.muslim.app.core.common.time.TimeFormats.formatTime(time, use24h)

private fun prayerNameRes(prayer: Prayer): Int = when (prayer) {
    Prayer.Fajr -> R.string.notif_preview_prayer_fajr
    Prayer.Sunrise -> R.string.notif_preview_prayer_sunrise
    Prayer.Dhuhr -> R.string.notif_preview_prayer_dhuhr
    Prayer.Asr -> R.string.notif_preview_prayer_asr
    Prayer.Maghrib -> R.string.notif_preview_prayer_maghrib
    Prayer.Isha -> R.string.notif_preview_prayer_isha
}

/**
 * Live preview of the permanent next-adhan countdown notification, mirroring
 * [org.muslim.app.feature.prayertimes.notifications.NextAdhanNotifications]:
 * the real next prayer with its time and one concise status line. Remaining
 * time and an optional missed prayer share the attention colour. Dims when the
 * category is disabled.
 */
@Composable
private fun CountdownNotificationPreview(
    preview: CountdownPreview,
    enabled: Boolean,
    showMissed: Boolean,
    use24h: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.notif_countdown_preview_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        CountdownPreviewCard(
            preview = preview,
            enabled = enabled,
            showMissed = showMissed,
            use24h = use24h,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.notif_countdown_preview_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CountdownPreviewCard(
    preview: CountdownPreview,
    enabled: Boolean,
    showMissed: Boolean,
    use24h: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            if (!preview.hasLocation || preview.nextPrayer == null) {
                Text(
                    text = stringResource(R.string.notif_preview_no_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                CountdownPreviewDetails(
                    preview = preview,
                    showMissed = showMissed,
                    use24h = use24h,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CountdownPreviewDetails(
    preview: CountdownPreview,
    showMissed: Boolean,
    use24h: Boolean,
    modifier: Modifier = Modifier,
) {
    val nextPrayer = preview.nextPrayer ?: return
    val nextTime = formatPreviewTime(preview.nextPrayerAt ?: LocalTime.MIDNIGHT, use24h)
    val nextTitle = stringResource(
        R.string.notif_preview_next_title,
        stringResource(prayerNameRes(nextPrayer)),
        nextTime,
    )
    val timeStart = nextTitle.lastIndexOf(nextTime).coerceAtLeast(0)
    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append(nextTitle.substring(0, timeStart))
                withStyle(SpanStyle(color = Color(0xFF1B5E20))) { append(nextTime) }
                append(nextTitle.substring((timeStart + nextTime.length).coerceAtMost(nextTitle.length)))
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = countdownPreviewStatusLine(preview, showMissed, use24h),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE53935),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun countdownPreviewStatusLine(
    preview: CountdownPreview,
    showMissed: Boolean,
    use24h: Boolean,
): String = buildString {
    append(stringResource(R.string.notif_preview_remaining, formatCountdown(preview.remainingSeconds)))
    if (showMissed && preview.missedPrayer != null && preview.missedPrayerAt != null) {
        append(" · ")
        append(
            stringResource(
                R.string.notif_preview_missed,
                stringResource(prayerNameRes(preview.missedPrayer)),
                formatPreviewTime(preview.missedPrayerAt, use24h),
            ),
        )
    }
}

private data class NotificationCategoryActions(
    val onToggleEnabled: (Boolean) -> Unit,
    val onToggleSound: (Boolean) -> Unit,
    val onToggleVibrate: (Boolean) -> Unit,
    val onSelectImportance: (NotificationImportance) -> Unit,
    val onToggleBadge: (Boolean) -> Unit,
    val onTest: () -> Unit,
    val onSystemSettings: () -> Unit,
    val onToggleShowMissedAdhan: (Boolean) -> Unit,
    val onOpenChannelSettings: () -> Unit,
)

private data class NotificationCategoryDetails(
    val showMissedAdhan: Boolean,
    val dailyHadithTime: Int,
    val onDailyHadithTimeClick: () -> Unit,
    val channelStatus: SystemChannelStatus,
    val countdownPreview: CountdownPreview?,
)

@Composable
private fun NotificationCategoryCard(
    use24h: Boolean,
    category: NotificationCategory,
    prefs: NotificationCategoryPrefs,
    actions: NotificationCategoryActions,
    details: NotificationCategoryDetails,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column {
            NotificationCategorySummary(
                category = category,
                enabled = prefs.enabled,
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                onEnabledChange = actions.onToggleEnabled,
            )
            AnimatedVisibility(visible = expanded) {
                NotificationCategoryExpandedDetails(
                    use24h = use24h,
                    category = category,
                    prefs = prefs,
                    actions = actions,
                    details = details,
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun NotificationCategorySummary(
    category: NotificationCategory,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(categoryLabelRes(category))) },
        supportingContent = { Text(stringResource(categoryDescriptionRes(category))) },
        leadingContent = { Icon(categoryIcon(category), contentDescription = null) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = stringResource(R.string.notif_details),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
        },
        modifier = Modifier.clickable(onClick = onExpandedChange),
    )
}

@Composable
private fun NotificationCategoryExpandedDetails(
    use24h: Boolean,
    category: NotificationCategory,
    prefs: NotificationCategoryPrefs,
    actions: NotificationCategoryActions,
    details: NotificationCategoryDetails,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        SystemChannelStatusRow(
            status = details.channelStatus,
            onOpenSettings = actions.onOpenChannelSettings,
        )
        NotificationToggleRows(prefs = prefs, actions = actions)
        NotificationCategorySpecificOptions(
            use24h = use24h,
            category = category,
            enabled = prefs.enabled,
            actions = actions,
            details = details,
        )
        NotificationImportanceSelector(
            selected = prefs.importance,
            onSelect = actions.onSelectImportance,
        )
        NotificationCategoryActionButtons(
            onTest = actions.onTest,
            onSystemSettings = actions.onSystemSettings,
        )
    }
}

@Composable
private fun NotificationToggleRows(
    prefs: NotificationCategoryPrefs,
    actions: NotificationCategoryActions,
) {
    SettingRow(
        label = stringResource(R.string.notif_sound),
        checked = prefs.soundEnabled,
        onCheckedChange = actions.onToggleSound,
    )
    SettingRow(
        label = stringResource(R.string.notif_vibration),
        checked = prefs.vibrateEnabled,
        onCheckedChange = actions.onToggleVibrate,
    )
    SettingRow(
        label = stringResource(R.string.notif_badge),
        checked = prefs.badgeEnabled,
        onCheckedChange = actions.onToggleBadge,
    )
}

@Composable
private fun NotificationCategorySpecificOptions(
    use24h: Boolean,
    category: NotificationCategory,
    enabled: Boolean,
    actions: NotificationCategoryActions,
    details: NotificationCategoryDetails,
) {
    if (category == NotificationCategory.PrayerCountdown) {
        SettingRow(
            label = stringResource(R.string.notif_show_missed_adhan),
            checked = details.showMissedAdhan,
            onCheckedChange = actions.onToggleShowMissedAdhan,
        )
        details.countdownPreview?.let { preview ->
            CountdownNotificationPreview(
                use24h = use24h,
                preview = preview,
                enabled = enabled,
                showMissed = details.showMissedAdhan,
            )
        }
    }
    if (category == NotificationCategory.HadithDaily) {
        HadithTimeRow(
            use24h = use24h,
            minutes = details.dailyHadithTime,
            onClick = details.onDailyHadithTimeClick,
        )
    }
}

@Composable
private fun NotificationImportanceSelector(
    selected: NotificationImportance,
    onSelect: (NotificationImportance) -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.notif_importance),
        style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NotificationImportance.entries.forEach { importance ->
            FilterChip(
                selected = selected == importance,
                onClick = { onSelect(importance) },
                label = { Text(stringResource(importanceLabelRes(importance))) },
            )
        }
    }
}

@Composable
private fun NotificationCategoryActionButtons(
    onTest: () -> Unit,
    onSystemSettings: () -> Unit,
) {
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



@Composable
private fun HadithTimeRow(
    use24h: Boolean,
    minutes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.notif_hadith_time),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(onClick = onClick) {
            Text(timeLabel(minutes, use24h))
        }
    }
}

@Composable
private fun SystemChannelStatusRow(
    status: SystemChannelStatus,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.notif_system_channel_status),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(systemChannelStatusLabelRes(status)),
                style = MaterialTheme.typography.bodyLarge,
                color = systemChannelStatusColor(status),
            )
        }
        TextButton(onClick = onOpenSettings) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.notif_open_channel_settings))
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun systemChannelStatusColor(status: SystemChannelStatus): Color = when (status) {
    SystemChannelStatus.Allowed -> Color(0xFF2E7D32)
    SystemChannelStatus.Blocked -> MaterialTheme.colorScheme.error
    SystemChannelStatus.NotCreated -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun systemChannelStatusLabelRes(status: SystemChannelStatus): Int = when (status) {
    SystemChannelStatus.Allowed -> R.string.notif_channel_allowed
    SystemChannelStatus.Blocked -> R.string.notif_channel_blocked
    SystemChannelStatus.NotCreated -> R.string.notif_channel_not_created
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

private fun timeLabel(minutes: Int, use24h: Boolean): String =
    org.muslim.app.core.common.time.TimeFormats.formatMinutes(minutes, use24h)

@Composable
private fun categoryIcon(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.Adhan -> Icons.Filled.NotificationsActive
    NotificationCategory.PrayerReminder -> Icons.Filled.Alarm
    NotificationCategory.QuranDaily -> Icons.AutoMirrored.Filled.MenuBook
    NotificationCategory.Ramadan -> Icons.Filled.Nightlight
    NotificationCategory.Adhkar -> Icons.Filled.AutoAwesome
    NotificationCategory.HadithDaily -> Icons.Filled.Book
    NotificationCategory.PrayerCountdown -> Icons.Filled.Timer
    NotificationCategory.Recitation -> Icons.AutoMirrored.Filled.PlaylistPlay
    NotificationCategory.Hajj -> Icons.Filled.LocationCity
    NotificationCategory.Family -> Icons.Filled.Groups
    NotificationCategory.Finance -> Icons.Filled.AccountBalanceWallet
    NotificationCategory.AppUpdate -> Icons.Filled.SystemUpdate
}

@Composable
private fun categoryLabelRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> R.string.notif_category_adhan
    NotificationCategory.PrayerReminder -> R.string.notif_category_reminder
    NotificationCategory.QuranDaily -> R.string.notif_category_quran
    NotificationCategory.Ramadan -> R.string.notif_category_ramadan
    NotificationCategory.Adhkar -> R.string.notif_category_adhkar
    NotificationCategory.HadithDaily -> R.string.notif_category_hadith
    NotificationCategory.PrayerCountdown -> R.string.notif_category_prayer_countdown
    NotificationCategory.Recitation -> R.string.notif_category_recitation
    NotificationCategory.Hajj -> R.string.notif_category_hajj
    NotificationCategory.Family -> R.string.notif_category_family
    NotificationCategory.Finance -> R.string.notif_category_finance
    NotificationCategory.AppUpdate -> R.string.notif_category_app_update
}

@Composable
private fun categoryDescriptionRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> R.string.notif_category_adhan_desc
    NotificationCategory.PrayerReminder -> R.string.notif_category_reminder_desc
    NotificationCategory.QuranDaily -> R.string.notif_category_quran_desc
    NotificationCategory.Ramadan -> R.string.notif_category_ramadan_desc
    NotificationCategory.Adhkar -> R.string.notif_category_adhkar_desc
    NotificationCategory.HadithDaily -> R.string.notif_category_hadith_desc
    NotificationCategory.PrayerCountdown -> R.string.notif_category_prayer_countdown_desc
    NotificationCategory.Recitation -> R.string.notif_category_recitation_desc
    NotificationCategory.Hajj -> R.string.notif_category_hajj_desc
    NotificationCategory.Family -> R.string.notif_category_family_desc
    NotificationCategory.Finance -> R.string.notif_category_finance_desc
    NotificationCategory.AppUpdate -> R.string.notif_category_app_update_desc
}

@Composable
private fun importanceLabelRes(importance: NotificationImportance): Int = when (importance) {
    NotificationImportance.Low -> R.string.notif_importance_low
    NotificationImportance.Default -> R.string.notif_importance_default
    NotificationImportance.High -> R.string.notif_importance_high
}
