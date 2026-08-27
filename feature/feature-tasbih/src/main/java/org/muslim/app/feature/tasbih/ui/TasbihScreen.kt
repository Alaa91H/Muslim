package org.muslim.app.feature.tasbih.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.feature.tasbih.R
import org.muslim.app.feature.tasbih.domain.DailyCount
import org.muslim.app.feature.tasbih.domain.TargetSoundSettings
import org.muslim.app.feature.tasbih.domain.TasbihCategory
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihState
import java.time.format.DateTimeFormatter
import kotlin.math.max

private val TARGETS = listOf(33, 99, 100, 1000)

/**
 * Digital misbaha (PROJECT_PROMPT.md §6 Phase 4): tap-to-count with haptic
 * feedback, an independent counter per dhikr phrase (grouped by category),
 * the virtue of each dhikr, undo, configurable/custom target, daily totals
 * and a weekly chart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasbihViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val soundSettings by viewModel.targetSoundSettings.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val roundCompleteFormat = stringResource(R.string.tasbih_round_complete)
    val currentSoundSettings by rememberUpdatedState(soundSettings)
    var selectedCategory by remember { mutableStateOf(state.phrase.category) }
    var showTargetDialog by remember { mutableStateOf(false) }
    val counterDescription = "${state.phrase.text}. ${state.count}. " +
        stringResource(R.string.tasbih_of_target, state.target.toString()) + ". " +
        stringResource(R.string.tasbih_tap_hint)

    // Vibrate + announce + optional tone whenever a full round completes (33/99/100/…).
    LaunchedEffect(Unit) {
        viewModel.roundCompleted.collect { event ->
            vibrateRoundComplete(context)
            playTargetSound(context, currentSoundSettings)
            snackbarHostState.showSnackbar(
                java.lang.String.format(roundCompleteFormat, event.count.toString()),
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasbih_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.tasbih_back))
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            CategorySelector(
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
            )

            Spacer(Modifier.height(8.dp))

            PhraseSelector(
                phrases = TasbihPhrase.entries.filter { it.category == selectedCategory },
                selected = state.phrase,
                onSelect = {
                    selectedCategory = it.category
                    viewModel.setPhrase(it)
                },
            )

            Spacer(Modifier.height(20.dp))

            // The counter remains the single, deliberately generous primary action.
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.increment()
                    }
                    .semantics {
                        role = Role.Button
                        contentDescription = counterDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                CounterRing(
                    progress = if (state.target > 0) {
                        state.count.coerceAtMost(state.target).toFloat() / state.target
                    } else {
                        0f
                    },
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f),
                    progressColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.count.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.tasbih_of_target, state.target.toString()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    if (state.targetReached) {
                        Text(
                            text = stringResource(R.string.tasbih_target_reached),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    if (state.rounds > 0) {
                        Text(
                            text = stringResource(R.string.tasbih_rounds, state.rounds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = state.phrase.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = state.phrase.transliteration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tasbih_tap_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            // Supporting meaning stays present but subordinate to the counting action.
            IslamicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.tasbih_virtue_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.phrase.virtue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Undo / reset / reset-all actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IslamicSecondaryButton(
                    onClick = viewModel::decrement,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.tasbih_undo))
                }
                IslamicSecondaryButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.tasbih_reset))
                }
                IslamicSecondaryButton(
                    onClick = viewModel::resetAll,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.tasbih_reset_all))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Target presets + custom target
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TARGETS.forEach { target ->
                    FilterChip(
                        selected = state.target == target,
                        onClick = { viewModel.setTarget(target) },
                        label = { Text(target.toString()) },
                    )
                }
                FilterChip(
                    selected = state.target !in TARGETS,
                    onClick = { showTargetDialog = true },
                    label = { Text(stringResource(R.string.tasbih_custom_target)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sound-on-target settings
            TargetSoundCard(
                settings = soundSettings,
                onToggle = viewModel::setTargetSoundEnabled,
            )

            Spacer(Modifier.height(20.dp))

            // Keep the summary compact and readable after the primary devotional action.
            MuslimSectionHeader(
                title = stringResource(R.string.tasbih_week_stats),
                supportingText = stringResource(R.string.tasbih_total_today, state.totalToday),
            )
            Spacer(Modifier.height(8.dp))
            WeeklyChart(
                days = (state.history + DailyCount(java.time.LocalDate.now(), state.totalToday))
                    .sortedBy { it.date }
                    .takeLast(7),
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTargetDialog) {
        CustomTargetDialog(
            initial = state.target,
            onConfirm = {
                showTargetDialog = false
                viewModel.setTarget(it)
            },
            onDismiss = { showTargetDialog = false },
        )
    }
}

@Composable
private fun CategorySelector(
    selected: TasbihCategory,
    onSelect: (TasbihCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TasbihCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun PhraseSelector(
    phrases: List<TasbihPhrase>,
    selected: TasbihPhrase,
    onSelect: (TasbihPhrase) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        phrases.forEach { phrase ->
            FilterChip(
                selected = selected == phrase,
                onClick = { onSelect(phrase) },
                label = { Text(phrase.text, maxLines = 1) },
            )
        }
    }
}

@Composable
private fun CustomTargetDialog(
    initial: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial.toString()) }
    val parsed = text.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tasbih_custom_target_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.tasbih_custom_target_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    // Normalize so Arabic-Indic/Persian keyboard digits are
                    // converted (not dropped) and always parse toIntOrNull().
                    onValueChange = { input ->
                        text = org.muslim.app.core.common.text.Digits.onlyDigits(input).take(6)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let { onConfirm(it.coerceIn(1, 100_000)) } },
                enabled = parsed != null && parsed > 0,
            ) {
                Text(stringResource(R.string.tasbih_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tasbih_cancel))
            }
        },
    )
}

/** Progress ring drawn behind the count (arc cycles with the target). */
@Composable
private fun CounterRing(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 10.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
    }
}

/** Toggle for the optional round-complete sound (always the notification tone). */
@Composable
private fun TargetSoundCard(
    settings: TargetSoundSettings,
    onToggle: (Boolean) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tasbih_sound_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.tasbih_sound_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = settings.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

/** Plays the system notification tone when the round-complete sound is enabled. */
private fun playTargetSound(context: Context, settings: TargetSoundSettings) {
    if (!settings.enabled) return
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: return
    RingtoneManager.getRingtone(context, uri)?.play()
}

/** Distinct double-buzz on a completed round (requires the VIBRATE permission). */
private fun vibrateRoundComplete(context: Context) {
    val vibrator = context.getSystemService(Vibrator::class.java) ?: return
    if (!vibrator.hasVibrator()) return
    vibrator.vibrate(
        VibrationEffect.createWaveform(longArrayOf(0, 180, 120, 260), -1),
    )
}

/** Simple last-7-days bar chart. */
@Composable
private fun WeeklyChart(days: List<DailyCount>) {
    val maxCount = max(days.maxOfOrNull { it.count } ?: 0, 1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val fraction = day.count.toFloat() / maxCount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.date.format(DateTimeFormatter.ofPattern("d")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
