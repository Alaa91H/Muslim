@file:Suppress("MagicNumber")

package org.muslim.app.feature.quran.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp

internal enum class QuranHighlightKind {
    Tapped,
    Playback,
    Opened,
    Selected,
}

internal data class QuranTextHighlightRange(
    val start: Int,
    val endExclusive: Int,
    val kind: QuranHighlightKind,
)

internal data class QuranHighlightVerticalBounds(
    val top: Float,
    val bottom: Float,
)

private val HighlightHorizontalPadding = 6.dp
private val HighlightVerticalInset = 1.5.dp
private val HighlightCornerRadius = 8.dp
private val HighlightBorderWidth = 1.dp

private const val TAPPED_FILL_ALPHA = 0.18f
private const val OPENED_FILL_ALPHA = 0.12f
private const val SELECTED_FILL_ALPHA = 0.08f

/**
 * Shrinks each line's highlight vertically instead of expanding it.
 *
 * Adjacent Compose text lines commonly share the same boundary
 * (line N bottom == line N+1 top). Expanding a background beyond those bounds
 * makes neighbouring highlights overlap. An inward inset guarantees a visual
 * gap while keeping the fill behind the glyphs.
 */
internal fun insetHighlightVerticalBounds(
    lineTop: Float,
    lineBottom: Float,
    insetPx: Float,
): QuranHighlightVerticalBounds? {
    if (lineBottom <= lineTop) return null
    val safeInset = insetPx.coerceAtLeast(0f)
    val top = lineTop + safeInset
    val bottom = lineBottom - safeInset
    return if (bottom > top) QuranHighlightVerticalBounds(top, bottom) else null
}

/**
 * Draws all Quran selection/playback highlights from actual laid-out glyph
 * geometry. Every wrapped line receives its own rounded rectangle; no
 * SpanStyle background is used, so a multi-line ayah cannot create one hard
 * rectangular block spanning neighbouring lines.
 */
internal fun DrawScope.drawQuranTextHighlights(
    layoutResult: TextLayoutResult,
    ranges: List<QuranTextHighlightRange>,
    primaryColor: Color,
    playbackFillAlpha: Float,
    playbackBorderAlpha: Float,
) {
    val textLength = layoutResult.layoutInput.text.length
    if (textLength == 0 || ranges.isEmpty()) return
    ranges.forEach { range ->
        drawHighlightRange(layoutResult, range, primaryColor, playbackFillAlpha, playbackBorderAlpha)
    }
}

private fun DrawScope.drawHighlightRange(
    layoutResult: TextLayoutResult,
    range: QuranTextHighlightRange,
    primaryColor: Color,
    playbackFillAlpha: Float,
    playbackBorderAlpha: Float,
) {
    if (range.endExclusive <= range.start) return
    val textLength = layoutResult.layoutInput.text.length
    val cornerRadiusPx = HighlightCornerRadius.toPx()
    val borderWidthPx = HighlightBorderWidth.toPx()
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    val start = range.start.coerceIn(0, textLength - 1)
    val endExclusive = range.endExclusive.coerceIn(start + 1, textLength)
    val startLine = layoutResult.getLineForOffset(start)
    val endLine = layoutResult.getLineForOffset(endExclusive - 1)
    val fillAlpha = when (range.kind) {
        QuranHighlightKind.Tapped -> TAPPED_FILL_ALPHA
        QuranHighlightKind.Playback -> playbackFillAlpha
        QuranHighlightKind.Opened -> OPENED_FILL_ALPHA
        QuranHighlightKind.Selected -> SELECTED_FILL_ALPHA
    }
    if (fillAlpha <= 0f) return

    val fill = primaryColor.copy(alpha = fillAlpha)
    val border = primaryColor.copy(alpha = playbackBorderAlpha)

    for (line in startLine..endLine) {
        val bounds = lineHighlightBounds(layoutResult, start, endExclusive, line) ?: continue
        drawRoundRect(
            color = fill,
            topLeft = bounds.topLeft,
            size = bounds.size,
            cornerRadius = cornerRadius,
        )

        if (range.kind == QuranHighlightKind.Playback && playbackBorderAlpha > 0f) {
            drawRoundRect(
                color = border,
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = cornerRadius,
                style = Stroke(width = borderWidthPx),
            )
        }
    }
}

private fun DrawScope.lineHighlightBounds(
    layoutResult: TextLayoutResult,
    start: Int,
    endExclusive: Int,
    line: Int,
): Rect? {
    val segmentStart = maxOf(start, layoutResult.getLineStart(line))
    val segmentEnd = minOf(endExclusive, layoutResult.getLineEnd(line, visibleEnd = true))
    if (segmentStart >= segmentEnd) return null
    var left = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    for (offset in segmentStart until segmentEnd) {
        val box = layoutResult.getBoundingBox(offset)
        left = minOf(left, box.left)
        right = maxOf(right, box.right)
    }
    if (!left.isFinite() || !right.isFinite()) return null
    val verticalBounds = insetHighlightVerticalBounds(
        lineTop = layoutResult.getLineTop(line),
        lineBottom = layoutResult.getLineBottom(line),
        insetPx = HighlightVerticalInset.toPx(),
    ) ?: return null
    val top = verticalBounds.top.coerceAtLeast(0f)
    val bottom = verticalBounds.bottom.coerceAtMost(size.height)
    left = (left - HighlightHorizontalPadding.toPx()).coerceAtLeast(0f)
    right = (right + HighlightHorizontalPadding.toPx()).coerceAtMost(size.width)
    if (right <= left || bottom <= top) return null
    return Rect(left, top, right, bottom)
}
