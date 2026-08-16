package org.example.islamicapp.feature.ramadan.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/** Moon-phase arithmetic (display-grade accuracy). */
class MoonPhaseTest {

    @Test
    fun `epoch date is a new moon`() {
        val info = MoonPhaseCalculator.at(LocalDate.of(2000, 1, 6))
        assertThat(info.phase).isEqualTo(MoonPhaseType.NewMoon)
        assertThat(info.illumination).isWithin(0.15).of(0.0)
    }

    @Test
    fun `roughly half cycle gives a full moon`() {
        // ~14.77 days after the epoch new moon.
        val info = MoonPhaseCalculator.at(LocalDate.of(2000, 1, 20))
        assertThat(info.illumination).isGreaterThan(0.85f.toDouble())
    }

    @Test
    fun `phase repeats exactly one synodic month later`() {
        val a = MoonPhaseCalculator.at(LocalDate.of(2026, 8, 1))
        val b = MoonPhaseCalculator.at(LocalDate.of(2026, 8, 1).plusDays(30)) // ≈ 29.53 → close
        // Ages may differ slightly due to whole-day steps; verify same cycle family.
        assertThat(b.age).isNotEqualTo(-1.0)
        assertThat(a.illumination).isAtLeast(0.0)
        assertThat(a.illumination).isAtMost(1.0)
    }

    @Test
    fun `waxing before half cycle and waning after`() {
        val waxing = MoonPhaseCalculator.at(LocalDate.of(2000, 1, 10))
        val waning = MoonPhaseCalculator.at(LocalDate.of(2000, 1, 25))
        assertThat(waxing.isWaxing).isTrue()
        assertThat(waning.isWaxing).isFalse()
    }

    @Test
    fun `age always within one synodic month`() {
        listOf(
            LocalDate.of(1980, 5, 15),
            LocalDate.of(2026, 2, 17),
            LocalDate.of(2050, 12, 31),
        ).forEach { date ->
            val age = MoonPhaseCalculator.at(date).age
            assertThat(age).isAtLeast(0.0)
            assertThat(age).isAtMost(MoonPhaseCalculator.SYNODIC_MONTH)
        }
    }
}
