package org.muslim.app.feature.ramadan.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.ramadan.R
import org.muslim.app.feature.ramadan.domain.HabitBadge
import org.muslim.app.feature.ramadan.domain.HabitDaySummary
import org.muslim.app.feature.ramadan.domain.HabitId
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.trackablePrayers
import java.time.LocalDate
import kotlin.math.roundToInt

/** Standalone habit dashboard, available from the More hub. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RamadanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habit_tracker_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ramadan_back))
                    }
                },
            )
        },
    ) { padding ->
        HabitTrackerPanel(
            state = state,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        )
    }
}

/** Reusable panel shown both in the habit screen and the Ramadan screen. */
@Composable
fun HabitTrackerPanel(
    state: RamadanUiState,
    viewModel: RamadanViewModel,
    modifier: Modifier = Modifier,
) {
    val summary = state.habitSummary
    val today = state.today
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.habit_tracker_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.habit_tracker_today),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.habit_tracker_progress,
                        summary.today.completedCount,
                        HabitId.entries.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { summary.today.completedCount.toFloat() / HabitId.entries.size },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PrayerTrackerCard(
            completedPrayers = state.completedPrayers,
            showOnHome = viewModel.showPrayerTrackerOnHome.collectAsStateWithLifecycle().value,
            onTogglePrayer = viewModel::togglePrayerCompletion,
            onShowOnHomeChanged = viewModel::setShowPrayerTrackerOnHome,
        )

        Spacer(Modifier.height(16.dp))
        HabitChecklist(today, summary.today, viewModel)

        Spacer(Modifier.height(16.dp))
        HabitReports(summary)

        Spacer(Modifier.height(16.dp))
        RamadanPlanCard(state, viewModel)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PrayerTrackerCard(
    completedPrayers: Set<Prayer>,
    showOnHome: Boolean,
    onTogglePrayer: (Prayer) -> Unit,
    onShowOnHomeChanged: (Boolean) -> Unit,
) {
    val homeToggleDescription = stringResource(R.string.habit_prayer_tracker_title)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.habit_prayer_tracker_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.habit_prayer_tracker_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = showOnHome,
                    onCheckedChange = onShowOnHomeChanged,
                    modifier = Modifier.semantics {
                        stateDescription = homeToggleDescription
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.habit_prayer_tracker_progress,
                    completedPrayers.size,
                    trackablePrayers.size,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            trackablePrayers.forEach { prayer ->
                val completed = prayer in completedPrayers
                val label = stringResource(prayerLabelRes(prayer))
                val status = stringResource(
                    if (completed) R.string.habit_prayer_completed else R.string.habit_prayer_pending,
                )
                val toggleDescription = stringResource(
                    R.string.habit_prayer_toggle_description,
                    label,
                    status,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTogglePrayer(prayer) }
                        .padding(vertical = 2.dp)
                        .semantics { stateDescription = toggleDescription },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { onTogglePrayer(prayer) },
                    )
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun HabitChecklist(
    today: LocalDate,
    summary: HabitDaySummary,
    viewModel: RamadanViewModel,
) {
    Text(
        text = stringResource(R.string.habit_tracker_daily_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(4.dp))
    HabitId.entries.forEach { habit ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleHabit(today, habit) }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = habit in summary.completed,
                onCheckedChange = { viewModel.toggleHabit(today, habit) },
            )
            Text(
                text = stringResource(habitLabelRes(habit)),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun HabitReports(summary: org.muslim.app.feature.ramadan.domain.HabitSummary) {
    Text(
        text = stringResource(R.string.habit_tracker_reports),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    HabitWeekChart(summary.week)
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportCard(
            title = stringResource(R.string.habit_tracker_weekly),
            value = "${summary.weeklyCompletionPercent}%",
            modifier = Modifier.weight(1f),
        )
        ReportCard(
            title = stringResource(R.string.habit_tracker_monthly),
            value = "${summary.monthlyCompletionPercent}%",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportCard(
            title = stringResource(R.string.habit_tracker_streak),
            value = stringResource(R.string.habit_tracker_days, summary.currentStreak),
            modifier = Modifier.weight(1f),
        )
        ReportCard(
            title = stringResource(R.string.habit_tracker_points),
            value = summary.monthlyPoints.toString(),
            modifier = Modifier.weight(1f),
        )
        ReportCard(
            title = stringResource(R.string.habit_tracker_level),
            value = summary.level.toString(),
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(habitBadgeRes(summary.badge)),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ReportCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HabitWeekChart(days: List<HabitDaySummary>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val fraction = day.completedCount.toFloat() / HabitId.entries.size
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction.coerceAtLeast(0.03f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (day.isComplete) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer,
                        ),
                )
                Spacer(Modifier.height(4.dp))
                Text(day.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun RamadanPlanCard(state: RamadanUiState, viewModel: RamadanViewModel) {
    val active = state.info.isRamadanDay(state.today)
    val plan = state.habitState
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (active) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NightsStay, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ramadan_plan_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(if (active) R.string.ramadan_plan_active else R.string.ramadan_plan_next),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Filled.SelfImprovement, contentDescription = null)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ramadan_khatma_progress, plan.khatmaJuz),
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = plan.khatmaJuz.toFloat(),
                onValueChange = { viewModel.setKhatmaJuz(it.roundToInt()) },
                valueRange = 0f..30f,
                steps = 29,
            )
            Text(
                text = stringResource(R.string.ramadan_plan_juz_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTaraweeh(state.today) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.today in plan.taraweehDates,
                    onCheckedChange = { viewModel.toggleTaraweeh(state.today) },
                )
                Text(stringResource(R.string.ramadan_taraweeh_today))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ramadan_itikaf),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = plan.itikafEnabled,
                    onCheckedChange = viewModel::setItikafEnabled,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ramadan_plan_dua),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun prayerLabelRes(prayer: Prayer): Int = when (prayer) {
    Prayer.Fajr -> R.string.habit_prayer_fajr
    Prayer.Sunrise -> error("Sunrise is not tracked as an obligatory prayer")
    Prayer.Dhuhr -> R.string.habit_prayer_dhuhr
    Prayer.Asr -> R.string.habit_prayer_asr
    Prayer.Maghrib -> R.string.habit_prayer_maghrib
    Prayer.Isha -> R.string.habit_prayer_isha
}

private fun habitLabelRes(habit: HabitId): Int = when (habit) {
    HabitId.Rawatib -> R.string.habit_rawatib
    HabitId.Duha -> R.string.habit_duha
    HabitId.Qiyam -> R.string.habit_qiyam
    HabitId.Congregation -> R.string.habit_congregation
}

private fun habitBadgeRes(badge: HabitBadge): Int = when (badge) {
    HabitBadge.Starting -> R.string.habit_badge_starting
    HabitBadge.Building -> R.string.habit_badge_building
    HabitBadge.Consistent -> R.string.habit_badge_consistent
    HabitBadge.Excellent -> R.string.habit_badge_excellent
}
