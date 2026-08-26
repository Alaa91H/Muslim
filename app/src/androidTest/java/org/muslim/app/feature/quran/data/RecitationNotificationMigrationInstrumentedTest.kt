package org.muslim.app.feature.quran.data

import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.notifications.NotificationChannels

/** Device regression for retiring the recitation card that used the former status glyph. */
@RunWith(AndroidJUnit4::class)
class RecitationNotificationMigrationInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val notificationManager get() = context.getSystemService(NotificationManager::class.java)

    @Before
    fun grantNotificationPermissionAndCreateChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
                .close()
        }
        NotificationChannels.create(context)
    }

    @After
    fun clearCards() {
        notificationManager.cancel(RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID)
        notificationManager.cancel(RecitationPlaybackService.RECITATION_NOTIFICATION_ID)
    }

    @Test
    fun recitationNotification_usesANewIdentityAndRemovesTheRetiredCard() {
        assertNotEquals(
            RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID,
            RecitationPlaybackService.RECITATION_NOTIFICATION_ID,
        )
        notificationManager.notify(
            RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID,
            NotificationCompat.Builder(context, NotificationChannels.RECITATION)
                .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2027)
                .setContentTitle("Retired recitation card")
                .setOngoing(true)
                .build(),
        )
        awaitNotificationState(
            RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID,
            expectedActive = true,
        )

        RecitationPlaybackService.cancelRetiredNotification(context)

        awaitNotificationState(
            RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID,
            expectedActive = false,
        )
    }

    private fun awaitNotificationState(notificationId: Int, expectedActive: Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 2_000L
        do {
            val isActive = notificationManager.activeNotifications.any { it.id == notificationId }
            if (isActive == expectedActive) return
            SystemClock.sleep(50L)
        } while (SystemClock.elapsedRealtime() < deadline)

        val finalState = notificationManager.activeNotifications.any { it.id == notificationId }
        assertTrue(
            "Notification $notificationId expected active=$expectedActive but was active=$finalState",
            finalState == expectedActive,
        )
    }
}
