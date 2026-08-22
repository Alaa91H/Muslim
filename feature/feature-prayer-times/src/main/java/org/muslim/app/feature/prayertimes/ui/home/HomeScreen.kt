package org.muslim.app.feature.prayertimes.ui.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.formatCountdown
import org.muslim.app.feature.prayertimes.ui.localDateFormatter
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.ui.theme.IslamicOrnament
import org.muslim.app.core.ui.theme.IslamicOrnamentImage
import org.muslim.app.core.ui.theme.IslamicOrnamentOpacity
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Main screen: Hijri/Gregorian date, live next-prayer countdown and today's
 * prayer times (PROJECT_PROMPT.md §6 Phase 1).
 */
@Composable
fun HomeScreen(
    onSelectLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        IslamicOrnamentImage(
            ornament = IslamicOrnament.Geometric12,
            tint = MaterialTheme.colorScheme.primary,
            alpha = IslamicOrnamentOpacity.LightBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(top = 24.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
        // ---- Date header ----
        state.hijri?.let { hijri ->
            Text(
                text = hijri.formatArabicLong(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.hijri?.gregorian?.format(localDateFormatter) ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Surface(
                onClick = onSelectLocation,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(Modifier.padding(start = 4.dp))
                    Text(
                        text = if (state.hasLocation) state.locationName
                        else stringResource(R.string.home_select_location),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!state.hasLocation) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_location_unknown),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Column
        }

        // ---- Next prayer + countdown ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.home_next_prayer),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                state.nextPrayer?.let { prayer ->
                    Text(
                        text = stringResource(prayerLabelRes(prayer)),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    state.nextPrayerAt?.let { at ->
                        Text(
                            text = at.format(TimeFormats.timeFormatter(use24h)),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatCountdown(state.countdownSeconds),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Today's times ----
        Text(
            text = stringResource(R.string.home_today_times),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(8.dp))

        if (!state.isValid) {
            Text(
                text = stringResource(R.string.home_cannot_compute),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Prayer.entries.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (prayer == state.nextPrayer) FontWeight.Bold else FontWeight.Normal,
                        )
                        Spacer(Modifier.weight(1f))
                        state.times[prayer]?.let { time ->
                            Text(
                                text = time.format(TimeFormats.timeFormatter(use24h)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (prayer == state.nextPrayer) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---- Day navigation + share + daily/monthly toggle ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::previousDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.times_previous_day))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.selectedDate.format(localDateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                )
                state.hijri?.let {
                    Text(
                        text = it.formatArabicLong(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = viewModel::nextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.times_next_day))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            OutlinedButton(
                onClick = { shareDailyTimes(context, state, use24h) },
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
            MonthlyGrid(state, use24h)
        }

        Spacer(Modifier.height(16.dp))
        }
    }
}

/** Shares the selected day's prayer times as plain text via the share sheet. */
private fun shareDailyTimes(context: Context, state: HomeViewModel.UiState, use24h: Boolean) {
    if (!state.isValid) return
    val label = { prayer: Prayer -> context.getString(prayerLabelRes(prayer)) }
    val lines = buildList {
        add(context.getString(R.string.times_export_header, state.selectedDate.format(localDateFormatter)))
        if (state.hasLocation) add(context.getString(R.string.times_export_location, state.locationName))
        add("")
        Prayer.entries.forEach { prayer ->
            state.times[prayer]?.let { add("${label(prayer)}: ${it.format(TimeFormats.timeFormatter(use24h))}") }
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

/** Monthly grid of fajr/maghrib times, like a printed yearly timetable. */
@Composable
private fun MonthlyGrid(state: HomeViewModel.UiState, use24h: Boolean) {
    val daysOfWeek = listOf(
        stringResource(R.string.times_week_sat),
        stringResource(R.string.times_week_sun),
        stringResource(R.string.times_week_mon),
        stringResource(R.string.times_week_tue),
        stringResource(R.string.times_week_wed),
        stringResource(R.string.times_week_thu),
        stringResource(R.string.times_week_fri),
    )
    Column(modifier = Modifier.fillMaxWidth()) {
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
            modifier = Modifier.height(380.dp),
        ) {
            val firstDayOfWeekIndex = state.month.atDay(1).dayOfWeek.value % 7
            items(state.monthDays.size + firstDayOfWeekIndex) { index ->
                val dayIndex = index - firstDayOfWeekIndex
                if (dayIndex < 0) {
                    Box(Modifier.padding(2.dp))
                } else {
                    MonthCell(state.monthDays[dayIndex], use24h)
                }
            }
        }
    }
}

@Composable
private fun MonthCell(day: HomeViewModel.DayTimes, use24h: Boolean) {
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
                text = it.format(TimeFormats.timeFormatter(use24h)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        day.maghrib?.let {
            Text(
                text = it.format(TimeFormats.timeFormatter(use24h)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
