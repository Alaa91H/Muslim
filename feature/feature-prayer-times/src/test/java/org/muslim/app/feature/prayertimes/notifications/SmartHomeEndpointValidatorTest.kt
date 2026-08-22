package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmartHomeEndpointValidatorTest {

    @Test
    fun `accepts complete https endpoint`() {
        assertThat(SmartHomeEndpointValidator.normalizedHttps(" https://bridge.example/adhan "))
            .isEqualTo("https://bridge.example/adhan")
    }

    @Test
    fun `rejects non https endpoint`() {
        assertThat(SmartHomeEndpointValidator.normalizedHttps("http://bridge.example/adhan")).isNull()
    }

    @Test
    fun `rejects credentials and fragments`() {
        assertThat(SmartHomeEndpointValidator.normalizedHttps("https://user@bridge.example/adhan")).isNull()
        assertThat(SmartHomeEndpointValidator.normalizedHttps("https://bridge.example/adhan#state")).isNull()
    }
}
