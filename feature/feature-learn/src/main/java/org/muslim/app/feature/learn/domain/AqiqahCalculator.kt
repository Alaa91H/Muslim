package org.muslim.app.feature.learn.domain

import java.time.LocalDate
import java.time.ZoneId

/** Recommended aqiqah dates calculated from the child's birth date. */
data class AqiqahSchedule(
    val birthDate: LocalDate,
    val seventhDay: LocalDate,
    val fourteenthDay: LocalDate,
    val twentyFirstDay: LocalDate,
) {
    val recommendedDates: List<LocalDate>
        get() = listOf(seventhDay, fourteenthDay, twentyFirstDay)
}

object AqiqahCalculator {
    const val REMINDER_HOUR = 9

    /**
     * The commonly cited options are the seventh, fourteenth or twenty-first
     * day after birth. The seventh is presented first; this is a reminder aid,
     * not a binding fatwa, and local scholars may explain other valid views.
     */
    fun schedule(birthDate: LocalDate): AqiqahSchedule = AqiqahSchedule(
        birthDate = birthDate,
        seventhDay = birthDate.plusDays(7),
        fourteenthDay = birthDate.plusDays(14),
        twentyFirstDay = birthDate.plusDays(21),
    )

    /** Days from [today] until the first recommended date; negative means passed. */
    fun daysUntilFirst(birthDate: LocalDate, today: LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(today, schedule(birthDate).seventhDay)

    /**
     * Epoch time for a one-time reminder at 09:00 local time on the seventh
     * day. Returning null avoids scheduling an already-past reminder.
     */
    fun nextReminderMillis(
        birthDate: LocalDate,
        nowMillis: Long,
        zone: ZoneId,
    ): Long? {
        val target = schedule(birthDate).seventhDay
            .atTime(REMINDER_HOUR, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        return target.takeIf { it > nowMillis }
    }
}
