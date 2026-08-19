package org.muslim.app.core.common.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalTime
import java.util.Locale

/**
 * The app-wide clock (12-hour default / 24-hour): both formats always render
 * Western digits, and the 12-hour form carries the locale's AM/PM marker.
 */
class TimeFormatsTest {

    @Test
    fun `24 hour - zero padded HH colon MM`() {
        assertThat(TimeFormats.formatTime(LocalTime.of(5, 3), use24h = true, locale = Locale.ENGLISH))
            .isEqualTo("05:03")
        assertThat(TimeFormats.formatTime(LocalTime.of(17, 23), use24h = true, locale = Locale.ENGLISH))
            .isEqualTo("17:23")
        assertThat(TimeFormats.formatTime(LocalTime.MIDNIGHT, use24h = true, locale = Locale.ENGLISH))
            .isEqualTo("00:00")
    }

    @Test
    fun `12 hour - h colon mm with AM slash PM marker`() {
        assertThat(TimeFormats.formatTime(LocalTime.of(5, 23), use24h = false, locale = Locale.ENGLISH))
            .isEqualTo("5:23 AM")
        assertThat(TimeFormats.formatTime(LocalTime.of(17, 5), use24h = false, locale = Locale.ENGLISH))
            .isEqualTo("5:05 PM")
        assertThat(TimeFormats.formatTime(LocalTime.NOON, use24h = false, locale = Locale.ENGLISH))
            .isEqualTo("12:00 PM")
        assertThat(TimeFormats.formatTime(LocalTime.MIDNIGHT, use24h = false, locale = Locale.ENGLISH))
            .isEqualTo("12:00 AM")
    }

    @Test
    fun `formatMinutes - hours from midnight`() {
        assertThat(TimeFormats.formatMinutes(5 * 60 + 23, use24h = true, locale = Locale.ENGLISH))
            .isEqualTo("05:23")
        assertThat(TimeFormats.formatMinutes(17 * 60 + 5, use24h = false, locale = Locale.ENGLISH))
            .isEqualTo("5:05 PM")
    }
}
