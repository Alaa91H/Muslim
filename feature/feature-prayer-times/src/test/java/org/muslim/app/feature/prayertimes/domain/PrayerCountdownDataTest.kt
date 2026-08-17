package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.SelectedLocation
import java.time.LocalDate
import java.time.ZoneId

class PrayerCountdownDataTest {

    private val calculator = PrayerTimesCalculator()
    private val zone = ZoneId.of("Asia/Riyadh")

    private fun epochAt(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun settingsWithLocation() = PrayerSettings(
        location = SelectedLocation("Makkah", 21.4225, 39.8262, zone.id),
    )

    @Test
    fun `no location - nothing to count down`() {
        val data = PrayerCountdownData.compute(
            PrayerSettings(), calculator, epochAt(LocalDate.of(2026, 8, 14), 12, 0),
        )
        assertThat(data.hasLocation).isFalse()
        assertThat(data.nextPrayer).isNull()
        assertThat(data.missedPrayer).isNull()
        assertThat(data.remainingSeconds).isEqualTo(0)
        assertThat(data.elapsedSeconds).isEqualTo(0)
    }

    @Test
    fun `before Dhuhr - next is Dhuhr and the missed adhan is Fajr`() {
        val data = PrayerCountdownData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 11, 0),
        )
        assertThat(data.hasLocation).isTrue()
        assertThat(data.isValid).isTrue()
        assertThat(data.nextPrayer).isEqualTo(Prayer.Dhuhr)
        assertThat(data.remainingSeconds).isGreaterThan(0)
        // Fajr has already passed and is reported as the missed adhan with elapsed time.
        assertThat(data.missedPrayer).isEqualTo(Prayer.Fajr)
        assertThat(data.missedPrayerAt).isNotNull()
        assertThat(data.elapsedSeconds).isGreaterThan(0)
    }

    @Test
    fun `after Isha - next is tomorrow's Fajr and the missed adhan is today's Isha`() {
        val data = PrayerCountdownData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 23, 0),
        )
        assertThat(data.nextPrayer).isEqualTo(Prayer.Fajr)
        assertThat(data.remainingSeconds).isGreaterThan(0)
        assertThat(data.remainingSeconds).isLessThan(6 * 3600) // Fajr before ~05:00 local
        assertThat(data.missedPrayer).isEqualTo(Prayer.Isha)
        assertThat(data.elapsedSeconds).isGreaterThan(0)
    }

    @Test
    fun `before Fajr - next is today's Fajr and the missed adhan is yesterday's Isha`() {
        val data = PrayerCountdownData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 1, 30),
        )
        assertThat(data.nextPrayer).isEqualTo(Prayer.Fajr)
        assertThat(data.remainingSeconds).isGreaterThan(0)
        assertThat(data.missedPrayer).isEqualTo(Prayer.Isha)
        // Roughly 7+ hours since yesterday's Isha (after ~19:30 local).
        assertThat(data.elapsedSeconds).isGreaterThan(5 * 3600)
    }

    @Test
    fun `countdown shrinks as the prayer approaches`() {
        val earlier = PrayerCountdownData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 10, 0),
        )
        val later = PrayerCountdownData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 11, 30),
        )
        assertThat(earlier.nextPrayer).isEqualTo(Prayer.Dhuhr)
        assertThat(later.nextPrayer).isEqualTo(Prayer.Dhuhr)
        assertThat(later.remainingSeconds).isLessThan(earlier.remainingSeconds)
    }

    @Test
    fun `formatCountdown renders minutes and hours`() {
        assertThat(formatCountdown(0)).isEqualTo("00:00")
        assertThat(formatCountdown(61)).isEqualTo("01:01")
        assertThat(formatCountdown(3_661)).isEqualTo("01:01:01")
        assertThat(formatCountdown(86_399)).isEqualTo("23:59:59")
        assertThat(formatCountdown(-5)).isEqualTo("00:00")
    }
}
