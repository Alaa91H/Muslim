package org.muslim.app.core.common.prayer

/**
 * Well-known global calculation methods (PROJECT_PROMPT.md §6 Phase 1).
 *
 * Parameters follow the reference implementation of the open-source Adhan
 * library (Batoul Apps, MIT license) which itself tracks the standard
 * definitions used by Muslim World League, ISNA, Umm al-Qura, etc.
 * "method adjustments" reflect the small offsets each authority applies to
 * align with its own published tables.
 */
enum class CalculationMethod {

    /** رابطة العالم الإسلامي — Fajr 18°, Isha 17°. */
    MuslimWorldLeague,

    /** الهيئة المصرية العامة للمساحة — Fajr 19.5°, Isha 17.5°. */
    Egyptian,

    /** جامعة العلوم الإسلامية بكراتشي — Fajr 18°, Isha 18°. */
    Karachi,

    /** أم القرى (مكة) — Fajr 18.5°, Isha = Maghrib + 90 min. */
    UmmAlQura,

    /** ISNA (أمريكا الشمالية) — Fajr 15°, Isha 15°. */
    NorthAmerica,

    /** دبي والخليج — Fajr 18.2°, Isha 18.2°. */
    Dubai,

    /** قطر — Fajr 18°, Isha = Maghrib + 90 min. */
    Qatar,

    /** الكويت — Fajr 18°, Isha 17.5°. */
    Kuwait,

    /** سنغافورة — Fajr 20°, Isha 18°. */
    Singapore,

    /** لجنة رؤية الهلال (Moonsighting) — Fajr 18°, Isha 18°, with seasonal adjustments. */
    MoonsightingCommittee,

    /** ديانت تركيا — Fajr 18°, Isha 17°. */
    Turkey,

    /** فرنسا (UOIF) — Fajr 18°, Isha 17°. */
    France,

    /** إعداد مخصص يُدخله المستخدم (زاويتا الفجر والعشاء) — see [customParameters]. */
    Custom,
    ;

    companion object {

        /**
         * Region-appropriate default method — maps a country or region keyword
         * from the bundled cities database to its officially used calculation
         * method. Falls back to the Muslim World League.
         */
        fun suggestedFor(region: String): CalculationMethod {
            val r = region.trim()
            return when {
                listOf("Saudi", "السعودية").any { r.contains(it, ignoreCase = true) } -> UmmAlQura
                listOf("Egypt", "مصر").any { r.contains(it, ignoreCase = true) } -> Egyptian
                listOf("Pakistan", "باكستان", "Kashmir").any { r.contains(it, ignoreCase = true) } -> Karachi
                listOf("India", "الهند", "Bangladesh", "بنغلاديش").any { r.contains(it, ignoreCase = true) } -> Karachi
                listOf("United States", "USA", "أمريكا", "Canada", "كندا").any { r.contains(it, ignoreCase = true) } -> NorthAmerica
                listOf("Turkey", "تركيا").any { r.contains(it, ignoreCase = true) } -> Turkey
                listOf("France", "فرنسا", "Belgium", "بلجيكا", "Netherlands", "هولندا").any { r.contains(it, ignoreCase = true) } -> France
                listOf("UAE", "الإمارات", "Dubai", "دبي").any { r.contains(it, ignoreCase = true) } -> Dubai
                listOf("Qatar", "قطر").any { r.contains(it, ignoreCase = true) } -> Qatar
                listOf("Kuwait", "الكويت").any { r.contains(it, ignoreCase = true) } -> Kuwait
                listOf("Singapore", "سنغافورة", "Malaysia", "ماليزيا", "Indonesia", "إندونيسيا").any { r.contains(it, ignoreCase = true) } -> Singapore
                listOf("UK", "بريطانيا", "Britain", "United Kingdom", "Germany", "ألمانيا", "Europe", "أوروبا").any { r.contains(it, ignoreCase = true) } -> MuslimWorldLeague
                // Non-Sunni institutes were deliberately removed from the
                // app; Iran falls back to the global Sunni default.
                else -> MuslimWorldLeague
            }
        }

        /**
         * Builds parameters for [Custom] from user-provided angles.
         * Falls back to Muslim World League values when the angles are blank.
         */
        fun customParameters(fajrAngle: Double, ishaAngle: Double): PrayerParameters {
            val safeFajr = fajrAngle.takeIf { it > 0 } ?: 18.0
            val safeIsha = ishaAngle.takeIf { it > 0 } ?: 17.0
            return PrayerParameters(
                method = Custom,
                fajrAngle = safeFajr,
                ishaAngle = safeIsha,
            )
        }
    }
}

/**
 * Immutable set of parameters that fully defines a prayer-time computation.
 */
data class PrayerParameters(
    val method: CalculationMethod,
    val fajrAngle: Double,
    /** Sun depression angle for Isha; `null` when Isha is Maghrib + [ishaMinutes]. */
    val ishaAngle: Double?,
    /** Minutes after Maghrib used for Isha when [ishaAngle] is null. */
    val ishaMinutes: Int = 0,
    /** Sun depression angle for Maghrib; `null` means sunset (0.833°). */
    val maghribAngle: Double? = null,
    /** Minutes after sunset added to Maghrib. */
    val maghribMinutes: Int = 0,
    /** Minutes after solar noon added to Dhuhr. */
    val dhuhrMinutes: Int = 0,
    /** Small offsets built into the method definition (see [CalculationMethod]). */
    val methodAdjustments: PrayerAdjustments = PrayerAdjustments(),
    /** Rule used to bound Fajr/Isha at high latitudes; null → recommended for latitude. */
    val highLatitudeRule: HighLatitudeRule? = null,
    /** Rounding mode applied to the final minute values. */
    val roundUp: Boolean = false,
) {
    companion object {
        fun of(method: CalculationMethod): PrayerParameters = when (method) {
            CalculationMethod.MuslimWorldLeague -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 17.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 1),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Egyptian -> PrayerParameters(
                method = method, fajrAngle = 19.5, ishaAngle = 17.5, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 1),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Karachi -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 18.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 1),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.UmmAlQura -> PrayerParameters(
                method = method, fajrAngle = 18.5, ishaAngle = null, ishaMinutes = 90,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.NorthAmerica -> PrayerParameters(
                method = method, fajrAngle = 15.0, ishaAngle = 15.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 1),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Dubai -> PrayerParameters(
                method = method, fajrAngle = 18.2, ishaAngle = 18.2, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(sunrise = -3, dhuhr = 3, asr = 3, maghrib = 3),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Qatar -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = null, ishaMinutes = 90,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Kuwait -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 17.5, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Singapore -> PrayerParameters(
                method = method, fajrAngle = 20.0, ishaAngle = 18.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 1),
                highLatitudeRule = null, roundUp = true,
            )
            CalculationMethod.MoonsightingCommittee -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 18.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(dhuhr = 5, maghrib = 3),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Turkey -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 17.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(sunrise = -7, dhuhr = 5, asr = 4, maghrib = 7),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.France -> PrayerParameters(
                method = method, fajrAngle = 18.0, ishaAngle = 17.0, ishaMinutes = 0,
                maghribAngle = null, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Custom -> CalculationMethod.customParameters(fajrAngle = 0.0, ishaAngle = 0.0)
        }
    }
}
