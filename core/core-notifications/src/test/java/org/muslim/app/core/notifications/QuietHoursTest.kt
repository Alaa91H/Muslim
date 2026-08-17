package org.muslim.app.core.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class QuietHoursTest {

    private val zone = ZoneId.of("Asia/Riyadh")

    private fun epochAt(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `overnight window contains night minutes only`() {
        val q = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 6 * 60)
        assertThat(q.contains(23 * 60)).isTrue()
        assertThat(q.contains(2 * 60)).isTrue()
        assertThat(q.contains(5 * 60 + 59)).isTrue()
        assertThat(q.contains(6 * 60)).isFalse()
        assertThat(q.contains(21 * 60)).isFalse()
        assertThat(q.contains(12 * 60)).isFalse()
    }

    @Test
    fun `same-day window contains the middle`() {
        val q = QuietHours(enabled = true, startMinutes = 13 * 60, endMinutes = 15 * 60)
        assertThat(q.contains(13 * 60)).isTrue()
        assertThat(q.contains(14 * 60)).isTrue()
        assertThat(q.contains(15 * 60)).isFalse()
        assertThat(q.contains(10 * 60)).isFalse()
    }

    @Test
    fun `disabled window never contains`() {
        val q = QuietHours(enabled = false)
        assertThat(q.contains(23 * 60)).isFalse()
        assertThat(q.contains(0)).isFalse()
    }

    @Test
    fun `nextEndMillis during overnight window is the next morning`() {
        val q = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 6 * 60)
        val now = epochAt(LocalDate.of(2026, 8, 14), 23, 0)
        val expected = epochAt(LocalDate.of(2026, 8, 15), 6, 0)
        assertThat(q.nextEndMillis(now, zone)).isEqualTo(expected)
    }

    @Test
    fun `nextEndMillis after the end already passed today rolls to tomorrow`() {
        val q = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 6 * 60)
        val now = epochAt(LocalDate.of(2026, 8, 14), 8, 0)
        val expected = epochAt(LocalDate.of(2026, 8, 15), 6, 0)
        assertThat(q.nextEndMillis(now, zone)).isEqualTo(expected)
    }

    @Test
    fun `nextEndMillis for a same-day window is today's end`() {
        val q = QuietHours(enabled = true, startMinutes = 13 * 60, endMinutes = 15 * 60)
        val now = epochAt(LocalDate.of(2026, 8, 14), 13, 30)
        val expected = epochAt(LocalDate.of(2026, 8, 14), 15, 0)
        assertThat(q.nextEndMillis(now, zone)).isEqualTo(expected)
    }
}
