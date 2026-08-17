package org.muslim.app.core.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

/**
 * Unit tests for the unified notification manager's [NotificationPrefsRepository].
 *
 * Uses Robolectric for a real Android [Context] (resources, NotificationManager
 * and channel creation) and a temp-file [androidx.datastore.core.DataStore] so
 * every test starts from a clean, isolated store. Covers the three contracts:
 * quiet hours, per-category toggling (including cancelling posted alerts) and
 * live application of presentation settings onto the Android channels.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    shadows = [ReplacingShadowNotificationManager::class],
)
class NotificationPrefsRepositoryTest {

    private lateinit var context: Context
    private lateinit var storeScope: CoroutineScope
    private lateinit var repository: NotificationPrefsRepository
    private lateinit var notificationManager: NotificationManager

    private val zone: ZoneId = ZoneId.of("Asia/Riyadh")

    private fun epochAt(hour: Int, minute: Int = 0): Long =
        LocalDate.of(2026, 8, 14).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    @Before
    fun setUp() {
        // The repository reads quiet hours with the device-default zone; pin it
        // to the same zone the epoch fixtures below are computed in.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Riyadh"))
        context = ApplicationProvider.getApplicationContext()
        storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val storeDir = File(context.cacheDir, "datastore_${System.nanoTime()}").apply { mkdirs() }
        val store = PreferenceDataStoreFactory.create(
            scope = storeScope,
            produceFile = { File(storeDir, "prefs.preferences_pb") },
        )
        repository = NotificationPrefsRepository(context, store)
        notificationManager = context.getSystemService(NotificationManager::class.java)
    }

    @After
    fun tearDown() {
        storeScope.cancel()
    }

    // ---------------------------------------------------------------- quiet hours

    @Test
    fun `quiet hours default to disabled with the standard window`() = runTest {
        val hours = repository.quietHours.first()
        assertThat(hours.enabled).isFalse()
        assertThat(hours.startMinutes).isEqualTo(22 * 60)
        assertThat(hours.endMinutes).isEqualTo(6 * 60)
    }

    @Test
    fun `setQuietHours persists the window and is exposed by the flow`() = runTest {
        repository.setQuietHours(QuietHours(enabled = true, startMinutes = 13 * 60, endMinutes = 15 * 60))
        val hours = repository.quietHours.first()
        assertThat(hours.enabled).isTrue()
        assertThat(hours.startMinutes).isEqualTo(13 * 60)
        assertThat(hours.endMinutes).isEqualTo(15 * 60)
    }

    @Test
    fun `isQuietHourActive is always false while quiet hours are disabled`() = runTest {
        assertThat(repository.isQuietHourActive(epochAt(2, 0))).isFalse()
        assertThat(repository.isQuietHourActive(epochAt(12, 0))).isFalse()
    }

    @Test
    fun `isQuietHourActive honours an overnight window`() = runTest {
        repository.setQuietHours(QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 6 * 60))
        assertThat(repository.isQuietHourActive(epochAt(23, 0))).isTrue()
        assertThat(repository.isQuietHourActive(epochAt(2, 0))).isTrue()
        assertThat(repository.isQuietHourActive(epochAt(5, 59))).isTrue()
        assertThat(repository.isQuietHourActive(epochAt(6, 0))).isFalse()
        assertThat(repository.isQuietHourActive(epochAt(21, 59))).isFalse()
    }

    @Test
    fun `isQuietHourActive honours a same-day window`() = runTest {
        repository.setQuietHours(QuietHours(enabled = true, startMinutes = 13 * 60, endMinutes = 15 * 60))
        assertThat(repository.isQuietHourActive(epochAt(13, 30))).isTrue()
        assertThat(repository.isQuietHourActive(epochAt(15, 0))).isFalse()
        assertThat(repository.isQuietHourActive(epochAt(10, 0))).isFalse()
    }

    // ---------------------------------------------------------- category toggling

    @Test
    fun `default prefs match the built-in defaults for every category`() = runTest {
        val all = repository.prefs.first()
        assertThat(all).hasSize(NotificationCategory.entries.size)
        NotificationCategory.entries.forEach { category ->
            assertThat(repository.prefsFor(category)).isEqualTo(category.defaultPrefs())
            assertThat(repository.isEnabled(category)).isEqualTo(category.defaultEnabled)
        }
    }

    @Test
    fun `setEnabled persists the switch and leaves other categories untouched`() = runTest {
        repository.setEnabled(NotificationCategory.Adhan, enabled = false)
        assertThat(repository.isEnabled(NotificationCategory.Adhan)).isFalse()
        assertThat(repository.prefsFor(NotificationCategory.Adhan).enabled).isFalse()
        NotificationCategory.entries
            .filter { it != NotificationCategory.Adhan }
            .forEach { assertThat(repository.isEnabled(it)).isEqualTo(it.defaultEnabled) }
    }

    @Test
    fun `disabling a category cancels its posted notifications but not others`() = runTest {
        notificationManager.notify(
            1,
            Notification.Builder(context, NotificationChannels.ADHAN)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("adhan")
                .build(),
        )
        notificationManager.notify(
            2,
            Notification.Builder(context, NotificationChannels.REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("reminder")
                .build(),
        )
        assertThat(notificationManager.activeNotifications.map { it.id }).containsExactly(1, 2)

        repository.setEnabled(NotificationCategory.Adhan, enabled = false)

        val active = notificationManager.activeNotifications.map { it.id }
        assertThat(active).doesNotContain(1) // adhan alert cancelled
        assertThat(active).contains(2) // reminder alert kept
    }

    @Test
    fun `presentation setters persist per category`() = runTest {
        repository.setSoundEnabled(NotificationCategory.Ramadan, soundEnabled = false)
        repository.setVibrateEnabled(NotificationCategory.Ramadan, vibrateEnabled = false)
        repository.setImportance(NotificationCategory.Ramadan, NotificationImportance.Low)
        repository.setBadgeEnabled(NotificationCategory.Ramadan, badgeEnabled = false)

        val prefs = repository.prefsFor(NotificationCategory.Ramadan)
        assertThat(prefs.soundEnabled).isFalse()
        assertThat(prefs.vibrateEnabled).isFalse()
        assertThat(prefs.importance).isEqualTo(NotificationImportance.Low)
        assertThat(prefs.badgeEnabled).isFalse()

        // An unrelated category keeps its defaults.
        assertThat(repository.prefsFor(NotificationCategory.Adhan))
            .isEqualTo(NotificationCategory.Adhan.defaultPrefs())
    }

    @Test
    fun `showMissedAdhan defaults to true and persists the toggle`() = runTest {
        assertThat(repository.showMissedAdhan.first()).isTrue()
        repository.setShowMissedAdhan(false)
        assertThat(repository.showMissedAdhan.first()).isFalse()
        repository.setShowMissedAdhan(true)
        assertThat(repository.showMissedAdhan.first()).isTrue()
    }

    // -------------------------------------------------------- channel application

    @Test
    fun `channels are created for every category with the default importance`() = runTest {
        NotificationChannels.create(context)
        NotificationCategory.entries.forEach { category ->
            val channel = notificationManager.getNotificationChannel(category.channelId)
            assertThat(channel).isNotNull()
            assertThat(channel!!.importance).isEqualTo(category.defaultImportance.channelImportance)
        }
    }

    @Test
    fun `changing importance applies to the Android channel immediately`() = runTest {
        NotificationChannels.create(context)
        repository.setImportance(NotificationCategory.Adhan, NotificationImportance.Low)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.ADHAN)!!.importance)
            .isEqualTo(NotificationManager.IMPORTANCE_LOW)
        repository.setImportance(NotificationCategory.Adhan, NotificationImportance.High)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.ADHAN)!!.importance)
            .isEqualTo(NotificationManager.IMPORTANCE_HIGH)
    }

    @Test
    fun `muting a category silences its channel`() = runTest {
        NotificationChannels.create(context)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.ADHAN)!!.sound)
            .isNotNull() // default: audible
        repository.setSoundEnabled(NotificationCategory.Adhan, soundEnabled = false)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.ADHAN)!!.sound)
            .isNull() // null sound = silent channel
    }

    @Test
    fun `disabling vibration clears the channel vibration pattern`() = runTest {
        NotificationChannels.create(context)
        repository.setVibrateEnabled(NotificationCategory.Adhan, vibrateEnabled = false)
        val channel = notificationManager.getNotificationChannel(NotificationChannels.ADHAN)!!
        assertThat(channel.vibrationPattern).isNull()
        assertThat(channel.shouldVibrate()).isFalse()
    }

    @Test
    fun `badge toggle applies to the Android channel immediately`() = runTest {
        NotificationChannels.create(context)
        repository.setBadgeEnabled(NotificationCategory.HadithDaily, badgeEnabled = false)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.HADITH_DAILY)!!.canShowBadge())
            .isFalse()
        repository.setBadgeEnabled(NotificationCategory.HadithDaily, badgeEnabled = true)
        assertThat(notificationManager.getNotificationChannel(NotificationChannels.HADITH_DAILY)!!.canShowBadge())
            .isTrue()
    }
}
