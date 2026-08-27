package org.muslim.app.feature.prayertimes.notifications

import android.app.Notification
import android.app.NotificationManager
import android.os.SystemClock
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalTime
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

/** Ensures old countdown cards are removed and the replacement has the requested two-surface layout. */
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
                    .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
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
    fun countdown_keepsTheNextAdhanOnOneCollapsedLine_andShowsMissedAdhanOnlyWhenExpanded() {
        val remainingSeconds = 42 * 60L
        val elapsedSeconds = 83 * 60L
        val nextPrayerTime = LocalTime.of(12, 30)
        val missedPrayerTime = LocalTime.of(5, 5)
        val notification = NextAdhanNotifications.build(
            context = context,
            data = PrayerCountdownData(
                hasLocation = true,
                isValid = true,
                nextPrayer = Prayer.Dhuhr,
                nextPrayerAt = nextPrayerTime,
                remainingSeconds = remainingSeconds,
                missedPrayer = Prayer.Fajr,
                missedPrayerAt = missedPrayerTime,
                elapsedSeconds = elapsedSeconds,
            ),
            showMissed = true,
            use24h = true,
        )

        assertTrue(
            notification.smallIcon.resId ==
                org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028,
        )
        val compactLine = requireNotNull(notification.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertNotNull(compactLine)
        val expectedNextTitle = context.getString(
            R.string.next_adhan_notification_title,
            context.getString(prayerLabelRes(Prayer.Dhuhr)),
            "12:30",
        )
        val expectedRemaining = context.getString(
            R.string.next_adhan_remaining,
            formatCountdown(remainingSeconds),
        )
        val expectedMissed = context.getString(
            R.string.next_adhan_missed,
            context.getString(prayerLabelRes(Prayer.Fajr)),
            "05:05",
        )
        assertTrue(compactLine.toString().contains(expectedNextTitle))
        assertTrue(compactLine.toString().contains(expectedRemaining))
        assertFalse(compactLine.toString().contains(expectedMissed))
        assertTrue(hasColorSpan(compactLine, context.getColor(R.color.adhan_accent)))
        assertTrue(hasColorSpan(compactLine, org.muslim.app.core.notifications.MissedAdhanColors.DEFAULT))

        val expandedLine = requireNotNull(notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        assertNotNull(expandedLine)
        assertTrue(expandedLine.toString().contains(expectedMissed))
        assertTrue(
            expandedLine.toString().contains(
                context.getString(R.string.next_adhan_elapsed, formatCountdown(elapsedSeconds)),
            ),
        )
        assertFalse(expandedLine.toString().contains("\n"))
        assertTrue(hasColorSpan(expandedLine, org.muslim.app.core.notifications.MissedAdhanColors.DEFAULT))
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

    private fun hasColorSpan(text: CharSequence, color: Int): Boolean {
        val spanned = text as? Spanned ?: return false
        return spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java)
            .any { it.foregroundColor == color }
    }
}
