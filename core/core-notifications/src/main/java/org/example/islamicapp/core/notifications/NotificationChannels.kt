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

    /** Low-importance channel for the optional daily ayah notification. */
    const val QURAN_DAILY = "quran_daily"
    const val QURAN_DAILY_NAME = "آية اليوم"

    /** High-importance channel for Ramadan iftar / suhoor alerts. */
    const val RAMADAN = "ramadan"
    const val RAMADAN_NAME = "رمضان"

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
            NotificationChannel(QURAN_DAILY, QURAN_DAILY_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "آية يومية اختيارية مع تذكير بالقراءة"
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(RAMADAN, RAMADAN_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تنبيهات الإفطار والسحور في رمضان"
                enableVibration(true)
            }
        )
    }
}
