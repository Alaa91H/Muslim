package org.muslim.app.feature.prayertimes.notifications

import android.app.NotificationManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels

/**
 * Device-level regression for the active Adhan alert.
 *
 * A non-throwing call to NotificationManager.notify() is not sufficient proof
 * of delivery. This verifies the same active-notification acknowledgement used
 * by the receiver on an Android emulator with POST_NOTIFICATIONS granted.
 */
@RunWith(AndroidJUnit4::class)
class AdhanNotificationsInstrumentedTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val notificationManager get() = context.getSystemService(NotificationManager::class.java)

    @Before
    fun grantNotificationPermissionAndCreateChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
                .close()
        }
        NotificationChannels.create(context)
    }

    @After
    fun clearActiveAdhanNotification() {
        notificationManager.cancel(AdhanNotifications.ADHAN_NOTIFICATION_ID)
    }

    @Test
    fun showAdhan_confirmsTheActiveAdhanNotificationAndChannel() {
        val result = AdhanNotifications.showAdhan(context, Prayer.Fajr)

        assertTrue(result.posted)
        assertNull(result.detail)
        assertTrue(
            notificationManager.activeNotifications.any { statusBarNotification ->
                statusBarNotification.id == AdhanNotifications.ADHAN_NOTIFICATION_ID &&
                    statusBarNotification.notification.channelId == NotificationChannels.ADHAN
            },
        )
    }
}
