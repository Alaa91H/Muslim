package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhanAlarmDeliveryPolicyTest {

    @Test
    fun `blocked visual notification does not block enabled Adhan audio`() {
        val policy = AdhanAlarmDeliveryPolicy.resolve(
            adhanEnabled = true,
            presentationAllowed = false,
        )

        assertThat(policy.postVisibleNotification).isFalse()
        assertThat(policy.startAudio).isTrue()
    }

    @Test
    fun `enabled Adhan with allowed presentation delivers both paths`() {
        val policy = AdhanAlarmDeliveryPolicy.resolve(
            adhanEnabled = true,
            presentationAllowed = true,
        )

        assertThat(policy.postVisibleNotification).isTrue()
        assertThat(policy.startAudio).isTrue()
    }

    @Test
    fun `disabled Adhan delivers neither visible alert nor audio`() {
        val policy = AdhanAlarmDeliveryPolicy.resolve(
            adhanEnabled = false,
            presentationAllowed = true,
        )

        assertThat(policy.postVisibleNotification).isFalse()
        assertThat(policy.startAudio).isFalse()
    }
}
