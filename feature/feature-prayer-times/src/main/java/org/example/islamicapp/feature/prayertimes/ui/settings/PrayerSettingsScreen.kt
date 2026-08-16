package org.example.islamicapp.feature.prayertimes.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.prayertimes.R
import org.example.islamicapp.core.common.prayer.AdhanSoundOption
import org.example.islamicapp.core.common.prayer.AsrMethod
import org.example.islamicapp.core.common.prayer.CalculationMethod
import org.example.islamicapp.core.common.prayer.HighLatitudeRule
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.core.datastore.prayer.PrayerSettings
import org.example.islamicapp.feature.prayertimes.ui.prayerLabelRes

@Composable
fun PrayerSettingsScreen(
    onOpenLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    var pendingSoundPrayer by remember { mutableStateOf<Prayer?>(null) }
    var downloadPrayer by remember { mutableStateOf<Prayer?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selected -> pendingSoundPrayer?.let { viewModel.setCustomSound(it, selected) } }
        pendingSoundPrayer = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        SectionHeader(stringResource(R.string.settings_method))
        MethodDropdown(settings.method) { viewModel.setMethod(it) }
        if (settings.method == CalculationMethod.Custom) {
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
        SwitchRow(stringResource(R.string.settings_vibrate), settings.vibrateEnabled, viewModel::setVibrateEnabled)
        ReminderDropdown(settings.reminderMinutes) { viewModel.setReminderMinutes(it) }

        SectionHeader(stringResource(R.string.settings_adhan_sound))
        Prayer.entries.filter { it != Prayer.Sunrise }.forEach { prayer ->
            AdhanSoundDropdown(
                prayer = prayer,
                current = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                onSelected = { viewModel.setAdhanSound(prayer, it) },
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
        VolumeRow(volume = settings.adhanVolume, onChanged = viewModel::setAdhanVolume)
        OutlinedButton(
            onClick = { viewModel.previewAdhan(Prayer.Fajr) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.settings_preview))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhanSoundDropdown(
    prayer: Prayer,
    current: AdhanSoundOption,
    onSelected: (AdhanSoundOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(adhanOptionLabelRes(current)),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(prayerLabelRes(prayer))) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AdhanSoundOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(adhanOptionLabelRes(option))) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun VolumeRow(volume: Int, onChanged: (Int) -> Unit) {
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
        onValueChange = { onChanged(it.toInt()) },
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun adhanOptionLabelRes(option: AdhanSoundOption): Int = when (option) {
    AdhanSoundOption.Default -> R.string.adhan_option_default
    AdhanSoundOption.VibrateOnly -> R.string.adhan_option_vibrate
    AdhanSoundOption.Silent -> R.string.adhan_option_silent
}

@Composable
private fun BatterySettingsButton() {
    val context = LocalContext.current
    Button(onClick = {
        // Opens the system battery-optimization list for this app. No special
        // permission needed (unlike ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))
        runCatching { context.startActivity(intent) }
    }) {
        Text(stringResource(R.string.settings_battery_open))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodDropdown(current: CalculationMethod, onSelected: (CalculationMethod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(methodLabelRes(current)),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CalculationMethod.entries.forEach { method ->
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

@Composable
private fun CustomAngles(settings: PrayerSettings, viewModel: PrayerSettingsViewModel) {
    var fajr by remember { mutableStateOf(settings.customFajrAngle.toString()) }
    var isha by remember { mutableStateOf(settings.customIshaAngle.toString()) }
    OutlinedTextField(
        value = fajr,
        onValueChange = {
            fajr = it
            it.toDoubleOrNull()?.let { angle -> viewModel.setCustomFajrAngle(angle) }
        },
        label = { Text(stringResource(R.string.settings_custom_fajr)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
    OutlinedTextField(
        value = isha,
        onValueChange = {
            isha = it
            it.toDoubleOrNull()?.let { angle -> viewModel.setCustomIshaAngle(angle) }
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
    CalculationMethod.Tehran -> R.string.method_tehran
    CalculationMethod.Jafari -> R.string.method_jafari
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
