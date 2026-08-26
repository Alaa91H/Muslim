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
        val fallbackStarted = scheduled.copy(stage = AdhanDeliveryStage.AudioFallbackStarted)
        val audioStarted = scheduled.copy(stage = AdhanDeliveryStage.AudioStarted)

        assertThat(scheduled.audioStarted).isFalse()
        assertThat(receiverReached.audioStarted).isFalse()
        assertThat(serviceStarted.audioStarted).isFalse()
        assertThat(fallbackStarted.audioStarted).isFalse()
        assertThat(audioStarted.audioStarted).isTrue()
    }

    @Test
    fun `audio fallback is an in-progress recovery rather than a terminal failure`() {
        val fallback = AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.AudioFallbackStarted,
            prayer = Prayer.Fajr,
            isProbe = true,
            detail = "Bundled audio start timed out; synthetic fallback started",
        )

        assertThat(fallback.audioStarted).isFalse()
        assertThat(fallback.stage).isNotEqualTo(AdhanDeliveryStage.Failed)
        assertThat(fallback.detail).contains("synthetic fallback")
    }

    @Test
    fun `visible alarm notification result remains independent from audio confirmation`() {
        val postedNotification = AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.VisibleNotificationPosted,
            prayer = Prayer.Fajr,
            isProbe = true,
            visibleNotificationResult = AdhanVisibleNotificationResult.Posted,
        )
        val blockedNotification = postedNotification.copy(
            stage = AdhanDeliveryStage.VisibleNotificationBlocked,
            visibleNotificationResult = AdhanVisibleNotificationResult.Blocked,
        )
        val audioStarted = postedNotification.copy(stage = AdhanDeliveryStage.AudioStarted)

        assertThat(postedNotification.visibleNotificationResult)
            .isEqualTo(AdhanVisibleNotificationResult.Posted)
        assertThat(postedNotification.audioStarted).isFalse()
        assertThat(blockedNotification.visibleNotificationResult)
            .isEqualTo(AdhanVisibleNotificationResult.Blocked)
        assertThat(audioStarted.audioStarted).isTrue()
        assertThat(audioStarted.visibleNotificationResult)
            .isEqualTo(AdhanVisibleNotificationResult.Posted)
    }
}
