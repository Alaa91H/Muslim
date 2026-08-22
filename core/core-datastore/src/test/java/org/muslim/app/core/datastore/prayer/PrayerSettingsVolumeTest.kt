package org.muslim.app.core.datastore.prayer

import org.junit.Assert.assertEquals
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer

class PrayerSettingsVolumeTest {

    private val prayer = Prayer.Fajr

    @Test
    fun `per-prayer override wins when global volume is off`() {
        val settings = PrayerSettings(
            adhanVolume = 80,
            adhanVolumes = mapOf(prayer to 35),
        )
        assertEquals(35, settings.adhanVolumeFor(prayer))
    }

    @Test
    fun `fallback to the default level when no per-prayer override exists`() {
        val settings = PrayerSettings(adhanVolume = 65)
        assertEquals(65, settings.adhanVolumeFor(prayer))
    }

    @Test
    fun `global volume wins over every per-prayer override when enabled`() {
        val settings = PrayerSettings(
            adhanVolume = 25,
            adhanVolumes = mapOf(prayer to 90),
            useGlobalAdhanVolume = true,
        )
        assertEquals(25, settings.adhanVolumeFor(prayer))
        assertEquals(25, settings.adhanVolumeFor(Prayer.Asr))
    }

    @Test
    fun `out-of-range values are coerced into the 0 to 100 range`() {
        val settings = PrayerSettings(
            adhanVolume = 150,
            adhanVolumes = mapOf(prayer to -20),
        )
        assertEquals(100, settings.adhanVolumeFor(Prayer.Maghrib))
        assertEquals(0, settings.adhanVolumeFor(prayer))
    }
}
