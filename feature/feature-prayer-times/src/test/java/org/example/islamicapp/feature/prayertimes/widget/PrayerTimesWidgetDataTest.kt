package org.example.islamicapp.feature.prayertimes.widget

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.core.datastore.prayer.PrayerSettings
import org.example.islamicapp.core.datastore.prayer.SelectedLocation
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.core.common.prayer.PrayerTimesCalculator
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerTimesWidgetDataTest {

    private val calculator = PrayerTimesCalculator()
    private val zone = ZoneId.of("Asia/Riyadh")

    private fun epochAt(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun settingsWithLocation() = PrayerSettings(
        location = SelectedLocation("Makkah", 21.4225, 39.8262, zone.id),
    )

    @Test
    fun `no location - widget prompts for location`() {
        val data = PrayerTimesWidgetData.compute(
            PrayerSettings(), calculator, epochAt(LocalDate.of(2026, 8, 14), 12, 0),
        )
        assertThat(data.hasLocation).isFalse()
        assertThat(data.nextPrayer).isNull()
        assertThat(data.times).isEmpty()
    }

    @Test
    fun `finds the next prayer with a positive countdown and today's five times`() {
        val data = PrayerTimesWidgetData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 11, 0),
        )
        assertThat(data.hasLocation).isTrue()
        assertThat(data.isValid).isTrue()
        // Just before noon the next prayer at Makkah is Dhuhr (solar transit).
        assertThat(data.nextPrayer).isEqualTo(Prayer.Dhuhr)
        assertThat(data.nextPrayerAt).isNotNull()
        assertThat(data.countdownSeconds).isGreaterThan(0)
        assertThat(data.times.keys)
            .containsExactly(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha)
    }

    @Test
    fun `after Isha the countdown targets tomorrow's Fajr`() {
        val data = PrayerTimesWidgetData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 23, 0),
        )
        assertThat(data.hasLocation).isTrue()
        assertThat(data.nextPrayer).isEqualTo(Prayer.Fajr)
        assertThat(data.countdownSeconds).isGreaterThan(0)
        assertThat(data.countdownSeconds).isLessThan(6 * 3600) // Fajr before ~05:00 local
    }

    @Test
    fun `times map is in mushaf order`() {
        val data = PrayerTimesWidgetData.compute(
            settingsWithLocation(), calculator, epochAt(LocalDate.of(2026, 8, 14), 11, 0),
        )
        assertThat(data.times.keys.toList())
            .containsExactly(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha)
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
