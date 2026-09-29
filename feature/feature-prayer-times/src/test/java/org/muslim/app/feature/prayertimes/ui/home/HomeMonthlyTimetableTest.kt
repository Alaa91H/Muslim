package org.muslim.app.feature.prayertimes.ui.home

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer

class HomeMonthlyTimetableTest {
    @Test
    fun dayTimes_preservesEveryPrayerTimeForMonthlyTimetable() {
        val times = Prayer.entries.associateWith { prayer ->
            LocalTime.of(5 + prayer.ordinal, prayer.ordinal)
        }

        val day = HomeViewModel.DayTimes(
            date = LocalDate.of(2026, 9, 29),
            hijriDay = 18,
            times = times,
        )

        assertEquals(Prayer.entries.toSet(), day.times.keys)
        assertEquals(times[Prayer.Fajr], day.fajr)
        assertEquals(times[Prayer.Maghrib], day.maghrib)
    }
}
