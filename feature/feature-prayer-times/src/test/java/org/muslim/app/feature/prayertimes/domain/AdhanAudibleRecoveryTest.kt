package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings

class AdhanAudibleRecoveryTest {

    @Test
    fun `one-percent global volume is diagnosed as quiet without changing the saved level`() {
        val settings = PrayerSettings(
            adhanVolume = 1,
            useGlobalAdhanVolume = true,
        )

        assertThat(settings.hasAudibleAdhanForEveryPrayer()).isFalse()
        assertThat(settings.adhanVolume).isEqualTo(1)
        assertThat(settings.useGlobalAdhanVolume).isTrue()
    }

    @Test
    fun `silent and vibration-only choices are diagnosed without changing per-prayer choices`() {
        val settings = PrayerSettings(
            adhanSounds = mapOf(
                Prayer.Fajr to AdhanSoundOption.Silent,
                Prayer.Isha to AdhanSoundOption.VibrateOnly,
            ),
        )

        assertThat(settings.hasAudibleAdhanForEveryPrayer()).isFalse()
        assertThat(settings.adhanSounds[Prayer.Fajr]).isEqualTo(AdhanSoundOption.Silent)
        assertThat(settings.adhanSounds[Prayer.Isha]).isEqualTo(AdhanSoundOption.VibrateOnly)
    }

    @Test
    fun `diagnostic check preserves global and individual volume choices exactly`() {
        val settings = PrayerSettings(
            adhanVolume = 17,
            adhanSounds = mapOf(Prayer.Fajr to AdhanSoundOption.Silent),
            adhanVolumes = mapOf(Prayer.Isha to 1),
            useGlobalAdhanVolume = false,
        )

        settings.hasAudibleAdhanForEveryPrayer()

        assertThat(settings.adhanVolume).isEqualTo(17)
        assertThat(settings.adhanVolumes).containsExactly(Prayer.Isha, 1)
        assertThat(settings.adhanSounds).containsExactly(Prayer.Fajr, AdhanSoundOption.Silent)
        assertThat(settings.useGlobalAdhanVolume).isFalse()
    }
}
