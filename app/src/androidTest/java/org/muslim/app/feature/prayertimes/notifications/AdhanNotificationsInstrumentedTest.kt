package org.muslim.app.feature.prayertimes.notifications

import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import java.time.LocalTime

/**
 * Device-level regression for the active Adhan alert inside the real Muslim
 * APK. A non-throwing notify call is not proof of delivery: the notification
 * must be retained by Android on the current Adhan channel.
 */
@RunWith(AndroidJUnit4::class)
class AdhanNotificationsInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
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
        notificationManager.cancel(AdhanNotifications.RETIRED_ADHAN_NOTIFICATION_ID)
        notificationManager.cancel(AdhanNotifications.ADHAN_NOTIFICATION_ID)
    }

    @Test
    fun application_usesThe2026LuxeLauncherAndRoundIcon() {
        val applicationInfo = context.applicationInfo

        assertEquals(org.muslim.app.R.mipmap.ic_muslim_launcher_v2026, applicationInfo.icon)
        assertEquals(org.muslim.app.R.mipmap.ic_muslim_launcher_round_v2026, applicationInfo.roundIcon)
    }

    @Test
    fun cancelRetiredAdhan_removesAllOngoingAlertsSavedByEarlierApks() {
        val retiredIds = listOf(AdhanNotifications.RETIRED_ADHAN_NOTIFICATION_ID, 1001)
        retiredIds.forEach { notificationId ->
            notificationManager.notify(
                notificationId,
                NotificationCompat.Builder(context, NotificationChannels.ADHAN)
                    .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2026)
                    .setContentTitle("Retired Adhan alert")
                    .setOngoing(true)
                    .build(),
            )
            awaitNotificationState(notificationId, expectedActive = true)
        }

        AdhanNotifications.cancelRetiredAdhan(context)
        retiredIds.forEach { notificationId ->
            awaitNotificationState(notificationId, expectedActive = false)
        }
    }

    private fun awaitNotificationState(notificationId: Int, expectedActive: Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 2_000L
        do {
            val isActive = notificationManager.activeNotifications.any { statusBarNotification ->
                statusBarNotification.id == notificationId
            }
            if (isActive == expectedActive) return
            SystemClock.sleep(50L)
        } while (SystemClock.elapsedRealtime() < deadline)

        val finalState = notificationManager.activeNotifications.any { statusBarNotification ->
            statusBarNotification.id == notificationId
        }
        assertTrue(
            "Notification $notificationId expected active=$expectedActive but was active=$finalState",
            finalState == expectedActive,
        )
    }

    @Test
    fun nextAdhanCountdown_usesTheCurrentPrayerNotificationIcon() {
        val notification = NextAdhanNotifications.build(
            context = context,
            data = PrayerCountdownData(
                hasLocation = true,
                isValid = true,
                nextPrayer = Prayer.Dhuhr,
                nextPrayerAt = LocalTime.of(12, 30),
                remainingSeconds = 3_600L,
                missedPrayer = null,
                missedPrayerAt = null,
                elapsedSeconds = 0L,
            ),
        )

        assertEquals(
            org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2026,
            notification.smallIcon.resId,
        )
    }

    @Test
    fun showAdhan_confirmsTheFreshActiveAdhanNotificationAndChannel() {
        val result = AdhanNotifications.showAdhan(context, Prayer.Fajr)

        assertTrue("Adhan notification was blocked: ${result.detail}", result.posted)
        assertNull(result.detail)
        val activeAdhan = notificationManager.activeNotifications.single { statusBarNotification ->
            statusBarNotification.id == AdhanNotifications.ADHAN_NOTIFICATION_ID &&
                statusBarNotification.notification.channelId == NotificationChannels.ADHAN
        }.notification
        assertEquals(
            org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2026,
            activeAdhan.smallIcon.resId,
        )
    }
}
