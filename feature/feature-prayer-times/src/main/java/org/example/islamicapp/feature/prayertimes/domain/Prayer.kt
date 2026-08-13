package org.example.islamicapp.feature.prayertimes.domain

/**
 * The five daily prayers plus sunrise.
 * Sunrise is not a prayer but is computed alongside (needed for display,
 * the qibla context and night-length calculations).
 */
enum class Prayer {
    Fajr,
    Sunrise,
    Dhuhr,
    Asr,
    Maghrib,
    Isha,
}
