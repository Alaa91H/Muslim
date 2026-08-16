package org.example.islamicapp.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Central notification-channel definitions (core-notifications owns the
 * unified notification system per PROJECT_PROMPT.md §3.3).
 */
object NotificationChannels {

    /** High-importance channel for the Adhan itself. */
    const val ADHAN = "adhan"
    const val ADHAN_NAME = "الأذان"

    /** Default-importance channel for pre-prayer reminders. */
    const val REMINDER = "prayer_reminder"
    const val REMINDER_NAME = "تذكير الصلاة"

    /** Default-importance channel for the periodic dhikr reminder (Phase 4). */
    const val DHIKR = "dhikr_reminder"
    const val DHIKR_NAME = "تذكير الأذكار"

    /** Low-importance channel for seasonal/occasion reminders (Ramadan, events). */
    const val OCCASION = "occasion_reminder"
    const val OCCASION_NAME = "تذكيرات المناسبات"

    /** Creates all channels; safe to call on every app start (idempotent). */
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ADHAN, ADHAN_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "صوت الأذان وإشعارات مواقيت الصلاة"
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(REMINDER, REMINDER_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تذكير قبل موعد الصلاة"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(DHIKR, DHIKR_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تذكير دوري بالأذكار والذكر"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(OCCASION, OCCASION_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "تذكيرات رمضان والمناسبات الإسلامية"
            }
        )
    }
}
