package org.example.islamicapp.feature.prayertimes.data

import org.example.islamicapp.feature.prayertimes.domain.CalculationMethod
import org.example.islamicapp.feature.prayertimes.domain.PrayerParameters

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
