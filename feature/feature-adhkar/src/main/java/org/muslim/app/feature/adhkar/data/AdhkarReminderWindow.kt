package org.muslim.app.feature.adhkar.data

/**
 * Pure day-time window math for the periodic adhkar reminder (Phase 4).
 * Kept free of Android types so it is trivially unit-testable.
 */
object AdhkarReminderWindow {

    /** Minutes since midnight. */
    fun minutesOfDay(hour: Int, minute: Int): Int =
        hour.coerceIn(0, 23) * 60 + minute.coerceIn(0, 59)

    /**
     * True when [nowMinutes] falls within [startMinutes]..[endMinutes].
     * Supports overnight windows (start after end, e.g. 22:00 → 06:00).
     */
    fun isWithinWindow(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean =
        if (startMinutes <= endMinutes) {
            nowMinutes in startMinutes..endMinutes
        } else {
            nowMinutes >= startMinutes || nowMinutes <= endMinutes
        }
}
