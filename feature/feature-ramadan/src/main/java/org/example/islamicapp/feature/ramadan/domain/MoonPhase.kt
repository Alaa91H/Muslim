package org.example.islamicapp.feature.ramadan.domain

import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.cos
import kotlin.math.PI

/**
 * Moon-phase computation (طلب الإضافة "أطوار القمر") — the Islamic months
 * are lunar, so showing the current phase connects the user to the calendar.
 *
 * Uses the mean synodic month from a known new-moon epoch; accurate to
 * within a fraction of a day for display purposes.
 */
enum class MoonPhaseType {
    NewMoon,
    WaxingCrescent,
    FirstQuarter,
    WaxingGibbous,
    FullMoon,
    WaningGibbous,
    LastQuarter,
    WaningCrescent,
}

data class MoonPhaseInfo(
    /** Days into the current lunar cycle (0 .. SYNODIC_MONTH). */
    val age: Double,
    /** Illuminated fraction of the disc, 0..1. */
    val illumination: Double,
    val phase: MoonPhaseType,
    /** True when the moon is growing (between new and full). */
    val isWaxing: Boolean,
)

object MoonPhaseCalculator {

    /** Mean length of the lunar cycle in days. */
    const val SYNODIC_MONTH = 29.530588853

    // A well-documented new moon: 2000-01-06 18:14 UTC (Meeus).
    private val EPOCH_NEW_MOON_UTC = LocalDate.of(2000, 1, 6).atTime(18, 14).toInstant(ZoneOffset.UTC)

    fun at(date: LocalDate): MoonPhaseInfo {
        val days = java.time.Duration.between(
            EPOCH_NEW_MOON_UTC,
            date.atStartOfDay().toInstant(ZoneOffset.UTC),
        ).toMillis() / 86_400_000.0

        val age = ((days % SYNODIC_MONTH) + SYNODIC_MONTH) % SYNODIC_MONTH
        val illumination = (1 - cos(2 * PI * age / SYNODIC_MONTH)) / 2
        return MoonPhaseInfo(
            age = age,
            illumination = illumination,
            phase = phaseFor(age),
            isWaxing = age < SYNODIC_MONTH / 2,
        )
    }

    private fun phaseFor(age: Double): MoonPhaseType = when (age) {
        in 0.0..1.0, in (SYNODIC_MONTH - 1.0)..SYNODIC_MONTH -> MoonPhaseType.NewMoon
        in 1.0..6.38 -> MoonPhaseType.WaxingCrescent
        in 6.38..8.38 -> MoonPhaseType.FirstQuarter
        in 8.38..13.76 -> MoonPhaseType.WaxingGibbous
        in 13.76..15.76 -> MoonPhaseType.FullMoon
        in 15.76..21.15 -> MoonPhaseType.WaningGibbous
        in 21.15..23.15 -> MoonPhaseType.LastQuarter
        else -> MoonPhaseType.WaningCrescent
    }
}
