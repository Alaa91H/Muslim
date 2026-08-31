package org.muslim.app.feature.prayertimes.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.formatCountdown
import org.muslim.app.feature.prayertimes.ui.localDateFormatter
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.ui.theme.IslamicOrnament
import org.muslim.app.core.ui.theme.IslamicOrnamentImage
import org.muslim.app.core.ui.theme.IslamicOrnamentOpacity
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import org.muslim.app.core.datastore.prayer.trackablePrayers

/**
 * Main screen: Hijri/Gregorian date, live next-prayer countdown and today's
 * prayer times (PROJECT_PROMPT.md §6 Phase 1).
 */
@Suppress("LongMethod")
@Composable
fun HomeScreen(
    onSelectLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    val ornamentStyle by viewModel.ornamentStyle.collectAsStateWithLifecycle()
    val showPrayerTrackerOnHome by viewModel.showPrayerTrackerOnHome.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locationLabel = if (state.hasLocation) state.locationName else stringResource(R.string.home_select_location)
    val locationDescription = stringResource(R.string.home_location_action, locationLabel)
    val nextPrayerLabel = state.nextPrayer?.let { stringResource(prayerLabelRes(it)) }
    val nextPrayerTime = state.nextPrayerAt?.format(TimeFormats.timeFormatter(use24h))
    val nextPrayerDescription = if (nextPrayerLabel != null && nextPrayerTime != null) {
        stringResource(
            R.string.home_next_prayer_accessibility,
            nextPrayerLabel,
            nextPrayerTime,
            formatCountdown(state.countdownSeconds),
        )
    } else {
        stringResource(R.string.home_next_prayer)
    }
    // Kept in the home composition so the alert action opens a modal and never
    // pushes the user into the full prayer-settings destination.
    var customizingPrayer by remember { mutableStateOf<Prayer?>(null) }

    MuslimContentFrame(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
        IslamicOrnamentImage(
            ornament = ornamentStyle.toIslamicOrnament(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = IslamicOrnamentOpacity.LightBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(top = IslamicSpacing.Large),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = IslamicSpacing.PageHorizontal,
                    vertical = IslamicSpacing.Small,
                ),
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
                modifier = Modifier.semantics {
                    contentDescription = locationDescription
                    role = Role.Button
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(Modifier.padding(start = 4.dp))
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Spacer(Modifier.height(IslamicSpacing.Medium))

        if (!state.hasLocation) {
            MuslimStateSurface(
                title = stringResource(R.string.home_location_unknown),
                tone = MuslimStateTone.Information,
                icon = Icons.Default.Place,
                iconContentDescription = null,
            )
            return@Column
        }

        // ---- Next prayer + countdown ----
        IslamicCard(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = nextPrayerDescription },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(IslamicSpacing.Comfortable),
        ) {
            Box {
                PrayerCardEdgeOrnaments(tint = MaterialTheme.colorScheme.tertiary)
                Column {
                    Text(
                        text = stringResource(R.string.home_next_prayer),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(IslamicSpacing.Small))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            state.nextPrayer?.let { prayer ->
                                Text(
                                    text = stringResource(prayerLabelRes(prayer)),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        state.nextPrayer?.let { prayer ->
                            Icon(
                                imageVector = prayerIcon(prayer),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(IslamicIconSize.Hero),
                            )
                        }
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
        }

        Spacer(Modifier.height(IslamicSpacing.SectionVertical))

        // ---- Today's times ----
        MuslimSectionHeader(
            title = stringResource(R.string.home_today_times),
            supportingText = state.selectedDate.format(localDateFormatter),
        )
        Spacer(Modifier.height(IslamicSpacing.Small))

        if (!state.isValid) {
            Text(
                text = stringResource(R.string.home_cannot_compute),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        IslamicCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = IslamicSpacing.Medium,
                vertical = IslamicSpacing.Small,
            ),
        ) {
            Box {
                PrayerCardEdgeOrnaments(tint = MaterialTheme.colorScheme.primary)
                Column {
                    Prayer.entries.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    val isNextPrayer = prayer == state.nextPrayer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = IslamicSpacing.XSmall)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (isNextPrayer) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
                            )
                            .padding(
                                horizontal = IslamicSpacing.Compact,
                                vertical = IslamicSpacing.Small,
                            )
                            .then(
                                if (isNextPrayer) {
                                    Modifier.semantics {
                                        stateDescription = stringResource(R.string.home_next_prayer)
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = prayerIcon(prayer),
                            contentDescription = null,
                            tint = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(IslamicIconSize.Standard),
                        )
                        Spacer(Modifier.width(IslamicSpacing.Small))
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.weight(1f))
                        state.times[prayer]?.let { time ->
                            Text(
                                text = time.format(TimeFormats.timeFormatter(use24h)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        PrayerAlertAction(
                            prayer = prayer,
                            alert = state.prayerAlerts[prayer] ?: HomeViewModel.PrayerAlert(),
                            isNextPrayer = isNextPrayer,
                            onClick = { customizingPrayer = prayer },
                        )
                    }
                    }
                }
            }
        }

        Spacer(Modifier.height(IslamicSpacing.Medium))

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = IslamicSpacing.XSmall),
            horizontalArrangement = Arrangement.End,
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { shareDailyTimes(context, state, use24h) },
                enabled = state.isValid,
            ) {
                Text(stringResource(R.string.times_share))
            }
            Spacer(Modifier.width(IslamicSpacing.Small))
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = viewModel::toggleMonthly,
            ) {
                Text(stringResource(if (state.monthly) R.string.times_daily else R.string.times_monthly))
            }
        }

        if (state.monthly) {
            MonthlyGrid(state, use24h)
        }

        if (showPrayerTrackerOnHome) {
            Spacer(Modifier.height(IslamicSpacing.SectionVertical))
            PrayerCompletionCard(
                completedPrayers = state.completedPrayers,
                onToggle = viewModel::togglePrayerCompletion,
            )

            Spacer(Modifier.height(IslamicSpacing.Medium))
        }
        }
        }
    }
    customizingPrayer?.let { prayer ->
        HomeAdhanCustomizationDialog(
            prayer = prayer,
            onDismiss = { customizingPrayer = null },
        )
    }
}

private fun AppOrnamentStyle.toIslamicOrnament(): IslamicOrnament = when (this) {
    AppOrnamentStyle.Geometry -> IslamicOrnament.Geometric12
    AppOrnamentStyle.Arabesque -> IslamicOrnament.Arabesque
    AppOrnamentStyle.Stars -> IslamicOrnament.Star12
    AppOrnamentStyle.Minimal -> IslamicOrnament.Corner
}

private fun prayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.Fajr -> Icons.Filled.Nightlight
    Prayer.Sunrise -> Icons.Filled.WbSunny
    Prayer.Dhuhr -> Icons.Filled.LightMode
    Prayer.Asr -> Icons.Filled.Brightness4
    Prayer.Maghrib -> Icons.Filled.Nightlight
    Prayer.Isha -> Icons.Filled.DarkMode
}

/** Direct per-prayer entry point for the same persisted Adhan choices exposed in Settings. */
@Composable
private fun PrayerAlertAction(
    prayer: Prayer,
    alert: HomeViewModel.PrayerAlert,
    isNextPrayer: Boolean,
    onClick: () -> Unit,
) {
    val configurable = prayer != Prayer.Sunrise
    val icon = when {
        !configurable || !alert.adhanEnabled || alert.option == AdhanSoundOption.Silent -> Icons.Filled.NotificationsOff
        alert.option == AdhanSoundOption.VibrateOnly -> Icons.Filled.Vibration
        else -> Icons.Filled.NotificationsActive
    }
    val contentColor = if (isNextPrayer) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentDescription = if (configurable) {
        stringResource(R.string.settings_adhan_customize_title, stringResource(prayerLabelRes(prayer)))
    } else {
        stringResource(R.string.adhan_option_silent)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = IslamicSpacing.Compact),
    ) {
        if (configurable) {
            Text(
                text = "${alert.volume}%",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
        IconButton(onClick = onClick, enabled = configurable) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
        }
    }
}

@Composable
private fun PrayerCardEdgeOrnaments(tint: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        IslamicOrnamentImage(
            ornament = IslamicOrnament.Corner,
            tint = tint,
            alpha = IslamicOrnamentOpacity.LightActive,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(72.dp),
        )
        IslamicOrnamentImage(
            ornament = IslamicOrnament.Corner,
            tint = tint,
            alpha = IslamicOrnamentOpacity.LightActive,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(72.dp)
                .graphicsLayer(rotationZ = 180f),
        )
    }
}

@Composable
private fun PrayerCompletionCard(
    completedPrayers: Set<Prayer>,
    onToggle: (Prayer) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(IslamicSpacing.Medium),
    ) {
        Box {
            PrayerCardEdgeOrnaments(tint = MaterialTheme.colorScheme.primary)
            Column {
            Text(
                text = stringResource(R.string.home_prayer_tracker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_prayer_tracker_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.home_prayer_tracker_progress,
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
                    if (completed) R.string.home_prayer_tracker_completed
                    else R.string.home_prayer_tracker_pending,
                )
                val toggleDescription = stringResource(
                    R.string.home_prayer_tracker_toggle_description,
                    label,
                    status,
                )
                FilterChip(
                    selected = completed,
                    onClick = { onToggle(prayer) },
                    label = { Text(label) },
                    leadingIcon = if (completed) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .semantics { stateDescription = toggleDescription },
                )
            }
            }
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
