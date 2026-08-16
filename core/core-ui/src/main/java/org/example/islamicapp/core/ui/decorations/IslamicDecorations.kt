package org.example.islamicapp.core.ui.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Islamic geometric ornament (PROJECT_PROMPT.md §4.5 — "زخارف هندسية
 * إسلامية بسيطة ومحترمة، لا تصويرية"): the classic eight-pointed star
 * (khatam) tessellation, drawn as a subtle line pattern.
 */
@Composable
fun IslamicStarPattern(
    modifier: Modifier = Modifier,
    lineColor: Color,
    alpha: Float = 0.08f,
    tile: Float = 72f,
) {
    Canvas(modifier) {
        val step = tile
        var row = 0
        var y = -step
        while (y < size.height + step) {
            var col = 0
            var x = -step
            while (x < size.width + step) {
                val center = Offset(
                    x + if (row % 2 == 0) 0f else step / 2,
                    y,
                )
                drawEightPointStar(center, step * 0.42f, lineColor.copy(alpha = alpha))
                col++
                x += step
            }
            row++
            y += step / 2
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEightPointStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    // Two overlapping squares (one rotated 45°) form the khatam star.
    drawPath(squarePath(center, radius, rotationDeg = 0f), color = color, style = Stroke(1.5f))
    drawPath(squarePath(center, radius, rotationDeg = 45f), color = color, style = Stroke(1.5f))
}

private fun squarePath(center: Offset, radius: Float, rotationDeg: Float): Path {
    val path = Path()
    val rad = Math.toRadians(rotationDeg.toDouble())
    for (i in 0 until 4) {
        val angle = rad + Math.toRadians((90.0 * i + 45.0))
        val point = Offset(
            center.x + (radius * cos(angle)).toFloat(),
            center.y + (radius * sin(angle)).toFloat(),
        )
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

/** Thin ornamental divider (a line with a small diamond centred). */
@Composable
fun OrnamentalDivider(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier) {
        val midY = size.height / 2
        drawLine(color.copy(alpha = 0.4f), Offset(0f, midY), Offset(size.width / 2 - 14f, midY))
        drawLine(color.copy(alpha = 0.4f), Offset(size.width / 2 + 14f, midY), Offset(size.width, midY))
        // Central diamond.
        val c = Offset(size.width / 2, midY)
        val r = 5f
        val diamond = Path().apply {
            moveTo(c.x, c.y - r)
            lineTo(c.x + r, c.y)
            lineTo(c.x, c.y + r)
            lineTo(c.x - r, c.y)
            close()
        }
        drawPath(diamond, color = color)
    }
}
