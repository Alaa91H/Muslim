package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.BundledAdhanSound
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.junit.Test

/**
 * Per-prayer adhan sound selection: every prayer falls back to the Makkah
 * recording unless a different bundled sound was chosen for it explicitly.
 */
class AdhanSoundSelectionTest {

    private fun PrayerSettings.bundledFor(prayer: Prayer): BundledAdhanSound =
        BundledAdhanSound.fromId(bundledAdhanSounds[prayer] ?: BundledAdhanSound.DEFAULT_ID)

    @Test
    fun `every prayer defaults to Makkah when no per-prayer sound chosen`() {
        val settings = PrayerSettings()
        Prayer.entries.forEach { prayer ->
            assertThat(settings.bundledFor(prayer)).isEqualTo(BundledAdhanSound.Makkah)
        }
    }

    @Test
    fun `a chosen sound applies only to its own prayer`() {
        val settings = PrayerSettings(
            bundledAdhanSounds = mapOf(Prayer.Fajr to BundledAdhanSound.UmayyadDamascus.id),
        )
        assertThat(settings.bundledFor(Prayer.Fajr)).isEqualTo(BundledAdhanSound.UmayyadDamascus)
        assertThat(settings.bundledFor(Prayer.Dhuhr)).isEqualTo(BundledAdhanSound.Makkah)
        assertThat(settings.bundledFor(Prayer.Isha)).isEqualTo(BundledAdhanSound.Makkah)
    }

    @Test
    fun `unknown stored id falls back to Makkah`() {
        val settings = PrayerSettings(bundledAdhanSounds = mapOf(Prayer.Asr to "not_a_sound"))
        assertThat(settings.bundledFor(Prayer.Asr)).isEqualTo(BundledAdhanSound.Makkah)
    }

    @Test
    fun `alert type is stored per prayer and defaults to sound`() {
        val settings = PrayerSettings()
        assertThat(settings.adhanSounds[Prayer.Fajr] ?: AdhanSoundOption.Default)
            .isEqualTo(AdhanSoundOption.Default)

        val customized = PrayerSettings(adhanSounds = mapOf(Prayer.Isha to AdhanSoundOption.VibrateOnly))
        assertThat(customized.adhanSounds[Prayer.Isha]).isEqualTo(AdhanSoundOption.VibrateOnly)
        assertThat(customized.adhanSounds[Prayer.Fajr] ?: AdhanSoundOption.Default)
            .isEqualTo(AdhanSoundOption.Default)
    }
}
