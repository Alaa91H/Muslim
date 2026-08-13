package org.example.islamicapp.feature.prayertimes.ui

import androidx.annotation.StringRes
import org.example.islamicapp.feature.prayertimes.R
import org.example.islamicapp.feature.prayertimes.domain.Prayer

/** Maps [Prayer] to its localized display name. */
@StringRes
fun prayerLabelRes(prayer: Prayer): Int = when (prayer) {
    Prayer.Fajr -> R.string.prayer_fajr
    Prayer.Sunrise -> R.string.prayer_sunrise
    Prayer.Dhuhr -> R.string.prayer_dhuhr
    Prayer.Asr -> R.string.prayer_asr
    Prayer.Maghrib -> R.string.prayer_maghrib
    Prayer.Isha -> R.string.prayer_isha
}
