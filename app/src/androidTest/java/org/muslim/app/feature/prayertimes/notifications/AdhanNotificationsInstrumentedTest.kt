package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val adhanChannel = checkNotNull(
                notificationManager.getNotificationChannel(NotificationChannels.ADHAN),
            )
            assertEquals(NotificationManager.IMPORTANCE_HIGH, adhanChannel.importance)
        }
    }

    @After
    fun clearActiveAdhanNotification() {
        listOf(
            AdhanNotifications.ADHAN_NOTIFICATION_ID,
            AdhanNotifications.REMINDER_NOTIFICATION_ID,
            1012,
            1010,
            1005,
            1001,
        ).forEach(notificationManager::cancel)
    }

    @Test
    fun application_usesThe2028LuxeLauncherAndRoundIcon() {
        val applicationInfo = context.applicationInfo

        assertEquals(org.muslim.app.R.mipmap.ic_muslim_launcher_v2028, applicationInfo.icon)
        assertEquals(
            "mipmap",
            context.resources.getResourceTypeName(org.muslim.app.R.mipmap.ic_muslim_launcher_round_v2028),
        )
    }

    @Test
    fun cancelRetiredAdhan_removesAllOngoingAlertsSavedByEarlierApks() {
        val retiredIds = listOf(AdhanNotifications.RETIRED_ADHAN_NOTIFICATION_ID, 1010, 1005, 1001)
        retiredIds.forEach { notificationId ->
            notificationManager.notify(
                notificationId,
                NotificationCompat.Builder(context, NotificationChannels.ADHAN)
                    .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
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
            org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028,
            notification.smallIcon.resId,
        )
    }

    @Test
    fun activeAdhan_isOngoingPublicHighPriority_andExposesOnlyTheExplicitStopAction() {
        val notification = AdhanNotifications.adhanNotification(context, Prayer.Fajr)

        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertFalse(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
        assertNull(notification.deleteIntent)
        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, notification.visibility)
        assertEquals(NotificationCompat.PRIORITY_HIGH, notification.priority)
        assertEquals(1, notification.actions.size)
        val stopAction = notification.actions.single()
        assertEquals(
            context.getString(org.muslim.app.feature.prayertimes.R.string.adhan_notification_stop),
            stopAction.title,
        )
        assertTrue("The sole Stop action must be executable", stopAction.actionIntent != null)
    }

    @Test
    fun showAdhan_confirmsTheFreshActiveCard_andCancelsTheEarlierReminder() {
        notificationManager.notify(
            AdhanNotifications.REMINDER_NOTIFICATION_ID,
            NotificationCompat.Builder(context, NotificationChannels.REMINDER)
                .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
                .setContentTitle("Earlier reminder")
                .build(),
        )
        awaitNotificationState(AdhanNotifications.REMINDER_NOTIFICATION_ID, expectedActive = true)

        val result = AdhanNotifications.showAdhan(context, Prayer.Fajr)

        assertTrue("Adhan notification was blocked: ${result.detail}", result.posted)
        assertNull(result.detail)
        awaitNotificationState(AdhanNotifications.REMINDER_NOTIFICATION_ID, expectedActive = false)
        val activeAdhan = notificationManager.activeNotifications.single { statusBarNotification ->
            statusBarNotification.id == AdhanNotifications.ADHAN_NOTIFICATION_ID &&
                statusBarNotification.notification.channelId == NotificationChannels.ADHAN
        }.notification
        assertEquals(
            org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028,
            activeAdhan.smallIcon.resId,
        )
    }
}
