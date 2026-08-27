package org.muslim.app.feature.prayertimes.domain

import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.NextPrayer
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Immutable snapshot of what the permanent "next adhan" notification shows:
 * the upcoming prayer with its wall-clock time and a live countdown, plus —
 * in red — the most recent prayer whose time already passed ("missed adhan")
 * with how long ago its adhan was. Pure and JVM-testable (see
 * `PrayerCountdownDataTest`); the notification layer only maps this to UI.
 */
data class PrayerCountdownData(
    val hasLocation: Boolean,
    val isValid: Boolean,
    val nextPrayer: Prayer?,
    val nextPrayerAt: LocalTime?,
    val remainingSeconds: Long,
    val missedPrayer: Prayer?,
    val missedPrayerAt: LocalTime?,
    val elapsedSeconds: Long,
) {

    companion object {

        /**
         * Computes the snapshot from the persisted [settings] at [nowMillis].
         * The next prayer is looked up in today's times first, then in
         * tomorrow's (e.g. after Isha the countdown targets tomorrow's Fajr).
         * The missed adhan is the most recent prayer that already passed
         * today, or yesterday's last prayer before today's Fajr.
         */
        fun compute(
            settings: PrayerSettings,
            calculator: PrayerTimesCalculator,
            nowMillis: Long,
        ): PrayerCountdownData {
            val location = settings.location
            if (location == null) {
                return PrayerCountdownData(
                    hasLocation = false,
                    isValid = false,
                    nextPrayer = null,
                    nextPrayerAt = null,
                    remainingSeconds = 0,
                    missedPrayer = null,
                    missedPrayerAt = null,
                    elapsedSeconds = 0,
                )
            }

            val zone = ZoneId.of(location.timeZone)
            val coordinates = Coordinates(location.latitude, location.longitude, location.elevation)
            val profile = settings.toPrayerCalculationProfile()
            val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

            fun computeFor(date: LocalDate) = calculator.compute(
                date = date,
                coordinates = coordinates,
                profile = profile,
                timeZone = zone,
            )

            val todayResult = computeFor(today)
            val todayEpochs = todayResult.epochMillis.filterKeys { it != Prayer.Sunrise }

            // Next prayer: today first, then tomorrow's Fajr (standard rollover).
            var next = NextPrayer.nextPrayer(todayEpochs, nowMillis)
            if (next == null && todayResult.isValid) {
                val tomorrowResult = computeFor(today.plusDays(1))
                if (tomorrowResult.isValid) {
                    next = NextPrayer.nextPrayer(tomorrowResult.epochMillis, nowMillis)
                }
            }

            // Missed adhan: the most recent prayer that already passed today;
            // before today's Fajr fall back to yesterday's last prayer.
            var missed: Pair<Prayer, Long>? = todayEpochs
                .filterValues { it < nowMillis }
                .maxByOrNull { it.value }
                ?.let { it.key to it.value }
            if (missed == null) {
                val yesterdayResult = computeFor(today.minusDays(1))
                if (yesterdayResult.isValid) {
                    missed = yesterdayResult.epochMillis
                        .filterKeys { it != Prayer.Sunrise }
                        .filterValues { it < nowMillis }
                        .maxByOrNull { it.value }
                        ?.let { it.key to it.value }
                }
            }

            return PrayerCountdownData(
                hasLocation = true,
                isValid = todayResult.isValid,
                nextPrayer = next?.prayer,
                nextPrayerAt = next?.atEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
                remainingSeconds = next?.let { NextPrayer.countdownSeconds(it.atEpochMillis, nowMillis) } ?: 0,
                missedPrayer = missed?.first,
                missedPrayerAt = missed?.second?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
                elapsedSeconds = missed?.let { ((nowMillis - it.second).coerceAtLeast(0) + 999) / 1000 } ?: 0,
            )
        }
    }
}

/** Formats a duration of [totalSeconds] as `HH:MM:SS` (or `MM:SS` under an hour). */
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
