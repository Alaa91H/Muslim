package org.muslim.app.feature.tasbih.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.core.ui.theme.MuslimFilterBar
import org.muslim.app.core.ui.theme.MuslimFilterOption
import org.muslim.app.core.ui.theme.MuslimGroup
import org.muslim.app.core.ui.theme.MuslimProgressHeader
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimSegmentedControl
import org.muslim.app.feature.tasbih.R
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionHistoryItem
import org.muslim.app.feature.tasbih.domain.TasbihSessionMode
import org.muslim.app.feature.tasbih.domain.TasbihState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ROUND_GOALS = listOf(1, 3, 5, 10)

@Composable
internal fun TasbihSessionControls(
    state: TasbihState,
    activeSession: TasbihSessionHistoryItem?,
    onModeSelected: (TasbihSessionMode) -> Unit,
    onRoundsGoalSelected: (Int) -> Unit,
    onPresetSelected: (TasbihSessionMode, Int, Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        Text(
            text = stringResource(R.string.tasbih_session_mode_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        SessionModeSelector(
            selected = state.sessionMode,
            onSelected = onModeSelected,
        )

        Text(
            text = sessionModeDescription(state.sessionMode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.sessionMode == TasbihSessionMode.Rounds) {
            RoundsGoalSelector(
                selected = state.roundsGoal,
                onSelected = onRoundsGoalSelected,
            )
        }

        SessionPresetSelector(
            state = state,
            onPresetSelected = onPresetSelected,
        )

        Text(
            text = stringResource(R.string.tasbih_session_active),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        ActiveSessionSummary(activeSession)
    }
}

@Composable
private fun SessionModeSelector(
    selected: TasbihSessionMode,
    onSelected: (TasbihSessionMode) -> Unit,
) {
    val modes = TasbihSessionMode.entries
    MuslimSegmentedControl(
        options = modes.map { sessionModeLabel(it) },
        selectedIndex = modes.indexOf(selected).coerceAtLeast(0),
        onSelectedIndexChange = { index ->
            modes.getOrNull(index)?.let(onSelected)
        },
    )
}

@Composable
private fun RoundsGoalSelector(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.tasbih_session_rounds_goal),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
    MuslimSegmentedControl(
        options = ROUND_GOALS.map(Int::toString),
        selectedIndex = ROUND_GOALS.indexOf(selected).coerceAtLeast(0),
        onSelectedIndexChange = { index ->
            ROUND_GOALS.getOrNull(index)?.let(onSelected)
        },
    )
}

@Composable
private fun SessionPresetSelector(
    state: TasbihState,
    onPresetSelected: (TasbihSessionMode, Int, Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.tasbih_session_presets),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )

    val selectedId = when {
        state.sessionMode == TasbihSessionMode.Free -> "free"
        state.sessionMode == TasbihSessionMode.Target && state.target == 33 -> "33"
        state.sessionMode == TasbihSessionMode.Target && state.target == 100 -> "100"
        state.sessionMode == TasbihSessionMode.Rounds &&
            state.target == 33 &&
            state.roundsGoal == 3 -> "33x3"
        else -> ""
    }
    val options = listOf(
        MuslimFilterOption("free", stringResource(R.string.tasbih_preset_free)),
        MuslimFilterOption("33", stringResource(R.string.tasbih_preset_33)),
        MuslimFilterOption("100", stringResource(R.string.tasbih_preset_100)),
        MuslimFilterOption("33x3", stringResource(R.string.tasbih_preset_33x3)),
    )

    MuslimFilterBar(
        options = options,
        selectedIds = selectedId.takeIf(String::isNotBlank)?.let(::setOf) ?: emptySet(),
        onToggle = { id ->
            when (id) {
                "free" -> onPresetSelected(TasbihSessionMode.Free, state.target, state.roundsGoal)
                "33" -> onPresetSelected(TasbihSessionMode.Target, 33, 1)
                "100" -> onPresetSelected(TasbihSessionMode.Target, 100, 1)
                "33x3" -> onPresetSelected(TasbihSessionMode.Rounds, 33, 3)
            }
        },
    )
}

@Composable
private fun ActiveSessionSummary(session: TasbihSessionHistoryItem?) {
    if (session == null) {
        MuslimEmptyState(
            title = stringResource(R.string.tasbih_session_ready),
        )
        return
    }

    val goal = sessionGoal(session)
    val phrase = TasbihPhrase.fromStorageId(session.phraseId)?.text ?: session.phraseId
    val countText = if (goal == null) {
        stringResource(R.string.tasbih_session_free_count, session.count)
    } else {
        stringResource(R.string.tasbih_session_progress, session.count, goal)
    }
    val modeText = sessionModeLabel(session.mode)

    if (goal != null && goal > 0L) {
        MuslimProgressHeader(
            title = phrase,
            progress = (session.count.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0).toFloat(),
            supportingText = "$countText • $modeText",
        )
    } else {
        MuslimGroup {
            Text(
                text = phrase,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$countText • $modeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun RecentTasbihSessions(
    sessions: List<TasbihSessionHistoryItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        MuslimSectionHeader(title = stringResource(R.string.tasbih_recent_sessions))

        val completed = sessions.filterNot { it.isActive }.take(5)
        if (completed.isEmpty()) {
            MuslimEmptyState(
                title = stringResource(R.string.tasbih_recent_sessions_empty),
            )
            return@Column
        }

        completed.forEach { session ->
            SessionHistoryRow(session)
        }
    }
}

@Composable
private fun SessionHistoryRow(session: TasbihSessionHistoryItem) {
    MuslimGroup {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = TasbihPhrase.fromStorageId(session.phraseId)?.text ?: session.phraseId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(IslamicSpacing.XXSmall))
                val modeLabel = sessionModeLabel(session.mode)
                val startedAt = formatSessionTime(session.startedAtEpochMillis)
                val duration = formatSessionDuration(session.durationMillis)
                Text(
                    text = "$modeLabel • $startedAt • $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.tasbih_session_item_count, session.count),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (session.target > 0 && session.count >= session.target) {
                    Text(
                        text = stringResource(
                            R.string.tasbih_session_item_rounds,
                            session.count / session.target,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun sessionGoal(session: TasbihSessionHistoryItem): Long? =
    when (session.mode) {
        TasbihSessionMode.Free -> null
        TasbihSessionMode.Target -> session.target.toLong()
        TasbihSessionMode.Rounds -> session.target.toLong() * session.roundsGoal.toLong()
    }

@Composable
private fun sessionModeLabel(mode: TasbihSessionMode): String =
    when (mode) {
        TasbihSessionMode.Free -> stringResource(R.string.tasbih_session_mode_free)
        TasbihSessionMode.Target -> stringResource(R.string.tasbih_session_mode_target)
        TasbihSessionMode.Rounds -> stringResource(R.string.tasbih_session_mode_rounds)
    }

@Composable
private fun sessionModeDescription(mode: TasbihSessionMode): String =
    when (mode) {
        TasbihSessionMode.Free -> stringResource(R.string.tasbih_session_mode_free_desc)
        TasbihSessionMode.Target -> stringResource(R.string.tasbih_session_mode_target_desc)
        TasbihSessionMode.Rounds -> stringResource(R.string.tasbih_session_mode_rounds_desc)
    }

private fun formatSessionTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.getDefault()))

@Composable
private fun formatSessionDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1_000L).coerceAtLeast(0L)
    val totalMinutes = totalSeconds / 60L
    return when {
        totalMinutes >= 60L -> {
            val hours = totalMinutes / 60L
            val minutes = totalMinutes % 60L
            stringResource(R.string.tasbih_session_duration_hours, hours, minutes)
        }
        totalMinutes >= 1L -> stringResource(R.string.tasbih_session_duration_minutes, totalMinutes)
        else -> stringResource(R.string.tasbih_session_duration_seconds, totalSeconds)
    }
}
