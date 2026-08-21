package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecitationPauseOnNotificationsTest {

    @Test
    fun `notification resume window is four seconds`() {
        assertThat(RecitationPauseOnNotifications.RESUME_DELAY_MS).isEqualTo(4_000L)
    }

    @Test
    fun `manual pause remains paused when focus returns`() {
        val policy = RecitationFocusPolicy()

        policy.onTransientLoss(wasPlaying = false)

        assertThat(policy.onGain()).isFalse()
        assertThat(policy.onGain()).isFalse()
    }
}
