package org.muslim.app.feature.qibla.ui

import android.content.Context
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.EntryPointAccessors
import org.muslim.app.core.location.MagneticDeclination
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionEntryPoint
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.domain.QiblaCalculator
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
) {
    val context = LocalContext.current
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()

    // Effective location: the persisted one until a live GPS fix arrives.
    var effectiveLat by remember { mutableDoubleStateOf(latitude) }
    var effectiveLng by remember { mutableDoubleStateOf(longitude) }
    var effectiveName by remember { mutableStateOf(locationName) }
    var usingGps by remember { mutableStateOf(false) }
    val gpsLiveName = stringResource(R.string.qibla_gps_live)

    LaunchedEffect(gpsState) {
        when (val s = gpsState) {
            is QiblaGpsState.Fix -> {
                effectiveLat = s.latitude
                effectiveLng = s.longitude
                effectiveName = gpsLiveName
                usingGps = true
            }
            else -> Unit
        }
    }

    // Dial must match the screen top in both portrait and landscape.
    val displayRotation = displayRotationDegrees(context)
    val declination = remember(effectiveLat, effectiveLng) {
        MagneticDeclination.declinationDegrees(context, effectiveLat, effectiveLng)
    }
    val headingState = rememberCompassHeading(displayRotationDegrees = displayRotation)
    val heading by headingState

    val bearing = QiblaCalculator.direction(effectiveLat, effectiveLng)
    val distanceKm = QiblaCalculator.distanceKm(effectiveLat, effectiveLng)
    val trueHeading = (heading.heading + declination) % 360f

    // Clockwise turn needed to face the Kaaba from the current heading.
    val turnClockwise = ((bearing - trueHeading) % 360.0 + 360.0) % 360.0
    val facingQibla = turnClockwise < 2.0 || turnClockwise > 358.0
    val turnRight = turnClockwise <= 180.0
    val turnDegrees = if (turnRight) turnClockwise else 360.0 - turnClockwise

    val cardinalNames = stringArrayResource(R.array.qibla_cardinal_directions)
    fun cardinal(degrees: Double): String = cardinalNames[(((degrees + 22.5) / 45.0).toInt() % 8 + 8) % 8]

    // Haptic pulse + short confirmation beep the moment the phone faces the qibla.
    var wasFacingQibla by remember { mutableStateOf(false) }
    LaunchedEffect(facingQibla) {
        if (facingQibla && !wasFacingQibla) {
            triggerQiblaFeedback(context)
        }
        wasFacingQibla = facingQibla
    }

    val requestGpsRefresh = rememberGpsRefreshAction(context, viewModel)
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
        ),
        modifier = modifier,
        onGpsRefresh = requestGpsRefresh,
    )
}


private data class QiblaPresentation(
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
)

@Composable
private fun rememberGpsRefreshAction(context: Context, viewModel: QiblaGpsViewModel): () -> Unit {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) viewModel.refresh()
    }
    return {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionEntryPoint::class.java,
        )
        val manager = entryPoint.permissionManager()
        if (manager.isGranted(AppPermission.Location)) viewModel.refresh()
        else permissionLauncher.launch(manager.runtimeRequestArray(AppPermission.Location) ?: arrayOf())
    }
}

@Composable
private fun QiblaCompassContent(
    gpsState: QiblaGpsState,
    presentation: QiblaPresentation,
    modifier: Modifier,
    onGpsRefresh: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(presentation.locationName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        GpsRefreshControl(gpsState, onGpsRefresh)
        Spacer(Modifier.height(16.dp))
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            CompassRose(
                trueHeading = presentation.trueHeading,
                bearing = presentation.bearing,
                modifier = Modifier.size(320.dp).padding(8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        QiblaDirectionDetails(presentation)
    }
}

@Composable
private fun GpsRefreshControl(gpsState: QiblaGpsState, onGpsRefresh: () -> Unit) {
    OutlinedButton(onClick = onGpsRefresh, enabled = gpsState != QiblaGpsState.Requesting) {
        if (gpsState == QiblaGpsState.Requesting) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(8.dp))
        Text(stringResource(if (gpsState == QiblaGpsState.Requesting) R.string.qibla_gps_refreshing else R.string.qibla_gps_refresh))
    }
    if (gpsState == QiblaGpsState.Error) {
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.qibla_gps_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun QiblaDirectionDetails(presentation: QiblaPresentation) {
    Text(
        stringResource(R.string.qibla_bearing_degree, presentation.bearing),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        "🕋 " + stringResource(R.string.qibla_bearing_cardinal, presentation.bearingCardinal),
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.qibla_heading_degree, presentation.trueHeading, presentation.headingCardinal),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        when {
            presentation.facingQibla -> stringResource(R.string.qibla_facing)
            presentation.turnRight -> stringResource(R.string.qibla_turn_right, presentation.turnDegrees)
            else -> stringResource(R.string.qibla_turn_left, presentation.turnDegrees)
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (presentation.facingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    if (presentation.needsCalibration) {
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.qibla_calibrate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.qibla_distance, stringResource(R.string.qibla_distance_km, presentation.distanceKm)),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(24.dp))
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
private fun CompassRose(trueHeading: Float, bearing: Double, modifier: Modifier = Modifier) {
    val northColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val rimColor = MaterialTheme.colorScheme.outline

    // Smooth dial motion — the needle glides instead of jumping.
    val animatedHeading by animateFloatAsState(
        targetValue = trueHeading,
        animationSpec = tween(durationMillis = 180),
        label = "qiblaHeading",
    )

    val textMeasurer = rememberTextMeasurer()
    val kaabaStyle = remember { TextStyle(fontSize = 34.sp) }
    val kaabaLayout = remember(textMeasurer) {
        textMeasurer.measure(AnnotatedString("🕋"), kaabaStyle)
    }
    // Degree numbers every 30° around the rim, oriented radially like a real compass.
    val degreeStyle = remember { TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold) }
    val degreeLayouts = remember(textMeasurer) {
        (0 until 360 step 30).associateWith { deg ->
            textMeasurer.measure(AnnotatedString(deg.toString()), degreeStyle)
        }
    }
    // Cardinal letters replace the numbers at the four main points.
    val cardinalStyle = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    val cardinalLayouts = remember(textMeasurer) {
        mapOf(
            0 to textMeasurer.measure(AnnotatedString("N"), cardinalStyle),
            90 to textMeasurer.measure(AnnotatedString("E"), cardinalStyle),
            180 to textMeasurer.measure(AnnotatedString("S"), cardinalStyle),
            270 to textMeasurer.measure(AnnotatedString("W"), cardinalStyle),
        )
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        // Extra margin so the Kaaba marker (drawn outside the rim) and the top
        // indicator never clip and the marker stays clear of the ring.
        val radius = min(size.width, size.height) / 2 - 52.dp.toPx()

        // Rim
        drawCircle(color = rimColor, radius = radius, style = Stroke(3.dp.toPx()))
        drawCircle(
            color = rimColor.copy(alpha = 0.3f),
            radius = radius * 0.96f,
            style = Stroke(1.dp.toPx()),
        )

        // The rose rotates so its north tick points to true north.
        rotate(degrees = -animatedHeading, pivot = center) {
            // Degree numbers + cardinal letters, radially oriented.
            for (deg in 0 until 360 step 30) {
                val layout = cardinalLayouts[deg] ?: degreeLayouts.getValue(deg)
                val a = Math.toRadians(deg.toDouble())
                val labelRadius = radius * 0.80f
                val pos = Offset(
                    center.x + (labelRadius * sin(a)).toFloat() - layout.size.width / 2f,
                    center.y - (labelRadius * cos(a)).toFloat() - layout.size.height / 2f,
                )
                // Keep each glyph upright relative to the dial (its top points
                // outward along the radius, exactly like a real compass rose).
                rotate(
                    degrees = deg.toFloat(),
                    pivot = Offset(pos.x + layout.size.width / 2f, pos.y + layout.size.height / 2f),
                ) {
                    drawText(layout, topLeft = pos, color = if (deg == 0) northColor else labelColor)
                }
            }

            // Cardinal ticks (longer at the four main points).
            for (i in 0 until 4) {
                val angle = i * 90.0
                val isNorth = i == 0
                val outer = radius * if (isNorth) 0.98f else 0.90f
                val inner = radius * 0.70f
                val a = Math.toRadians(angle)
                drawLine(
                    color = if (isNorth) northColor else tickColor,
                    start = Offset(
                        center.x + (inner * sin(a)).toFloat(),
                        center.y - (inner * cos(a)).toFloat(),
                    ),
                    end = Offset(
                        center.x + (outer * sin(a)).toFloat(),
                        center.y - (outer * cos(a)).toFloat(),
                    ),
                    strokeWidth = if (isNorth) 5.dp.toPx() else 2.5.dp.toPx(),
                )
            }

            // Minor ticks every 30° (aligned with the degree labels) so the
            // rose reads like a real compass dial.
            for (deg in 0 until 360 step 30) {
                if (deg % 90 == 0) continue
                val a = Math.toRadians(deg.toDouble())
                val outer = radius * 0.84f
                val inner = radius * 0.78f
                drawLine(
                    color = tickColor.copy(alpha = 0.7f),
                    start = Offset(
                        center.x + (inner * sin(a)).toFloat(),
                        center.y - (inner * cos(a)).toFloat(),
                    ),
                    end = Offset(
                        center.x + (outer * sin(a)).toFloat(),
                        center.y - (outer * cos(a)).toFloat(),
                    ),
                    strokeWidth = 1.5.dp.toPx(),
                )
            }

        }

        // The Kaaba marker rides just outside the rim at the dial-relative
        // bearing, always upright, with a clearance gap so it never touches
        // the ring.
        val dialAngle = Math.toRadians(bearing - animatedHeading)
        val markerRadius = radius + kaabaLayout.size.height / 2f + 10.dp.toPx()
        val markerPos = Offset(
            center.x + (markerRadius * sin(dialAngle)).toFloat() - kaabaLayout.size.width / 2f,
            center.y - (markerRadius * cos(dialAngle)).toFloat() - kaabaLayout.size.height / 2f,
        )
        drawText(kaabaLayout, topLeft = markerPos)

        // Fixed indicator at the top: the phone's forward direction.
        val indicator = Path().apply {
            moveTo(center.x - 10.dp.toPx(), center.y - radius + 12.dp.toPx())
            lineTo(center.x, center.y - radius - 6.dp.toPx())
            lineTo(center.x + 10.dp.toPx(), center.y - radius + 12.dp.toPx())
            close()
        }
        drawPath(indicator, color = northColor)

        // Center pivot dot for a finished look.
        drawCircle(color = rimColor, radius = 3.dp.toPx())
    }
}

/**
 * Haptic pulse + short confirmation beep fired once when the phone first
 * aligns with the qibla (then again only after the user turns away).
 */
private fun triggerQiblaFeedback(context: Context) {
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
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 400)
    }
}