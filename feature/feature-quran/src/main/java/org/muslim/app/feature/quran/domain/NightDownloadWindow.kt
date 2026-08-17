package org.muslim.app.feature.quran.domain

import java.time.Instant
import java.time.ZoneId

/**
 * Pure night-download window logic (التحميل الليلي): downloads marked as
 * night-only start only inside [startMinutes, endMinutes) (minutes from
 * midnight, supporting overnight ranges like 23:00–05:00). Used by the
 * download service to hold a request until the window opens and to schedule
 * the exact wake-up alarm.
 */
object NightDownloadWindow {

    /** True when [minutesOfDay] falls inside the window (overnight-safe). */
    fun contains(minutesOfDay: Int, startMinutes: Int, endMinutes: Int): Boolean {
        if (startMinutes == endMinutes) return false
        return if (startMinutes < endMinutes) {
            minutesOfDay in startMinutes until endMinutes
        } else {
            minutesOfDay >= startMinutes || minutesOfDay < endMinutes
        }
    }

    /** True when the instant [nowMillis] is inside the window in [zone]. */
    fun contains(nowMillis: Long, zone: ZoneId, startMinutes: Int, endMinutes: Int): Boolean =
        contains(minutesOfDayAt(nowMillis, zone), startMinutes, endMinutes)

    /**
     * The epoch-millis instant when the window next opens: [startMinutes]
     * today if that time hasn't passed yet, otherwise tomorrow. Used to
     * schedule the alarm that kicks off a night-only download.
     */
    fun nextOpenMillis(nowMillis: Long, zone: ZoneId, startMinutes: Int): Long {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val openToday = now.toLocalDate()
            .atTime(startMinutes / 60, startMinutes % 60)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        return if (openToday > nowMillis) {
            openToday
        } else {
            now.toLocalDate().plusDays(1)
                .atTime(startMinutes / 60, startMinutes % 60)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }
    }

    private fun minutesOfDayAt(nowMillis: Long, zone: ZoneId): Int {
        val time = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalTime()
        return time.hour * 60 + time.minute
    }
}
