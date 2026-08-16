package org.example.islamicapp.feature.prayertimes.widget

import org.example.islamicapp.core.datastore.prayer.PrayerSettings
import org.example.islamicapp.core.datastore.prayer.toPrayerParameters
import org.example.islamicapp.feature.prayertimes.domain.Coordinates
import org.example.islamicapp.feature.prayertimes.domain.NextPrayer
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.feature.prayertimes.domain.PrayerTimesCalculator
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Immutable snapshot of everything the home-screen widget renders: the next
 * prayer with its time and a live countdown, plus today's five prayer times.
 *
 * Pure and JVM-testable (see `PrayerTimesWidgetDataTest`); the Glance layer
 * only maps this to UI.
 */
data class PrayerTimesWidgetData(
    val hasLocation: Boolean,
    val locationName: String,
    val nextPrayer: Prayer?,
    val nextPrayerAt: LocalTime?,
    val countdownSeconds: Long,
    /** Today's five prayer times (sunrise excluded), in mushaf order. */
    val times: Map<Prayer, LocalTime>,
    val isValid: Boolean,
) {
    companion object {

        /**
         * Computes the widget snapshot from the persisted [settings] at
         * [nowMillis]. Mirrors the home screen's logic: the next prayer is
         * looked up in today's times first, then in tomorrow's (e.g. after
         * Isha the countdown targets tomorrow's Fajr).
         */
        fun compute(
            settings: PrayerSettings,
            calculator: PrayerTimesCalculator,
            nowMillis: Long,
        ): PrayerTimesWidgetData {
            val location = settings.location
            if (location == null) {
                return PrayerTimesWidgetData(
                    hasLocation = false,
                    locationName = "",
                    nextPrayer = null,
                    nextPrayerAt = null,
                    countdownSeconds = 0,
                    times = emptyMap(),
                    isValid = false,
                )
            }

            val zone = ZoneId.of(location.timeZone)
            val coordinates = Coordinates(location.latitude, location.longitude)
            val params = settings.toPrayerParameters()
            val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

            val todayResult = calculator.compute(
                today, coordinates, params, zone, settings.asrMethod, settings.adjustments,
            )
            var next = NextPrayer.nextPrayer(todayResult.epochMillis, nowMillis)
            if (next == null && todayResult.isValid) {
                val tomorrowResult = calculator.compute(
                    today.plusDays(1), coordinates, params, zone, settings.asrMethod, settings.adjustments,
                )
                if (tomorrowResult.isValid) {
                    next = NextPrayer.nextPrayer(tomorrowResult.epochMillis, nowMillis)
                }
            }

            val times = Prayer.entries
                .filter { it != Prayer.Sunrise }
                .mapNotNull { prayer -> todayResult.timeFor(prayer)?.let { prayer to it } }
                .toMap()

            return PrayerTimesWidgetData(
                hasLocation = true,
                locationName = location.name,
                nextPrayer = next?.prayer,
                nextPrayerAt = next?.atEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
                countdownSeconds = next?.let { NextPrayer.countdownSeconds(it.atEpochMillis, nowMillis) } ?: 0,
                times = times,
                isValid = todayResult.isValid,
            )
        }
    }
}

/**
 * Formats a countdown of [totalSeconds] as `HH:MM:SS` (or `MM:SS` when under
 * an hour, e.g. `04:30` → `04:30`, `01:02:03` stays full).
 */
fun formatCountdown(totalSeconds: Long): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val hours = clamped / 3600
    val minutes = (clamped % 3600) / 60
    val seconds = clamped % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
