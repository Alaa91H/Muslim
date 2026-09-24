package org.muslim.app.feature.prayertimes.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.formatCountdown
import org.muslim.app.feature.prayertimes.ui.localDateFormatter
import org.muslim.app.core.common.time.TimeFormats
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.IslamicDecorationCorners
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import org.muslim.app.core.datastore.prayer.trackablePrayers
import java.time.DayOfWeek

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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val narrowLayout = maxWidth < 360.dp
        val wideLayout = maxWidth >= 600.dp
        val compactLayout = narrowLayout || maxHeight < 760.dp
        val pageHorizontalPadding = when {
            narrowLayout -> IslamicSpacing.Compact
            wideLayout -> IslamicSpacing.Section
            else -> IslamicSpacing.PageHorizontal
        }
        val cardPadding = if (compactLayout) IslamicSpacing.Compact else IslamicSpacing.Comfortable
        val sectionGap = if (compactLayout) IslamicSpacing.Medium else IslamicSpacing.SectionVertical
        val heroIconSize = if (narrowLayout) IslamicIconSize.Standard else IslamicIconSize.Prominent
        val heroVerticalPadding = if (compactLayout) IslamicSpacing.XSmall else IslamicSpacing.Small
        val prayerRowOuterVerticalPadding = if (compactLayout) 0.dp else IslamicSpacing.XXSmall
        val prayerRowHorizontalPadding = if (narrowLayout) IslamicSpacing.Small else IslamicSpacing.Compact
        val prayerRowInnerVerticalPadding = if (compactLayout) IslamicSpacing.XXSmall else IslamicSpacing.XSmall
        val prayerIconSize = if (narrowLayout) IslamicIconSize.Supporting else IslamicIconSize.Standard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = pageHorizontalPadding,
                    end = pageHorizontalPadding,
                    top = 0.dp,
                    bottom = IslamicSpacing.Small,
                ),
        ) {
        // Keep the shared identity primitive in this surface without reserving
        // the former 56–84dp header band; the screen now uses that space for content.
        IslamicDecorationBand(
            modifier = Modifier.height(0.dp),
            compact = true,
        )

        // ---- Date header ----
        state.hijri?.let { hijri ->
            Text(
                text = hijri.formatArabicLong(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.hijri?.gregorian?.format(localDateFormatter) ?: "",
                modifier = Modifier.weight(if (narrowLayout) 0.8f else 1f),
                style = if (narrowLayout) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Spacer(Modifier.width(IslamicSpacing.Small))
            Surface(
                onClick = onSelectLocation,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .weight(if (narrowLayout) 1.45f else 1.15f)
                    .semantics {
                        contentDescription = locationDescription
                        role = Role.Button
                    },
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = if (narrowLayout) IslamicSpacing.Small else IslamicSpacing.Compact,
                        vertical = if (compactLayout) IslamicSpacing.XSmall else 6.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(
                            if (narrowLayout) IslamicIconSize.Supporting else IslamicIconSize.Standard,
                        ),
                    )
                    Spacer(Modifier.width(IslamicSpacing.XSmall))
                    Text(
                        text = locationLabel,
                        modifier = Modifier.weight(1f),
                        style = if (narrowLayout) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(if (compactLayout) IslamicSpacing.Small else IslamicSpacing.Medium))

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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = cardPadding,
                vertical = heroVerticalPadding,
            ),
        ) {
            Box {
                IslamicDecorationCorners(
                    tint = MaterialTheme.colorScheme.tertiary,
                    compact = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.nextPrayer?.let { prayer ->
                        PrayerTimeIcon(
                            prayer = prayer,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(heroIconSize),
                        )
                        Spacer(Modifier.width(IslamicSpacing.Small))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_next_prayer),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            state.nextPrayer?.let { prayer ->
                                Text(
                                    text = stringResource(prayerLabelRes(prayer)),
                                    style = if (narrowLayout) {
                                        MaterialTheme.typography.titleLarge
                                    } else {
                                        MaterialTheme.typography.headlineSmall
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            state.nextPrayerAt?.let { at ->
                                Spacer(Modifier.width(IslamicSpacing.Small))
                                Text(
                                    text = at.format(TimeFormats.timeFormatter(use24h)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(IslamicSpacing.Small))
                    Text(
                        text = formatCountdown(state.countdownSeconds),
                        style = if (narrowLayout) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(sectionGap))

        // ---- Today's times ----
        MuslimSectionHeader(
            title = stringResource(R.string.home_today_times),
            supportingText = state.selectedDate.format(localDateFormatter),
        )
        IslamicDecorationDivider(
            modifier = Modifier.padding(
                horizontal = if (narrowLayout) IslamicSpacing.Section else IslamicSpacing.PageHorizontal,
            ),
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
                horizontal = when {
                    narrowLayout -> IslamicSpacing.XSmall
                    compactLayout -> IslamicSpacing.Compact
                    else -> IslamicSpacing.Medium
                },
                vertical = IslamicSpacing.XSmall,
            ),
        ) {
            Box {
                IslamicDecorationCorners(
                    tint = MaterialTheme.colorScheme.primary,
                    compact = compactLayout,
                )
                Column {
                    trackablePrayers.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    val isNextPrayer = prayer == state.nextPrayer
                    val nextPrayerStateDescription = if (isNextPrayer) {
                        stringResource(R.string.home_next_prayer)
                    } else {
                        null
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = prayerRowOuterVerticalPadding)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (isNextPrayer) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
                            )
                            .padding(
                                horizontal = prayerRowHorizontalPadding,
                                vertical = prayerRowInnerVerticalPadding,
                            )
                            .then(
                                nextPrayerStateDescription?.let { description ->
                                    Modifier.semantics { stateDescription = description }
                                } ?: Modifier,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PrayerTimeIcon(
                            prayer = prayer,
                            tint = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(prayerIconSize),
                        )
                        Spacer(Modifier.width(IslamicSpacing.Small))
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            style = if (narrowLayout) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.weight(1f))
                        state.times[prayer]?.let { time ->
                            Text(
                                text = time.format(TimeFormats.timeFormatter(use24h)),
                                style = if (narrowLayout) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNextPrayer) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        PrayerAlertAction(
                            prayer = prayer,
                            alert = state.prayerAlerts[prayer] ?: HomeViewModel.PrayerAlert(),
                            isNextPrayer = isNextPrayer,
                            compact = compactLayout,
                            onClick = { customizingPrayer = prayer },
                        )
                    }
                    }
                }
            }
        }

        Spacer(Modifier.height(if (compactLayout) IslamicSpacing.Small else IslamicSpacing.Medium))

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
            IslamicSecondaryButton(
                modifier = Modifier.weight(1f),
                onClick = { shareDailyTimes(context, state, use24h) },
                enabled = state.isValid,
            ) {
                Text(stringResource(R.string.times_share))
            }
            Spacer(Modifier.width(IslamicSpacing.Small))
            IslamicSecondaryButton(
                modifier = Modifier.weight(1f),
                onClick = viewModel::toggleMonthly,
            ) {
                Text(stringResource(if (state.monthly) R.string.times_daily else R.string.times_monthly))
            }
        }

        if (state.monthly) {
            MonthlyPrayerTable(state, use24h)
        }

        if (showPrayerTrackerOnHome) {
            Spacer(Modifier.height(sectionGap))
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

@Composable
private fun PrayerTimeIcon(
    prayer: Prayer,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val unit = minOf(width, height)
        val stroke = (unit * 0.07f).coerceAtLeast(1f)

        fun line(x1: Float, y1: Float, x2: Float, y2: Float, widthScale: Float = 1f) {
            drawLine(
                color = tint,
                start = Offset(width * x1, height * y1),
                end = Offset(width * x2, height * y2),
                strokeWidth = stroke * widthScale,
                cap = StrokeCap.Round,
            )
        }

        fun sun(cx: Float, cy: Float, radius: Float, withRays: Boolean) {
            drawCircle(
                color = tint,
                radius = unit * radius,
                center = Offset(width * cx, height * cy),
            )
            if (withRays) {
                line(cx, cy - 0.36f, cx, cy - 0.27f)
                line(cx, cy + 0.27f, cx, cy + 0.36f)
                line(cx - 0.36f, cy, cx - 0.27f, cy)
                line(cx + 0.27f, cy, cx + 0.36f, cy)
                line(cx - 0.25f, cy - 0.25f, cx - 0.19f, cy - 0.19f)
                line(cx + 0.19f, cy - 0.19f, cx + 0.25f, cy - 0.25f)
                line(cx - 0.25f, cy + 0.25f, cx - 0.19f, cy + 0.19f)
                line(cx + 0.19f, cy + 0.19f, cx + 0.25f, cy + 0.25f)
            }
        }

        when (prayer) {
            Prayer.Fajr -> {
                val horizon = 0.72f
                line(0.12f, horizon, 0.88f, horizon, 1.05f)
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(width * 0.36f, height * 0.55f),
                    size = Size(width * 0.28f, height * 0.28f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                line(0.50f, 0.43f, 0.50f, 0.35f)
                line(0.36f, 0.49f, 0.30f, 0.43f)
                line(0.64f, 0.49f, 0.70f, 0.43f)
            }
            Prayer.Sunrise -> {
                val horizon = 0.72f
                line(0.10f, horizon, 0.90f, horizon, 1.05f)
                sun(0.50f, 0.58f, 0.13f, withRays = true)
            }
            Prayer.Dhuhr -> sun(0.50f, 0.50f, 0.15f, withRays = true)
            Prayer.Asr -> {
                line(0.10f, 0.78f, 0.90f, 0.78f, 1.05f)
                sun(0.64f, 0.48f, 0.13f, withRays = true)
            }
            Prayer.Maghrib -> {
                val horizon = 0.63f
                line(0.10f, horizon, 0.90f, horizon, 1.1f)
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(width * 0.33f, height * 0.46f),
                    size = Size(width * 0.34f, height * 0.34f),
                    style = Stroke(width = stroke * 1.15f, cap = StrokeCap.Round),
                )
                line(0.24f, 0.76f, 0.76f, 0.76f, 0.85f)
            }
            Prayer.Isha -> {
                drawArc(
                    color = tint,
                    startAngle = 52f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(width * 0.16f, height * 0.14f),
                    size = Size(width * 0.60f, height * 0.70f),
                    style = Stroke(width = stroke * 1.65f, cap = StrokeCap.Round),
                )
                line(0.77f, 0.22f, 0.77f, 0.36f, 0.85f)
                line(0.70f, 0.29f, 0.84f, 0.29f, 0.85f)
            }
        }
    }
}

@Composable
private fun monthWeekdayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.SATURDAY -> stringResource(R.string.times_week_sat)
    DayOfWeek.SUNDAY -> stringResource(R.string.times_week_sun)
    DayOfWeek.MONDAY -> stringResource(R.string.times_week_mon)
    DayOfWeek.TUESDAY -> stringResource(R.string.times_week_tue)
    DayOfWeek.WEDNESDAY -> stringResource(R.string.times_week_wed)
    DayOfWeek.THURSDAY -> stringResource(R.string.times_week_thu)
    DayOfWeek.FRIDAY -> stringResource(R.string.times_week_fri)
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

/** Full monthly timetable: one day per row, with all five obligatory prayers. */
@Composable
private fun MonthlyPrayerTable(state: HomeViewModel.UiState, use24h: Boolean) {
    val prayers = trackablePrayers
    val formatter = TimeFormats.timeFormatter(use24h)

    Spacer(Modifier.height(IslamicSpacing.Small))
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MonthlyTableHeader(
                monthLabel = "${state.month.monthValue}/${state.month.year}",
                prayers = prayers,
            )
            state.monthDays.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider()
                MonthlyTableDayRow(
                    index = index,
                    day = day,
                    prayers = prayers,
                    formatter = formatter,
                    selected = day.date == state.selectedDate,
                )
            }
        }
    }
}

@Composable
private fun MonthlyTableHeader(
    monthLabel: String,
    prayers: List<Prayer>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f))
            .padding(vertical = IslamicSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(0.92f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
        }
        prayers.forEach { prayer ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PrayerTimeIcon(
                    prayer = prayer,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(IslamicIconSize.Supporting),
                )
                Text(
                    text = stringResource(prayerLabelRes(prayer)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun MonthlyTableDayRow(
    index: Int,
    day: HomeViewModel.DayTimes,
    prayers: List<Prayer>,
    formatter: java.time.format.DateTimeFormatter,
    selected: Boolean,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when {
                    selected -> MaterialTheme.colorScheme.tertiaryContainer
                    index % 2 == 1 -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.34f)
                    else -> Color.Transparent
                },
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthlyDateCell(day = day, selected = selected)
        prayers.forEach { prayer ->
            MonthlyPrayerTimeCell(
                value = day.timeFor(prayer)?.format(formatter) ?: "—",
                selected = selected,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun MonthlyDateCell(
    day: HomeViewModel.DayTimes,
    selected: Boolean,
) {
    Column(
        modifier = Modifier.weight(0.92f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = monthWeekdayLabel(day.date.dayOfWeek),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
        )
        Text(
            text = day.hijriDay.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

@Composable
private fun MonthlyPrayerTimeCell(
    value: String,
    selected: Boolean,
    contentColor: Color,
) {
    Text(
        text = value,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Clip,
    )
}

