package org.muslim.app.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhkarReminderWindowTest {

    @Test
    fun `minutesOfDay converts hour and minute`() {
        assertThat(AdhkarReminderWindow.minutesOfDay(9, 30)).isEqualTo(570)
        assertThat(AdhkarReminderWindow.minutesOfDay(0, 0)).isEqualTo(0)
        assertThat(AdhkarReminderWindow.minutesOfDay(23, 59)).isEqualTo(1_439)
    }

    @Test
    fun `daytime window includes boundaries`() {
        val start = AdhkarReminderWindow.minutesOfDay(9, 0)
        val end = AdhkarReminderWindow.minutesOfDay(17, 0)
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(12, 0), start, end)).isTrue()
        assertThat(AdhkarReminderWindow.isWithinWindow(start, start, end)).isTrue()
        assertThat(AdhkarReminderWindow.isWithinWindow(end, start, end)).isTrue()
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(8, 59), start, end)).isFalse()
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(17, 1), start, end)).isFalse()
    }

    @Test
    fun `overnight window wraps past midnight`() {
        val start = AdhkarReminderWindow.minutesOfDay(22, 0)
        val end = AdhkarReminderWindow.minutesOfDay(6, 0)
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(23, 0), start, end)).isTrue()
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(2, 30), start, end)).isTrue()
        assertThat(AdhkarReminderWindow.isWithinWindow(AdhkarReminderWindow.minutesOfDay(12, 0), start, end)).isFalse()
    }
}
