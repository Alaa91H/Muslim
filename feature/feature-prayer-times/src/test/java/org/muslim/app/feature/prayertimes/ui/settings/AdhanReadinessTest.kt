package org.muslim.app.feature.prayertimes.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhanReadinessTest {

    @Test
    fun `readiness is complete only when every delivery condition passes`() {
        val ready = AdhanReadiness(
            adhanEnabled = true,
            hasLocation = true,
            notificationsAllowed = true,
            exactAlarmsAllowed = true,
            nextPrayerHasAudibleSound = true,
            scheduledNotificationPosted = true,
            scheduledAudioVerified = true,
            alarmVolumeAudible = true,
        )

        assertThat(ready.isReady).isTrue()
        assertThat(ready.copy(alarmVolumeAudible = false).isReady).isFalse()
        assertThat(ready.copy(nextPrayerHasAudibleSound = false).isReady).isFalse()
        assertThat(ready.copy(scheduledNotificationPosted = false).isReady).isFalse()
        assertThat(ready.copy(scheduledAudioVerified = false).isReady).isFalse()
        assertThat(ready.copy(exactAlarmsAllowed = false).isReady).isFalse()
        assertThat(ready.copy(notificationsAllowed = false).isReady).isFalse()
        assertThat(ready.copy(hasLocation = false).isReady).isFalse()
        assertThat(ready.copy(adhanEnabled = false).isReady).isFalse()
    }
}
