package org.muslim.app.core.datastore.prayer

import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.PrayerParameters

/** Resolves the effective [PrayerParameters] from persisted settings. */
fun PrayerSettings.toPrayerParameters(): PrayerParameters =
    if (method == CalculationMethod.Custom) {
        PrayerParameters(
            method = CalculationMethod.Custom,
            fajrAngle = customFajrAngle,
            ishaAngle = customIshaAngle,
            highLatitudeRule = highLatitudeRule,
        )
    } else {
        PrayerParameters.of(method).copy(highLatitudeRule = highLatitudeRule)
    }
