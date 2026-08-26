package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NextAdhanNotificationIdentityTest {

    @Test
    fun `next adhan notification uses a fresh identity separate from the retired ongoing card`() {
        assertThat(NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID)
            .isNotEqualTo(NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID)
        assertThat(NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID).isEqualTo(1015)
        assertThat(NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID).isEqualTo(1013)
    }
}
