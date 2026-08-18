package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.time.HijriDate

class HajjCompanionTest {

    @Test
    fun `mansikFor maps dhul-hijjah 8 to 13 in order`() {
        assertThat(HajjCompanion.mansikFor(8)).isEqualTo(HajjMansik.TARWIYAH)
        assertThat(HajjCompanion.mansikFor(9)).isEqualTo(HajjMansik.ARAFAT)
        assertThat(HajjCompanion.mansikFor(10)).isEqualTo(HajjMansik.NAHR)
        assertThat(HajjCompanion.mansikFor(11)).isEqualTo(HajjMansik.TASHREEQ_1)
        assertThat(HajjCompanion.mansikFor(12)).isEqualTo(HajjMansik.TASHREEQ_2)
        assertThat(HajjCompanion.mansikFor(13)).isEqualTo(HajjMansik.TASHREEQ_3)
    }

    @Test
    fun `mansikFor is null outside the season`() {
        assertThat(HajjCompanion.mansikFor(7)).isNull()
        assertThat(HajjCompanion.mansikFor(14)).isNull()
        assertThat(HajjCompanion.mansikFor(1)).isNull()
        assertThat(HajjCompanion.mansikFor(30)).isNull()
    }

    @Test
    fun `isCompanionDay is true only during dhul-hijjah 8 to 13`() {
        for (day in 8..13) {
            assertThat(HajjCompanion.isCompanionDay(HijriDate.of(1447, 12, day))).isTrue()
        }
    }

    @Test
    fun `isCompanionDay is false outside the season`() {
        assertThat(HajjCompanion.isCompanionDay(HijriDate.of(1447, 12, 7))).isFalse()
        assertThat(HajjCompanion.isCompanionDay(HijriDate.of(1447, 12, 14))).isFalse()
        assertThat(HajjCompanion.isCompanionDay(HijriDate.of(1447, 11, 9))).isFalse()
        assertThat(HajjCompanion.isCompanionDay(HijriDate.of(1447, 1, 10))).isFalse()
    }
}
