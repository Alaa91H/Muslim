package org.example.islamicapp.feature.ramadan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.ramadan.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Ramadan hub (PROJECT_PROMPT.md §6 Phase 6): suhoor/iftar countdowns, the
 * fasting-day tracker with qada counter, upcoming occasions and sunnah
 * fasting days.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanScreen(
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
    viewModel: RamadanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(latitude, longitude) {
        viewModel.setLocation(latitude, longitude)
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.ramadan_title)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SeasonCard(state) }
            item { MoonPhaseCard() }
            if (state.ramadan.dayNumber != null && (state.suhoorAt != null || state.iftarAt != null)) {
                item { CountdownRow(state) }
            }
            item { TrackerCard(state, viewModel) }
            item {
                Text(
                    stringResource(R.string.ramadan_events),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.events, key = { it.id + it.nextDate }) { event ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(event.titleAr, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                event.nextDate.format(DateTimeFormatter.ofPattern("d MMM uuuu")),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            stringResource(R.string.ramadan_days_until, event.daysUntil),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.ramadan_sunnah_fasting),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.sunnahFasts.take(8), key = { it.date }) { fast ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(fast.labelAr, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                fast.date.format(DateTimeFormatter.ofPattern("EEEE d MMM")),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            if (fast.daysUntil == 0L) stringResource(R.string.ramadan_today)
                            else stringResource(R.string.ramadan_after_days, fast.daysUntil),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoonPhaseCard() {
    val info = remember {
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseCalculator.at(LocalDate.now())
    }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoonDisc(illumination = info.illumination, isWaxing = info.isWaxing)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    stringResource(R.string.ramadan_moon_phase),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(moonPhaseLabel(info.phase), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(
                        R.string.ramadan_moon_illumination,
                        (info.illumination * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun moonPhaseLabel(
    phase: org.example.islamicapp.feature.ramadan.domain.MoonPhaseType,
): String = stringResource(
    when (phase) {
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.NewMoon -> R.string.moon_new
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.WaxingCrescent -> R.string.moon_waxing_crescent
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.FirstQuarter -> R.string.moon_first_quarter
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.WaxingGibbous -> R.string.moon_waxing_gibbous
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.FullMoon -> R.string.moon_full
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.WaningGibbous -> R.string.moon_waning_gibbous
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.LastQuarter -> R.string.moon_last_quarter
        org.example.islamicapp.feature.ramadan.domain.MoonPhaseType.WaningCrescent -> R.string.moon_waning_crescent
    },
)

/**
 * Draws the moon disc: lit half + terminator ellipse (lens geometry),
 * mirrored for waning phases. Illumination 0 → dark disc, 1 → full disc.
 */
@Composable
private fun MoonDisc(illumination: Double, isWaxing: Boolean) {
    val darkColor = MaterialTheme.colorScheme.surfaceVariant
    val lightColor = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
        val radius = size.minDimension / 2
        drawCircle(color = darkColor, radius = radius)
        // 1) Lit half of the disc.
        drawArc(
            color = lightColor,
            startAngle = if (isWaxing) -90f else 90f,
            sweepAngle = 180f,
            useCenter = true,
        )
        // 2) Terminator lens: semi-width grows from 0 (half moon) to the full
        //    radius at new/full; lit-coloured for gibbous, dark for crescent.
        val semiWidth = (kotlin.math.abs(2 * illumination - 1) * radius).toFloat()
        drawOval(
            color = if (illumination >= 0.5) lightColor else darkColor,
            topLeft = androidx.compose.ui.geometry.Offset(-semiWidth, -radius),
            size = androidx.compose.ui.geometry.Size(2 * semiWidth, 2 * radius),
        )
    }
}

@Composable
private fun SeasonCard(state: RamadanUiState) {
    val info = state.ramadan
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            val inside = info.dayNumber != null
            Text(
                if (inside) stringResource(R.string.ramadan_day_n, info.dayNumber!!, info.totalDays)
                else stringResource(R.string.ramadan_countdown_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (inside) stringResource(R.string.ramadan_remaining_days, info.totalDays - info.dayNumber!!)
                else stringResource(R.string.ramadan_days_until, info.daysUntilStart ?: 0),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun CountdownRow(state: RamadanUiState) {
    val now = LocalDateTime.now()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CountdownCard(
            title = stringResource(R.string.ramadan_suhoor_ends),
            target = state.suhoorAt,
            now = now,
            modifier = Modifier.weight(1f),
        )
        CountdownCard(
            title = stringResource(R.string.ramadan_iftar_in),
            target = state.iftarAt,
            now = now,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CountdownCard(
    title: String,
    target: LocalDateTime?,
    now: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            val remaining = target?.let { java.time.Duration.between(now, it) }
            Text(
                target?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (remaining != null && !remaining.isNegative) {
                Text(
                    "-${remaining.formatHm()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrackerCard(state: RamadanUiState, viewModel: RamadanViewModel) {
    val today = LocalDate.now().toString()
    val fastedToday = today in state.fastedDays
    val info = state.ramadan

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.ramadan_fasting_tracker),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                AssistChip(
                    onClick = viewModel::toggleTodayFasted,
                    label = {
                        Text(
                            if (fastedToday) stringResource(R.string.ramadan_fasted_today)
                            else stringResource(R.string.ramadan_mark_fasted),
                        )
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            if (info.dayNumber != null) {
                Text(
                    stringResource(R.string.ramadan_month_progress),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                val days = (0 until info.totalDays).map { info.startDate.plusDays(it.toLong()) }
                // Fixed height grid inside the scroll: use rows of 10 chips.
                days.chunked(10).forEach { week ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        week.forEach { date ->
                            val fasted = date.toString() in state.fastedDays
                            val isToday = date == LocalDate.now()
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when {
                                    fasted -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                onClick = { viewModel.markRamadanDay(date, !fasted) },
                                modifier = Modifier.size(34.dp),
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        (ChronoUnit.DAYS.between(info.startDate, date) + 1).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (fasted) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.ramadan_qada, state.qadaRemaining),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { viewModel.adjustQada(-1) }) {
                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.ramadan_qada_decrease))
                }
                IconButton(onClick = { viewModel.adjustQada(1) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ramadan_qada_increase))
                }
            }
        }
    }
}
