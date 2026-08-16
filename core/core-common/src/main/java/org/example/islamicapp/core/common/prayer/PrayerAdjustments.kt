package org.example.islamicapp.core.common.prayer

/**
 * Manual adjustment (in minutes) applied to each prayer time on top of the
 * astronomical calculation. Persisted as a user preference (PROJECT_PROMPT.md
 * §6, Phase 1: "تعديل يدوي دقيق (بالدقيقة) لكل صلاة على حدة").
 */
data class PrayerAdjustments(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0,
) {
    operator fun get(prayer: Prayer): Int = when (prayer) {
        Prayer.Fajr -> fajr
        Prayer.Sunrise -> sunrise
        Prayer.Dhuhr -> dhuhr
        Prayer.Asr -> asr
        Prayer.Maghrib -> maghrib
        Prayer.Isha -> isha
    }

    /** Returns a copy with [minutes] set for [prayer]. */
    fun with(prayer: Prayer, minutes: Int): PrayerAdjustments = when (prayer) {
        Prayer.Fajr -> copy(fajr = minutes)
        Prayer.Sunrise -> copy(sunrise = minutes)
        Prayer.Dhuhr -> copy(dhuhr = minutes)
        Prayer.Asr -> copy(asr = minutes)
        Prayer.Maghrib -> copy(maghrib = minutes)
        Prayer.Isha -> copy(isha = minutes)
    }
}
