package org.example.islamicapp.feature.tasbih.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.tasbih.R
import org.example.islamicapp.feature.tasbih.domain.TasbihState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Electronic tasbih (PROJECT_PROMPT.md §6 Phase 4): a large haptic tap
 * target with a progress ring, customizable target, and daily/weekly stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    modifier: Modifier = Modifier,
    viewModel: TasbihViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasbih_title)) },
                actions = {
                    IconButton(onClick = viewModel::resetToday) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.tasbih_reset_day))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                TasbihDial(
                    counter = state.counter,
                    onTap = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.tap()
                    },
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.tasbih_cycles_done, state.counter.cycles),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.tasbih_today_total, state.counter.todayTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.tasbih_target_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TasbihState.TARGET_CHOICES.forEach { target ->
                        FilterChip(
                            selected = state.counter.target == target,
                            onClick = { viewModel.setTarget(target) },
                            label = { Text(target.toString()) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.counter.count == 0,
                        onClick = viewModel::resetCycle,
                        label = { Text(stringResource(R.string.tasbih_reset_cycle)) },
                    )
                }
            }
            item {
                Spacer(Modifier.height(24.dp))
                WeeklyChart(week = state.week)
            }
        }
    }
}

@Composable
private fun TasbihDial(
    counter: TasbihState,
    onTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .pointerInput(counter.target) { detectTapGestures { onTap() } },
        contentAlignment = Alignment.Center,
    ) {
        val progressColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18f, cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * counter.progress,
                useCenter = false,
                style = stroke,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = counter.count.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.tasbih_of_target, counter.target),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tasbih_tap_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeeklyChart(week: List<Pair<LocalDate, Int>>) {
    val max = (week.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Column(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.tasbih_weekly),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            val barColor = MaterialTheme.colorScheme.primary
            val dayFormatter = DateTimeFormatter.ofPattern("E")
            week.forEach { (day, total) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        total.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Canvas(
                        Modifier
                            .size(width = 22.dp, height = 90.dp),
                    ) {
                        val barHeight = (size.height) * (total.toFloat() / max)
                        drawRoundRect(
                            color = barColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
                            size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        day.format(dayFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
