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
}
