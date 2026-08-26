package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings

class AdhanAudibleRecoveryTest {

    @Test
    fun `one-percent global volume is not treated as audible`() {
        val settings = PrayerSettings(
            adhanVolume = 1,
            useGlobalAdhanVolume = true,
        )

        assertThat(settings.hasAudibleAdhanForEveryPrayer()).isFalse()
    }

    @Test
    fun `silent and vibration-only prayer choices require recovery`() {
        val settings = PrayerSettings(
            adhanSounds = mapOf(
                Prayer.Fajr to AdhanSoundOption.Silent,
                Prayer.Isha to AdhanSoundOption.VibrateOnly,
            ),
        )

        assertThat(settings.hasAudibleAdhanForEveryPrayer()).isFalse()
    }

    @Test
    fun `audible recovery restores every obligatory prayer to bundled sound`() {
        val repaired = PrayerSettings(
            adhanVolume = 1,
            adhanSounds = mapOf(Prayer.Fajr to AdhanSoundOption.Silent),
            adhanVolumes = mapOf(Prayer.Isha to 0),
            useGlobalAdhanVolume = false,
        ).repairedAudibleAdhanDefaults()

        assertThat(repaired.useGlobalAdhanVolume).isTrue()
        assertThat(repaired.adhanVolume)
            .isEqualTo(PrayerSettings.DEFAULT_AUDIBLE_ADHAN_VOLUME)
        assertThat(repaired.adhanSounds).isEmpty()
        assertThat(repaired.adhanVolumes).isEmpty()
        assertThat(repaired.hasAudibleAdhanForEveryPrayer()).isTrue()
    }
}
