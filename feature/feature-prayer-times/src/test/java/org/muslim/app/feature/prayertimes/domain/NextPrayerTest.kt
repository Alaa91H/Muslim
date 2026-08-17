package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.muslim.app.core.common.prayer.NextPrayer
import org.muslim.app.core.common.prayer.Prayer
import org.junit.Test

class NextPrayerTest {

    private fun epoch(hour: Int, minute: Int = 0): Long =
        // 2026-08-13 00:00 UTC + hour:minute
        1_784_851_200_000L + hour * 3_600_000L + minute * 60_000L

    @Test
    fun `nextPrayer returns the earliest upcoming prayer`() {
        val times = mapOf(
            Prayer.Fajr to epoch(4, 30),
            Prayer.Sunrise to epoch(6, 12),
            Prayer.Dhuhr to epoch(12, 45),
            Prayer.Asr to epoch(16, 20),
            Prayer.Maghrib to epoch(19, 5),
            Prayer.Isha to epoch(20, 30),
        )
        val next = NextPrayer.nextPrayer(times, nowMillis = epoch(12, 0))
        assertThat(next).isNotNull()
        assertThat(next!!.prayer).isEqualTo(Prayer.Dhuhr)
        assertThat(next.atEpochMillis).isEqualTo(epoch(12, 45))
    }

    @Test
    fun `nextPrayer returns null when all prayers have passed`() {
        val times = mapOf(
            Prayer.Fajr to epoch(4, 30),
            Prayer.Dhuhr to epoch(12, 45),
            Prayer.Isha to epoch(20, 30),
        )
        assertThat(NextPrayer.nextPrayer(times, nowMillis = epoch(21, 0))).isNull()
    }

    @Test
    fun `nextPrayer ignores sunrise`() {
        val times = mapOf(
            Prayer.Fajr to epoch(4, 30),
            Prayer.Sunrise to epoch(6, 12), // not a prayer — must be skipped
            Prayer.Dhuhr to epoch(12, 45),
        )
        val next = NextPrayer.nextPrayer(times, nowMillis = epoch(6, 0))
        assertThat(next!!.prayer).isEqualTo(Prayer.Dhuhr)
    }

    @Test
    fun `countdownSeconds rounds up and never goes negative`() {
        assertThat(NextPrayer.countdownSeconds(targetMillis = epoch(12, 0, 0) + 500, nowMillis = epoch(12, 0, 0)))
            .isEqualTo(1)
        assertThat(NextPrayer.countdownSeconds(targetMillis = epoch(12, 1), nowMillis = epoch(12, 0)))
            .isEqualTo(60)
        assertThat(NextPrayer.countdownSeconds(targetMillis = epoch(11, 0), nowMillis = epoch(12, 0)))
            .isEqualTo(0)
    }

    private fun epoch(hour: Int, minute: Int, second: Int): Long =
        epoch(hour, minute) + second * 1000L
}
