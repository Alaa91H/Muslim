package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.prayertimes.domain.formatCountdown
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/** Verifies migration plus the compact/expanded custom notification surfaces. */
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
        listOf(NextAdhanNotifications.NEXT_ADHAN_NOTIFICATION_ID, 1013, 1011, 1004, 1003)
            .forEach(notificationManager::cancel)
    }

    @Test
    fun cancelRetiredCountdown_removesAllOldOngoingCardsBeforeTheNewIdentityIsUsed() {
        val retiredIds = listOf(NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID, 1011, 1004, 1003)
        retiredIds.forEach { notificationId ->
            notificationManager.notify(
                notificationId,
                NotificationCompat.Builder(context, NotificationChannels.PRAYER_COUNTDOWN)
                    .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
                    .setContentTitle("Legacy countdown")
                    .setOngoing(true)
                    .build(),
            )
            awaitNotificationState(notificationId, expectedActive = true)
        }

        NextAdhanNotifications.cancelRetiredCountdown(context)
        retiredIds.forEach { notificationId ->
            awaitNotificationState(notificationId, expectedActive = false)
        }
    }

    @Test
    fun countdown_usesCustomCompactAndFivePrayerExpandedSurface_withNativeFallbackText() {
        val remainingSeconds = 42 * 60L
        val elapsedSeconds = 83 * 60L
        val times = linkedMapOf(
            Prayer.Fajr to LocalTime.of(5, 5),
            Prayer.Dhuhr to LocalTime.of(12, 30),
            Prayer.Asr to LocalTime.of(15, 47),
            Prayer.Maghrib to LocalTime.of(18, 22),
            Prayer.Isha to LocalTime.of(19, 48),
        )
        val notification = NextAdhanNotifications.build(
            context = context,
            data = PrayerCountdownData(
                hasLocation = true,
                isValid = true,
                nextPrayer = Prayer.Dhuhr,
                nextPrayerAt = times.getValue(Prayer.Dhuhr),
                remainingSeconds = remainingSeconds,
                missedPrayer = Prayer.Fajr,
                missedPrayerAt = times.getValue(Prayer.Fajr),
                elapsedSeconds = elapsedSeconds,
                prayerTimes = times,
            ),
            showMissed = true,
            use24h = true,
        )

        assertTrue(
            notification.smallIcon.resId ==
                org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029,
        )
        val (compact, expanded) = NextAdhanNotifications.buildCustomRemoteViews(
            context = context,
            data = PrayerCountdownData(
                hasLocation = true,
                isValid = true,
                nextPrayer = Prayer.Dhuhr,
                nextPrayerAt = times.getValue(Prayer.Dhuhr),
                remainingSeconds = remainingSeconds,
                missedPrayer = Prayer.Fajr,
                missedPrayerAt = times.getValue(Prayer.Fajr),
                elapsedSeconds = elapsedSeconds,
                prayerTimes = times,
            ),
            showMissed = true,
            use24h = true,
        )
        assertEquals(R.layout.notification_next_adhan_compact, compact.layoutId)
        assertEquals(R.layout.notification_next_adhan_expanded, expanded.layoutId)

        val title = requireNotNull(notification.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertTrue(title.toString().contains(context.getString(prayerLabelRes(Prayer.Dhuhr))))
        assertTrue(title.toString().contains("12:30"))

        val compactBody = requireNotNull(notification.extras.getCharSequence(Notification.EXTRA_TEXT))
        assertTrue(
            compactBody.toString().contains(
                context.getString(R.string.next_adhan_remaining, formatCountdown(remainingSeconds)),
            ),
        )
        assertFalse(title.toString().contains(context.getString(prayerLabelRes(Prayer.Fajr))))
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
}
