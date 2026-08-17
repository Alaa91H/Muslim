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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationCategoryPrefs
import org.muslim.app.core.notifications.NotificationImportance
import org.muslim.app.core.notifications.NotificationPrefsRepository
import org.muslim.app.core.notifications.QuietHours
import org.muslim.app.feature.settings.R
import javax.inject.Inject

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
) : ViewModel() {

    val preferences: StateFlow<Map<NotificationCategory, NotificationCategoryPrefs>> =
        prefsRepository.prefs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val quietHours: StateFlow<QuietHours> =
        prefsRepository.quietHours
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuietHours())

    /** True when the app may post notifications (always true below Android 13). */
    fun notificationPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun setEnabled(category: NotificationCategory, enabled: Boolean) = launch {
        prefsRepository.setEnabled(category, enabled)
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
            .setSmallIcon(context.applicationInfo.icon)
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
    }

    private companion object {
        const val TEST_NOTIFICATION_ID = 60_000
    }
}
