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
import org.muslim.app.feature.qibla.domain.HeadingSmoother
import kotlin.math.PI

/** Device heading (degrees from magnetic north, 0..360) and sensor accuracy. */
data class CompassHeading(
    val heading: Float,
    val accuracy: Int,
)

/**
 * Feeds the phone's heading for the qibla compass.
 *
 * Source priority (best first, never faked):
 *  1. [Sensor.TYPE_ROTATION_VECTOR] — the framework's fused product
 *     (gyroscope + accelerometer + magnetometer), the most stable source.
 *  2. [Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR] — fusion without gyroscope
 *     drift; ideal on devices with a weak gyro.
 *  3. Accelerometer + magnetometer — tilt-compensated fallback using the
 *     classic rotation-matrix algorithm.
 *
 * The raw azimuth is passed through an exponential angle smoother
 * ([HeadingSmoother]) so the dial does not jitter, and is corrected by the
 * current screen rotation so the reading always matches the top of the
 * display in both portrait and landscape.
 *
 * Sensors run only while this composable is composed — never in the
 * background (battery principle).
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
            // Screen-up remap so the dial follows the natural device posture.
            if (SensorManager.remapCoordinateSystem(
                    matrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remapped,
                )
            ) {
                SensorManager.getOrientation(remapped, orientation)
                // orientation[0]: azimuth in radians from magnetic north (-π..π).
                val azimuth = (((orientation[0] / PI * 180.0) % 360.0 + 360.0) % 360.0).toFloat()
                if (azimuth.isNaN() || azimuth.isInfinite()) return
                val smoothed = smoother.update(azimuth)
                // Rotate into the display frame: when the device is held
                // sideways the screen top no longer matches the device top.
                val displayHeading = (smoothed - displayRotationDegrees + 360f) % 360f
                state.value = CompassHeading(displayHeading, sourceAccuracy)
            }
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