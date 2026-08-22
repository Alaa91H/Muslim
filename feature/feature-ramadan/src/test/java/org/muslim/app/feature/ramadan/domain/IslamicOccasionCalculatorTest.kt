package org.muslim.app.feature.ramadan.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class IslamicOccasionCalculatorTest {
    @Test
    fun core_occasions_include_ramadan_eids_and_arafah() {
        val occasions = IslamicOccasionCalculator.occasionsForYear(1447)
        assertThat(occasions.map { it.hijriMonth to it.hijriDay }).containsAtLeast(
            9 to 1, 10 to 1, 12 to 9, 12 to 10,
        )
    }

    @Test
    fun white_days_and_ashura_are_calculated() {
        val dates = IslamicOccasionCalculator.recommendedFastingDates(1447, 1)
        assertThat(dates).hasSize(4)
        assertThat(dates).contains(
            IslamicOccasionCalculator.dateOf(1447, 1, 10),
        )
    }

    @Test
    fun manual_adjustment_shifts_event() {
        val base = IslamicOccasionCalculator.dateOf(1447, 9, 1)
        val shifted = IslamicOccasionCalculator.dateOf(1447, 9, 1, 1)
        assertThat(shifted).isEqualTo(base.plusDays(1))
    }

    @Test
    fun friday_reminder_is_classified_as_friday() {
        val occasion = IslamicOccasionCalculator.fridayReminder(LocalDate.of(2026, 2, 20))
        assertThat(occasion.category).isEqualTo(OccasionCategory.Friday)
    }

    @Test
    fun next_occasion_is_not_before_requested_date() {
        val from = LocalDate.of(2026, 1, 1)
        val next = IslamicOccasionCalculator.nextOccasion(from)
        assertThat(next).isNotNull()
        assertThat(next!!.date).isAtLeast(from)
    }
}
