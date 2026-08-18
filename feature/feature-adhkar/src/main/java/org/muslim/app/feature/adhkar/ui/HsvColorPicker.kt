package org.muslim.app.feature.adhkar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/** Maps an alpha byte (0..255) to a percentage for display. */
fun alphaPercent(alpha: Int): Int = ((alpha * 100) / 255).coerceIn(0, 100)

/**
 * Full HSV colour picker: a draggable saturation/value square and a hue
 * strip. Reports opaque RGB (0xRRGGBB); the caller keeps the alpha.
 */
@Composable
fun HsvColorPicker(
    rgb: Int,
    onRgbChanged: (Int) -> Unit,
) {
    val hsv = remember(rgb) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(rgb, it) }
    }
    var hue by remember(rgb) { mutableFloatStateOf(hsv[0]) }
    var sat by remember(rgb) { mutableFloatStateOf(hsv[1]) }
    var value by remember(rgb) { mutableFloatStateOf(hsv[2]) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Saturation / value square.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(hue) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / size.width).coerceIn(0f, 1f)
                        val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        sat = s
                        value = v
                        onRgbChanged(android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v)) and 0xFFFFFF)
                    }
                },
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(Color.hsv(hue, 1f, 1f))
                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color.White, Color.White.copy(alpha = 0f))),
                )
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                )
                val handle = Offset(sat * size.width, (1f - value) * size.height)
                drawCircle(Color.Black, radius = 11f, center = handle)
                drawCircle(Color.White, radius = 8f, center = handle)
            }
        }
        Spacer(Modifier.height(10.dp))
        // Hue strip.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        hue = ((change.position.x / size.width).coerceIn(0f, 1f) * 360f) % 360f
                        onRgbChanged(
                            android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value)) and 0xFFFFFF,
                        )
                    }
                },
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Red, Color.Yellow, Color.Green, Color.Cyan,
                            Color.Blue, Color.Magenta, Color.Red,
                        ),
                    ),
                )
                val x = hue / 360f * size.width
                drawCircle(Color.Black, radius = 9f, center = Offset(x, size.height / 2f))
                drawCircle(Color.White, radius = 6f, center = Offset(x, size.height / 2f))
            }
        }
    }
}
