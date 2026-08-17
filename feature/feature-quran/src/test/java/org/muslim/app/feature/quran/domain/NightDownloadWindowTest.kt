package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId

/** Tests the pure night-download window logic. */
class NightDownloadWindowTest {

    private val zone = ZoneId.of("Asia/Riyadh")

    private fun millisAt(isoLocal: String): Long =
        java.time.LocalDateTime.parse(isoLocal).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `overnight window contains night minutes only`() {
        // Window 23:00–05:00 (overnight).
        assertThat(NightDownloadWindow.contains(23 * 60, 23 * 60, 5 * 60)).isTrue()
        assertThat(NightDownloadWindow.contains(0, 23 * 60, 5 * 60)).isTrue()
        assertThat(NightDownloadWindow.contains(4 * 60 + 59, 23 * 60, 5 * 60)).isTrue()
        // Boundaries are half-open.
        assertThat(NightDownloadWindow.contains(22 * 60 + 59, 23 * 60, 5 * 60)).isFalse()
        assertThat(NightDownloadWindow.contains(5 * 60, 23 * 60, 5 * 60)).isFalse()
        assertThat(NightDownloadWindow.contains(12 * 60, 23 * 60, 5 * 60)).isFalse()
    }

    @Test
    fun `same-day window contains its middle only`() {
        // Window 01:00–03:00.
        assertThat(NightDownloadWindow.contains(60, 60, 3 * 60)).isTrue()
        assertThat(NightDownloadWindow.contains(2 * 60 + 30, 60, 3 * 60)).isTrue()
        assertThat(NightDownloadWindow.contains(59, 60, 3 * 60)).isFalse()
        assertThat(NightDownloadWindow.contains(3 * 60, 60, 3 * 60)).isFalse()
    }

    @Test
    fun `zero-length window is always false`() {
        assertThat(NightDownloadWindow.contains(12 * 60, 60, 60)).isFalse()
    }

    @Test
    fun `contains works on real instants`() {
        // 23:30 is inside the default window.
        assertThat(NightDownloadWindow.contains(millisAt("2026-08-18T23:30"), zone, 23 * 60, 5 * 60)).isTrue()
        // Noon is outside.
        assertThat(NightDownloadWindow.contains(millisAt("2026-08-18T12:00"), zone, 23 * 60, 5 * 60)).isFalse()
    }

    @Test
    fun `nextOpenMillis picks today when the start has not passed`() {
        // 22:00, window opens at 23:00 today.
        val now = millisAt("2026-08-18T22:00")
        val next = NightDownloadWindow.nextOpenMillis(now, zone, 23 * 60)
        assertThat(next).isEqualTo(millisAt("2026-08-18T23:00"))
    }

    @Test
    fun `nextOpenMillis rolls to tomorrow when inside or past the start`() {
        // 23:30 (already inside) -> opens tomorrow 23:00.
        val inside = millisAt("2026-08-18T23:30")
        assertThat(NightDownloadWindow.nextOpenMillis(inside, zone, 23 * 60))
            .isEqualTo(millisAt("2026-08-19T23:00"))

        // 08:00 (past today's 23:00 already? no — 08:00 is before 23:00, so today).
        val morning = millisAt("2026-08-18T08:00")
        assertThat(NightDownloadWindow.nextOpenMillis(morning, zone, 23 * 60))
            .isEqualTo(millisAt("2026-08-18T23:00"))
    }

    @Test
    fun `nextOpenMillis wraps at end of month`() {
        val now = millisAt("2026-08-31T23:30")
        val next = NightDownloadWindow.nextOpenMillis(now, zone, 23 * 60)
        assertThat(next).isEqualTo(millisAt("2026-09-01T23:00"))
    }
}
