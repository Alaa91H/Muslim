package org.muslim.app.feature.qibla.ui

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
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
    onOpenMosques: (() -> Unit)? = null,
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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) viewModel.refresh()
    }

    fun requestGpsRefresh() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionEntryPoint::class.java,
        )
        val manager = entryPoint.permissionManager()
        if (manager.isGranted(AppPermission.Location)) {
            viewModel.refresh()
        } else {
            permissionLauncher.launch(
                manager.runtimeRequestArray(AppPermission.Location) ?: arrayOf()
            )
        }
    }

    var viewMode by remember { mutableStateOf(QiblaViewMode.Compass) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = effectiveName,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = viewMode == QiblaViewMode.Compass,
                onClick = { viewMode = QiblaViewMode.Compass },
                label = { Text(stringResource(R.string.qibla_mode_compass)) },
            )
            Spacer(Modifier.size(8.dp))
            FilterChip(
                selected = viewMode == QiblaViewMode.Map,
                onClick = { viewMode = QiblaViewMode.Map },
                label = { Text(stringResource(R.string.qibla_mode_map)) },
            )
            if (onOpenMosques != null) {
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = onOpenMosques) {
                    Text(stringResource(R.string.qibla_open_mosques))
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { requestGpsRefresh() },
            enabled = gpsState != QiblaGpsState.Requesting,
        ) {
            if (gpsState == QiblaGpsState.Requesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.size(8.dp))
            } else {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text = stringResource(
                    if (gpsState == QiblaGpsState.Requesting) R.string.qibla_gps_refreshing
                    else R.string.qibla_gps_refresh
                )
            )
        }
        if (gpsState == QiblaGpsState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.qibla_gps_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(16.dp))

        when (viewMode) {
            QiblaViewMode.Compass -> {
                Surface(shape = MaterialTheme.shapes.extraLarge) {
                    CompassRose(
                        trueHeading = trueHeading,
                        bearing = bearing,
                        modifier = Modifier
                            .size(320.dp)
                            .padding(8.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.qibla_bearing_degree, bearing),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "🕋 " + stringResource(R.string.qibla_bearing_cardinal, cardinal(bearing)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.qibla_heading_degree,
                        trueHeading,
                        cardinal(trueHeading.toDouble()),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        facingQibla -> stringResource(R.string.qibla_facing)
                        turnRight -> stringResource(R.string.qibla_turn_right, turnDegrees)
                        else -> stringResource(R.string.qibla_turn_left, turnDegrees)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (facingQibla) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (heading.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.qibla_calibrate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            QiblaViewMode.Map -> {
                QiblaMapView(
                    latitude = effectiveLat,
                    longitude = effectiveLng,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(Modifier.height(12.dp))
                QiblaMapLegend()
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(
                R.string.qibla_distance,
                stringResource(R.string.qibla_distance_km, distanceKm),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
    }
}

private enum class QiblaViewMode { Compass, Map }

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
    val northColor = MaterialTheme.colorScheme.error
    val qiblaColor = Color(0xFFD4A017)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val rimColor = MaterialTheme.colorScheme.outline

    // Smooth dial motion — the needle glides instead of jumping.
    val animatedHeading by animateFloatAsState(
        targetValue = trueHeading,
        animationSpec = tween(durationMillis = 180),
        label = "qiblaHeading",
    )

    val textMeasurer = rememberTextMeasurer()
    val kaabaStyle = remember { TextStyle(fontSize = 40.sp) }
    val kaabaLayout = remember(textMeasurer) {
        textMeasurer.measure(AnnotatedString("🕋"), kaabaStyle)
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = min(size.width, size.height) / 2 - 8.dp.toPx()

        // Rim
        drawCircle(color = rimColor, radius = radius, style = Stroke(3.dp.toPx()))
        drawCircle(
            color = rimColor.copy(alpha = 0.3f),
            radius = radius * 0.92f,
            style = Stroke(1.dp.toPx()),
        )

        // The rose rotates so its north tick points to true north.
        rotate(degrees = -animatedHeading, pivot = center) {
            // Cardinal ticks (0° = north at top).
            for (i in 0 until 4) {
                val angle = i * 90.0
                val isNorth = i == 0
                val outer = radius * if (isNorth) 0.92f else 0.80f
                val inner = radius * 0.66f
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
                    strokeWidth = if (isNorth) 6.dp.toPx() else 3.dp.toPx(),
                )
            }

            // Qibla marker (gold needle + Kaaba emoji) at the bearing.
            rotate(degrees = bearing.toFloat(), pivot = center) {
                drawLine(
                    color = qiblaColor,
                    start = Offset(center.x, center.y - radius * 0.55f),
                    end = Offset(center.x, center.y - radius * 0.92f),
                    strokeWidth = 7.dp.toPx(),
                )
                drawText(
                    textLayoutResult = kaabaLayout,
                    topLeft = Offset(
                        center.x - kaabaLayout.size.width / 2f,
                        center.y - radius * 0.72f - kaabaLayout.size.height / 2f,
                    ),
                )
            }
        }

        // Fixed indicator at the top: the phone's forward direction.
        val indicator = Path().apply {
            moveTo(center.x - 10.dp.toPx(), center.y - radius + 12.dp.toPx())
            lineTo(center.x, center.y - radius - 6.dp.toPx())
            lineTo(center.x + 10.dp.toPx(), center.y - radius + 12.dp.toPx())
            close()
        }
        drawPath(indicator, color = northColor)
    }
}