package org.muslim.app.feature.prayertimes.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.common.prayer.Prayer

@RunWith(AndroidJUnit4::class)
class AdhanDeliveryJournalInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearJournal() {
        context.getSharedPreferences(PREFS, 0).edit().clear().commit()
    }

    @After
    fun clearJournalAfterTest() {
        context.getSharedPreferences(PREFS, 0).edit().clear().commit()
    }

    @Test
    fun probe_doesNotOverwriteTheLastRealAdhanDelivery() {
        val journal = AdhanDeliveryJournal(context)
        journal.audioStarted(Prayer.Dhuhr, isProbe = false)
        journal.probeScheduled(Prayer.Fajr)

        assertEquals(Prayer.Dhuhr, journal.lastDelivery.value.prayer)
        assertFalse(journal.lastDelivery.value.isProbe)
        assertEquals(AdhanDeliveryStage.AudioStarted, journal.lastDelivery.value.stage)

        assertEquals(Prayer.Fajr, journal.lastProbe.value.prayer)
        assertEquals(AdhanDeliveryStage.ProbeScheduled, journal.lastProbe.value.stage)
        assertTrue(journal.lastProbe.value.isProbe)
    }

    private companion object {
        const val PREFS = "adhan_delivery_journal"
    }
}
