package org.muslim.app.feature.qibla.ui

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Surface
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.EntryPointAccessors
import org.muslim.app.core.location.MagneticDeclination
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionEntryPoint
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicDecorationMedallion
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.domain.QiblaCalculator
import org.muslim.app.feature.qibla.mosques.NearbyMosquesTab
import org.muslim.app.feature.qibla.mosques.NearbyMosquesViewModel
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Qibla compass (PROJECT_PROMPT.md §6 Phase 1).
 *
 * The rose rotates with the device so that its north tick always points to
 * true north (magnetic heading corrected by the local magnetic declination);
 * the 🕋 marker shows the Qibla bearing. Rotate the phone until the Kaaba
 * sits at the top indicator — you are then facing the Kaaba.
 *
 * A live GPS refresh button lets the user replace the persisted location with
 * an on-demand fix (requested only while the screen is open).
 */
@Composable
fun QiblaScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    modifier: Modifier = Modifier,
    viewModel: QiblaGpsViewModel = hiltViewModel(),
    nearbyMosquesViewModel: NearbyMosquesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(QIBLA_TAB_INDEX) }
    val mosquePresentation by nearbyMosquesViewModel.presentation.collectAsStateWithLifecycle()
    val requestLocationFor = rememberLocationRefreshAction(
        context = context,
        qiblaViewModel = viewModel,
        mosquesViewModel = nearbyMosquesViewModel,
    )
    DisposableEffect(selectedTab) {
        onDispose {
            if (selectedTab != QIBLA_TAB_INDEX) nearbyMosquesViewModel.deactivate()
        }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab != QIBLA_TAB_INDEX) requestLocationFor(LocationConsumer.Mosques)
    }

    Column(modifier = modifier.fillMaxSize()) {
        QiblaTopTabs(
            selectedTab = selectedTab,
            onSelect = { tab ->
                selectedTab = tab
                if (tab == QIBLA_TAB_INDEX) nearbyMosquesViewModel.deactivate()
            },
        )
        if (selectedTab == QIBLA_TAB_INDEX) {
            QiblaCompassTab(
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                viewModel = viewModel,
                onGpsRefresh = { requestLocationFor(LocationConsumer.Qibla) },
                modifier = Modifier.weight(1f),
            )
        } else {
            NearbyMosquesTab(
                presentation = mosquePresentation,
                onRefresh = { requestLocationFor(LocationConsumer.Mosques) },
                onRadiusSelected = nearbyMosquesViewModel::selectRadius,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val QIBLA_TAB_INDEX = 0

private enum class LocationConsumer { Qibla, Mosques }

@Composable
internal fun QiblaTopTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.qibla_tab_qibla),
        stringResource(R.string.qibla_tab_mosques),
    )
    PrimaryTabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                modifier = Modifier.heightIn(min = 48.dp),
                text = {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
            )
        }
    }
}

@Composable
private fun QiblaCompassTab(
    latitude: Double,
    longitude: Double,
    locationName: String,
    viewModel: QiblaGpsViewModel,
    onGpsRefresh: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()

    // Effective location: the persisted one until a live GPS fix arrives.
    var effectiveLat by remember { mutableDoubleStateOf(latitude) }
    var effectiveLng by remember { mutableDoubleStateOf(longitude) }
    var effectiveName by remember { mutableStateOf(locationName) }
    val gpsLiveName = stringResource(R.string.qibla_gps_live)

    LaunchedEffect(gpsState) {
        when (val s = gpsState) {
            is QiblaGpsState.Fix -> {
                effectiveLat = s.latitude
                effectiveLng = s.longitude
                effectiveName = gpsLiveName
            }
            else -> Unit
        }
    }

    // Sensor work is composed only while the Qibla tab is visible.
    val displayRotation = displayRotationDegrees(context)
    val declination = remember(effectiveLat, effectiveLng) {
        MagneticDeclination.declinationDegrees(context, effectiveLat, effectiveLng)
    }
    val headingState = rememberCompassHeading(displayRotationDegrees = displayRotation)
    val heading by headingState

    val bearing = QiblaCalculator.direction(effectiveLat, effectiveLng)
    val distanceKm = QiblaCalculator.distanceKm(effectiveLat, effectiveLng)
    val trueHeading = (heading.heading + declination) % 360f
    val turnClockwise = ((bearing - trueHeading) % 360.0 + 360.0) % 360.0
    val facingQibla = heading.isLevel && (turnClockwise < 2.0 || turnClockwise > 358.0)
    val turnRight = turnClockwise <= 180.0
    val turnDegrees = if (turnRight) turnClockwise else 360.0 - turnClockwise

    val cardinalNames = stringArrayResource(R.array.qibla_cardinal_directions)
    fun cardinal(degrees: Double): String = cardinalNames[(((degrees + 22.5) / 45.0).toInt() % 8 + 8) % 8]

    var wasFacingQibla by remember { mutableStateOf(false) }
    LaunchedEffect(facingQibla) {
        if (facingQibla && !wasFacingQibla) triggerQiblaHapticFeedback(context)
        wasFacingQibla = facingQibla
    }

    QiblaCompassContent(
        gpsState = gpsState,
        presentation = QiblaPresentation(
            locationName = effectiveName,
            trueHeading = trueHeading,
            bearing = bearing,
            distanceKm = distanceKm,
            bearingCardinal = cardinal(bearing),
            headingCardinal = cardinal(trueHeading.toDouble()),
            facingQibla = facingQibla,
            turnRight = turnRight,
            turnDegrees = turnDegrees,
            needsCalibration = heading.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM,
            needsFlatPosture = !heading.isLevel,
        ),
        modifier = modifier,
        onGpsRefresh = onGpsRefresh,
    )
}


internal data class QiblaPresentation(
    val locationName: String,
    val trueHeading: Float,
    val bearing: Double,
    val distanceKm: Double,
    val bearingCardinal: String,
    val headingCardinal: String,
    val facingQibla: Boolean,
    val turnRight: Boolean,
    val turnDegrees: Double,
    val needsCalibration: Boolean,
    val needsFlatPosture: Boolean,
)

@Composable
private fun rememberLocationRefreshAction(
    context: Context,
    qiblaViewModel: QiblaGpsViewModel,
    mosquesViewModel: NearbyMosquesViewModel,
): (LocationConsumer) -> Unit {
    var pendingConsumer by remember { mutableStateOf<LocationConsumer?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val consumer = pendingConsumer
        pendingConsumer = null
        if (result.values.any { it }) {
            when (consumer) {
                LocationConsumer.Qibla -> qiblaViewModel.refresh()
                LocationConsumer.Mosques -> mosquesViewModel.refresh()
                null -> Unit
            }
        } else if (consumer == LocationConsumer.Mosques) {
            mosquesViewModel.onPermissionDenied()
        }
    }
    return { consumer ->
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionEntryPoint::class.java,
        )
        val manager = entryPoint.permissionManager()
        if (manager.isGranted(AppPermission.Location)) {
            when (consumer) {
                LocationConsumer.Qibla -> qiblaViewModel.refresh()
                LocationConsumer.Mosques -> mosquesViewModel.refresh()
            }
        } else {
            pendingConsumer = consumer
            permissionLauncher.launch(manager.runtimeRequestArray(AppPermission.Location) ?: arrayOf())
        }
    }
}

@Composable
internal fun QiblaCompassContent(
    gpsState: QiblaGpsState,
    presentation: QiblaPresentation,
    modifier: Modifier,
    onGpsRefresh: () -> Unit,
) {
    val compassDescription = stringResource(
        R.string.qibla_compass_description,
        presentation.trueHeading,
        presentation.headingCardinal,
        stringResource(R.string.qibla_marker_description),
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxHeight < 620.dp
        val horizontalPadding = when {
            maxWidth < 360.dp -> 10.dp
            maxWidth >= 600.dp -> 24.dp
            else -> 16.dp
        }
        val verticalPadding = if (compact) 6.dp else 10.dp
        val gap = if (compact) 6.dp else 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QiblaLocationSummary(
                gpsState = gpsState,
                presentation = presentation,
                onGpsRefresh = onGpsRefresh,
                compact = compact,
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
            )

            Spacer(Modifier.height(gap))

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val compassSide = minOf(maxWidth, maxHeight, 420.dp)
                IslamicCard(
                    modifier = Modifier.size(compassSide),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(if (compact) 6.dp else 10.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        IslamicDecorationMedallion(
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxSize(),
                        )
                        CompassRose(
                            trueHeading = presentation.trueHeading,
                            bearing = presentation.bearing,
                            aligned = presentation.facingQibla,
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics { contentDescription = compassDescription },
                        )
                    }
                }
            }

            Spacer(Modifier.height(gap))

            QiblaDirectionDetails(
                presentation = presentation,
                compact = compact,
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
            )
        }
    }
}

@Composable
private fun QiblaLocationSummary(
    gpsState: QiblaGpsState,
    presentation: QiblaPresentation,
    onGpsRefresh: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    IslamicCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(
            horizontal = if (compact) 12.dp else 16.dp,
            vertical = if (compact) 10.dp else 12.dp,
        ),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = presentation.locationName,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.qibla_bearing_cardinal, presentation.bearingCardinal),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                )
            }
            GpsRefreshControl(
                gpsState = gpsState,
                onGpsRefresh = onGpsRefresh,
                compact = compact,
            )
        }
        if (gpsState == QiblaGpsState.Error) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.qibla_gps_error),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun GpsRefreshControl(
    gpsState: QiblaGpsState,
    onGpsRefresh: () -> Unit,
    compact: Boolean,
) {
    val requesting = gpsState == QiblaGpsState.Requesting
    val description = stringResource(
        if (requesting) R.string.qibla_gps_refreshing else R.string.qibla_gps_refresh,
    )
    OutlinedIconButton(
        onClick = onGpsRefresh,
        enabled = !requesting,
        modifier = Modifier
            .size(if (compact) 44.dp else 48.dp)
            .semantics { contentDescription = description },
    ) {
        if (requesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 18.dp else 20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 19.dp else 21.dp),
            )
        }
    }
}

@Composable
private fun QiblaDirectionDetails(
    presentation: QiblaPresentation,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val direction = when {
        presentation.facingQibla -> stringResource(R.string.qibla_facing)
        presentation.turnRight -> stringResource(R.string.qibla_turn_right, presentation.turnDegrees)
        else -> stringResource(R.string.qibla_turn_left, presentation.turnDegrees)
    }
    val postureMessage = when {
        presentation.needsFlatPosture -> stringResource(R.string.qibla_hold_flat)
        presentation.needsCalibration -> stringResource(R.string.qibla_calibrate)
        else -> null
    }

    IslamicCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(
            horizontal = if (compact) 12.dp else 16.dp,
            vertical = if (compact) 9.dp else 12.dp,
        ),
        containerColor = if (presentation.facingQibla) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Text(
                text = "🕋",
                fontSize = if (compact) 22.sp else 26.sp,
            )
            Column(modifier = Modifier.weight(0.9f)) {
                Text(
                    text = stringResource(R.string.qibla_bearing_degree, presentation.bearing),
                    maxLines = 1,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.qibla_bearing_cardinal, presentation.bearingCardinal),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = direction,
                modifier = Modifier.weight(1.1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (presentation.facingQibla) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }

        Spacer(Modifier.height(if (compact) 5.dp else 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.qibla_heading_degree,
                    presentation.trueHeading,
                    presentation.headingCardinal,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.qibla_distance,
                    stringResource(R.string.qibla_distance_km, presentation.distanceKm),
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        postureMessage?.let { message ->
            Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
            Text(
                text = message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

/** Current display rotation in degrees (0/90/180/270), API-agnostic. */
@Suppress("DEPRECATION")
private fun displayRotationDegrees(context: Context): Int {
    val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display?.rotation ?: Surface.ROTATION_0
    } else {
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }
    return when (rotation) {
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }
}

@Composable
private fun CompassRose(
    trueHeading: Float,
    bearing: Double,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    val northColor = MaterialTheme.colorScheme.tertiary
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val rimColor = MaterialTheme.colorScheme.outline
    val qiblaColor = if (aligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    val animatedHeading by animateFloatAsState(
        targetValue = trueHeading,
        animationSpec = tween(durationMillis = 180),
        label = "qiblaHeading",
    )

    val textMeasurer = rememberTextMeasurer()
    val degreeStyle = remember { TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium) }
    val degreeLayouts = remember(textMeasurer) {
        (0 until 360 step 30).associateWith { deg ->
            textMeasurer.measure(AnnotatedString(deg.toString()), degreeStyle)
        }
    }
    val cardinalStyle = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    val cardinalLayouts = remember(textMeasurer) {
        mapOf(
            0 to textMeasurer.measure(AnnotatedString("N"), cardinalStyle),
            90 to textMeasurer.measure(AnnotatedString("E"), cardinalStyle),
            180 to textMeasurer.measure(AnnotatedString("S"), cardinalStyle),
            270 to textMeasurer.measure(AnnotatedString("W"), cardinalStyle),
        )
    }
    val kaabaEmojiLayout = remember(textMeasurer) {
        textMeasurer.measure(AnnotatedString("🕋"), TextStyle(fontSize = 22.sp))
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (min(size.width, size.height) / 2f - 12.dp.toPx()).coerceAtLeast(1f)

        // Layered bezel gives the dial depth without shadows or bitmap assets.
        drawCircle(
            color = qiblaColor.copy(alpha = if (aligned) 0.12f else 0.06f),
            radius = radius,
        )
        drawCircle(
            color = rimColor.copy(alpha = 0.90f),
            radius = radius,
            style = Stroke(2.2.dp.toPx()),
        )
        drawCircle(
            color = rimColor.copy(alpha = 0.28f),
            radius = radius * 0.93f,
            style = Stroke(1.dp.toPx()),
        )
        drawCircle(
            color = rimColor.copy(alpha = 0.16f),
            radius = radius * 0.67f,
            style = Stroke(1.dp.toPx()),
        )

        rotate(degrees = -animatedHeading, pivot = center) {
            // Precision ticks every 5°; stronger marks at 10°, 30° and cardinal axes.
            for (deg in 0 until 360 step 5) {
                val radians = Math.toRadians(deg.toDouble())
                val majorCardinal = deg % 90 == 0
                val major = deg % 30 == 0
                val medium = deg % 10 == 0
                val outer = radius * 0.90f
                val inner = when {
                    majorCardinal -> radius * 0.77f
                    major -> radius * 0.80f
                    medium -> radius * 0.84f
                    else -> radius * 0.87f
                }
                val color = if (deg == 0) northColor else tickColor
                drawLine(
                    color = color.copy(
                        alpha = when {
                            majorCardinal -> 1f
                            major -> 0.82f
                            medium -> 0.58f
                            else -> 0.32f
                        },
                    ),
                    start = Offset(
                        center.x + (inner * sin(radians)).toFloat(),
                        center.y - (inner * cos(radians)).toFloat(),
                    ),
                    end = Offset(
                        center.x + (outer * sin(radians)).toFloat(),
                        center.y - (outer * cos(radians)).toFloat(),
                    ),
                    strokeWidth = when {
                        majorCardinal -> 2.8.dp.toPx()
                        major -> 2.dp.toPx()
                        medium -> 1.35.dp.toPx()
                        else -> 0.9.dp.toPx()
                    },
                )
            }

            // Degree labels every 30° with clear cardinal anchors.
            for (deg in 0 until 360 step 30) {
                val layout = cardinalLayouts[deg] ?: degreeLayouts.getValue(deg)
                val radians = Math.toRadians(deg.toDouble())
                val labelRadius = radius * 0.70f
                val pos = Offset(
                    center.x + (labelRadius * sin(radians)).toFloat() - layout.size.width / 2f,
                    center.y - (labelRadius * cos(radians)).toFloat() - layout.size.height / 2f,
                )
                rotate(
                    degrees = deg.toFloat(),
                    pivot = Offset(
                        pos.x + layout.size.width / 2f,
                        pos.y + layout.size.height / 2f,
                    ),
                ) {
                    drawText(
                        textLayoutResult = layout,
                        topLeft = pos,
                        color = if (deg == 0) northColor else labelColor,
                    )
                }
            }
        }

        // Qibla vector stays screen-relative while the dial rotates underneath it.
        val dialAngle = Math.toRadians(bearing - animatedHeading)
        val qiblaLineEndRadius = radius * 0.79f
        val markerRadius = radius * 0.58f
        val lineEnd = Offset(
            center.x + (qiblaLineEndRadius * sin(dialAngle)).toFloat(),
            center.y - (qiblaLineEndRadius * cos(dialAngle)).toFloat(),
        )
        val markerCenter = Offset(
            center.x + (markerRadius * sin(dialAngle)).toFloat(),
            center.y - (markerRadius * cos(dialAngle)).toFloat(),
        )
        drawLine(
            color = qiblaColor.copy(alpha = if (aligned) 0.88f else 0.62f),
            start = center,
            end = lineEnd,
            strokeWidth = if (aligned) 3.dp.toPx() else 2.dp.toPx(),
        )
        drawCircle(
            color = qiblaColor.copy(alpha = if (aligned) 0.28f else 0.16f),
            radius = 21.dp.toPx(),
            center = markerCenter,
        )
        drawCircle(
            color = qiblaColor.copy(alpha = 0.78f),
            radius = 18.dp.toPx(),
            center = markerCenter,
            style = Stroke(1.2.dp.toPx()),
        )
        drawText(
            textLayoutResult = kaabaEmojiLayout,
            topLeft = Offset(
                markerCenter.x - kaabaEmojiLayout.size.width / 2f,
                markerCenter.y - kaabaEmojiLayout.size.height / 2f,
            ),
        )

        // Fixed phone-forward indicator.
        val indicatorTop = center.y - radius - 1.dp.toPx()
        val indicatorBase = center.y - radius + 20.dp.toPx()
        val indicator = Path().apply {
            moveTo(center.x, indicatorTop)
            lineTo(center.x - 11.dp.toPx(), indicatorBase)
            lineTo(center.x + 11.dp.toPx(), indicatorBase)
            close()
        }
        drawPath(indicator, color = northColor)

        // Refined central hub.
        drawCircle(color = qiblaColor.copy(alpha = 0.16f), radius = 8.dp.toPx(), center = center)
        drawCircle(color = qiblaColor, radius = 3.5.dp.toPx(), center = center)
    }
}

/**
 * A non-audio haptic pulse fired once when the phone first aligns with the
 * Qibla (then again only after the user turns away). No system tone is used.
 */
private fun triggerQiblaHapticFeedback(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() == true) {
        runCatching {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}