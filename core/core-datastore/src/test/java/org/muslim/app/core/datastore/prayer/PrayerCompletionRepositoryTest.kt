package org.muslim.app.core.datastore.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer

class PrayerCompletionRepositoryTest {

    @Test
    fun `trackable prayers contain exactly the five daily prayers`() {
        assertEquals(
            listOf(
                Prayer.Fajr,
                Prayer.Dhuhr,
                Prayer.Asr,
                Prayer.Maghrib,
                Prayer.Isha,
            ),
            trackablePrayers,
        )
    }

    @Test
    fun `sunrise is not a trackable prayer`() {
        assertFalse(Prayer.Sunrise.isTrackablePrayer())
        assertTrue(Prayer.Fajr.isTrackablePrayer())
    }
}
