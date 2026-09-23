package org.muslim.app.feature.tasbih.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimSectionHeader
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
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = stringResource(R.string.tasbih_session_mode_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TasbihSessionMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.sessionMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text(sessionModeLabel(mode)) },
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = sessionModeDescription(state.sessionMode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.sessionMode == TasbihSessionMode.Rounds) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.tasbih_session_rounds_goal),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ROUND_GOALS.forEach { rounds ->
                    FilterChip(
                        selected = state.roundsGoal == rounds,
                        onClick = { onRoundsGoalSelected(rounds) },
                        label = { Text(rounds.toString()) },
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.tasbih_session_presets),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.sessionMode == TasbihSessionMode.Free,
                onClick = { onPresetSelected(TasbihSessionMode.Free, state.target, state.roundsGoal) },
                label = { Text(stringResource(R.string.tasbih_preset_free)) },
            )
            FilterChip(
                selected = state.sessionMode == TasbihSessionMode.Target && state.target == 33,
                onClick = { onPresetSelected(TasbihSessionMode.Target, 33, 1) },
                label = { Text(stringResource(R.string.tasbih_preset_33)) },
            )
            FilterChip(
                selected = state.sessionMode == TasbihSessionMode.Target && state.target == 100,
                onClick = { onPresetSelected(TasbihSessionMode.Target, 100, 1) },
                label = { Text(stringResource(R.string.tasbih_preset_100)) },
            )
            FilterChip(
                selected = state.sessionMode == TasbihSessionMode.Rounds &&
                    state.target == 33 &&
                    state.roundsGoal == 3,
                onClick = { onPresetSelected(TasbihSessionMode.Rounds, 33, 3) },
                label = { Text(stringResource(R.string.tasbih_preset_33x3)) },
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.tasbih_session_active),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        ActiveSessionSummary(activeSession)
    }
}

@Composable
private fun ActiveSessionSummary(session: TasbihSessionHistoryItem?) {
    if (session == null) {
        Text(
            text = stringResource(R.string.tasbih_session_ready),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val goal = sessionGoal(session)
    val progress = if (goal == null || goal <= 0L) {
        0f
    } else {
        (session.count.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0).toFloat()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
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
            Text(
                text = if (goal == null) {
                    stringResource(R.string.tasbih_session_free_count, session.count)
                } else {
                    stringResource(R.string.tasbih_session_progress, session.count, goal)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = sessionModeLabel(session.mode),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    if (goal != null) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(99.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(99.dp),
                    ),
            )
        }
    }
}

@Composable
internal fun RecentTasbihSessions(
    sessions: List<TasbihSessionHistoryItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MuslimSectionHeader(title = stringResource(R.string.tasbih_recent_sessions))
        Spacer(Modifier.height(8.dp))

        val completed = sessions.filterNot { it.isActive }.take(5)
        if (completed.isEmpty()) {
            IslamicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = stringResource(R.string.tasbih_recent_sessions_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        completed.forEachIndexed { index, session ->
            SessionHistoryRow(session)
            if (index != completed.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(session: TasbihSessionHistoryItem) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                Spacer(Modifier.height(2.dp))
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
