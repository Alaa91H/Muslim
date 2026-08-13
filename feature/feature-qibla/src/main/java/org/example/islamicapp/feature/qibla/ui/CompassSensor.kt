package org.example.islamicapp.feature.qibla.ui

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
 * Feeds the phone's heading via the accelerometer + magnetometer rotation
 * matrix. Sensors are only active while this composable is composed — the
 * compass never runs in the background (battery principle, PROJECT_PROMPT.md
 * §7).
 */
@Composable
fun rememberCompassHeading(): State<CompassHeading> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(CompassHeading(0f, SensorManager.SENSOR_STATUS_UNRELIABLE))
    }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotation = FloatArray(9)
        val orientation = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(gravity)
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        event.values.copyInto(geomagnetic)
                        hasMagnetic = true
                    }
                }
                if (hasGravity && hasMagnetic &&
                    SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)
                ) {
                    SensorManager.getOrientation(rotation, orientation)
                    // orientation[0] is the azimuth in radians from magnetic north (-π..π).
                    val azimuth = ((orientation[0] / PI * 180).toFloat() % 360 + 360) % 360
                    state.value = CompassHeading(
                        heading = azimuth,
                        accuracy = state.value.accuracy,
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    state.value = state.value.copy(accuracy = accuracy)
                }
            }
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magneticField, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return state
}
