package org.example.islamicapp.core.prayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pins every calculation method to its documented parameters.
 *
 * Reference sources (see PROJECT_PROMPT.md §12):
 *  - PrayTimes.org official "Calculation Methods" docs (praytimes.org/docs/methods)
 *  - the Adhan library (Batoul Apps, MIT) — same values, plus the small
 *    "method adjustments" each authority applies to align with its published
 *    tables. Confirmed against Adhan's `CalculationMethod.kt` source.
 *
 * Any change to `CalculationMethod.kt` / `PrayerParameters.of` that drifts
 * from these values fails here.
 */
class CalculationMethodParametersTest {

    private fun of(method: CalculationMethod) = PrayerParameters.of(method)

    // ---- Angle-based methods (PrayTimes table) ----

    @Test
    fun `Muslim World League - fajr 18 isha 17, dhuhr +1`() {
        val p = of(CalculationMethod.MuslimWorldLeague)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(17.0)
        assertThat(p.ishaMinutes).isEqualTo(0)
        assertThat(p.maghribAngle).isNull()
        assertThat(p.roundUp).isFalse()
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(1)
    }

    @Test
    fun `Egyptian - fajr 19_5 isha 17_5, dhuhr +1`() {
        val p = of(CalculationMethod.Egyptian)
        assertThat(p.fajrAngle).isEqualTo(19.5)
        assertThat(p.ishaAngle).isEqualTo(17.5)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(1)
    }

    @Test
    fun `Karachi - fajr 18 isha 18, dhuhr +1`() {
        val p = of(CalculationMethod.Karachi)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(18.0)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(1)
    }

    @Test
    fun `ISNA North America - fajr 15 isha 15, dhuhr +1`() {
        val p = of(CalculationMethod.NorthAmerica)
        assertThat(p.fajrAngle).isEqualTo(15.0)
        assertThat(p.ishaAngle).isEqualTo(15.0)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(1)
    }

    @Test
    fun `Kuwait - fajr 18 isha 17_5, no adjustments`() {
        val p = of(CalculationMethod.Kuwait)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(17.5)
        assertThat(p.methodAdjustments).isEqualTo(PrayerAdjustments())
    }

    @Test
    fun `Singapore - fajr 20 isha 18, dhuhr +1, rounds up`() {
        val p = of(CalculationMethod.Singapore)
        assertThat(p.fajrAngle).isEqualTo(20.0)
        assertThat(p.ishaAngle).isEqualTo(18.0)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(1)
        assertThat(p.roundUp).isTrue()
    }

    @Test
    fun `Moonsighting - fajr 18 isha 18, dhuhr +5 maghrib +3`() {
        val p = of(CalculationMethod.MoonsightingCommittee)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(18.0)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(5)
        assertThat(p.methodAdjustments.maghrib).isEqualTo(3)
    }

    @Test
    fun `Dubai - fajr 18_2 isha 18_2 with sunrise -3 dhuhr 3 asr 3 maghrib 3`() {
        val p = of(CalculationMethod.Dubai)
        assertThat(p.fajrAngle).isEqualTo(18.2)
        assertThat(p.ishaAngle).isEqualTo(18.2)
        assertThat(p.methodAdjustments.sunrise).isEqualTo(-3)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(3)
        assertThat(p.methodAdjustments.asr).isEqualTo(3)
        assertThat(p.methodAdjustments.maghrib).isEqualTo(3)
    }

    @Test
    fun `Turkey - fajr 18 isha 17 with sunrise -7 dhuhr 5 asr 4 maghrib 7`() {
        val p = of(CalculationMethod.Turkey)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(17.0)
        assertThat(p.methodAdjustments.sunrise).isEqualTo(-7)
        assertThat(p.methodAdjustments.dhuhr).isEqualTo(5)
        assertThat(p.methodAdjustments.asr).isEqualTo(4)
        assertThat(p.methodAdjustments.maghrib).isEqualTo(7)
    }

    @Test
    fun `Tehran - fajr 17_7 isha 14, maghrib at sun angle 4_5`() {
        val p = of(CalculationMethod.Tehran)
        assertThat(p.fajrAngle).isEqualTo(17.7)
        assertThat(p.ishaAngle).isEqualTo(14.0)
        assertThat(p.maghribAngle).isEqualTo(4.5)
        assertThat(p.methodAdjustments).isEqualTo(PrayerAdjustments())
    }

    @Test
    fun `France UOIF - fajr 12 isha 12 per PrayTimes official docs`() {
        val p = of(CalculationMethod.France)
        assertThat(p.fajrAngle).isEqualTo(12.0)
        assertThat(p.ishaAngle).isEqualTo(12.0)
        assertThat(p.maghribAngle).isNull()
        assertThat(p.roundUp).isFalse()
        assertThat(p.methodAdjustments).isEqualTo(PrayerAdjustments())
    }

    // ---- Interval-based methods ----

    @Test
    fun `Umm al-Qura - fajr 18_5, isha = maghrib + 90 minutes`() {
        val p = of(CalculationMethod.UmmAlQura)
        assertThat(p.fajrAngle).isEqualTo(18.5)
        assertThat(p.ishaAngle).isNull()
        assertThat(p.ishaMinutes).isEqualTo(90)
    }

    @Test
    fun `Qatar - fajr 18, isha = maghrib + 90 minutes`() {
        val p = of(CalculationMethod.Qatar)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isNull()
        assertThat(p.ishaMinutes).isEqualTo(90)
    }

    @Test
    fun `Custom - falls back to sane defaults when angles are blank`() {
        val p = CalculationMethod.customParameters(fajrAngle = 0.0, ishaAngle = 0.0)
        assertThat(p.fajrAngle).isEqualTo(18.0)
        assertThat(p.ishaAngle).isEqualTo(17.0)
        assertThat(p.method).isEqualTo(CalculationMethod.Custom)
    }
}
