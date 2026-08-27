package org.muslim.app.core.datastore.prayer

import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.PrayerCalculationProfile
import org.muslim.app.core.common.prayer.PrayerParameters

/**
 * Resolves one complete calculation profile from the persisted settings.
 *
 * This is the only application-level conversion from preferences into
 * astronomical input. UI, widgets, notifications and Ramadan consumers use
 * the same profile to prevent a parameter drift between surfaces.
 */
fun PrayerSettings.toPrayerCalculationProfile(): PrayerCalculationProfile {
    val parameters = if (method == CalculationMethod.Custom) {
        PrayerParameters(
            method = CalculationMethod.Custom,
            fajrAngle = customFajrAngle,
            ishaAngle = customIshaAngle,
        )
    } else {
        PrayerParameters.of(method)
    }
    return PrayerCalculationProfile.from(
        parameters = parameters,
        asrMethod = asrMethod,
        highLatitudeRule = highLatitudeRule,
        userAdjustments = adjustments,
    )
}

/**
 * Compatibility adapter for callers that only need the astronomical parameter
 * layer. New calculation calls should use [toPrayerCalculationProfile].
 */
fun PrayerSettings.toPrayerParameters(): PrayerParameters =
    toPrayerCalculationProfile().toParameters()
