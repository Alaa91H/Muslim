package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.time.HijriDate
import java.time.LocalDate

class HajjDaysCalculatorTest {

    @Test
    fun `arafah is 9 dhul-hijjah of the entered year`() {
        val days = HajjDaysCalculator.daysForYear(1447)
        assertThat(days[0].kind).isEqualTo(HajjKeyDayKind.ARAFAH)
        assertThat(days[0].hijri.year).isEqualTo(1447)
        assertThat(days[0].hijri.month).isEqualTo(HajjDaysCalculator.DHUL_HIJJAH)
        assertThat(days[0].hijri.day).isEqualTo(9)
    }

    @Test
    fun `season covers arafah nahr and three tashreeq days in order`() {
        val kinds = HajjDaysCalculator.daysForYear(1447).map { it.kind }
        assertThat(kinds).containsExactly(
            HajjKeyDayKind.ARAFAH,
            HajjKeyDayKind.NAHR,
            HajjKeyDayKind.TASHREEQ_FIRST,
            HajjKeyDayKind.TASHREEQ_SECOND,
            HajjKeyDayKind.TASHREEQ_THIRD,
        ).inOrder()
    }

    @Test
    fun `tashreeq days are 11 12 and 13 dhul-hijjah`() {
        val days = HajjDaysCalculator.daysForYear(1447)
        assertThat(days.drop(2).map { it.hijri.day }).containsExactly(11, 12, 13).inOrder()
    }

    @Test
    fun `known anchor - arafah 1445 is 15 june 2024 and nahr is 16 june`() {
        // From HijriDateTest: 10 Dhul-Hijjah 1445 == 2024-06-16, so 9 is 2024-06-15.
        val days = HajjDaysCalculator.daysForYear(1445)
        assertThat(days[0].gregorian).isEqualTo(LocalDate.of(2024, 6, 15))
        assertThat(days[1].gregorian).isEqualTo(LocalDate.of(2024, 6, 16))
    }

    @Test
    fun `seasonFor uses the year of the entered date`() {
        val entered = HijriDate.of(1447, HajjDaysCalculator.DHUL_HIJJAH, 8)
        val days = HajjDaysCalculator.seasonFor(entered)
        assertThat(days).hasSize(5)
        assertThat(days.first().hijri.year).isEqualTo(1447)
        // 9 Dhul-Hijjah is exactly one day after 8 Dhul-Hijjah.
        assertThat(days.first().daysFrom(entered)).isEqualTo(1L)
    }

    @Test
    fun `daysFrom is negative for days already passed`() {
        val entered = HijriDate.of(1447, HajjDaysCalculator.DHUL_HIJJAH, 14)
        assertThat(HajjDaysCalculator.seasonFor(entered).first().daysFrom(entered)).isEqualTo(-5L)
    }

    @Test
    fun `parse accepts valid dates and rejects out of range ones`() {
        assertThat(HajjDaysCalculator.parse(1447, 12, 9)).isNotNull()
        assertThat(HajjDaysCalculator.parse(1447, 13, 1)).isNull()
        assertThat(HajjDaysCalculator.parse(1447, 0, 1)).isNull()
        assertThat(HajjDaysCalculator.parse(1447, 12, 0)).isNull()
        assertThat(HajjDaysCalculator.parse(1299, 1, 1)).isNull()
        assertThat(HajjDaysCalculator.parse(1601, 1, 1)).isNull()
    }
}
