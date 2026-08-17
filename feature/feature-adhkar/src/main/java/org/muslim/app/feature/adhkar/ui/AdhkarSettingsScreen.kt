package org.muslim.app.feature.adhkar.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.adhkar.R

private val DURATION_OPTIONS = listOf(5, 10, 15, 30, 60)
private val HOUR_OPTIONS = (0..23).map { String.format("%02d", it) }
private val MINUTE_OPTIONS = (0..59 step 5).map { String.format("%02d", it) }

/** Adhkar settings: floating overlay, reminders, and visibility customization. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarSettingsScreen(
    onBack: () -> Unit,
    onOpenCustomize: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarSettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhkar_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.adhkar_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            SectionHeader(stringResource(R.string.adhkar_overlay_section))

            SwitchRow(
                label = stringResource(R.string.adhkar_overlay_toggle),
                checked = prefs.overlayEnabled,
                onCheckedChange = viewModel::setOverlayEnabled,
            )

            Text(
                text = stringResource(R.string.adhkar_overlay_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            if (!viewModel.overlayPermissionGranted) {
                OutlinedButtonFill {
                    Text(stringResource(R.string.adhkar_overlay_grant_permission))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.adhkar_overlay_permission_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DurationDropdown(
                    current = prefs.overlayDurationSeconds,
                    onSelected = viewModel::setOverlayDurationSeconds,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::testOverlay,
                    enabled = prefs.overlayEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.adhkar_overlay_test))
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenCustomize)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.adhkar_customize_title),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
            }

            SectionHeader(stringResource(R.string.adhkar_reminders_section))

            ReminderSlot(
                title = stringResource(R.string.adhkar_morning_reminder),
                enabled = prefs.morningReminderEnabled,
                hour = prefs.morningHour,
                minute = prefs.morningMinute,
                onEnabledChanged = { enabled ->
                    viewModel.setMorningReminder(enabled, prefs.morningHour, prefs.morningMinute)
                },
                onHourChanged = { hour -> viewModel.setMorningReminder(prefs.morningReminderEnabled, hour, prefs.morningMinute) },
                onMinuteChanged = { minute -> viewModel.setMorningReminder(prefs.morningReminderEnabled, prefs.morningHour, minute) },
            )

            ReminderSlot(
                title = stringResource(R.string.adhkar_evening_reminder),
                enabled = prefs.eveningReminderEnabled,
                hour = prefs.eveningHour,
                minute = prefs.eveningMinute,
                onEnabledChanged = { enabled ->
                    viewModel.setEveningReminder(enabled, prefs.eveningHour, prefs.eveningMinute)
                },
                onHourChanged = { hour -> viewModel.setEveningReminder(prefs.eveningReminderEnabled, hour, prefs.eveningMinute) },
                onMinuteChanged = { minute -> viewModel.setEveningReminder(prefs.eveningReminderEnabled, prefs.eveningHour, minute) },
            )

            Spacer(Modifier.height(24.dp))
        }
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
private fun OutlinedButtonFill(content: @Composable () -> Unit) {
    val context = LocalContext.current
    androidx.compose.material3.OutlinedButton(
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationDropdown(current: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(R.string.adhkar_overlay_duration_value, current),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.adhkar_overlay_duration)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DURATION_OPTIONS.forEach { seconds ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.adhkar_overlay_duration_value, seconds)) },
                    onClick = { expanded = false; onSelected(seconds) },
                )
            }
        }
    }
}

/** One daily reminder slot: toggle + hour/minute pickers. */
@Composable
private fun ReminderSlot(
    title: String,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChanged: (Boolean) -> Unit,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SwitchRow(label = title, checked = enabled, onCheckedChange = onEnabledChanged)
            if (enabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.adhkar_reminder_time),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TimeDropdown(
                        options = HOUR_OPTIONS,
                        value = String.format("%02d", hour),
                        onSelected = { onHourChanged(it.toInt()) },
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    TimeDropdown(
                        options = MINUTE_OPTIONS,
                        value = String.format("%02d", minute),
                        onSelected = { onMinuteChanged(it.toInt()) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDropdown(options: List<String>, value: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .width(88.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { expanded = false; onSelected(option) },
                )
            }
        }
    }
}
