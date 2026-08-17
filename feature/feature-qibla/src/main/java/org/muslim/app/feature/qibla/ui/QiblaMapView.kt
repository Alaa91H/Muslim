package org.muslim.app.feature.qibla.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muslim.app.feature.qibla.R
import org.muslim.app.feature.qibla.domain.GeoPoint
import org.muslim.app.feature.qibla.domain.QiblaCalculator
import kotlin.math.atan2

/**
 * Offline qibla map (PROJECT_PROMPT.md §6 Phase 1: خريطة القبلة).
 *
 * Draws the great-circle route from the user's location to the Kaaba on a
 * schematic equirectangular map — North up, fully offline, no map tiles and
 * no network permission. The route is interpolated by [QiblaCalculator.routePoints].
 */
@Composable
fun QiblaMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val route = QiblaCalculator.routePoints(latitude, longitude)
    val projected = QiblaCalculator.projectToUnitSquare(
        points = route,
        centerLatitude = latitude,
        centerLongitude = longitude,
        width = 1.0,
        height = 1.0,
    )
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val routeColor = MaterialTheme.colorScheme.primary
    val startColor = MaterialTheme.colorScheme.tertiary
    val kaabaColor = Color(0xFFD4A017)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val youLabel = stringResource(R.string.qibla_map_you)
    val kaabaLabelText = stringResource(R.string.qibla_map_kaaba)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = surfaceColor,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val gridStep = size.width / 6f

            // Grid (graticule) — lightweight offline map feel.
            var x = gridStep
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += gridStep
            }
            var y = gridStep
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += gridStep
            }

            // Route polyline.
            if (projected.size >= 2) {
                val path = Path()
                projected.forEachIndexed { index, (px, py) ->
                    val point = Offset(px.toFloat() * size.width, py.toFloat() * size.height)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(path, color = routeColor, style = Stroke(width = 4.dp.toPx()))

                // Arrow at the Kaaba end indicating the final heading.
                val last = projected.last()
                val beforeLast = projected[projected.size - 2]
                val tip = Offset(last.first.toFloat() * size.width, last.second.toFloat() * size.height)
                val tail = Offset(beforeLast.first.toFloat() * size.width, beforeLast.second.toFloat() * size.height)
                val angle = atan2(tip.y - tail.y, tip.x - tail.x)
                val arrowLength = 18.dp.toPx()
                val spread = 0.45f
                val arrow = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(
                        tip.x - arrowLength * kotlin.math.cos(angle - spread),
                        tip.y - arrowLength * kotlin.math.sin(angle - spread),
                    )
                    moveTo(tip.x, tip.y)
                    lineTo(
                        tip.x - arrowLength * kotlin.math.cos(angle + spread),
                        tip.y - arrowLength * kotlin.math.sin(angle + spread),
                    )
                }
                drawPath(arrow, color = routeColor, style = Stroke(width = 3.dp.toPx()))
            }

            // Start marker (user's location).
            val start = projected.firstOrNull() ?: return@Canvas
            val startOffset = Offset(start.first.toFloat() * size.width, start.second.toFloat() * size.height)
            drawCircle(color = startColor, radius = 10.dp.toPx(), center = startOffset)
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = startOffset,
            )

            // Kaaba marker (gold square).
            val kaaba = projected.lastOrNull() ?: return@Canvas
            val kaabaOffset = Offset(kaaba.first.toFloat() * size.width, kaaba.second.toFloat() * size.height)
            drawRect(
                color = kaabaColor,
                topLeft = Offset(kaabaOffset.x - 9.dp.toPx(), kaabaOffset.y - 9.dp.toPx()),
                size = Size(18.dp.toPx(), 18.dp.toPx()),
            )

            // Labels.
            val startLabel = textMeasurer.measure(
                text = youLabel,
                style = TextStyle(color = textColor, fontSize = 11.sp),
            )
            drawText(
                textLayoutResult = startLabel,
                topLeft = Offset(startOffset.x + 12.dp.toPx(), startOffset.y - 20.dp.toPx()),
            )
            val kaabaLabel = textMeasurer.measure(
                text = kaabaLabelText,
                style = TextStyle(color = kaabaColor, fontSize = 11.sp),
            )
            drawText(
                textLayoutResult = kaabaLabel,
                topLeft = Offset(kaabaOffset.x + 12.dp.toPx(), kaabaOffset.y - 20.dp.toPx()),
            )
        }
    }
}

/** Small legend row shown above the map. */
@Composable
fun QiblaMapLegend(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.qibla_map_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
