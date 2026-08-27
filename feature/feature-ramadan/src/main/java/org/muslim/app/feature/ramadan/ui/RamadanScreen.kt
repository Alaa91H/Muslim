package org.muslim.app.feature.ramadan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.ramadan.R
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Ramadan mode (PROJECT_PROMPT.md §6 Phase 6): countdowns to suhoor and
 * iftar, the fasting-day tracker, and the optional notifications.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RamadanScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RamadanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ramadan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ramadan_back))
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
            RamadanHeaderCard(state)
            Spacer(Modifier.height(12.dp))
            IftarCard(state, viewModel, use24h)
            Spacer(Modifier.height(12.dp))
            SuhoorCard(state, viewModel, use24h)
            Spacer(Modifier.height(16.dp))
            HabitTrackerPanel(state = state, viewModel = viewModel)
            Spacer(Modifier.height(16.dp))
            FastingTracker(
                info = state.info,
                today = state.today,
                fastingDays = state.fastingDays,
                onToggle = viewModel::toggleFastingDay,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ramadan_hijri_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RamadanHeaderCard(state: RamadanUiState) {
    val info = state.info
    val inRamadan = info.isRamadanDay(state.today)
    val day = info.dayOfRamadan(state.today)
    val untilStart = info.daysUntilStart(state.today)
    val remaining = info.daysRemaining(state.today)

    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.NightsStay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (inRamadan) {
                    stringResource(R.string.ramadan_day_of_month, day.toString())
                } else {
                    stringResource(R.string.ramadan_countdown, untilStart.toString())
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (inRamadan) {
                    stringResource(R.string.ramadan_days_left, remaining.toString())
                } else {
                    stringResource(R.string.ramadan_starts_on, formatDate(info.start), formatDate(info.end))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ramadan_hijri_year, info.hijriYear.toString()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun IftarCard(state: RamadanUiState, viewModel: RamadanViewModel, use24h: Boolean) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ramadan_iftar),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.settings.iftarNotificationEnabled,
                    onCheckedChange = viewModel::setIftarNotificationEnabled,
                )
            }
            Spacer(Modifier.height(8.dp))
            state.iftarTime?.let { time ->
                Text(
                    text = stringResource(R.string.ramadan_iftar_time, formatTime(time, use24h)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            state.nextIftarMillis?.let { target ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ramadan_countdown_to_iftar, formatCountdown(state.nowMillis, target)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } ?: MuslimStateSurface(
                title = stringResource(R.string.ramadan_location_required),
                tone = MuslimStateTone.Critical,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ramadan_iftar_dua),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuhoorCard(state: RamadanUiState, viewModel: RamadanViewModel, use24h: Boolean) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.NightsStay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ramadan_suhoor),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.settings.suhoorReminderEnabled,
                    onCheckedChange = viewModel::setSuhoorReminderEnabled,
                )
            }
            Spacer(Modifier.height(8.dp))
            state.suhoorTime?.let { time ->
                Text(
                    text = stringResource(R.string.ramadan_suhoor_time, formatTime(time, use24h)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            state.nextSuhoorMillis?.let { target ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ramadan_countdown_to_suhoor, formatCountdown(state.nowMillis, target)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ramadan_suhoor_remind_before),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(8.dp))
                listOf(15, 30, 45, 60).forEach { minutes ->
                    FilterChip(
                        selected = state.settings.suhoorMinutesBefore == minutes,
                        onClick = { viewModel.setSuhoorMinutesBefore(minutes) },
                        label = { Text(stringResource(R.string.ramadan_minutes, minutes.toString())) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ramadan_notify_outside),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.settings.notifyOutsideRamadan,
                    onCheckedChange = viewModel::setNotifyOutsideRamadan,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FastingTracker(
    info: org.muslim.app.feature.ramadan.domain.RamadanInfo,
    today: LocalDate,
    fastingDays: Set<LocalDate>,
    onToggle: (LocalDate) -> Unit,
) {
    Column {
        MuslimSectionHeader(
            title = stringResource(R.string.ramadan_fasting_tracker),
            supportingText = stringResource(
                R.string.ramadan_fasting_progress,
                fastingDays.count { it in info.days }.toString(),
                info.days.size.toString(),
            ),
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            info.days.forEachIndexed { index, date ->
                val dayNumber = index + 1
                val fasted = date in fastingDays
                val isToday = date == today
                DayCell(
                    dayNumber = dayNumber,
                    fasted = fasted,
                    isToday = isToday,
                    onClick = { onToggle(date) },
                )
            }
        }
    }
}

@Composable
private fun DayCell(dayNumber: Int, fasted: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val container = when {
        fasted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        fasted -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = content,
        )
    }
}

private fun formatTime(time: LocalTime, use24h: Boolean): String =
    org.muslim.app.core.common.time.TimeFormats.formatTime(time, use24h)

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd/MM"))

private fun formatCountdown(nowMillis: Long, targetMillis: Long): String {
    val seconds = Duration.ofMillis((targetMillis - nowMillis).coerceAtLeast(0)).seconds
    val h = (seconds / 3600).toInt()
    val m = ((seconds % 3600) / 60).toInt()
    val s = (seconds % 60).toInt()
    return String.format(Locale.ROOT, "%02d:%02d:%02d", h, m, s)
}
