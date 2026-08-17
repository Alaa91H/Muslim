package org.muslim.app.feature.ramadan.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class RamadanDatesTest {

    @Test
    fun `known Ramadan start for 1447 AH`() {
        // Ramadan 1, 1447 AH corresponds to 2026-02-18 (Umm al-Qura).
        val start = RamadanDates.ramadanStart(1447)
        assertThat(start).isEqualTo(LocalDate.of(2026, 2, 18))
    }

    @Test
    fun `ramadan length is 29 or 30 days`() {
        val length = RamadanDates.ramadanLength(1447)
        assertThat(length).isIn(listOf(29, 30))
    }

    @Test
    fun `upcoming returns the current ramadan when in ramadan`() {
        val today = LocalDate.of(2026, 2, 25) // mid-Ramadan 1447
        val info = RamadanDates.upcoming(today)
        assertThat(info.hijriYear).isEqualTo(1447)
        assertThat(info.isRamadanDay(today)).isTrue()
        assertThat(info.dayOfRamadan(today)).isEqualTo(8)
    }

    @Test
    fun `upcoming returns next ramadan after ramadan ends`() {
        val today = LocalDate.of(2026, 5, 1) // after Ramadan 1447
        val info = RamadanDates.upcoming(today)
        assertThat(info.isRamadanDay(today)).isFalse()
        assertThat(info.hijriYear).isEqualTo(1448)
        assertThat(info.start).isGreaterThan(today)
    }

    @Test
    fun `upcoming returns same-year ramadan before it starts`() {
        val today = LocalDate.of(2026, 1, 1) // Muharram 1447, before Ramadan
        val info = RamadanDates.upcoming(today)
        assertThat(info.hijriYear).isEqualTo(1447)
        assertThat(info.daysUntilStart(today)).isGreaterThan(0)
    }

    @Test
    fun `manual hijri adjustment shifts the window`() {
        val base = RamadanDates.ramadanStart(1447)
        val shifted = RamadanDates.ramadanStart(1447, adjustment = 1)
        assertThat(shifted).isEqualTo(base.plusDays(1))
    }

    @Test
    fun `days list covers the whole month contiguously`() {
        val info = RamadanDates.upcoming(LocalDate.of(2026, 2, 18))
        assertThat(info.days.size).isEqualTo(RamadanDates.ramadanLength(1447))
        assertThat(info.days.zipWithNext().all { (a, b) -> b == a.plusDays(1) }).isTrue()
    }
}
