package org.muslim.app.core.common.wear

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity

class WearPrayerSnapshotTest {

    @Test
    fun `accepts a minimal valid companion snapshot`() {
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = "الفجر",
            nextPrayerAtEpochMillis = 1_700_000_000_000L,
            tasbihPhrase = "سُبْحَانَ اللَّهِ",
            tasbihCount = 12,
            tasbihTarget = 33,
            syncedAtEpochMillis = 1_699_999_000_000L,
        )

        assertThat(snapshot.isValid()).isTrue()
    }

    @Test
    fun `rejects invalid counter limits and missing sync timestamp`() {
        val invalid = WearPrayerSnapshot(
            nextPrayerName = null,
            nextPrayerAtEpochMillis = null,
            tasbihPhrase = "",
            tasbihCount = -1,
            tasbihTarget = 0,
            syncedAtEpochMillis = 0L,
        )

        assertThat(invalid.isValid()).isFalse()
    }

    @Test
    fun `appearance defaults keep old snapshots compatible`() {
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = "الفجر",
            nextPrayerAtEpochMillis = null,
            tasbihPhrase = "الحمد لله",
            tasbihCount = 0,
            tasbihTarget = 33,
            syncedAtEpochMillis = 1L,
        )

        assertThat(snapshot.ornamentStyle).isEqualTo(AppOrnamentStyle.Geometry)
        assertThat(snapshot.ornamentIntensity).isEqualTo(OrnamentIntensity.Balanced)
        assertThat(snapshot.nextPrayerId).isNull()
        assertThat(snapshot.languageTag).isNull()
    }

    @Test
    fun `snapshot carries selected ornament appearance`() {
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = "العشاء",
            nextPrayerAtEpochMillis = null,
            tasbihPhrase = "الله أكبر",
            tasbihCount = 3,
            tasbihTarget = 33,
            syncedAtEpochMillis = 2L,
            ornamentStyle = AppOrnamentStyle.Ottoman,
            ornamentIntensity = OrnamentIntensity.Rich,
        )

        assertThat(snapshot.ornamentStyle).isEqualTo(AppOrnamentStyle.Ottoman)
        assertThat(snapshot.ornamentIntensity).isEqualTo(OrnamentIntensity.Rich)
        assertThat(snapshot.isValid()).isTrue()
    }

    @Test
    fun `snapshot carries phone language and stable prayer id`() {
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = "Fajr",
            nextPrayerId = "fajr",
            nextPrayerAtEpochMillis = 1_700_000_000_000L,
            tasbihPhrase = "Subhan Allah",
            tasbihCount = 1,
            tasbihTarget = 33,
            syncedAtEpochMillis = 1_699_999_000_000L,
            languageTag = "de",
        )

        assertThat(snapshot.nextPrayerId).isEqualTo("fajr")
        assertThat(snapshot.languageTag).isEqualTo("de")
        assertThat(snapshot.isValid()).isTrue()
    }

    @Test
    fun `recognizes only versioned watch command paths`() {
        assertThat(WearSyncContract.isSupportedIncrementPath(WearSyncContract.TASBIH_INCREMENT_PATH)).isTrue()
        assertThat(WearSyncContract.isSupportedSyncRequestPath(WearSyncContract.SYNC_REQUEST_PATH)).isTrue()
        assertThat(WearSyncContract.isSupportedIncrementPath("/muslim/wear/unknown")).isFalse()
        assertThat(WearSyncContract.isSupportedSyncRequestPath("/muslim/wear/unknown")).isFalse()
    }

    @Test
    fun `phone and watch advertise distinct capabilities`() {
        assertThat(WearSyncContract.CAPABILITY_PHONE_APP).isNotEqualTo(WearSyncContract.CAPABILITY_WATCH_APP)
        assertThat(WearSyncContract.CAPABILITY_PHONE_APP).contains("phone")
        assertThat(WearSyncContract.CAPABILITY_WATCH_APP).contains("watch")
    }
}
