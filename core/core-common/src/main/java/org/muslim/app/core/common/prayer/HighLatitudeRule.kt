package org.muslim.app.core.common.prayer

/**
 * Rules for placing bounds on Fajr and Isha at high latitudes where the sun
 * may never reach the required depression angle (Scandinavia, Alaska, ...).
 *
 * - [MiddleOfTheNight] طريقة منتصف الليل: Fajr no earlier than the middle of the
 *   night, Isha no later than the middle of the night.
 * - [SeventhOfTheNight] طريقة السُبع الأخير: Fajr no earlier than the start of
 *   the last seventh of the night, Isha no later than the end of the first seventh.
 * - [TwilightAngle] طريقة الزاوية: the fraction of the night used is
 *   fajrAngle/60 for Fajr and ishaAngle/60 for Isha.
 */
enum class HighLatitudeRule {
    MiddleOfTheNight,
    SeventhOfTheNight,
    TwilightAngle,
}
