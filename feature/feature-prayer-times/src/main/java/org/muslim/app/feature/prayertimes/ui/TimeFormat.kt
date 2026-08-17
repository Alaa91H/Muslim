package org.muslim.app.feature.prayertimes.ui

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Formats a time with the device locale, e.g. "5:23 AM" / "٥:٢٣ ص". */
val localTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())

/** Formats "13 Aug 2026" with the device locale. */
val localDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

/** Countdown "HH:MM:SS". */
fun formatCountdown(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

/** Formats a kilometre distance compactly: "12 934 كم" / "12,934 km". */
fun formatDistanceKm(km: Double): String =
    String.format(Locale.getDefault(), "%,.0f", km)
