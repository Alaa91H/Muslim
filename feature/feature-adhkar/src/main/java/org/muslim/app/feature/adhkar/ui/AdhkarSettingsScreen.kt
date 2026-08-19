package org.muslim.app.feature.adhkar.ui

import android.content.Intent
import androidx.core.net.toUri
import android.provider.Settings
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory

private val DURATION_OPTIONS = listOf(5, 10, 15, 30, 60)
private val INTERVAL_OPTIONS = listOf(15, 30, 60, 120, 180)
private val HOUR_OPTIONS = (0..23).map { String.format(Locale.ROOT, "%02d", it) }
private val MINUTE_OPTIONS = (0..59 step 5).map { String.format(Locale.ROOT, "%02d", it) }

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
    val previewDhikr by viewModel.previewDhikr.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
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

            FloatingMessagePreview(
                dhikr = previewDhikr,
                durationSeconds = prefs.overlayDurationSeconds,
                enabled = prefs.overlayEnabled,
                backgroundColor = prefs.overlayBackgroundColor,
                cornerRadiusDp = prefs.overlayCornerRadiusDp,
                fontSizeSp = prefs.overlayFontSizeSp,
            )

            BubbleAppearanceControls(
                backgroundColor = prefs.overlayBackgroundColor,
                backgroundAlpha = prefs.overlayBackgroundAlpha,
                cornerRadiusDp = prefs.overlayCornerRadiusDp,
                fontSizeSp = prefs.overlayFontSizeSp,
                onBackgroundColor = viewModel::setOverlayBackgroundColor,
                onBackgroundAlpha = viewModel::setOverlayBackgroundAlpha,
                onCornerRadius = viewModel::setOverlayCornerRadiusDp,
                onFontSize = viewModel::setOverlayFontSizeSp,
                onReset = viewModel::resetOverlayAppearance,
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

            ReminderNotificationPreview(
                use24h = use24h,
                title = stringResource(R.string.adhkar_morning_reminder),
                dhikr = previewDhikr,
                hour = prefs.morningHour,
                minute = prefs.morningMinute,
                enabled = prefs.morningReminderEnabled,
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

            ReminderNotificationPreview(
                use24h = use24h,
                title = stringResource(R.string.adhkar_evening_reminder),
                dhikr = previewDhikr,
                hour = prefs.eveningHour,
                minute = prefs.eveningMinute,
                enabled = prefs.eveningReminderEnabled,
            )

            SectionHeader(stringResource(R.string.adhkar_periodic_section))

            SwitchRow(
                label = stringResource(R.string.adhkar_periodic_toggle),
                checked = prefs.periodicReminderEnabled,
                onCheckedChange = viewModel::setPeriodicReminderEnabled,
            )
            Text(
                text = stringResource(R.string.adhkar_periodic_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            if (prefs.periodicReminderEnabled) {
                IntervalDropdown(
                    current = prefs.periodicReminderIntervalMinutes,
                    onSelected = viewModel::setPeriodicReminderInterval,
                )
                Spacer(Modifier.height(8.dp))
                CategoryDropdown(
                    current = prefs.periodicReminderCategoryId,
                    onSelected = viewModel::setPeriodicReminderCategory,
                )
                Spacer(Modifier.height(8.dp))
                WindowSlot(
                    enabled = prefs.periodicReminderWindowEnabled,
                    startHour = prefs.periodicReminderWindowStartHour,
                    startMinute = prefs.periodicReminderWindowStartMinute,
                    endHour = prefs.periodicReminderWindowEndHour,
                    endMinute = prefs.periodicReminderWindowEndMinute,
                    onEnabledChanged = { enabled ->
                        viewModel.setPeriodicReminderWindow(
                            enabled,
                            prefs.periodicReminderWindowStartHour,
                            prefs.periodicReminderWindowStartMinute,
                            prefs.periodicReminderWindowEndHour,
                            prefs.periodicReminderWindowEndMinute,
                        )
                    },
                    onStartHourChanged = { hour ->
                        viewModel.setPeriodicReminderWindow(
                            prefs.periodicReminderWindowEnabled,
                            hour,
                            prefs.periodicReminderWindowStartMinute,
                            prefs.periodicReminderWindowEndHour,
                            prefs.periodicReminderWindowEndMinute,
                        )
                    },
                    onStartMinuteChanged = { minute ->
                        viewModel.setPeriodicReminderWindow(
                            prefs.periodicReminderWindowEnabled,
                            prefs.periodicReminderWindowStartHour,
                            minute,
                            prefs.periodicReminderWindowEndHour,
                            prefs.periodicReminderWindowEndMinute,
                        )
                    },
                    onEndHourChanged = { hour ->
                        viewModel.setPeriodicReminderWindow(
                            prefs.periodicReminderWindowEnabled,
                            prefs.periodicReminderWindowStartHour,
                            prefs.periodicReminderWindowStartMinute,
                            hour,
                            prefs.periodicReminderWindowEndMinute,
                        )
                    },
                    onEndMinuteChanged = { minute ->
                        viewModel.setPeriodicReminderWindow(
                            prefs.periodicReminderWindowEnabled,
                            prefs.periodicReminderWindowStartHour,
                            prefs.periodicReminderWindowStartMinute,
                            prefs.periodicReminderWindowEndHour,
                            minute,
                        )
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Live preview of the floating adhkar message. Mirrors
 * [org.muslim.app.feature.adhkar.overlay.AdhkarOverlayService]'s card: the same
 * dark rounded background, stroke, text colours and sizes, so the user sees the
 * exact look before enabling the overlay. Dims when the overlay is disabled.
 */
@Composable
private fun FloatingMessagePreview(
    dhikr: Dhikr?,
    durationSeconds: Int,
    enabled: Boolean,
    backgroundColor: Int,
    cornerRadiusDp: Int,
    fontSizeSp: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.adhkar_overlay_preview_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(cornerRadiusDp.dp),
            color = Color(backgroundColor),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.45f),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val sample = dhikr
                if (sample == null) {
                    Text(
                        text = stringResource(R.string.adhkar_overlay_preview_loading),
                        fontSize = 14.sp,
                        color = Color(0xFFB8BEC9),
                    )
                } else {
                    Text(
                        text = sample.arabic,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    if (sample.translation.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = sample.translation,
                            fontSize = (fontSizeSp - 8).coerceAtLeast(12).sp,
                            color = Color(0xFFB8BEC9),
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (sample.source.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = sample.source,
                            fontSize = (fontSizeSp - 10).coerceAtLeast(10).sp,
                            color = Color(0xFF8A93A3),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.adhkar_overlay_preview_dismiss, durationSeconds.toString()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val CORNER_RADIUS_OPTIONS = listOf(0, 12, 20, 28, 40)
private val FONT_SIZE_OPTIONS = listOf(18, 20, 22, 24, 26, 28)

/**
 * Bubble appearance controls: background colour swatches, corner radius and
 * font size chips. Every change is applied instantly to the live preview
 * above ([FloatingMessagePreview]) and persisted for the real overlay card.
 */
@Composable
private fun BubbleAppearanceControls(
    backgroundColor: Int,
    backgroundAlpha: Int,
    cornerRadiusDp: Int,
    fontSizeSp: Int,
    onBackgroundColor: (Int) -> Unit,
    onBackgroundAlpha: (Int) -> Unit,
    onCornerRadius: (Int) -> Unit,
    onFontSize: (Int) -> Unit,
    onReset: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.adhkar_overlay_appearance),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.adhkar_overlay_bg_color),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(backgroundColor))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
        }
        Spacer(Modifier.height(8.dp))
        HsvColorPicker(
            rgb = backgroundColor and 0xFFFFFF,
            onRgbChanged = onBackgroundColor,
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.adhkar_overlay_alpha, alphaPercent(backgroundAlpha)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = backgroundAlpha.toFloat(),
            onValueChange = { onBackgroundAlpha(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.adhkar_overlay_corner_radius),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        ChoiceChips(
            options = CORNER_RADIUS_OPTIONS,
            selected = cornerRadiusDp,
            label = { stringResource(R.string.adhkar_overlay_corner_radius_value, it) },
            onSelected = onCornerRadius,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.adhkar_overlay_font_size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        ChoiceChips(
            options = FONT_SIZE_OPTIONS,
            selected = fontSizeSp,
            label = { stringResource(R.string.adhkar_overlay_font_size_value, it) },
            onSelected = onFontSize,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.adhkar_overlay_reset))
        }
    }
}

/** A row of selectable pill chips (used for corner radius and font size). */
@Composable
private fun ChoiceChips(
    options: List<Int>,
    selected: Int,
    label: @Composable (Int) -> String,
    onSelected: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                ),
                modifier = Modifier.clickable { onSelected(option) },
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
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
                        "package:${context.packageName}".toUri(),
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
/**
 * Live preview of the daily adhkar reminder notification, mirroring
 * [org.muslim.app.feature.adhkar.data.AdhkarNotifications.showReminder]: a
 * notification icon, the slot title and the dhikr body with its time. Dims
 * when the slot is disabled.
 */
@Composable
private fun ReminderNotificationPreview(
    title: String,
    dhikr: Dhikr?,
    hour: Int,
    minute: Int,
    enabled: Boolean,
    use24h: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.adhkar_reminder_preview_label, title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
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
                        text = stringResource(R.string.adhkar_reminder_notification_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dhikr?.arabic?.take(140) ?: stringResource(R.string.adhkar_overlay_preview_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = org.muslim.app.core.common.time.TimeFormats.formatMinutes(hour * 60 + minute, use24h),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.adhkar_reminder_preview_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
                        value = String.format(Locale.ROOT, "%02d", hour),
                        onSelected = { onHourChanged(it.toInt()) },
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    TimeDropdown(
                        options = MINUTE_OPTIONS,
                        value = String.format(Locale.ROOT, "%02d", minute),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalDropdown(current: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(R.string.adhkar_periodic_interval_value, current),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.adhkar_periodic_interval)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            INTERVAL_OPTIONS.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.adhkar_periodic_interval_value, minutes)) },
                    onClick = { expanded = false; onSelected(minutes) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(current: String?, onSelected: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = current
        ?.let { id -> DhikrCategory.fromId(id).let { stringResource(it.titleRes) } }
        ?: stringResource(R.string.adhkar_periodic_category_random)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.adhkar_periodic_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.adhkar_periodic_category_random)) },
                onClick = { expanded = false; onSelected(null) },
            )
            DhikrCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(stringResource(category.titleRes)) },
                    onClick = { expanded = false; onSelected(category.id) },
                )
            }
        }
    }
}

@Composable
private fun WindowSlot(
    enabled: Boolean,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onEnabledChanged: (Boolean) -> Unit,
    onStartHourChanged: (Int) -> Unit,
    onStartMinuteChanged: (Int) -> Unit,
    onEndHourChanged: (Int) -> Unit,
    onEndMinuteChanged: (Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SwitchRow(
                label = stringResource(R.string.adhkar_periodic_window_toggle),
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
            if (enabled) {
                Text(
                    text = stringResource(R.string.adhkar_periodic_window_start),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeDropdown(HOUR_OPTIONS, String.format(Locale.ROOT, "%02d", startHour)) { onStartHourChanged(it.toInt()) }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    TimeDropdown(MINUTE_OPTIONS, String.format(Locale.ROOT, "%02d", startMinute)) { onStartMinuteChanged(it.toInt()) }
                }
                Text(
                    text = stringResource(R.string.adhkar_periodic_window_end),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeDropdown(HOUR_OPTIONS, String.format(Locale.ROOT, "%02d", endHour)) { onEndHourChanged(it.toInt()) }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    TimeDropdown(MINUTE_OPTIONS, String.format(Locale.ROOT, "%02d", endMinute)) { onEndMinuteChanged(it.toInt()) }
                }
            }
        }
    }
}
