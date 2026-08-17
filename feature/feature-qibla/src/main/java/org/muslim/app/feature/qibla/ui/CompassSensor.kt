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

/** Device heading (degrees from magnetic north, 0..360) and sensor accuracy. */
data class CompassHeading(
    val heading: Float,
    val accuracy: Int,
)

/**
 * Feeds the phone's heading for the qibla compass.
 *
 * Preferred source: the fused [Sensor.TYPE_ROTATION_VECTOR] — modern handsets
 * fuse gyroscope + accelerometer + magnetometer on-device, giving a far more
 * stable heading than raw sensors (the requested gyro-based precision).
 * Devices without it fall back to the classic accelerometer + magnetometer
 * rotation matrix.
 *
 * Sensors run only while this composable is composed — never in the
 * background (battery principle, PROJECT_PROMPT.md §7).
 */
@Composable
fun rememberCompassHeading(): State<CompassHeading> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(CompassHeading(0f, SensorManager.SENSOR_STATUS_UNRELIABLE))
    }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val rotationMatrix = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false

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
                state.value = state.value.copy(heading = azimuth)
            }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
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
                state.value = state.value.copy(accuracy = accuracy)
            }
        }

        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val sensors: List<Sensor> = if (rotationVector != null) {
            listOf(rotationVector)
        } else {
            listOfNotNull(
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
