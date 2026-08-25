package org.muslim.app.feature.prayertimes.notifications

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RescheduleActionsTest {

    @Test
    fun `boot recovery listens for boot and in-place app update`() {
        assertThat(RescheduleActions.boot).contains(Intent.ACTION_BOOT_COMPLETED)
        assertThat(RescheduleActions.boot).contains(Intent.ACTION_MY_PACKAGE_REPLACED)
    }

    @Test
    fun `exact alarm access recovery listens for platform grant broadcast`() {
        assertThat(RescheduleActions.exactAlarmAccess)
            .contains(RescheduleActions.EXACT_ALARM_PERMISSION_CHANGED)
    }
}
