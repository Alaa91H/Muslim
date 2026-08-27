package org.muslim.app.feature.prayertimes.ui.settings

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.BundledAdhanSound
import org.muslim.app.core.common.prayer.AsrMethod
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import java.time.LocalTime

@Composable
fun PrayerSettingsScreen(
    onOpenLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    val isPreviewing by viewModel.isPreviewing.collectAsStateWithLifecycle()
    val adhanReadiness by viewModel.adhanReadiness.collectAsStateWithLifecycle()

    // Keep the slider value local while it is being dragged so every prayer
    // card can show the exact percentage that will be applied immediately.
    var masterVolume by remember { mutableIntStateOf(settings.adhanVolume) }
    LaunchedEffect(settings.adhanVolume) { masterVolume = settings.adhanVolume }

    var pendingSoundPrayer by remember { mutableStateOf<Prayer?>(null) }
    var downloadPrayer by remember { mutableStateOf<Prayer?>(null) }
    var customizingPrayer by remember { mutableStateOf<Prayer?>(null) }

    // The prayer whose adhan is currently ringing as a preview. Cleared when
    // the playback ends on its own (or when the user stops it) so the card
    // icons and the bottom button always show the correct play/stop state.
    var previewingPrayer by remember { mutableStateOf<Prayer?>(null) }
    LaunchedEffect(isPreviewing) {
        if (!isPreviewing) previewingPrayer = null
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selected -> pendingSoundPrayer?.let { viewModel.setCustomSound(it, selected) } }
        pendingSoundPrayer = null
    }

    // Stop any adhan preview when leaving this screen (back or navigation).
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPreview() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        SectionHeader(stringResource(R.string.settings_method))
        MethodDropdown(
            current = settings.method,
            isAutomatic = !settings.methodChosenManually,
            onAutomatic = viewModel::setMethodAutomatic,
            onSelected = viewModel::setMethod,
        )
        val autoInfo by viewModel.autoMethodInfo.collectAsStateWithLifecycle()
        if (!settings.methodChosenManually && autoInfo != null) {
            Text(
                text = stringResource(
                    R.string.settings_method_automatic_picked,
                    stringResource(methodLabelRes(autoInfo!!.method)),
                    autoInfo!!.country,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp),
            )
        }
        if (settings.method == CalculationMethod.Custom && settings.methodChosenManually) {
            CustomAngles(settings, viewModel)
        }

        SectionHeader(stringResource(R.string.settings_asr))
        RadioRow(stringResource(R.string.settings_asr_standard), settings.asrMethod == AsrMethod.Standard) {
            viewModel.setAsrMethod(AsrMethod.Standard)
        }
        RadioRow(stringResource(R.string.settings_asr_hanafi), settings.asrMethod == AsrMethod.Hanafi) {
            viewModel.setAsrMethod(AsrMethod.Hanafi)
        }

        SectionHeader(stringResource(R.string.settings_high_lat))
        HighLatitudeDropdown(settings.highLatitudeRule) { viewModel.setHighLatitudeRule(it) }

        SectionHeader(stringResource(R.string.settings_adjustments))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Prayer.entries.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    StepperRow(
                        label = stringResource(prayerLabelRes(prayer)),
                        value = settings.adjustments[prayer],
                        onChanged = { viewModel.setAdjustment(prayer, it) },
                    )
                }
            }
        }

        SectionHeader(stringResource(R.string.settings_adhan))
        SwitchRow(stringResource(R.string.settings_adhan_enabled), settings.adhanEnabled, viewModel::setAdhanEnabled)
        AdhanReadinessCard(
            readiness = adhanReadiness,
            onVerify = viewModel::verifyAdhanReadiness,
        )

        val nextPrayer by viewModel.nextPrayerPreview.collectAsStateWithLifecycle()

        // Master volume: one level for every prayer. When enabled, the
        // per-prayer sliders inside the cards are replaced by a “follows the
        // global level” note, and this slider applies everywhere.
        SwitchRow(
            stringResource(R.string.settings_adhan_global_volume),
            settings.useGlobalAdhanVolume,
            viewModel::setUseGlobalAdhanVolume,
        )
        if (settings.useGlobalAdhanVolume) {
            Text(
                text = stringResource(R.string.settings_adhan_global_volume_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
            VolumeRow(
                volume = masterVolume,
                onChanged = { masterVolume = it },
                onLiveVolume = viewModel::setLivePreviewVolume,
                onPreview = {
                    viewModel.setAdhanVolume(masterVolume)
                    // Restart the preview so the new master level is heard
                    // exactly as it will sound at adhan time.
                    val target = nextPrayer?.first ?: Prayer.Fajr
                    if (settings.adhanEnabled) viewModel.previewAdhan(target)
                },
            )
        }

        AdhanNotificationPreview(
            use24h = use24h,
            prayer = nextPrayer?.first,
            time = nextPrayer?.second,
            enabled = settings.adhanEnabled,
        )

        ReminderNotificationPreview(
            prayer = nextPrayer?.first,
            minutesBefore = settings.reminderMinutes,
            enabled = settings.adhanEnabled && settings.reminderMinutes > 0,
        )

        ReminderDropdown(settings.reminderMinutes) { viewModel.setReminderMinutes(it) }

        SectionHeader(stringResource(R.string.settings_dnd))
        SwitchRow(stringResource(R.string.settings_dnd_toggle), settings.dndEnabled, viewModel::setDndEnabled)
        if (settings.dndEnabled) {
            DndDurationDropdown(settings.dndDurationMinutes) { viewModel.setDndDurationMinutes(it) }
            val notificationManager = LocalContext.current.getSystemService(android.app.NotificationManager::class.java)
            val policyGranted = remember(notificationManager) {
                notificationManager.isNotificationPolicyAccessGranted
            }
            if (!policyGranted) {
                Spacer(Modifier.height(4.dp))
                val settingsContext = LocalContext.current
                OutlinedButton(
                    onClick = {
                        runCatching {
                            settingsContext.startActivity(
                                Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.settings_dnd_grant))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_dnd_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionHeader(stringResource(R.string.settings_adhan_sound))
        Prayer.entries.filter { it != Prayer.Sunrise }.forEach { prayer ->
            val option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default
            val soundId = settings.bundledAdhanSounds[prayer]
                ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID
            AdhanSoundRow(
                prayer = prayer,
                option = option,
                sound = BundledAdhanSound.fromId(soundId),
                volume = settings.adhanVolumeFor(prayer),
                vibrate = settings.vibrateFor(prayer),
                globalVolume = settings.useGlobalAdhanVolume,
                globalVolumeValue = masterVolume,
                previewing = previewingPrayer == prayer,
                onPreview = {
                    if (previewingPrayer == prayer) {
                        viewModel.stopPreview()
                    } else {
                        previewingPrayer = prayer
                        viewModel.previewAdhan(prayer)
                    }
                },
                onVolumeChange = { v ->
                    viewModel.setAdhanVolume(prayer, v)
                    // If this prayer's preview is ringing, restart it so the
                    // new level is heard exactly as saved.
                    if (previewingPrayer == prayer) viewModel.previewAdhan(prayer)
                },
                onLiveVolume = viewModel::setLivePreviewVolume,
                onVibrateChange = { viewModel.setVibrateEnabled(prayer, it) },
                onCustomize = { customizingPrayer = prayer },
            )
            CustomSoundRow(
                prayer = prayer,
                customPath = settings.adhanSoundFiles[prayer],
                progress = downloadProgress[prayer],
                onPickFile = {
                    pendingSoundPrayer = prayer
                    filePicker.launch(arrayOf("audio/*", "application/ogg", "video/mp4"))
                },
                onDownload = { downloadPrayer = prayer },
                onClear = { viewModel.clearCustomSound(prayer) },
            )
        }
        customizingPrayer?.let { prayer ->
            AdhanCustomizeDialog(
                prayer = prayer,
                option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                sound = BundledAdhanSound.fromId(
                    settings.bundledAdhanSounds[prayer]
                        ?: org.muslim.app.core.common.prayer.BundledAdhanSound.DEFAULT_ID
                ),
                volume = settings.adhanVolumeFor(prayer),
                vibrate = settings.vibrateFor(prayer),
                adjustmentMinutes = settings.adjustments[prayer],
                useGlobalVolume = settings.useGlobalAdhanVolume,
                onPreview = { sound, vol -> viewModel.previewBundled(prayer, sound, vol) },
                onLiveVolume = viewModel::setLivePreviewVolume,
                onDismiss = {
                    viewModel.stopPreview()
                    customizingPrayer = null
                },
                onConfirm = { chosenOption, chosenSound, chosenVolume, chosenVibrate, chosenAdjustment, globalVolume ->
                    viewModel.saveAdhanCustomization(
                        prayer = prayer,
                        option = chosenOption,
                        bundledSound = chosenSound,
                        volume = chosenVolume,
                        vibrate = chosenVibrate,
                        adjustmentMinutes = chosenAdjustment,
                        useGlobalVolume = globalVolume,
                    )
                    viewModel.stopPreview()
                    customizingPrayer = null
                },
            )
        }
        downloadPrayer?.let { prayer ->
            DownloadSoundDialog(
                prayer = prayer,
                onDismiss = { downloadPrayer = null },
                onConfirm = { url ->
                    viewModel.downloadSound(prayer, url)
                    downloadPrayer = null
                },
            )
        }
        // Preview / stop toggle: the first tap rings the adhan exactly as it
        // is configured for the next prayer (the one that would actually ring
        // at adhan time); tapping again while it is ringing stops it.
        val previewTarget = nextPrayer?.first ?: Prayer.Fajr
        OutlinedButton(
            onClick = {
                if (previewingPrayer != null) {
                    viewModel.stopPreview()
                } else {
                    previewingPrayer = previewTarget
                    viewModel.previewAdhan(previewTarget)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Icon(
                imageVector = if (previewingPrayer != null) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (previewingPrayer != null) {
                    stringResource(R.string.settings_preview_stop)
                } else {
                    stringResource(
                        R.string.settings_preview_for_prayer,
                        stringResource(prayerLabelRes(previewTarget)),
                    )
                },
            )
        }

        // Transparent guidance, never forced (PROJECT_PROMPT.md §3.5).
        SectionHeader(stringResource(R.string.settings_battery_title))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_battery_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                BatterySettingsButton()
                ExactAlarmButton()
            }
        }

        SectionHeader(stringResource(R.string.settings_location))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenLocation),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = settings.location?.name ?: stringResource(R.string.home_select_location),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }
        }

        SectionHeader(stringResource(R.string.settings_hijri_adjustment))
        StepperRow(
            label = stringResource(R.string.settings_hijri_adjustment),
            value = settings.hijriAdjustment,
            onChanged = viewModel::setHijriAdjustment,
        )

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Live preview of the adhan notification, mirroring
 * [org.muslim.app.feature.prayertimes.notifications.AdhanNotifications.adhanNotification]:
 * small icon, title and the real next prayer name/time (computed from the saved
 * location, refreshed every minute). Dims when adhan is disabled.
 */
private data class AdhanRecoveryActions(
    val openNotificationSettings: () -> Unit,
    val openExactAlarmSettings: () -> Unit,
)

@Composable
private fun rememberAdhanRecoveryActions(): AdhanRecoveryActions {
    val context = LocalContext.current
    return AdhanRecoveryActions(
        openNotificationSettings = { openAdhanNotificationSettings(context) },
        openExactAlarmSettings = { openExactAlarmSettings(context) },
    )
}

private fun openAdhanNotificationSettings(context: Context) {
    val notificationsEnabled = context
        .getSystemService(NotificationManager::class.java)
        .areNotificationsEnabled()
    val intent = if (!notificationsEnabled) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.ADHAN)
    }
    runCatching { context.startActivity(intent) }
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData("package:${context.packageName}".toUri())
        runCatching { context.startActivity(intent) }
    }
}

@Composable
private fun AdhanReadinessCard(
    readiness: AdhanReadiness,
    onVerify: () -> Unit,
) {
    val recoveryActions = rememberAdhanRecoveryActions()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_adhan_verify_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_adhan_verify_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            AdhanReadinessStatusBanner(readiness)
            Spacer(Modifier.height(8.dp))
            AdhanReadinessItem(readiness.adhanEnabled, R.string.settings_adhan_check_enabled)
            AdhanReadinessItem(readiness.hasLocation, R.string.settings_adhan_check_location)
            AdhanReadinessItem(
                readiness.notificationsAllowed,
                R.string.settings_adhan_check_notifications,
                onResolve = recoveryActions.openNotificationSettings,
            )
            AdhanReadinessItem(
                readiness.exactAlarmsAllowed,
                R.string.settings_adhan_check_exact_alarm,
                onResolve = recoveryActions.openExactAlarmSettings,
            )
            AdhanReadinessItem(
                readiness.scheduledNotificationPosted,
                R.string.settings_adhan_check_notification_posted,
            )
            AdhanReadinessItem(readiness.scheduledAudioVerified, R.string.settings_adhan_check_sound)
            AdhanReadinessItem(readiness.alarmVolumeAudible, R.string.settings_adhan_check_alarm_volume)
            readiness.lastProbeDetail?.let { detail ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onVerify,
                enabled = !readiness.isVerifying,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (readiness.isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.settings_adhan_verify_action))
            }
        }
    }
}

@Composable
private fun AdhanReadinessStatusBanner(readiness: AdhanReadiness) {
    val statusColor = if (readiness.isReady) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = statusColor.copy(alpha = 0.12f),
        contentColor = statusColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (readiness.isReady) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (readiness.isReady) {
                        R.string.settings_adhan_ready
                    } else {
                        R.string.settings_adhan_needs_attention
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AdhanReadinessItem(
    passed: Boolean,
    @androidx.annotation.StringRes labelRes: Int,
    onResolve: (() -> Unit)? = null,
) {
    val stateText = stringResource(
        if (passed) R.string.settings_adhan_check_passed else R.string.settings_adhan_check_failed,
    )
    val stateColor = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        if (!passed && onResolve != null) {
            IconButton(
                onClick = onResolve,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(labelRes),
                    tint = stateColor,
                )
            }
        } else {
            Text(
                text = stateText,
                style = MaterialTheme.typography.labelSmall,
                color = stateColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun AdhanNotificationPreview(
    prayer: Prayer?,
    time: LocalTime?,
    enabled: Boolean,
    use24h: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.settings_adhan_preview_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.adhan_notification_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (prayer == null) {
                            stringResource(R.string.settings_adhan_preview_no_location)
                        } else {
                            stringResource(R.string.prayer_name, stringResource(prayerLabelRes(prayer)))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (time != null) {
                    Text(
                        text = time.format(TimeFormats.timeFormatter(use24h)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_adhan_preview_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Live preview of the prayer reminder notification, mirroring
 * [org.muslim.app.feature.prayertimes.notifications.AdhanNotifications.showReminder]:
 * alarm icon, title and the next prayer with the configured lead minutes.
 * Dims when the reminder is disabled (0 minutes or adhan off).
 */
@Composable
private fun ReminderNotificationPreview(
    prayer: Prayer?,
    minutesBefore: Int,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (prayer == null) {
                            stringResource(R.string.settings_adhan_preview_no_location)
                        } else {
                            stringResource(
                                R.string.reminder_message,
                                stringResource(prayerLabelRes(prayer)),
                                minutesBefore,
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_reminder_preview_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StepperRow(label: String, value: Int, onChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChanged(value - 1) }, enabled = value > -10) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = { onChanged(value + 1) }, enabled = value < 10) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun AdhanSoundRow(
    prayer: Prayer,
    option: AdhanSoundOption,
    sound: BundledAdhanSound,
    volume: Int,
    vibrate: Boolean,
    globalVolume: Boolean,
    globalVolumeValue: Int,
    previewing: Boolean,
    onPreview: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onLiveVolume: (Int) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onCustomize: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onCustomize),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Volume + vibration controls live at the top of the card so the
            // full per-prayer configuration is adjustable right here, without
            // opening the customize dialog.
            if (globalVolume) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.settings_adhan_follows_global,
                            "$globalVolumeValue%",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "$globalVolumeValue%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                LinearProgressIndicator(
                    progress = { globalVolumeValue.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                )
            } else {
                var dragVolume by remember { mutableIntStateOf(volume) }
                LaunchedEffect(volume) { dragVolume = volume }
                VolumeRow(
                    volume = dragVolume,
                    onChanged = { dragVolume = it },
                    onLiveVolume = onLiveVolume,
                    onPreview = { onVolumeChange(dragVolume) },
                )
                SwitchRow(
                    label = stringResource(R.string.settings_vibrate),
                    checked = vibrate,
                    onCheckedChange = onVibrateChange,
                )
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(prayerLabelRes(prayer)),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.settings_adhan_sound_summary,
                            stringResource(adhanOptionLabelRes(option)),
                            stringResource(bundledSoundLabelRes(sound)),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onPreview, enabled = true) {
                    Icon(
                        imageVector = if (previewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (previewing) R.string.settings_preview_stop else R.string.settings_preview,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.settings_adhan_customize),
                )
            }
        }
    }
}

/**
 * Alert dialog to fully customise one prayer's adhan: alert type, sound,
 * per-prayer volume and per-prayer vibration, with live preview.
 */
@Composable
internal fun AdhanCustomizeDialog(
    prayer: Prayer,
    option: AdhanSoundOption,
    sound: BundledAdhanSound,
    volume: Int,
    vibrate: Boolean,
    adjustmentMinutes: Int,
    useGlobalVolume: Boolean,
    onPreview: (BundledAdhanSound, Int) -> Unit,
    onLiveVolume: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (AdhanSoundOption, BundledAdhanSound, Int, Boolean, Int, Boolean) -> Unit,
) {
    var chosenOption by remember { mutableStateOf(option) }
    var chosenSound by remember { mutableStateOf(sound) }
    var chosenVolume by remember { mutableIntStateOf(volume) }
    var chosenVibrate by remember { mutableStateOf(vibrate) }
    var chosenAdjustment by remember { mutableIntStateOf(adjustmentMinutes) }
    var chosenGlobalVolume by remember { mutableStateOf(useGlobalVolume) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_adhan_customize_title, stringResource(prayerLabelRes(prayer)))) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.settings_adhan_alert_type),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                AdhanSoundOption.entries.forEach { entry ->
                    RadioRow(
                        label = stringResource(adhanOptionLabelRes(entry)),
                        selected = chosenOption == entry,
                        onClick = { chosenOption = entry },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_adjustments),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                StepperRow(
                    label = stringResource(prayerLabelRes(prayer)),
                    value = chosenAdjustment,
                    onChanged = { chosenAdjustment = it },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_adhan_sound_choice),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                BundledAdhanSound.entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                chosenSound = entry
                                onPreview(entry, chosenVolume)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = chosenSound == entry,
                            onClick = {
                                chosenSound = entry
                                onPreview(entry, chosenVolume)
                            },
                        )
                        Text(
                            text = stringResource(bundledSoundLabelRes(entry)),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onPreview(entry, chosenVolume) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.settings_listen),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    label = stringResource(R.string.settings_adhan_global_volume),
                    checked = chosenGlobalVolume,
                    onCheckedChange = { chosenGlobalVolume = it },
                )
                if (chosenGlobalVolume) {
                    Text(
                        text = stringResource(R.string.settings_adhan_global_volume_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                VolumeRow(
                    volume = chosenVolume,
                    onChanged = { chosenVolume = it },
                    // Preview instantly at the chosen level so the volume
                    // slider is audible while adjusting; also push the new
                    // level into the playing player so the change is heard
                    // without restarting the preview.
                    onPreview = { onPreview(chosenSound, chosenVolume) },
                    onLiveVolume = onLiveVolume,
                )
                SwitchRow(
                    label = stringResource(R.string.settings_vibrate),
                    checked = chosenVibrate,
                    onCheckedChange = { chosenVibrate = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        chosenOption,
                        chosenSound,
                        chosenVolume,
                        chosenVibrate,
                        chosenAdjustment,
                        chosenGlobalVolume,
                    )
                },
            ) {
                Text(stringResource(R.string.settings_adhan_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_adhan_cancel))
            }
        },
    )
}

@Composable
private fun bundledSoundLabelRes(sound: BundledAdhanSound): Int = when (sound) {
    BundledAdhanSound.Makkah -> R.string.bundled_adhan_makkah
    BundledAdhanSound.Madinah -> R.string.bundled_adhan_madinah
    BundledAdhanSound.AbdulBasit -> R.string.bundled_adhan_abdul_basit
    BundledAdhanSound.Minshawi -> R.string.bundled_adhan_minshawi
    BundledAdhanSound.Egypt -> R.string.bundled_adhan_egypt
    BundledAdhanSound.AlAqsa -> R.string.bundled_adhan_alaqsa
    BundledAdhanSound.Halab -> R.string.bundled_adhan_halab
    BundledAdhanSound.AbdulGhaffar -> R.string.bundled_adhan_abdul_ghaffar
    BundledAdhanSound.AbdulHakam -> R.string.bundled_adhan_abdul_hakam
    BundledAdhanSound.AlHussaini -> R.string.bundled_adhan_al_hussaini
    BundledAdhanSound.BakirBash -> R.string.bundled_adhan_bakir_bash
    BundledAdhanSound.Hafez -> R.string.bundled_adhan_hafez
    BundledAdhanSound.HafizMurad -> R.string.bundled_adhan_hafiz_murad
    BundledAdhanSound.Naghshbandi -> R.string.bundled_adhan_naghshbandi
    BundledAdhanSound.Saber -> R.string.bundled_adhan_saber
    BundledAdhanSound.SharifDoman -> R.string.bundled_adhan_sharif_doman
    BundledAdhanSound.YusufIslam -> R.string.bundled_adhan_yusuf_islam
}

@Composable
private fun adhanOptionLabelRes(option: AdhanSoundOption): Int = when (option) {
    AdhanSoundOption.Default -> R.string.adhan_option_default
    AdhanSoundOption.VibrateOnly -> R.string.adhan_option_vibrate
    AdhanSoundOption.Silent -> R.string.adhan_option_silent
}

@Composable
private fun VolumeRow(
    volume: Int,
    onChanged: (Int) -> Unit,
    onPreview: () -> Unit,
    onLiveVolume: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_volume),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$volume%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Slider(
        value = volume.toFloat(),
        onValueChange = {
            val v = it.toInt()
            onChanged(v)
            // Live-update the playing preview so the user hears the change
            // immediately instead of waiting for the slider release.
            onLiveVolume(v)
        },
        onValueChangeFinished = onPreview,
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BatterySettingsButton() {
    val context = LocalContext.current
    Button(onClick = {
        // Opens the system battery-optimization list for this app. No special
        // permission needed (unlike ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .setData("package:${context.packageName}".toUri())
        runCatching { context.startActivity(intent) }
    }) {
        Text(stringResource(R.string.settings_battery_open))
    }
}

/**
 * Android 12+ only: offers the one-tap system dialog to grant exact alarms
 * (the app degrades to inexact alarms otherwise). This keeps the Adhan firing
 * precisely on time even in Doze.
 */
@Composable
private fun ExactAlarmButton() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val context = LocalContext.current
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (alarmManager.canScheduleExactAlarms()) return

    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_exact_alarm_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            .setData("package:${context.packageName}".toUri()),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_exact_alarm_open))
        }
    }
}

/**
 * Only the officially recognised Sunni calculation methods are offered
 * (PROJECT_PROMPT.md: "ازل جميع الطرق غير السنية"). Non-Sunni institutes
 * (e.g. Jafari, Tehran) were deliberately removed from the app.
 */
private val SUNNI_METHODS: List<CalculationMethod> = CalculationMethod.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodDropdown(
    current: CalculationMethod,
    isAutomatic: Boolean,
    onAutomatic: () -> Unit,
    onSelected: (CalculationMethod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (isAutomatic) {
                stringResource(R.string.settings_method_automatic)
            } else {
                stringResource(methodLabelRes(current))
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings_method_automatic)) },
                onClick = {
                    expanded = false
                    onAutomatic()
                },
            )
            SUNNI_METHODS.forEach { method ->
                DropdownMenuItem(
                    text = { Text(stringResource(methodLabelRes(method))) },
                    onClick = {
                        expanded = false
                        onSelected(method)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HighLatitudeDropdown(current: HighLatitudeRule?, onSelected: (HighLatitudeRule?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(highLatLabelRes(current)),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_high_lat)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings_high_lat_auto)) },
                onClick = { expanded = false; onSelected(null) },
            )
            HighLatitudeRule.entries.forEach { rule ->
                DropdownMenuItem(
                    text = { Text(stringResource(highLatLabelRes(rule))) },
                    onClick = { expanded = false; onSelected(rule) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDropdown(current: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 5, 10, 15, 30)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (current == 0) stringResource(R.string.settings_reminder_off)
            else stringResource(R.string.settings_reminder_minutes, current),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_reminder)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (minutes == 0) stringResource(R.string.settings_reminder_off)
                            else stringResource(R.string.settings_reminder_minutes, minutes)
                        )
                    },
                    onClick = { expanded = false; onSelected(minutes) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DndDurationDropdown(current: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(10, 15, 20, 30, 45, 60)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(R.string.settings_dnd_minutes, current),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_dnd_duration)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_dnd_minutes, minutes)) },
                    onClick = { expanded = false; onSelected(minutes) },
                )
            }
        }
    }
}

@Composable
private fun CustomAngles(settings: PrayerSettings, viewModel: PrayerSettingsViewModel) {
    var fajr by remember { mutableStateOf(settings.customFajrAngle.toString()) }
    var isha by remember { mutableStateOf(settings.customIshaAngle.toString()) }
    OutlinedTextField(
        value = fajr,
        onValueChange = {
            // Normalize so Arabic-Indic/Persian digits parse without loss.
            val western = org.muslim.app.core.common.text.Digits.toWesternDigits(it)
            fajr = western
            western.toDoubleOrNull()?.let { angle -> viewModel.setCustomFajrAngle(angle) }
        },
        label = { Text(stringResource(R.string.settings_custom_fajr)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
    OutlinedTextField(
        value = isha,
        onValueChange = {
            // Normalize so Arabic-Indic/Persian digits parse without loss.
            val western = org.muslim.app.core.common.text.Digits.toWesternDigits(it)
            isha = western
            western.toDoubleOrNull()?.let { angle -> viewModel.setCustomIshaAngle(angle) }
        },
        label = { Text(stringResource(R.string.settings_custom_isha)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
}

@Composable
private fun methodLabelRes(method: CalculationMethod): Int = when (method) {
    CalculationMethod.MuslimWorldLeague -> R.string.method_mwl
    CalculationMethod.Egyptian -> R.string.method_egyptian
    CalculationMethod.Karachi -> R.string.method_karachi
    CalculationMethod.UmmAlQura -> R.string.method_umm_al_qura
    CalculationMethod.NorthAmerica -> R.string.method_isna
    CalculationMethod.Dubai -> R.string.method_dubai
    CalculationMethod.Qatar -> R.string.method_qatar
    CalculationMethod.Kuwait -> R.string.method_kuwait
    CalculationMethod.Singapore -> R.string.method_singapore
    CalculationMethod.MoonsightingCommittee -> R.string.method_moonsighting
    CalculationMethod.Turkey -> R.string.method_turkey
    CalculationMethod.France -> R.string.method_france
    CalculationMethod.Custom -> R.string.settings_custom_method
}

@Composable
private fun highLatLabelRes(rule: HighLatitudeRule?): Int = when (rule) {
    null -> R.string.settings_high_lat_auto
    HighLatitudeRule.MiddleOfTheNight -> R.string.settings_high_lat_midnight
    HighLatitudeRule.SeventhOfTheNight -> R.string.settings_high_lat_seventh
    HighLatitudeRule.TwilightAngle -> R.string.settings_high_lat_angle
}

/**
 * Per-prayer custom sound controls (PROJECT_PROMPT.md §6: مكتبة أصوات الأذان):
 * pick a local audio file, download one from a URL, or revert to the bundled tone.
 */
@Composable
private fun CustomSoundRow(
    prayer: Prayer,
    customPath: String?,
    progress: Float?,
    onPickFile: () -> Unit,
    onDownload: () -> Unit,
    onClear: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        if (progress != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_sound_downloading),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        } else if (customPath != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_sound_custom_active),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.settings_sound_remove))
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onPickFile, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_sound_pick))
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onDownload, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_sound_download))
                }
            }
        }
    }
}

@Composable
private fun DownloadSoundDialog(
    prayer: Prayer,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_sound_dialog_title, stringResource(prayerLabelRes(prayer)))) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.settings_sound_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.settings_sound_dialog_url)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(url.trim()) }, enabled = url.isNotBlank()) {
                Text(stringResource(R.string.settings_sound_dialog_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_sound_dialog_cancel))
            }
        },
    )
}
