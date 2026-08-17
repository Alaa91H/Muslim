package org.muslim.app.feature.prayertimes.ui

import androidx.annotation.StringRes
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.Prayer

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
