package org.muslim.app.core.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhanChannelIdentityTest {

    @Test
    fun `adhan uses a fresh high-priority channel distinct from the silent countdown`() {
        assertThat(NotificationChannels.ADHAN).isEqualTo("adhan_alert_v2")
        assertThat(NotificationCategory.Adhan.channelId).isEqualTo(NotificationChannels.ADHAN)
        assertThat(NotificationCategory.Adhan.channelId).isNotEqualTo(NotificationChannels.PRAYER_COUNTDOWN)
        assertThat(NotificationCategory.Adhan.defaultImportance).isEqualTo(NotificationImportance.High)
        assertThat(NotificationCategory.Adhan.defaultSound).isTrue()
        assertThat(NotificationCategory.Adhan.defaultVibrate).isTrue()
    }
}
