package org.muslim.app.feature.family.domain

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

class AqiqahCalculatorTest {
    private val birthDate = LocalDate.of(2026, 1, 10)

    @Test
    fun `schedule exposes seventh fourteenth and twenty first days in order`() {
        val schedule = AqiqahCalculator.schedule(birthDate)

        assertThat(schedule.seventhDay).isEqualTo(LocalDate.of(2026, 1, 17))
        assertThat(schedule.fourteenthDay).isEqualTo(LocalDate.of(2026, 1, 24))
        assertThat(schedule.twentyFirstDay).isEqualTo(LocalDate.of(2026, 1, 31))
        assertThat(schedule.recommendedDates).containsExactly(
            LocalDate.of(2026, 1, 17),
            LocalDate.of(2026, 1, 24),
            LocalDate.of(2026, 1, 31),
        ).inOrder()
    }

    @Test
    fun `days until first reminder is negative after seventh day`() {
        assertThat(
            AqiqahCalculator.daysUntilFirst(birthDate, LocalDate.of(2026, 1, 16)),
        ).isEqualTo(1)
        assertThat(
            AqiqahCalculator.daysUntilFirst(birthDate, LocalDate.of(2026, 1, 17)),
        ).isEqualTo(0)
        assertThat(
            AqiqahCalculator.daysUntilFirst(birthDate, LocalDate.of(2026, 1, 18)),
        ).isEqualTo(-1)
    }

    @Test
    fun `next reminder is nine local time before the seventh day and absent after it`() {
        val zone = ZoneId.of("Europe/Berlin")
        val target = LocalDate.of(2026, 1, 17)
            .atTime(AqiqahCalculator.REMINDER_HOUR, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val before = LocalDate.of(2026, 1, 17)
            .atTime(8, 59)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val after = LocalDate.of(2026, 1, 17)
            .atTime(9, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertThat(AqiqahCalculator.nextReminderMillis(birthDate, before, zone)).isEqualTo(target)
        assertThat(AqiqahCalculator.nextReminderMillis(birthDate, after, zone)).isNull()
    }
    @Test
    fun `selected reminder day maps to the expected planning date`() {
        assertThat(AqiqahCalculator.reminderDate(birthDate, AqiqahReminderDay.Seventh))
            .isEqualTo(LocalDate.of(2026, 1, 17))
        assertThat(AqiqahCalculator.reminderDate(birthDate, AqiqahReminderDay.Fourteenth))
            .isEqualTo(LocalDate.of(2026, 1, 24))
        assertThat(AqiqahCalculator.reminderDate(birthDate, AqiqahReminderDay.TwentyFirst))
            .isEqualTo(LocalDate.of(2026, 1, 31))
    }

    @Test
    fun `later reminder option remains schedulable after seventh day`() {
        val zone = ZoneId.of("Europe/Berlin")
        val now = LocalDate.of(2026, 1, 20)
            .atTime(12, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertThat(
            AqiqahCalculator.nextReminderMillis(
                birthDate = birthDate,
                nowMillis = now,
                zone = zone,
                day = AqiqahReminderDay.Seventh,
            ),
        ).isNull()
        assertThat(
            AqiqahCalculator.nextReminderMillis(
                birthDate = birthDate,
                nowMillis = now,
                zone = zone,
                day = AqiqahReminderDay.Fourteenth,
            ),
        ).isNotNull()
    }

}
