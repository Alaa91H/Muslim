package org.muslim.app.core.common.wear

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
    fun `recognizes only the versioned increment path`() {
        assertThat(WearSyncContract.isSupportedIncrementPath(WearSyncContract.TASBIH_INCREMENT_PATH)).isTrue()
        assertThat(WearSyncContract.isSupportedIncrementPath("/muslim/wear/unknown")).isFalse()
    }
}
