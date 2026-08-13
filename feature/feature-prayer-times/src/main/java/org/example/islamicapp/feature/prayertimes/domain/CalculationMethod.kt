package org.example.islamicapp.feature.prayertimes.domain

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

    /** معهد الجيوفيزياء بطهران — Fajr 17.7°, Isha 14°, Maghrib angle 4.5°. */
    Tehran,

    /** الجعفري (الشيعة الاثنا عشرية) — Fajr 16°, Isha 14°, Maghrib angle 4°. */
    Jafari,

    /** فرنسا (UOIF) — Fajr 18°, Isha 17°. */
    France,

    /** إعداد مخصص يُدخله المستخدم (زاويتا الفجر والعشاء) — see [customParameters]. */
    Custom,
    ;

    companion object {
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
            CalculationMethod.Tehran -> PrayerParameters(
                method = method, fajrAngle = 17.7, ishaAngle = 14.0, ishaMinutes = 0,
                maghribAngle = 4.5, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
                highLatitudeRule = null, roundUp = false,
            )
            CalculationMethod.Jafari -> PrayerParameters(
                method = method, fajrAngle = 16.0, ishaAngle = 14.0, ishaMinutes = 0,
                maghribAngle = 4.0, maghribMinutes = 0, dhuhrMinutes = 0,
                methodAdjustments = PrayerAdjustments(),
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
