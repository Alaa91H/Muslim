package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer

class AdhanDeliveryStatusTest {

    @Test
    fun `only confirmed audio output is treated as a verified delivery`() {
        val scheduled = AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.ProbeScheduled,
            prayer = Prayer.Fajr,
            isProbe = true,
        )
        val receiverReached = scheduled.copy(stage = AdhanDeliveryStage.ReceiverReached)
        val serviceStarted = scheduled.copy(stage = AdhanDeliveryStage.ServiceStarted)
        val audioStarted = scheduled.copy(stage = AdhanDeliveryStage.AudioStarted)

        assertThat(scheduled.audioStarted).isFalse()
        assertThat(receiverReached.audioStarted).isFalse()
        assertThat(serviceStarted.audioStarted).isFalse()
        assertThat(audioStarted.audioStarted).isTrue()
    }
}
