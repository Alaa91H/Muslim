package org.example.islamicapp.feature.ramadan.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/** Ramadan season and occasion date logic (Phase 6 + Phase 8 slices). */
class RamadanDomainTest {

    @Test
    fun `inside ramadan the day number is correct`() {
        // 1 Ramadan 1447 ≈ 2026-02-18 (Umm al-Qura). Take the computed start
        // itself so the test never depends on a hand-transcribed date.
        val info = RamadanSeason.info(LocalDate.of(2026, 6, 1))
        val inside = RamadanSeason.info(info.startDate.plusDays(9))
        assertThat(inside.dayNumber).isEqualTo(10)
        assertThat(inside.daysUntilStart).isNull()
    }

    @Test
    fun `before ramadan shows a countdown`() {
        val info = RamadanSeason.info(LocalDate.of(2026, 6, 1))
        val before = RamadanSeason.info(info.startDate.minusDays(5))
        assertThat(before.daysUntilStart).isEqualTo(5)
        assertThat(before.dayNumber).isNull()
    }

    @Test
    fun `after ramadan rolls to next year`() {
        val info = RamadanSeason.info(LocalDate.of(2026, 6, 1))
        val after = RamadanSeason.info(info.endDate.plusDays(3))
        assertThat(after.dayNumber).isNull()
        assertThat(after.daysUntilStart).isNotNull()
        assertThat(after.startDate.isAfter(info.endDate)).isTrue()
    }

    @Test
    fun `month length is 29 or 30 days`() {
        val info = RamadanSeason.info(LocalDate.of(2026, 6, 1))
        assertThat(info.totalDays).isAnyOf(29, 30)
        assertThat(info.endDate).isEqualTo(info.startDate.plusDays((info.totalDays - 1).toLong()))
    }

    @Test
    fun `upcoming events are sorted and within horizon`() {
        val events = IslamicEvents.upcoming(LocalDate.of(2026, 6, 1))
        assertThat(events).isNotEmpty()
        val dates = events.map { it.nextDate }
        assertThat(dates.zipWithNext { a, b -> !b.isBefore(a) }.all { it }).isTrue()
        events.forEach { assertThat(it.daysUntil).isAtLeast(0L) }
        val ids = events.map { it.id }
        assertThat(ids).contains("eid_al_adha")
    }

    @Test
    fun `sunnah fasting finds monday thursday and white days`() {
        val today = LocalDate.of(2026, 6, 1) // a Monday
        val fasts = SunnahFasting.upcoming(today, days = 21)
        val labels = fasts.map { it.labelAr }
        assertThat(labels.any { it.contains("الاثنين") }).isTrue()
        assertThat(labels.any { it.contains("الخميس") }).isTrue()
        val dates = fasts.map { it.date }
        assertThat(dates.zipWithNext { a, b -> !b.isBefore(a) }.all { it }).isTrue()
        assertThat(fasts.distinctBy { it.date }).hasSize(fasts.size)
    }
}
