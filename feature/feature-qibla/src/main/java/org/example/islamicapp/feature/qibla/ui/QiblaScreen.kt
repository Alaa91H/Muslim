package org.example.islamicapp.feature.qibla.ui

import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.islamicapp.core.location.MagneticDeclination
import org.example.islamicapp.feature.qibla.R
import org.example.islamicapp.feature.qibla.domain.QiblaCalculator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** View mode of the qibla screen: live compass or map alternative. */
private const val MODE_COMPASS = 0
private const val MODE_MAP = 1

/**
 * Qibla screen (PROJECT_PROMPT.md §6 Phase 1).
 *
 * Two alternatives: the live compass (the rose rotates with the device so its
 * north tick points to true north, corrected by the local magnetic
 * declination; the gold marker shows the Qibla bearing — rotate until the
 * marker sits at the top indicator) and an OpenStreetMap view drawing a
 * straight line from the user's location to the Kaaba ([QiblaMap]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val declination = remember(latitude, longitude) {
        MagneticDeclination.declinationDegrees(context, latitude, longitude)
    }
    val headingState = rememberCompassHeading()
    val heading by headingState

    val bearing = QiblaCalculator.direction(latitude, longitude)
    val distanceKm = QiblaCalculator.distanceKm(latitude, longitude)
    val trueHeading = (heading.heading + declination) % 360f

    var mode by rememberSaveable { mutableIntStateOf(MODE_COMPASS) }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = locationName,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = mode == MODE_COMPASS,
                onClick = { mode = MODE_COMPASS },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(stringResource(R.string.qibla_mode_compass))
            }
            SegmentedButton(
                selected = mode == MODE_MAP,
                onClick = { mode = MODE_MAP },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(stringResource(R.string.qibla_mode_map))
            }
        }

        Spacer(Modifier.height(20.dp))

        when (mode) {
            MODE_COMPASS -> {
                Surface(shape = MaterialTheme.shapes.extraLarge) {
                    CompassRose(
                        trueHeading = trueHeading,
                        bearing = bearing,
                        modifier = Modifier
                            .size(320.dp)
                            .padding(8.dp),
                    )
                }
            }
            else -> {
                QiblaMap(
                    latitude = latitude,
                    longitude = longitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.qibla_bearing, bearing),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(
                R.string.qibla_distance,
                stringResource(R.string.qibla_distance_km, distanceKm),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (mode == MODE_COMPASS && heading.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.qibla_calibrate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        if (mode == MODE_COMPASS) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompassRose(trueHeading: Float, bearing: Double, modifier: Modifier = Modifier) {
    val northColor = MaterialTheme.colorScheme.error
    val qiblaColor = Color(0xFFD4A017)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val rimColor = MaterialTheme.colorScheme.outline

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
        rotate(degrees = -trueHeading, pivot = center) {
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

            // Qibla marker (gold) at the bearing from north.
            rotate(degrees = bearing.toFloat(), pivot = center) {
                drawLine(
                    color = qiblaColor,
                    start = Offset(center.x, center.y - radius * 0.55f),
                    end = Offset(center.x, center.y - radius * 0.92f),
                    strokeWidth = 7.dp.toPx(),
                )
                drawCircle(
                    color = qiblaColor,
                    radius = 12.dp.toPx(),
                    center = Offset(center.x, center.y - radius * 0.72f),
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
