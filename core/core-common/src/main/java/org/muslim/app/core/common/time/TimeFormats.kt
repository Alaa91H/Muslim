package org.muslim.app.core.common.time

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/**
 * App-wide time formatting (the user picks 12- or 24-hour in Settings, 12-hour
 * by default). Always renders Western digits — Arabic-Indic numerals are
 * banned across the project (PROJECT_PROMPT.md: "تقييد وإزالة الأرقام الهندية").
 */
object TimeFormats {

    /**
     * A formatter for the chosen system: "5:23 AM" / "5:23 ص" (12-hour, with
     * the locale's AM/PM marker) or "17:23" (24-hour).
     */
    fun timeFormatter(use24h: Boolean, locale: Locale = Locale.getDefault()): DateTimeFormatter =
        DateTimeFormatter.ofPattern(if (use24h) "HH:mm" else "h:mm a", locale)
            .withDecimalStyle(DecimalStyle.STANDARD)

    /** Formats a [LocalTime] with the chosen system. */
    fun formatTime(time: LocalTime, use24h: Boolean, locale: Locale = Locale.getDefault()): String =
        time.format(timeFormatter(use24h, locale))

    /** Formats minutes-from-midnight (e.g. a notification time) with the chosen system. */
    fun formatMinutes(minutes: Int, use24h: Boolean, locale: Locale = Locale.getDefault()): String =
        formatTime(LocalTime.of(minutes / 60, minutes % 60), use24h, locale)
}
