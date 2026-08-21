package org.muslim.app.feature.ramadan.notifications

/**
 * Pure, JVM-testable decision logic for the iftar/suhoor alarms.
 *
 * The rule (matching the app's default behaviour): by default the alarms
 * only fire on Ramadan days — iftar on a day that is in Ramadan, suhoor only
 * when the *next* day is a fasting day. When [outsideEnabled] is true the
 * user opted into year-round reminders and both alarms keep running.
 */
data class RamadanAlarmPlan(
    /** Epoch millis for the next iftar alarm, or null when it must not fire. */
    val iftarAtMillis: Long? = null,
    /** Epoch millis for the next suhoor reminder, or null when it must not fire. */
    val suhoorAtMillis: Long? = null,
) {
    val any: Boolean get() = iftarAtMillis != null || suhoorAtMillis != null
}

object RamadanSchedulePlanner {

    fun plan(
        now: Long,
        todayInRamadan: Boolean,
        tomorrowInRamadan: Boolean,
        outsideEnabled: Boolean,
        suhoorMinutesBefore: Int,
        todayMaghrib: Long?,
        tomorrowMaghrib: Long?,
        tomorrowFajr: Long?,
    ): RamadanAlarmPlan {
        val iftarAt: Long? = if (todayInRamadan || outsideEnabled) {
            when {
                todayMaghrib != null && todayMaghrib > now -> todayMaghrib
                // Only fall forward to tomorrow when tomorrow is also a
                // fasting day (or the user opted into year-round reminders),
                // otherwise the alarm would fire outside Ramadan.
                tomorrowMaghrib != null && (tomorrowInRamadan || outsideEnabled) && tomorrowMaghrib > now ->
                    tomorrowMaghrib
                else -> null
            }
        } else {
            null
        }

        val suhoorAt: Long? = if (tomorrowInRamadan || outsideEnabled) {
            if (tomorrowFajr != null) {
                val reminderAt = tomorrowFajr - suhoorMinutesBefore.coerceAtLeast(0) * 60_000L
                if (reminderAt > now) reminderAt else null
            } else {
                null
            }
        } else {
            null
        }

        return RamadanAlarmPlan(iftarAtMillis = iftarAt, suhoorAtMillis = suhoorAt)
    }
}
