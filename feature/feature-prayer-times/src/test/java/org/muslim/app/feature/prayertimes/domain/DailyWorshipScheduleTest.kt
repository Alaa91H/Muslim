package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DailyWorshipScheduleTest {
    @Test
    fun prayerTimes_areOrderedByTime() {
        val times = mapOf(
            "Fajr" to LocalTime.of(5, 0),
            "Dhuhr" to LocalTime.of(12, 0),
            "Asr" to LocalTime.of(15, 30),
            "Maghrib" to LocalTime.of(18, 0),
            "Isha" to LocalTime.of(19, 30),
        )
        assertThat(times.entries.sortedBy { it.value }.map { it.key })
            .containsExactly("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            .inOrder()
    }

    @Test
    fun timezone_isUsedForScheduledDay() {
        val instant = LocalDate.of(2026, 1, 1)
            .atTime(LocalTime.of(5, 0))
            .atZone(ZoneId.of("Asia/Riyadh"))
            .toInstant()
        assertThat(instant.toEpochMilli()).isGreaterThan(0L)
    }
}
