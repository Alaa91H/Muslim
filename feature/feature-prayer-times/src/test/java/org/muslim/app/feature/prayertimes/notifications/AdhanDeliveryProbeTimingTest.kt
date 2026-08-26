package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhanDeliveryProbeTimingTest {

    @Test
    fun `explicit delivery probe is near-immediate without bypassing AlarmManager`() {
        assertThat(AdhanScheduler.DELIVERY_PROBE_DELAY_MS).isAtLeast(1_000L)
        assertThat(AdhanScheduler.DELIVERY_PROBE_DELAY_MS).isAtMost(3_000L)
    }
}
