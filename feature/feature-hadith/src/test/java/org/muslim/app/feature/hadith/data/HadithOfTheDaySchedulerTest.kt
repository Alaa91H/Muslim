package org.muslim.app.feature.hadith.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class HadithOfTheDaySchedulerTest {

    private val zone = ZoneId.of("UTC")

    private fun now(hour: Int, minute: Int): Long =
        ZonedDateTime.of(2026, 8, 18, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun `delay targets a later time today`() {
        val delay = HadithOfTheDayScheduler.nextNotificationDelayMillis(now(7, 0), zone, 8 * 60)
        assertThat(delay).isEqualTo(60 * 60 * 1000L)
    }

    @Test
    fun `delay rolls over to tomorrow when the time already passed`() {
        val delay = HadithOfTheDayScheduler.nextNotificationDelayMillis(now(9, 0), zone, 8 * 60)
        assertThat(delay).isEqualTo(23 * 60 * 60 * 1000L)
    }

    @Test
    fun `delay clamps out-of-range minutes to the end of day`() {
        // 3000 minutes is invalid; clamps to 23:59.
        val delay = HadithOfTheDayScheduler.nextNotificationDelayMillis(now(7, 0), zone, 3000)
        val expected = ((23 * 60 + 59) - 7 * 60).toLong() * 60 * 1000L
        assertThat(delay).isEqualTo(expected)
    }

    @Test
    fun `exact target minute rolls to the next day`() {
        val delay = HadithOfTheDayScheduler.nextNotificationDelayMillis(now(8, 0), zone, 8 * 60)
        assertThat(delay).isEqualTo(24 * 60 * 60 * 1000L)
    }
}
