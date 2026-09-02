package org.muslim.app.feature.qibla.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import org.muslim.app.feature.qibla.domain.CompassPosture
import org.muslim.app.feature.qibla.domain.HeadingSmoother

/** Device heading and whether the phone is held in a valid flat measuring posture. */
data class CompassHeading(
    val heading: Float,
    val accuracy: Int,
    val isLevel: Boolean = false,
)

/**
 * Feeds a tilt-compensated heading for the Qibla compass.
 *
 * A compass bearing is meaningful only while the phone's screen plane is
 * reasonably horizontal. In particular, a phone held vertically produces an
 * azimuth that can look plausible while being physically unsuitable for a
 * flat compass reading. Such samples are rejected and never used for Qibla
 * alignment. Screen rotation is remapped explicitly rather than using a
 * fixed X/Z transform, so portrait and landscape do not change the bearing.
 */
@Composable
fun rememberCompassHeading(
    displayRotationDegrees: Int = 0,
): State<CompassHeading> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(CompassHeading(0f, SensorManager.SENSOR_STATUS_UNRELIABLE))
    }
    val smoother = remember { HeadingSmoother() }

    DisposableEffect(context, displayRotationDegrees) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationMatrix = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false
        var sourceAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

        fun emit(matrix: FloatArray) {
            val (xAxis, yAxis) = when (displayRotationDegrees) {
                90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
            }
            if (!SensorManager.remapCoordinateSystem(matrix, xAxis, yAxis, remapped)) return
            SensorManager.getOrientation(remapped, orientation)

            val pitchDegrees = orientation[1] / PI.toFloat() * 180f
            val rollDegrees = orientation[2] / PI.toFloat() * 180f
            val isLevel = CompassPosture.isLevel(pitchDegrees, rollDegrees)
            if (!isLevel) {
                // Keep the last valid heading, but make the invalid posture
                // visible to the Qibla UI so it cannot report a false match.
                state.value = state.value.copy(
                    accuracy = sourceAccuracy,
                    isLevel = false,
                )
                return
            }

            val azimuth = (((orientation[0] / PI * 180.0) % 360.0 + 360.0) % 360.0).toFloat()
            if (azimuth.isNaN() || azimuth.isInfinite()) return
            val smoothed = smoother.update(azimuth)
            val displayHeading = (smoothed + 360f) % 360f
            state.value = CompassHeading(displayHeading, sourceAccuracy, isLevel = true)
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR,
                    Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                    -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        emit(rotationMatrix)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(gravity)
                        hasGravity = true
                        emitLegacy()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        event.values.copyInto(geomagnetic)
                        hasMagnetic = true
                        emitLegacy()
                    }
                }
            }

            private fun emitLegacy() {
                if (hasGravity && hasMagnetic &&
                    SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                ) {
                    emit(rotationMatrix)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sourceAccuracy = accuracy
                state.value = state.value.copy(accuracy = accuracy)
            }
        }

        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val geomagneticRotationVector =
            sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val sensors: List<Sensor> = when {
            rotationVector != null -> listOf(rotationVector)
            geomagneticRotationVector != null -> listOf(geomagneticRotationVector)
            else -> listOfNotNull(
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            )
        }
        sensors.forEach { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return state
}
