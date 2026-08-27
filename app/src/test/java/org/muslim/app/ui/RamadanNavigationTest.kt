package org.muslim.app.ui

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.temporal.ChronoUnit
import org.junit.Test

class RamadanNavigationTest {

    @Test
    fun `Ramadan tab follows the calculated Hijri month instead of a fixed Gregorian range`() {
        val start = LocalDate.from(HijrahChronology.INSTANCE.date(1447, 9, 1))
        val endExclusive = LocalDate.from(HijrahChronology.INSTANCE.date(1447, 10, 1))

        assertThat(RamadanNavigation.isRamadan(start.minusDays(1), 0)).isFalse()
        assertThat(RamadanNavigation.isRamadan(start, 0)).isTrue()
        assertThat(RamadanNavigation.isRamadan(endExclusive.minusDays(1), 0)).isTrue()
        assertThat(RamadanNavigation.isRamadan(endExclusive, 0)).isFalse()
    }

    @Test
    fun `Hijri adjustment controls the seasonal destination consistently`() {
        val start = LocalDate.from(HijrahChronology.INSTANCE.date(1447, 9, 1))
        val dayBeforeCalculatedRamadan = start.minusDays(1)

        assertThat(RamadanNavigation.isRamadan(dayBeforeCalculatedRamadan, 0)).isFalse()
        assertThat(RamadanNavigation.isRamadan(dayBeforeCalculatedRamadan, 1)).isTrue()
        assertThat(ChronoUnit.DAYS.between(dayBeforeCalculatedRamadan, start)).isEqualTo(1)
    }
}
