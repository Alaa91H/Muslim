package org.muslim.app.feature.prayertimes.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.formatCountdown
import org.muslim.app.feature.prayertimes.ui.localDateFormatter
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.core.ui.theme.MuslimGroup
import org.muslim.app.core.ui.theme.MuslimHero
import org.muslim.app.core.ui.theme.MuslimMenuAction
import org.muslim.app.core.ui.theme.MuslimOverflowMenu
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicLayout
import org.muslim.app.core.designsystem.MuslimTouchTarget
import org.muslim.app.core.designsystem.MuslimWindowWidthClass
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import org.muslim.app.core.datastore.prayer.trackablePrayers

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
    val showPrayerTrackerOnHome by viewModel.showPrayerTrackerOnHome.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationLabel = if (state.hasLocation) {
        state.locationName
    } else {
        stringResource(R.string.home_select_location)
    }
    val locationDescription = stringResource(R.string.home_location_action, locationLabel)
    val nextPrayerLabel = state.nextPrayer?.let { stringResource(prayerLabelRes(it)) }
    val nextPrayerTime = state.nextPrayerAt?.format(TimeFormats.timeFormatter(use24h))
    val countdown = formatCountdown(state.countdownSeconds)
    val nextPrayerDescription = if (nextPrayerLabel != null && nextPrayerTime != null) {
        stringResource(
            R.string.home_next_prayer_accessibility,
            nextPrayerLabel,
            nextPrayerTime,
            countdown,
        )
    } else {
        stringResource(R.string.home_next_prayer)
    }

    var customizingPrayer by remember { mutableStateOf<Prayer?>(null) }
    var overflowExpanded by remember { mutableStateOf(false) }

    MuslimContentFrame(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val adaptiveSpec = IslamicLayout.adaptiveSpec(maxWidth)
            val compactHeight = maxHeight < 760.dp
            val compactLayout =
                adaptiveSpec.widthClass == MuslimWindowWidthClass.Compact || compactHeight
            val sectionGap = if (compactHeight) {
                IslamicSpacing.Medium
            } else {
                IslamicSpacing.SectionVertical
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = adaptiveSpec.horizontalPadding,
                        end = adaptiveSpec.horizontalPadding,
                        top = IslamicSpacing.XSmall,
                        bottom = IslamicSpacing.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
            ) {
                PrayerDateHeader(
                    state = state,
                    locationLabel = locationLabel,
                    locationDescription = locationDescription,
                    compact = compactLayout,
                    onSelectLocation = onSelectLocation,
                    onPrevious = viewModel::previousPeriod,
                    onNext = viewModel::nextPeriod,
                )

                if (!state.hasLocation) {
                    MuslimStateSurface(
                        title = stringResource(R.string.home_location_unknown),
                        tone = MuslimStateTone.Information,
                        icon = Icons.Default.Place,
                        iconContentDescription = null,
                    )
                    return@Column
                }

                MuslimHero(
                    title = stringResource(R.string.home_next_prayer),
                    value = nextPrayerLabel ?: "—",
                    supportingText = listOfNotNull(nextPrayerTime, countdown)
                        .filter(String::isNotBlank)
                        .joinToString(" · "),
                    icon = state.nextPrayer?.let(::prayerIcon),
                    iconContentDescription = null,
                    modifier = Modifier.semantics {
                        contentDescription = nextPrayerDescription
                    },
                )

                Spacer(Modifier.height(sectionGap - IslamicSpacing.Small))

                MuslimSectionHeader(
                    title = stringResource(
                        if (state.monthly) R.string.times_monthly else R.string.home_today_times,
                    ),
                    action = {
                        MuslimOverflowMenu(
                            expanded = overflowExpanded,
                            onExpandedChange = { overflowExpanded = it },
                            contentDescription = stringResource(R.string.settings_adhan_customize),
                            actions = listOf(
                                MuslimMenuAction(
                                    id = "share",
                                    label = stringResource(R.string.times_share),
                                    enabled = state.isValid,
                                    onClick = { shareDailyTimes(context, state, use24h) },
                                ),
                                MuslimMenuAction(
                                    id = "view",
                                    label = stringResource(
                                        if (state.monthly) R.string.times_daily else R.string.times_monthly,
                                    ),
                                    onClick = viewModel::toggleMonthly,
                                ),
                            ),
                        )
                    },
                )

                if (!state.isValid) {
                    MuslimStateSurface(
                        title = stringResource(R.string.home_cannot_compute),
                        tone = MuslimStateTone.Critical,
                    )
                    return@Column
                }

                if (state.monthly) {
                    MonthlyTimetable(state = state, use24h = use24h)
                } else {
                    DailyPrayerSchedule(
                        state = state,
                        use24h = use24h,
                        compact = compactLayout,
                        onCustomizePrayer = { customizingPrayer = it },
                    )
                }

                if (showPrayerTrackerOnHome) {
                    Spacer(Modifier.height(sectionGap - IslamicSpacing.Small))
                    PrayerCompletionCard(
                        completedPrayers = state.completedPrayers,
                        onToggle = viewModel::togglePrayerCompletion,
                    )
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

@Composable
private fun PrayerDateHeader(
    state: HomeViewModel.UiState,
    locationLabel: String,
    locationDescription: String,
    compact: Boolean,
    onSelectLocation: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.times_previous_day),
            )
        }
        Text(
            text = state.hijri?.formatArabicLong().orEmpty(),
            modifier = Modifier.weight(1f),
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.times_next_day),
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        Text(
            text = state.hijri?.gregorian?.format(localDateFormatter).orEmpty(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Surface(
            onClick = onSelectLocation,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .weight(1.35f)
                .semantics {
                    contentDescription = locationDescription
                    role = Role.Button
                },
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = IslamicSpacing.Compact,
                    vertical = IslamicSpacing.XSmall,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(IslamicIconSize.Supporting),
                )
                Text(
                    text = locationLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DailyPrayerSchedule(
    state: HomeViewModel.UiState,
    use24h: Boolean,
    compact: Boolean,
    onCustomizePrayer: (Prayer) -> Unit,
) {
    MuslimGroup(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = if (compact) IslamicSpacing.Small else IslamicSpacing.Compact,
            vertical = IslamicSpacing.XSmall,
        ),
    ) {
        trackablePrayers.forEachIndexed { index, prayer ->
            if (index > 0) HorizontalDivider()
            val isNextPrayer = prayer == state.nextPrayer
            val stateDescription = if (isNextPrayer) {
                stringResource(R.string.home_next_prayer)
            } else {
                null
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        stateDescription?.let { description ->
                            Modifier.semantics { this.stateDescription = description }
                        } ?: Modifier,
                    ),
                shape = MaterialTheme.shapes.medium,
                color = if (isNextPrayer) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    Color.Transparent
                },
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = IslamicSpacing.Compact,
                        vertical = if (compact) IslamicSpacing.Small else IslamicSpacing.Compact,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
                ) {
                    Icon(
                        imageVector = prayerIcon(prayer),
                        contentDescription = null,
                        tint = if (isNextPrayer) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(IslamicIconSize.Standard),
                    )
                    Text(
                        text = stringResource(prayerLabelRes(prayer)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNextPrayer) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Spacer(Modifier.weight(1f))
                    state.times[prayer]?.let { time ->
                        Text(
                            text = time.format(TimeFormats.timeFormatter(use24h)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNextPrayer) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                    PrayerAlertAction(
                        prayer = prayer,
                        alert = state.prayerAlerts[prayer] ?: HomeViewModel.PrayerAlert(),
                        isNextPrayer = isNextPrayer,
                        onClick = { onCustomizePrayer(prayer) },
                    )
                }
            }
        }
    }
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
    compact: Boolean,
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
        modifier = Modifier.padding(
            start = if (compact) IslamicSpacing.Small else IslamicSpacing.Compact,
        ),
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
                modifier = Modifier.size(
                    if (compact) IslamicIconSize.Supporting else IslamicIconSize.Standard,
                ),
            )
        }
    }
}

@Composable
private fun PrayerCompletionCard(
    completedPrayers: Set<Prayer>,
    onToggle: (Prayer) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(IslamicSpacing.Compact),
    ) {
        Box {
            IslamicDecorationCorners(tint = MaterialTheme.colorScheme.primary)
            Column {
            Text(
                text = stringResource(R.string.home_prayer_tracker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(IslamicSpacing.XSmall))
            Text(
                text = stringResource(R.string.home_prayer_tracker_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(IslamicSpacing.Small))
            Text(
                text = stringResource(
                    R.string.home_prayer_tracker_progress,
                    completedPrayers.size,
                    trackablePrayers.size,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(IslamicSpacing.XSmall))
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
        trackablePrayers.forEach { prayer ->
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
        Spacer(Modifier.height(IslamicSpacing.XSmall))
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
