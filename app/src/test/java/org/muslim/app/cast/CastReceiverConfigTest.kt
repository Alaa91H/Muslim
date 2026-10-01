package org.muslim.app.cast

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CastReceiverConfigTest {
    @Test
    fun `only registered custom application ids enable the Quran receiver`() {
        assertThat(CastReceiverConfig.isValidCustomApplicationId("A1B2C3D4")).isTrue()
        assertThat(CastReceiverConfig.isValidCustomApplicationId("CC1AD845")).isFalse()
        assertThat(CastReceiverConfig.isValidCustomApplicationId("REPLACE_WITH_CAST_RECEIVER_ID")).isFalse()
        assertThat(CastReceiverConfig.isValidCustomApplicationId("1234567")).isFalse()
    }
}
