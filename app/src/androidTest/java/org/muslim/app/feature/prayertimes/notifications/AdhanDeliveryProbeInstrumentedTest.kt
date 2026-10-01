package org.muslim.app.feature.prayertimes.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.core.notifications.NotificationChannels

/**
 * End-to-end device regression for the exact AlarmManager probe used by
 * Verify Adhan. It runs in the actual Muslim application, exercising Hilt,
 * scheduling, the manifest receiver, the foreground playback service, and the
 * persisted delivery journal.
 */
@RunWith(AndroidJUnit4::class)
class AdhanDeliveryProbeInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val entryPoint get() = EntryPointAccessors.fromApplication(context, AdhanEntryPoint::class.java)
    private val notificationManager get() = context.getSystemService(NotificationManager::class.java)

    @Before
    fun grantRequiredSystemAccessAndSaveAudibleSettings() = runBlocking {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runShell(automation.executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}",
            ))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runShell(automation.executeShellCommand(
                "appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow",
            ))
        }
        runShell(automation.executeShellCommand("cmd deviceidle whitelist +${context.packageName}"))
        NotificationChannels.create(context)
        entryPoint.settingsRepository().save(
            PrayerSettings(
                location = SelectedLocation(
                    name = "Makkah",
                    latitude = 21.4225,
                    longitude = 39.8262,
                    timeZone = "Asia/Riyadh",
                ),
                adhanEnabled = true,
                adhanVolume = USER_SELECTED_VOLUME_PERCENT,
            ),
        )
    }

    @After
    fun stopTestServicesAndClearNotification() {
        context.stopService(Intent(context, AdhanPlaybackService::class.java))
        context.stopService(Intent(context, NextAdhanService::class.java))
        notificationManager.cancel(AdhanNotifications.ADHAN_NOTIFICATION_ID)
        runShell(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "cmd deviceidle whitelist -${context.packageName}",
            ),
        )
    }

    @Test
    fun scheduledProbe_reachesReceiver_postsActiveAdhanAndStartsAudio() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val appOps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runShell(
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                    "appops get ${context.packageName} SCHEDULE_EXACT_ALARM",
                ),
            )
        } else {
            "not required"
        }
        val exactAlarmAccess = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        assertTrue(
            "The test emulator must grant exact-alarm access before exercising the real probe; appops=$appOps",
            exactAlarmAccess,
        )
        Log.i(TAG, "Exact alarm access=$exactAlarmAccess appops=$appOps")

        val settings = runBlocking { entryPoint.settingsRepository().settings.first() }
        assertEquals(USER_SELECTED_VOLUME_PERCENT, settings.adhanVolume)
        assertTrue(entryPoint.scheduler().scheduleDeliveryProbe(settings, Prayer.Fajr))
        // A concurrent settings observer must not cancel the user-triggered probe.
        entryPoint.scheduler().schedule(settings)
        val alarmDump = runShell(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("dumpsys alarm"),
        )
        val scheduledProbe = alarmDump.lineSequence()
            .filter { context.packageName in it || "AdhanAlarmReceiver" in it }
            .joinToString(" ")
            .take(MAX_ALARM_DIAGNOSTICS_CHARS)
        Log.i(TAG, "Scheduled probe alarm=$scheduledProbe")

        val result = waitForProbeTerminalState()
        assertNotNull(
            "Probe did not reach a terminal state: ${entryPoint.deliveryJournal().lastProbe.value}; " +
                "appops=$appOps; alarm=$scheduledProbe",
            result,
        )
        assertEquals(Prayer.Fajr, result!!.prayer)
        assertTrue(
            "Active Adhan notification was not retained: ${result.detail}",
            result.visibleNotificationResult == AdhanVisibleNotificationResult.Posted,
        )
        assertTrue("Adhan audio did not report start: ${result.detail}", result.audioStarted)
        assertTrue(
            notificationManager.activeNotifications.any { statusBarNotification ->
                statusBarNotification.id == AdhanNotifications.ADHAN_NOTIFICATION_ID &&
                    statusBarNotification.notification.channelId == NotificationChannels.ADHAN
            },
        )
    }

    private fun waitForProbeTerminalState(): AdhanDeliveryStatus? {
        val deadline = SystemClock.elapsedRealtime() + PROBE_TIMEOUT_MS
        do {
            val status = entryPoint.deliveryJournal().lastProbe.value
            if (status.audioStarted || status.stage == AdhanDeliveryStage.Failed) return status
            SystemClock.sleep(POLL_INTERVAL_MS)
        } while (SystemClock.elapsedRealtime() < deadline)
        return null
    }

    private fun runShell(descriptor: ParcelFileDescriptor): String =
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes().toString(Charsets.UTF_8) }

    private companion object {
        const val TAG = "AdhanDeliveryProbeTest"
        const val MAX_ALARM_DIAGNOSTICS_CHARS = 2_000
        const val USER_SELECTED_VOLUME_PERCENT = 17
        const val PROBE_TIMEOUT_MS = 35_000L
        const val POLL_INTERVAL_MS = 200L
    }
}
