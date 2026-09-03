package org.muslim.app.feature.ramadan.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.NotificationCompat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.notifications.notificationAllowed
import org.muslim.app.feature.ramadan.R
import org.muslim.app.feature.ramadan.data.RamadanRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Posts the actual iftar / suhoor notifications. */
@Singleton
class RamadanNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun notify(type: String) {
        val isIftar = type == RamadanAlarmReceiver.TYPE_IFTAR
        val title = context.getString(if (isIftar) R.string.ramadan_notification_iftar_title else R.string.ramadan_notification_suhoor_title)
        val body = context.getString(if (isIftar) R.string.ramadan_notification_iftar_body else R.string.ramadan_notification_suhoor_body)
        val notification = NotificationCompat.Builder(context, NotificationChannels.RAMADAN)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(true)
            .setShowWhen(true)
            .build()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(if (isIftar) 3001 else 3002, notification)
    }
}

/** Fires at the scheduled times; posts the notification and re-schedules the next day. */
class RamadanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_IFTAR
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, RamadanEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            // Respect the unified manager AND the per-type toggles; still
            // re-arm the alarms either way.
            val ramadanSettings = entryPoint.ramadanRepository().settings.first()
            val typeEnabled = when (type) {
                TYPE_IFTAR -> ramadanSettings.iftarNotificationEnabled
                else -> ramadanSettings.suhoorReminderEnabled
            }
            if (typeEnabled &&
                context.applicationContext.notificationAllowed(NotificationCategory.Ramadan)
            ) {
                entryPoint.notifier().notify(type)
            }
            val prayerSettings = entryPoint.prayerSettingsRepository().settings.first()
            entryPoint.ramadanScheduler().schedule(
                prayerSettings = prayerSettings,
                ramadanSettings = ramadanSettings,
            )
        }
    }

    companion object {
        const val EXTRA_TYPE = "ramadan_alarm_type"
        const val TYPE_IFTAR = "iftar"
        const val TYPE_SUHOOR = "suhoor"
    }
}

/** Re-schedules the Ramadan alarms after reboot / clock / timezone changes. */
abstract class RamadanRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, RamadanEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            val prayerSettings = entryPoint.prayerSettingsRepository().settings.first()
            val ramadanSettings = entryPoint.ramadanRepository().settings.first()
            entryPoint.ramadanScheduler().schedule(
                prayerSettings = prayerSettings,
                ramadanSettings = ramadanSettings,
            )
        }
    }
}

class RamadanBootReceiver : RamadanRescheduleReceiver()

class RamadanTimeChangeReceiver : RamadanRescheduleReceiver()

/** Intent actions this module's receivers listen to. */
object RamadanActions {
    val BOOT = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
    val TIME_CHANGE = IntentFilter().apply {
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RamadanEntryPoint {
    fun notifier(): RamadanNotifier
    fun ramadanScheduler(): RamadanScheduler
    fun prayerSettingsRepository(): PrayerSettingsRepository
    fun ramadanRepository(): RamadanRepository
}
