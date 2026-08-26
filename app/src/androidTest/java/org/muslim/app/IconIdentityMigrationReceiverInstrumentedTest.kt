package org.muslim.app

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.prayertimes.notifications.AdhanNotifications
import org.muslim.app.feature.prayertimes.notifications.NextAdhanNotifications
import org.muslim.app.feature.quran.data.RecitationPlaybackService

/**
 * Verifies that Android's in-place package-replacement broadcast clears the
 * latest retained cards from every user-visible system-notification surface.
 */
@RunWith(AndroidJUnit4::class)
class IconIdentityMigrationReceiverInstrumentedTest {

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
    fun clearCards() {
        listOf(1014, 1012, 1010, 1005, 1001, 1015, 1013, 1011, 1004, 1003, 7008, 7007, 7006)
            .forEach(notificationManager::cancel)
    }

    @Test
    fun packageReplacement_clearsTheLatestRetiredSystemCardsImmediately() {
        val retainedCards = listOf(
            RetainedCard(AdhanNotifications.RETIRED_ADHAN_NOTIFICATION_ID, NotificationChannels.ADHAN),
            RetainedCard(
                NextAdhanNotifications.RETIRED_COUNTDOWN_NOTIFICATION_ID,
                NotificationChannels.PRAYER_COUNTDOWN,
            ),
            RetainedCard(
                RecitationPlaybackService.RETIRED_RECITATION_NOTIFICATION_ID,
                NotificationChannels.RECITATION,
            ),
        )

        retainedCards.forEach { card ->
            notificationManager.notify(
                card.id,
                NotificationCompat.Builder(context, card.channelId)
                    .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
                    .setContentTitle("Retained identity card")
                    .setOngoing(true)
                    .build(),
            )
            awaitNotificationState(card.id, expectedActive = true)
        }

        IconIdentityMigrationReceiver().onReceive(
            context,
            Intent(Intent.ACTION_MY_PACKAGE_REPLACED).setPackage(context.packageName),
        )

        retainedCards.forEach { card ->
            awaitNotificationState(card.id, expectedActive = false)
        }
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

    private data class RetainedCard(
        val id: Int,
        val channelId: String,
    )
}
