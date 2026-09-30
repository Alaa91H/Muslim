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
import androidx.compose.ui.platform.LocalDensity
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
                            contentDescription = stringResource(R.string.times_title),
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
            text = state.hijri?.formatLocalizedLong().orEmpty(),
            modifier = Modifier.weight(1f),
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.times_next_day),
            )
        }
    }

    val dateAndLocation: @Composable (Modifier, Modifier) -> Unit = { dateModifier, locationModifier ->
        Text(
            text = state.hijri?.gregorian?.format(localDateFormatter).orEmpty(),
            modifier = dateModifier,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            onClick = onSelectLocation,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = locationModifier
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
    if (compact && LocalDensity.current.fontScale >= 1.5f) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            dateAndLocation(Modifier.fillMaxWidth(), Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            dateAndLocation(Modifier.weight(1f), Modifier.weight(1.35f))
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
            DailyPrayerRow(
                prayer = prayer,
                state = state,
                use24h = use24h,
                compact = compact,
                onCustomizePrayer = onCustomizePrayer,
            )
        }
    }
}

@Composable
private fun DailyPrayerRow(
    prayer: Prayer,
    state: HomeViewModel.UiState,
    use24h: Boolean,
    compact: Boolean,
    onCustomizePrayer: (Prayer) -> Unit,
) {
    val isNextPrayer = prayer == state.nextPrayer
    val rowContentColor = if (isNextPrayer) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val nextPrayerStateDescription = if (isNextPrayer) {
        stringResource(R.string.home_next_prayer)
    } else {
        null
    }
    val semanticModifier = nextPrayerStateDescription?.let { description ->
        Modifier.semantics { stateDescription = description }
    } ?: Modifier

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(semanticModifier),
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
                tint = if (isNextPrayer) rowContentColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
            Text(
                text = stringResource(prayerLabelRes(prayer)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                color = rowContentColor,
            )
            Spacer(Modifier.weight(1f))
            state.times[prayer]?.let { time ->
                Text(
                    text = time.format(TimeFormats.timeFormatter(use24h)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                    color = rowContentColor,
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

private fun prayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.Fajr -> Icons.Filled.WbTwilight
    Prayer.Sunrise -> Icons.Filled.WbSunny
    Prayer.Dhuhr -> Icons.Filled.LightMode
    Prayer.Asr -> Icons.Filled.Brightness5
    Prayer.Maghrib -> Icons.Filled.Brightness4
    Prayer.Isha -> Icons.Filled.NightsStay
}

/** Direct per-prayer entry point with a concise visual state instead of raw volume percentages. */
@Composable
private fun PrayerAlertAction(
    prayer: Prayer,
    alert: HomeViewModel.PrayerAlert,
    isNextPrayer: Boolean,
    onClick: () -> Unit,
) {
    val configurable = prayer != Prayer.Sunrise
    val icon = when {
        !configurable || !alert.adhanEnabled || alert.option == AdhanSoundOption.Silent ->
            Icons.Filled.NotificationsOff
        alert.option == AdhanSoundOption.VibrateOnly -> Icons.Filled.Vibration
        else -> Icons.Filled.NotificationsActive
    }
    val status = stringResource(
        when {
            !configurable || !alert.adhanEnabled || alert.option == AdhanSoundOption.Silent ->
                R.string.adhan_option_silent
            alert.option == AdhanSoundOption.VibrateOnly -> R.string.adhan_option_vibrate
            else -> R.string.adhan_option_default
        },
    )
    val contentColor = if (isNextPrayer) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val containerColor = if (isNextPrayer) {
        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentDescription = if (configurable) {
        stringResource(
            R.string.settings_adhan_customize_title,
            stringResource(prayerLabelRes(prayer)),
        )
    } else {
        status
    }

    Surface(
        onClick = onClick,
        enabled = configurable,
        modifier = Modifier
            .size(MuslimTouchTarget.Min)
            .semantics { stateDescription = status },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(IslamicIconSize.Standard),
            )
        }
    }
}

@Composable
private fun PrayerCompletionCard(
    completedPrayers: Set<Prayer>,
    onToggle: (Prayer) -> Unit,
) {
    MuslimGroup {
        Text(
            text = stringResource(R.string.home_prayer_tracker_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.home_prayer_tracker_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.home_prayer_tracker_progress,
                completedPrayers.size,
                trackablePrayers.size,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        trackablePrayers.forEach { prayer ->
            val completed = prayer in completedPrayers
            val label = stringResource(prayerLabelRes(prayer))
            val status = stringResource(
                if (completed) {
                    R.string.home_prayer_tracker_completed
                } else {
                    R.string.home_prayer_tracker_pending
                },
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
                    .semantics { stateDescription = toggleDescription },
            )
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

/** Complete monthly prayer timetable with days in the leading RTL column. */
private val MonthlyDayColumnWidth = 80.dp
private val MonthlyPrayerColumnWidth = 88.dp

@Composable
private fun MonthlyTimetable(
    state: HomeViewModel.UiState,
    use24h: Boolean,
) {
    val horizontalState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalState),
    ) {
        MonthlyTimetableHeader(state)
        HorizontalDivider()
        state.monthDays.forEach { day ->
            MonthlyTimetableRow(
                day = day,
                selected = day.date == state.selectedDate,
                use24h = use24h,
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun MonthlyTimetableHeader(state: HomeViewModel.UiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MonthlyHeaderCell(
            text = state.month.toString(),
            width = MonthlyDayColumnWidth,
        )
        Prayer.entries.forEach { prayer ->
            MonthlyHeaderCell(
                text = stringResource(prayerLabelRes(prayer)),
                width = MonthlyPrayerColumnWidth,
            )
        }
    }
}

@Composable
private fun MonthlyTimetableRow(
    day: HomeViewModel.DayTimes,
    selected: Boolean,
    use24h: Boolean,
) {
    val friday = day.date.dayOfWeek == DayOfWeek.FRIDAY
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        color = when {
            selected -> MaterialTheme.colorScheme.secondaryContainer
            friday -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> Color.Transparent
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonthlyDayCell(
                day = day,
                selected = selected,
                contentColor = contentColor,
            )
            Prayer.entries.forEach { prayer ->
                Text(
                    text = day.times[prayer]
                        ?.format(TimeFormats.timeFormatter(use24h))
                        ?: "—",
                    modifier = Modifier
                        .width(MonthlyPrayerColumnWidth)
                        .padding(
                            horizontal = IslamicSpacing.XSmall,
                            vertical = IslamicSpacing.Compact,
                        ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun MonthlyDayCell(
    day: HomeViewModel.DayTimes,
    selected: Boolean,
    contentColor: Color,
) {
    Column(
        modifier = Modifier
            .width(MonthlyDayColumnWidth)
            .padding(
                horizontal = IslamicSpacing.Small,
                vertical = IslamicSpacing.Small,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = contentColor,
        )
        Text(
            text = day.hijriDay.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

@Composable
private fun MonthlyHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(
                horizontal = IslamicSpacing.XSmall,
                vertical = IslamicSpacing.Small,
            ),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

