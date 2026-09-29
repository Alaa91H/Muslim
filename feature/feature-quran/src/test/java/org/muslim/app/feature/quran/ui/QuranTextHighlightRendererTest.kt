package org.muslim.app.feature.quran.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranTextHighlightRendererTest {
    @Test
    fun adjacentLines_keepAVisualGapAfterInset() {
        val first = insetHighlightVerticalBounds(
            lineTop = 0f,
            lineBottom = 20f,
            insetPx = 2f,
        )
        val second = insetHighlightVerticalBounds(
            lineTop = 20f,
            lineBottom = 40f,
            insetPx = 2f,
        )

        requireNotNull(first)
        requireNotNull(second)
        assertEquals(2f, first.top)
        assertEquals(18f, first.bottom)
        assertEquals(22f, second.top)
        assertEquals(38f, second.bottom)
        assertTrue(first.bottom < second.top)
    }

    @Test
    fun invalidOrCollapsedLine_returnsNoHighlightBounds() {
        assertNull(insetHighlightVerticalBounds(10f, 10f, 1f))
        assertNull(insetHighlightVerticalBounds(12f, 8f, 1f))
        assertNull(insetHighlightVerticalBounds(0f, 2f, 2f))
    }

    @Test
    fun negativeInset_isClampedInsteadOfExpandingOutsideTheLine() {
        val bounds = insetHighlightVerticalBounds(
            lineTop = 4f,
            lineBottom = 14f,
            insetPx = -5f,
        )

        requireNotNull(bounds)
        assertEquals(4f, bounds.top)
        assertEquals(14f, bounds.bottom)
    }
}
