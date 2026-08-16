package org.example.islamicapp.feature.prayertimes.ui.times

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.prayertimes.R
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.feature.prayertimes.ui.localDateFormatter
import org.example.islamicapp.feature.prayertimes.ui.localTimeFormatter
import org.example.islamicapp.feature.prayertimes.ui.prayerLabelRes

@Composable
fun PrayerTimesScreen(
    modifier: Modifier = Modifier,
    viewModel: PrayerTimesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::previousDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "السابق")
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.selectedDate.format(localDateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                )
                // Hijri date beside the Gregorian one (PROJECT_PROMPT.md §6).
                state.hijri?.let {
                    Text(
                        text = it.formatArabicLong(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(
                        if (state.monthly) R.string.times_monthly else R.string.times_daily
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = viewModel::nextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "التالي")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            OutlinedButton(
                onClick = { shareDailyTimes(context, state) },
                enabled = state.isValid,
            ) {
                Text(stringResource(R.string.times_share))
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = viewModel::toggleMonthly) {
                Text(stringResource(if (state.monthly) R.string.times_daily else R.string.times_monthly))
            }
        }

        if (state.monthly) {
            MonthlyGrid(state)
        } else {
            DailyList(state)
        }
    }
}

@Composable
private fun DailyList(state: PrayerTimesViewModel.UiState) {
    if (!state.isValid) {
        Text(
            text = stringResource(R.string.home_cannot_compute),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Prayer.entries.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.weight(1f))
                        state.times[prayer]?.let {
                            Text(it.format(localTimeFormatter), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyGrid(state: PrayerTimesViewModel.UiState) {
    val daysOfWeek = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Leading empty cells so the first day aligns to its weekday.
            val firstDayOfWeekIndex = state.month.atDay(1).dayOfWeek.value % 7
            items(state.monthDays.size + firstDayOfWeekIndex) { index ->
                val dayIndex = index - firstDayOfWeekIndex
                if (dayIndex < 0) {
                    Box(Modifier.padding(2.dp))
                } else {
                    MonthCell(state.monthDays[dayIndex])
                }
            }
        }
    }
}

@Composable
private fun MonthCell(day: PrayerTimesViewModel.DayTimes) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.hijriDay.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        day.fajr?.let {
            Text(
                text = it.format(localTimeFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        day.maghrib?.let {
            Text(
                text = it.format(localTimeFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Shares today's prayer times as plain text via the system share sheet
 * (covers the "قابل للتصدير/الطباعة" checkbox — export without a network).
 */
private fun shareDailyTimes(context: Context, state: PrayerTimesViewModel.UiState) {
    if (!state.isValid) return
    val label = { prayer: Prayer -> context.getString(prayerLabelRes(prayer)) }
    val lines = buildList {
        add(context.getString(R.string.times_export_header, state.selectedDate.format(localDateFormatter)))
        state.settings.location?.let { add(context.getString(R.string.times_export_location, it.name)) }
        add("")
        Prayer.entries.forEach { prayer ->
            state.times[prayer]?.let { add("${label(prayer)}: ${it.format(localTimeFormatter)}") }
        }
    }

    val share = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.times_export_subject))
        putExtra(Intent.EXTRA_TEXT, lines.joinToString("\n"))
    }
    val chooser = Intent.createChooser(share, context.getString(R.string.times_share))
    runCatching { context.startActivity(chooser) }
}
