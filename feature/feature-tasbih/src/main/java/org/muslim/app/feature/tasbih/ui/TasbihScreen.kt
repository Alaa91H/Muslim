package org.muslim.app.feature.tasbih.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicDecorationMedallion
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.MuslimExpandableSection
import org.muslim.app.core.ui.theme.MuslimFilterBar
import org.muslim.app.core.ui.theme.MuslimFilterOption
import org.muslim.app.core.ui.theme.MuslimMenuAction
import org.muslim.app.core.ui.theme.MuslimOverflowMenu
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.core.ui.theme.MuslimTopBar
import org.muslim.app.feature.tasbih.R
import org.muslim.app.feature.tasbih.domain.DailyCount
import org.muslim.app.feature.tasbih.domain.TargetSoundSettings
import org.muslim.app.feature.tasbih.domain.TasbihCategory
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionHistoryItem
import org.muslim.app.feature.tasbih.domain.TasbihSessionMode
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
@Composable
fun TasbihScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasbihViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
    val soundSettings by viewModel.targetSoundSettings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val roundCompleteFormat = stringResource(R.string.tasbih_round_complete)
    val currentSoundSettings by rememberUpdatedState(soundSettings)

    LaunchedEffect(Unit) {
        viewModel.roundCompleted.collect { event ->
            vibrateRoundComplete(context)
            playTargetSound(context, currentSoundSettings)
            snackbarHostState.showSnackbar(
                java.lang.String.format(roundCompleteFormat, event.count.toString()),
            )
        }
    }

    MuslimScreen(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.tasbih_title),
                onNavigateBack = onBack,
                navigationContentDescription = stringResource(R.string.tasbih_back),
            )
        },
    ) {
        TasbihContent(
            state = state,
            activeSession = activeSession,
            sessionHistory = sessionHistory,
            soundSettings = soundSettings,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun TasbihContent(
    state: TasbihState,
    activeSession: TasbihSessionHistoryItem?,
    sessionHistory: List<TasbihSessionHistoryItem>,
    soundSettings: TargetSoundSettings,
    viewModel: TasbihViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = IslamicSpacing.PageHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(IslamicSpacing.Small))
        TasbihSelectionSection(
            state = state,
            onPhraseSelected = viewModel::setPhrase,
        )

        IslamicDecorationDivider(
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(
                horizontal = IslamicSpacing.Large,
                vertical = IslamicSpacing.Small,
            ),
        )
        Spacer(Modifier.height(IslamicSpacing.XSmall))

        TasbihCounterSection(
            state = state,
            activeSession = activeSession,
            onIncrement = viewModel::increment,
        )
        TasbihPhraseAndActions(
            state = state,
            onUndo = viewModel::decrement,
            onReset = viewModel::reset,
            onResetAll = viewModel::resetAll,
        )
        TasbihSecondarySections(
            state = state,
            activeSession = activeSession,
            sessionHistory = sessionHistory,
            soundSettings = soundSettings,
            viewModel = viewModel,
        )
        Spacer(Modifier.height(IslamicSpacing.Large))
    }
}

@Composable
private fun TasbihSelectionSection(
    state: TasbihState,
    onPhraseSelected: (TasbihPhrase) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(state.phrase.category) }

    CategorySelector(
        selected = selectedCategory,
        onSelect = { selectedCategory = it },
    )
    Spacer(Modifier.height(IslamicSpacing.Small))
    PhraseSelector(
        phrases = TasbihPhrase.entries.filter { it.category == selectedCategory },
        selected = state.phrase,
        onSelect = { phrase ->
            selectedCategory = phrase.category
            onPhraseSelected(phrase)
        },
    )
}

@Composable
private fun TasbihCounterSection(
    state: TasbihState,
    activeSession: TasbihSessionHistoryItem?,
    onIncrement: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val counterDescription = "${state.phrase.text}. ${state.count}. " +
        stringResource(R.string.tasbih_of_target, state.target.toString()) + ". " +
        stringResource(R.string.tasbih_tap_hint)
    val progress = tasbihSessionRingProgress(activeSession)

    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onIncrement()
            }
            .semantics {
                role = Role.Button
                contentDescription = counterDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        IslamicDecorationMedallion(
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxSize(),
        )
        CounterRing(
            progress = progress,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f),
            progressColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TasbihCounterText(
            state = state,
            activeSession = activeSession,
        )
    }
}

@Composable
private fun TasbihCounterText(
    state: TasbihState,
    activeSession: TasbihSessionHistoryItem?,
) {
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
        if (state.sessionMode == TasbihSessionMode.Free && state.targetReached) {
            Text(
                text = stringResource(R.string.tasbih_target_reached),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        val visibleRounds = when (state.sessionMode) {
            TasbihSessionMode.Free -> state.rounds.toLong()
            TasbihSessionMode.Target -> 0L
            TasbihSessionMode.Rounds ->
                activeSession?.count?.div(state.target.toLong()) ?: 0L
        }
        if (visibleRounds > 0L) {
            Text(
                text = stringResource(R.string.tasbih_rounds, visibleRounds),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun TasbihPhraseAndActions(
    state: TasbihState,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onResetAll: () -> Unit,
) {
    var virtueExpanded by rememberSaveable { mutableStateOf(false) }

    Spacer(Modifier.height(IslamicSpacing.Compact))
    Text(
        text = state.phrase.text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(IslamicSpacing.XXSmall))
    Text(
        text = state.phrase.transliteration,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(IslamicSpacing.XSmall))
    Text(
        text = stringResource(R.string.tasbih_tap_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(IslamicSpacing.Compact))

    MuslimExpandableSection(
        title = stringResource(R.string.tasbih_virtue_label),
        expanded = virtueExpanded,
        onExpandedChange = { virtueExpanded = it },
    ) {
        Text(
            text = state.phrase.virtue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(Modifier.height(IslamicSpacing.Compact))
    TasbihCounterActions(
        onUndo = onUndo,
        onReset = onReset,
        onResetAll = onResetAll,
    )
}

@Composable
private fun TasbihSecondarySections(
    state: TasbihState,
    activeSession: TasbihSessionHistoryItem?,
    sessionHistory: List<TasbihSessionHistoryItem>,
    soundSettings: TargetSoundSettings,
    viewModel: TasbihViewModel,
) {
    var showTargetDialog by remember { mutableStateOf(false) }
    var sessionSettingsExpanded by rememberSaveable { mutableStateOf(false) }
    var activityExpanded by rememberSaveable { mutableStateOf(false) }

    Spacer(Modifier.height(IslamicSpacing.Medium))
    MuslimExpandableSection(
        title = stringResource(R.string.tasbih_session_settings),
        expanded = sessionSettingsExpanded,
        onExpandedChange = { sessionSettingsExpanded = it },
    ) {
        TasbihSessionControls(
            state = state,
            activeSession = activeSession,
            onModeSelected = viewModel::setSessionMode,
            onRoundsGoalSelected = viewModel::setRoundsGoal,
            onPresetSelected = viewModel::applySessionPreset,
        )
        TargetSelector(
            selectedTarget = state.target,
            onTargetSelected = viewModel::setTarget,
            onCustomTarget = { showTargetDialog = true },
        )
        TargetSoundCard(
            settings = soundSettings,
            onToggle = viewModel::setTargetSoundEnabled,
        )
    }

    Spacer(Modifier.height(IslamicSpacing.Medium))
    TasbihActivitySection(
        state = state,
        sessionHistory = sessionHistory,
        expanded = activityExpanded,
        onExpandedChange = { activityExpanded = it },
    )

    if (showTargetDialog) {
        CustomTargetDialog(
            initial = state.target,
            onConfirm = { target ->
                showTargetDialog = false
                viewModel.setTarget(target)
            },
            onDismiss = { showTargetDialog = false },
        )
    }
}

@Composable
private fun TasbihActivitySection(
    state: TasbihState,
    sessionHistory: List<TasbihSessionHistoryItem>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    MuslimExpandableSection(
        title = stringResource(R.string.tasbih_activity),
        supportingText = stringResource(R.string.tasbih_total_today, state.totalToday),
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        MuslimSectionHeader(
            title = stringResource(R.string.tasbih_week_stats),
        )
        WeeklyChart(
            days = (state.history + DailyCount(java.time.LocalDate.now(), state.totalToday))
                .sortedBy { it.date }
                .takeLast(7),
        )
        RecentTasbihSessions(sessions = sessionHistory)
    }
}

private fun tasbihSessionRingProgress(
    session: TasbihSessionHistoryItem?,
): Float = session?.let { current ->
    when (current.mode) {
        TasbihSessionMode.Free -> {
            val remainder = current.count % current.target.toLong()
            if (current.count > 0L && remainder == 0L) {
                1f
            } else {
                remainder.toFloat() / current.target.toFloat()
            }
        }
        TasbihSessionMode.Target ->
            (current.count.toFloat() / current.target.toFloat()).coerceIn(0f, 1f)
        TasbihSessionMode.Rounds -> {
            val goal = current.target.toLong() * current.roundsGoal.toLong()
            (current.count.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0).toFloat()
        }
    }
} ?: 0f

@Composable
private fun CategorySelector(
    selected: TasbihCategory,
    onSelect: (TasbihCategory) -> Unit,
) {
    MuslimFilterBar(
        options = TasbihCategory.entries.map { category ->
            MuslimFilterOption(category.id, category.label)
        },
        selectedIds = setOf(selected.id),
        onToggle = { id ->
            TasbihCategory.entries.firstOrNull { it.id == id }?.let(onSelect)
        },
    )
}

@Composable
private fun PhraseSelector(
    phrases: List<TasbihPhrase>,
    selected: TasbihPhrase,
    onSelect: (TasbihPhrase) -> Unit,
) {
    MuslimFilterBar(
        options = phrases.map { phrase ->
            MuslimFilterOption(phrase.storageId, phrase.text)
        },
        selectedIds = setOf(selected.storageId),
        onToggle = { id ->
            phrases.firstOrNull { it.storageId == id }?.let(onSelect)
        },
    )
}

@Composable
private fun TargetSelector(
    selectedTarget: Int,
    onTargetSelected: (Int) -> Unit,
    onCustomTarget: () -> Unit,
) {
    val customId = "custom"
    val options = TARGETS.map { target ->
        MuslimFilterOption(target.toString(), target.toString())
    } + MuslimFilterOption(
        id = customId,
        label = stringResource(R.string.tasbih_custom_target),
    )
    val selectedId = if (selectedTarget in TARGETS) selectedTarget.toString() else customId

    MuslimFilterBar(
        options = options,
        selectedIds = setOf(selectedId),
        onToggle = { id ->
            if (id == customId) {
                onCustomTarget()
            } else {
                id.toIntOrNull()?.let(onTargetSelected)
            }
        },
    )
}

@Composable
private fun TasbihCounterActions(
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onResetAll: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IslamicSecondaryButton(
            onClick = onUndo,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(IslamicSpacing.Small))
            Text(stringResource(R.string.tasbih_undo))
        }
        MuslimOverflowMenu(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it },
            contentDescription = stringResource(R.string.tasbih_more_actions),
            actions = listOf(
                MuslimMenuAction(
                    id = "reset",
                    label = stringResource(R.string.tasbih_reset),
                    icon = Icons.Filled.Refresh,
                    onClick = onReset,
                ),
                MuslimMenuAction(
                    id = "reset-all",
                    label = stringResource(R.string.tasbih_reset_all),
                    icon = Icons.Filled.DeleteSweep,
                    onClick = onResetAll,
                ),
            ),
        )
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
                Spacer(Modifier.height(IslamicSpacing.Small))
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
    MuslimSettingsItem(
        title = stringResource(R.string.tasbih_sound_title),
        supportingText = stringResource(R.string.tasbih_sound_desc),
        onClick = { onToggle(!settings.enabled) },
        trailing = {
            Switch(
                checked = settings.enabled,
                onCheckedChange = onToggle,
            )
        },
    )
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
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
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
                Spacer(Modifier.height(IslamicSpacing.XSmall))
                Text(
                    text = day.date.format(DateTimeFormatter.ofPattern("d")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
