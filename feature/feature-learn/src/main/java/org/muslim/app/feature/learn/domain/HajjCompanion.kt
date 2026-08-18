package org.muslim.app.feature.learn.domain

import org.muslim.app.core.common.time.HijriDate

/** The rite (mansik) the Pilgrim Companion reminds about on each Hajj day. */
enum class HajjMansik { TARWIYAH, ARAFAT, NAHR, TASHREEQ_1, TASHREEQ_2, TASHREEQ_3 }

/**
 * Pure logic of the Pilgrim Companion (PROJECT_PROMPT.md section Hajj): which
 * rite belongs to which day of Dhul-Hijjah, and whether a given Hijri date is
 * a companion day at all. The reminder fires each morning during Dhul-Hijjah
 * 8-13 so the pilgrim is reminded of the intended rite at its time.
 */
object HajjCompanion {

    /** Maps a Dhul-Hijjah day-of-month to its mansik; null outside 8-13. */
    fun mansikFor(dhulHijjahDay: Int): HajjMansik? = when (dhulHijjahDay) {
        8 -> HajjMansik.TARWIYAH
        9 -> HajjMansik.ARAFAT
        10 -> HajjMansik.NAHR
        11 -> HajjMansik.TASHREEQ_1
        12 -> HajjMansik.TASHREEQ_2
        13 -> HajjMansik.TASHREEQ_3
        else -> null
    }

    /** True when [date] is a reminder day (Dhul-Hijjah 8-13). */
    fun isCompanionDay(date: HijriDate): Boolean =
        date.month == HajjDaysCalculator.DHUL_HIJJAH && mansikFor(date.day) != null
}
