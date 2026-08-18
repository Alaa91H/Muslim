package org.muslim.app.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhkarPrefsTest {

    @Test
    fun `master switch is off by default`() {
        assertThat(AdhkarPrefs().morningEveningReminderEnabled).isFalse()
    }

    @Test
    fun `master switch is on when only the morning reminder is enabled`() {
        val prefs = AdhkarPrefs(morningReminderEnabled = true)
        assertThat(prefs.morningEveningReminderEnabled).isTrue()
    }

    @Test
    fun `master switch is on when only the evening reminder is enabled`() {
        val prefs = AdhkarPrefs(eveningReminderEnabled = true)
        assertThat(prefs.morningEveningReminderEnabled).isTrue()
    }

    @Test
    fun `master switch is on when both reminders are enabled`() {
        val prefs = AdhkarPrefs(morningReminderEnabled = true, eveningReminderEnabled = true)
        assertThat(prefs.morningEveningReminderEnabled).isTrue()
    }

    @Test
    fun `master switch is off when both reminders are disabled`() {
        val prefs = AdhkarPrefs(morningReminderEnabled = false, eveningReminderEnabled = false)
        assertThat(prefs.morningEveningReminderEnabled).isFalse()
    }

    @Test
    fun `no dhikr is a favorite by default`() {
        assertThat(AdhkarPrefs().isDhikrFavorite(7)).isFalse()
    }

    @Test
    fun `dhikr in the favorite set is a favorite`() {
        val prefs = AdhkarPrefs(favoriteDhikrIds = setOf(7, 42))
        assertThat(prefs.isDhikrFavorite(7)).isTrue()
        assertThat(prefs.isDhikrFavorite(42)).isTrue()
    }

    @Test
    fun `dhikr outside the favorite set is not a favorite`() {
        val prefs = AdhkarPrefs(favoriteDhikrIds = setOf(7))
        assertThat(prefs.isDhikrFavorite(8)).isFalse()
    }

    @Test
    fun `bubble appearance defaults match the original overlay card`() {
        val prefs = AdhkarPrefs()
        assertThat(prefs.overlayBackgroundColor).isEqualTo(0xE6282830.toInt())
        assertThat(prefs.overlayBackgroundAlpha).isEqualTo(230)
        assertThat(prefs.overlayCornerRadiusDp).isEqualTo(20)
        assertThat(prefs.overlayFontSizeSp).isEqualTo(22)
    }

    @Test
    fun `bubble alpha defaults to the original card alpha`() {
        val prefs = AdhkarPrefs(overlayBackgroundColor = 0xE6282830.toInt())
        assertThat(prefs.overlayBackgroundAlpha).isEqualTo(230)
    }

    @Test
    fun `bubble alpha round-trips with an opaque colour`() {
        val prefs = AdhkarPrefs(
            overlayBackgroundColor = 0xE6401B47.toInt(),
            overlayBackgroundAlpha = 128,
        )
        assertThat(prefs.overlayBackgroundColor).isEqualTo(0xE6401B47.toInt())
        assertThat(prefs.overlayBackgroundAlpha).isEqualTo(128)
    }

    @Test
    fun `bubble appearance preserves custom values`() {
        val prefs = AdhkarPrefs(
            overlayBackgroundColor = 0xE6172C52.toInt(),
            overlayCornerRadiusDp = 0,
            overlayFontSizeSp = 28,
        )
        assertThat(prefs.overlayBackgroundColor).isEqualTo(0xE6172C52.toInt())
        assertThat(prefs.overlayCornerRadiusDp).isEqualTo(0)
        assertThat(prefs.overlayFontSizeSp).isEqualTo(28)
    }
}
