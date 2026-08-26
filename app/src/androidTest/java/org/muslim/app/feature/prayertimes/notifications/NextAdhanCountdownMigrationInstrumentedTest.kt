package org.muslim.app.feature.prayertimes.notifications

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.notifications.NotificationChannels

/** Ensures an ongoing countdown card from a previous APK cannot survive an update. */
@RunWith(AndroidJUnit4::class)
class NextAdhanCountdownMigrationInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val notificationManager get() = context.getSystemService(NotificationManager::class.java)

    @Before
    fun createChannels() {
        NotificationChannels.create(context)
    }

    @After
    fun clearCards() {
        notificationManager.cancel(NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID)
        notificationManager.cancel(NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID)
    }

    @Test
    fun cancelRetiredCountdown_removesTheOldOngoingCardBeforeTheNewIdentityIsUsed() {
        notificationManager.notify(
            NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID,
            NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
                .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
                .setContentTitle("Legacy countdown")
                .setOngoing(true)
                .build(),
        )

        NextAdhanNotifications.cancelRetiredCountdown(context)

        assertFalse(
            notificationManager.activeNotifications.any { statusBarNotification ->
                statusBarNotification.id == NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID
            },
        )
    }
}
