package org.muslim.app.core.common.prayer

/**
 * Complete, immutable input profile for one prayer-time calculation.
 *
 * A profile keeps the religious calculation choice, astronomical parameters,
 * juristic Asr method, high-latitude bound and user offsets together. Every
 * consumer should build a single profile from the persisted settings and pass
 * that same snapshot to the calculator, rather than reconstructing a partial
 * parameter set for UI, widgets or alarms.
 */
data class PrayerCalculationProfile(
    val calculationMethod: CalculationMethod,
    val fajrAngle: Double,
    val ishaAngle: Double?,
    val ishaMinutes: Int,
    val maghribAngle: Double?,
    val maghribMinutes: Int,
    val dhuhrMinutes: Int,
    val methodAdjustments: PrayerAdjustments,
    val asrMethod: AsrMethod,
    val highLatitudeRule: HighLatitudeRule,
    val userAdjustments: PrayerAdjustments,
    val roundUp: Boolean,
) {
    /** Returns the pure astronomical parameter set, without user offsets. */
    fun toParameters(): PrayerParameters = PrayerParameters(
        method = calculationMethod,
        fajrAngle = fajrAngle,
        ishaAngle = ishaAngle,
        ishaMinutes = ishaMinutes,
        maghribAngle = maghribAngle,
        maghribMinutes = maghribMinutes,
        dhuhrMinutes = dhuhrMinutes,
        methodAdjustments = methodAdjustments,
        highLatitudeRule = highLatitudeRule,
        roundUp = roundUp,
    )

    companion object {
        /**
         * Combines an already-resolved method parameter set with the user
         * choices that deliberately apply after the astronomical calculation.
         */
        fun from(
            parameters: PrayerParameters,
            asrMethod: AsrMethod,
            highLatitudeRule: HighLatitudeRule,
            userAdjustments: PrayerAdjustments,
        ): PrayerCalculationProfile = PrayerCalculationProfile(
            calculationMethod = parameters.method,
            fajrAngle = parameters.fajrAngle,
            ishaAngle = parameters.ishaAngle,
            ishaMinutes = parameters.ishaMinutes,
            maghribAngle = parameters.maghribAngle,
            maghribMinutes = parameters.maghribMinutes,
            dhuhrMinutes = parameters.dhuhrMinutes,
            methodAdjustments = parameters.methodAdjustments,
            asrMethod = asrMethod,
            highLatitudeRule = highLatitudeRule,
            userAdjustments = userAdjustments,
            roundUp = parameters.roundUp,
        )
    }
}
