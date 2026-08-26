package org.muslim.app.feature.settings

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.NextPrayer
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.toPrayerParameters
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationCategoryPrefs
import org.muslim.app.core.notifications.NotificationImportance
import org.muslim.app.core.notifications.MissedAdhanColors
import org.muslim.app.core.notifications.NotificationPrefsRepository
import org.muslim.app.core.notifications.QuietHours
import org.muslim.app.feature.hadith.data.HadithOfTheDayScheduler
import org.muslim.app.feature.hadith.data.HadithPrefsRepository
import org.muslim.app.feature.learn.data.HajjCompanionScheduler
import org.muslim.app.feature.settings.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Real OS-level state of one Android notification channel. */
enum class SystemChannelStatus { Allowed, Blocked, NotCreated }

/** Live snapshot shown in the countdown-notification preview. */
data class CountdownPreview(
    val hasLocation: Boolean = false,
    val nextPrayer: Prayer? = null,
    val nextPrayerAt: LocalTime? = null,
    val remainingSeconds: Long = 0,
    val missedPrayer: Prayer? = null,
    val missedPrayerAt: LocalTime? = null,
    val elapsedSeconds: Long = 0,
)

/**
 * Unified notification manager (PROJECT_PROMPT.md §3.3). One switch per
 * [NotificationCategory] plus fine-grained presentation (sound, vibration,
 * importance, badge) that is mirrored onto the Android channels in real time,
 * a global quiet-hours window, a test-notification button per category, and
 * direct access to the system notification settings.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: NotificationPrefsRepository,
    private val hadithPrefsRepository: HadithPrefsRepository,
    private val prayerSettingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerTimesCalculator,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<Map<NotificationCategory, NotificationCategoryPrefs>> =
        prefsRepository.prefs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val quietHours: StateFlow<QuietHours> =
        prefsRepository.quietHours
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuietHours())

    /**
     * Whether the permanent next-adhan notification shows the missed-adhan
     * line (user can hide it; defaults to true).
     */
    val showMissedAdhan: StateFlow<Boolean> =
        prefsRepository.showMissedAdhan
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val missedAdhanColor: StateFlow<Int> =
        prefsRepository.missedAdhanColor
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MissedAdhanColors.DEFAULT)

    private val secondTicker = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }

    /**
     * Live snapshot of the permanent next-adhan countdown notification,
     * recomputed every second while the screen is open: the real next prayer
     * from the saved location plus the missed adhan (when one exists).
     */
    val countdownPreview: StateFlow<CountdownPreview> =
        combine(prayerSettingsRepository.settings, secondTicker) { settings, _ ->
            computeCountdown(settings)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CountdownPreview())

    private fun computeCountdown(settings: PrayerSettings): CountdownPreview {
        val location = settings.location ?: return CountdownPreview(hasLocation = false)
        val zone = ZoneId.of(location.timeZone)
        val now = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val coordinates = Coordinates(location.latitude, location.longitude, location.elevation)
        val params = settings.toPrayerParameters()
        val todayResult = calculator.compute(
            today, coordinates, params, zone, settings.asrMethod, settings.adjustments,
        )
        var next = NextPrayer.nextPrayer(todayResult.epochMillis, now)
        if (next == null) {
            next = NextPrayer.nextPrayer(
                calculator.compute(
                    today.plusDays(1), coordinates, params, zone, settings.asrMethod, settings.adjustments,
                ).epochMillis,
                now,
            )
        }
        val missed = todayResult.epochMillis
            .filterValues { it <= now }
            .maxByOrNull { it.value }
        return CountdownPreview(
            hasLocation = true,
            nextPrayer = next?.prayer,
            nextPrayerAt = next?.atEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
            remainingSeconds = next?.let { (it.atEpochMillis - now) / 1_000L } ?: 0,
            missedPrayer = missed?.key,
            missedPrayerAt = missed?.value?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
            elapsedSeconds = missed?.let { (now - it.value) / 1_000L } ?: 0,
        )
    }

    /**
     * Daily hadith-of-the-day notification time, in minutes from midnight.
     * Mirrors the picker in the hadith screen so both stay in sync.
     */
    val dailyHadithTimeMinutes: StateFlow<Int> =
        hadithPrefsRepository.dailyNotificationTimeMinutes
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                HadithPrefsRepository.DEFAULT_NOTIFICATION_TIME_MINUTES,
            )

    /** True when the app may post notifications (always true below Android 13). */
    fun notificationPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun setEnabled(category: NotificationCategory, enabled: Boolean) = launch {
        prefsRepository.setEnabled(category, enabled)
        // The Pilgrim Companion is a mode, not just a channel: enabling it
        // schedules its daily reminder; disabling it cancels the job.
        if (category == NotificationCategory.Hajj) {
            if (enabled) {
                HajjCompanionScheduler.schedule(context)
            } else {
                HajjCompanionScheduler.cancel(context)
            }
        }
    }

    fun setSoundEnabled(category: NotificationCategory, enabled: Boolean) = launch {
        prefsRepository.setSoundEnabled(category, enabled)
    }

    fun setVibrateEnabled(category: NotificationCategory, enabled: Boolean) = launch {
        prefsRepository.setVibrateEnabled(category, enabled)
    }

    fun setImportance(category: NotificationCategory, importance: NotificationImportance) = launch {
        prefsRepository.setImportance(category, importance)
    }

    fun setBadgeEnabled(category: NotificationCategory, enabled: Boolean) = launch {
        prefsRepository.setBadgeEnabled(category, enabled)
    }

    fun setShowMissedAdhan(show: Boolean) = launch {
        prefsRepository.setShowMissedAdhan(show)
    }

    /** Persists the new daily-hadith time and re-schedules when it is enabled. */
    fun setDailyHadithTimeMinutes(minutes: Int) = launch {
        hadithPrefsRepository.setDailyNotificationTimeMinutes(minutes)
        if (hadithPrefsRepository.dailyNotificationEnabled.first()) {
            HadithOfTheDayScheduler.schedule(context, minutes)
        }
    }

    fun setMissedAdhanColor(color: Int) = launch {
        prefsRepository.setMissedAdhanColor(color)
    }

    fun setQuietHoursEnabled(enabled: Boolean) = launch {
        prefsRepository.setQuietHours(quietHours.value.copy(enabled = enabled))
    }

    fun setQuietStartMinutes(minutes: Int) = launch {
        prefsRepository.setQuietHours(quietHours.value.copy(startMinutes = minutes))
    }

    fun setQuietEndMinutes(minutes: Int) = launch {
        prefsRepository.setQuietHours(quietHours.value.copy(endMinutes = minutes))
    }

    /** Posts a sample notification on [category]'s channel so the user sees the exact result. */
    fun testNotification(category: NotificationCategory) {
        val notification = Notification.Builder(context, category.channelId)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1250)
            .setContentTitle(context.getString(categoryLabelRes(category)))
            .setContentText(context.getString(R.string.notif_test_body))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(TEST_NOTIFICATION_ID + category.ordinal, notification)
    }

    /** Opens the system screen for this app's notifications. */
    fun openSystemNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /**
     * Reads the live OS state of [category]'s channel: blocked when the
     * notification permission is denied (Android 13+) or when the channel's
     * importance was set to "none" in system settings.
     */
    fun systemChannelStatus(category: NotificationCategory): SystemChannelStatus {
        if (!notificationPermissionGranted()) return SystemChannelStatus.Blocked
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(category.channelId)
            ?: return SystemChannelStatus.NotCreated
        return if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
            SystemChannelStatus.Blocked
        } else {
            SystemChannelStatus.Allowed
        }
    }

    /** Snapshot of every category's system channel status (for the screen). */
    fun channelStatuses(): Map<NotificationCategory, SystemChannelStatus> =
        NotificationCategory.entries.associateWith { systemChannelStatus(it) }

    /** Opens the system settings for [category]'s specific channel. */
    fun openSystemChannelSettings(category: NotificationCategory) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, category.channelId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private fun categoryLabelRes(category: NotificationCategory): Int = when (category) {
        NotificationCategory.Adhan -> R.string.notif_category_adhan
        NotificationCategory.PrayerReminder -> R.string.notif_category_reminder
        NotificationCategory.QuranDaily -> R.string.notif_category_quran
        NotificationCategory.Ramadan -> R.string.notif_category_ramadan
        NotificationCategory.Adhkar -> R.string.notif_category_adhkar
        NotificationCategory.HadithDaily -> R.string.notif_category_hadith
        NotificationCategory.PrayerCountdown -> R.string.notif_category_prayer_countdown
        NotificationCategory.Recitation -> R.string.notif_category_recitation
        NotificationCategory.Hajj -> R.string.notif_category_hajj
        NotificationCategory.Family -> R.string.notif_category_family
        NotificationCategory.Finance -> R.string.notif_category_finance
        NotificationCategory.AppUpdate -> R.string.notif_category_app_update
    }

    private companion object {
        const val TEST_NOTIFICATION_ID = 60_000
    }
}
