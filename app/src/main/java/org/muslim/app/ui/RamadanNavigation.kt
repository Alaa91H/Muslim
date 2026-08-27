package org.muslim.app.ui

import java.time.LocalDate
import org.muslim.app.core.common.time.HijriDate

/**
 * Seasonal bottom-navigation rule. Ramadan is a primary destination only when
 * the same adjusted Umm al-Qura date used throughout the app is in month 9.
 */
internal object RamadanNavigation {
    fun isRamadan(today: LocalDate, hijriAdjustment: Int): Boolean =
        HijriDate.from(today, hijriAdjustment).month == RAMADAN_MONTH

    private const val RAMADAN_MONTH = 9
}
