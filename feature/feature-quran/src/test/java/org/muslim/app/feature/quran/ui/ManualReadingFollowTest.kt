package org.muslim.app.feature.quran.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ManualReadingFollowTest {

    private val positions = listOf(
        AyahViewportPosition(globalNumber = 101, topPx = 40f),
        AyahViewportPosition(globalNumber = 102, topPx = 120f),
        AyahViewportPosition(globalNumber = 103, topPx = 220f),
    )

    @Test
    fun `manual reading chooses the first ayah at the viewport top`() {
        assertThat(ayahAtReaderTop(positions, viewportTopPx = 100f)).isEqualTo(102)
    }

    @Test
    fun `manual reading follows an upward or downward scroll without a fixed offset`() {
        assertThat(ayahAtReaderTop(positions, viewportTopPx = 20f)).isEqualTo(101)
        assertThat(ayahAtReaderTop(positions, viewportTopPx = 180f)).isEqualTo(103)
    }

    @Test
    fun `manual reading keeps the final ayah when the viewport moves below the page`() {
        assertThat(ayahAtReaderTop(positions, viewportTopPx = 300f)).isEqualTo(103)
        assertThat(ayahAtReaderTop(emptyList(), viewportTopPx = 0f)).isNull()
    }

    @Test
    fun `reader pager mapping accounts for the leading virtual edge page`() {
        assertThat(contentIndexToReaderPagerPage(0)).isEqualTo(1)
        assertThat(contentIndexToReaderPagerPage(7)).isEqualTo(8)
    }

    @Test
    fun `follow scroll keeps a fully visible multi line ayah in place`() {
        assertThat(
            calculateAyahFollowScrollDelta(
                ayahTopPx = 200f,
                ayahBottomPx = 400f,
                viewportTopPx = 100f,
                viewportHeightPx = 500,
            ),
        ).isEqualTo(0f)
    }

    @Test
    fun `follow scroll reveals the clipped bottom of the complete ayah`() {
        assertThat(
            calculateAyahFollowScrollDelta(
                ayahTopPx = 360f,
                ayahBottomPx = 580f,
                viewportTopPx = 100f,
                viewportHeightPx = 500,
            ),
        ).isEqualTo(40f)
    }

    @Test
    fun `follow scroll reveals the clipped top of the complete ayah`() {
        assertThat(
            calculateAyahFollowScrollDelta(
                ayahTopPx = 120f,
                ayahBottomPx = 300f,
                viewportTopPx = 100f,
                viewportHeightPx = 500,
            ),
        ).isEqualTo(-40f)
    }

    @Test
    fun `initial ayah centering uses the whole ayah instead of its first line`() {
        assertThat(
            calculateAyahFollowScrollDelta(
                ayahTopPx = 300f,
                ayahBottomPx = 500f,
                viewportTopPx = 100f,
                viewportHeightPx = 500,
                center = true,
            ),
        ).isEqualTo(50f)
    }

    @Test
    fun `oversized ayah aligns its first line to the safe top band`() {
        assertThat(
            calculateAyahFollowScrollDelta(
                ayahTopPx = 120f,
                ayahBottomPx = 600f,
                viewportTopPx = 100f,
                viewportHeightPx = 500,
            ),
        ).isEqualTo(-40f)
    }
}
