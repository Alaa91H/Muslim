package org.example.islamicapp.core.prayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * End-to-end behavior checks across every calculation method, on top of the
 * exact vectors in [PrayerTimesCalculatorTest]. These verify that the
 * documented method parameters flow correctly through the astronomy: relative
 * ordering of Isha/Fajr across angles, the Maghrib+90-minute Isha rule of the
 * interval methods, and angle-based Maghrib (Tehran).
 */
class CalculationMethodBehaviorTest {

    private val calculator = PrayerTimesCalculator()
    private val zone = ZoneId.of("Asia/Istanbul")
    private val date = LocalDate.of(2020, 1, 1)
    private val coordinates = Coordinates(41.005616, 28.976380) // Istanbul

    private fun times(method: CalculationMethod): PrayerTimesResult =
        calculator.compute(date, coordinates, PrayerParameters.of(method), zone)

    private fun minutesBetween(a: LocalTime, b: LocalTime): Long =
        (b.toSecondOfDay() - a.toSecondOfDay()).toLong() / 60

    @Test
    fun `every method produces valid ordered times`() {
        for (method in CalculationMethod.entries) {
            if (method == CalculationMethod.Custom) continue
            val result = times(method)
            assertThat(result.isValid).isTrue()
            val order = listOf(
                result.timeFor(Prayer.Fajr),
                result.timeFor(Prayer.Sunrise),
                result.timeFor(Prayer.Dhuhr),
                result.timeFor(Prayer.Asr),
                result.timeFor(Prayer.Maghrib),
                result.timeFor(Prayer.Isha),
            )
            order.zipWithNext().forEach { (a, b) ->
                assertThat(a).isNotNull()
                assertThat(b).isNotNull()
                assertThat(minutesBetween(a!!, b!!)).isAtLeast(1)
            }
        }
    }

    @Test
    fun `Umm al-Qura and Qatar - isha is exactly maghrib plus 90 minutes`() {
        for (method in listOf(CalculationMethod.UmmAlQura, CalculationMethod.Qatar)) {
            val result = times(method)
            assertThat(minutesBetween(result.timeFor(Prayer.Maghrib)!!, result.timeFor(Prayer.Isha)!!))
                .isEqualTo(90)
        }
    }

    @Test
    fun `karachi isha is later than MWL isha (deeper 18 degree angle)`() {
        val mwl = times(CalculationMethod.MuslimWorldLeague)
        val karachi = times(CalculationMethod.Karachi)
        // Same 18° Fajr → identical Fajr; only the Isha angle differs.
        assertThat(karachi.timeFor(Prayer.Fajr)).isEqualTo(mwl.timeFor(Prayer.Fajr))
        assertThat(minutesBetween(mwl.timeFor(Prayer.Isha)!!, karachi.timeFor(Prayer.Isha)!!))
            .isAtLeast(1)
    }

    @Test
    fun `kuwait isha sits between MWL (17) and karachi (18) angles`() {
        val mwl = times(CalculationMethod.MuslimWorldLeague)
        val kuwait = times(CalculationMethod.Kuwait)
        val karachi = times(CalculationMethod.Karachi)
        assertThat(minutesBetween(mwl.timeFor(Prayer.Isha)!!, kuwait.timeFor(Prayer.Isha)!!))
            .isAtLeast(1)
        assertThat(minutesBetween(kuwait.timeFor(Prayer.Isha)!!, karachi.timeFor(Prayer.Isha)!!))
            .isAtLeast(1)
    }

    @Test
    fun `singapore isha matches karachi (same angle, up-rounding adds at most a minute)`() {
        val singapore = times(CalculationMethod.Singapore)
        val karachi = times(CalculationMethod.Karachi)
        val diff = minutesBetween(karachi.timeFor(Prayer.Isha)!!, singapore.timeFor(Prayer.Isha)!!)
        assertThat(diff).isIn(0L..1L)
    }

    @Test
    fun `tehran maghrib uses a sun angle so comes after sunset`() {
        val tehran = times(CalculationMethod.Tehran)
        val mwl = times(CalculationMethod.MuslimWorldLeague)

        // Sunset itself is angle-independent; MWL's Maghrib is exactly sunset.
        val sunset = mwl.timeFor(Prayer.Maghrib)!!
        assertThat(minutesBetween(sunset, tehran.timeFor(Prayer.Maghrib)!!)).isAtLeast(5)
    }

    @Test
    fun `france UOIF - shallower angles shift fajr later and isha earlier than MWL`() {
        val france = times(CalculationMethod.France)
        val mwl = times(CalculationMethod.MuslimWorldLeague)
        // 12° vs 18°: the sun must sink less, so Fajr is later...
        assertThat(minutesBetween(mwl.timeFor(Prayer.Fajr)!!, france.timeFor(Prayer.Fajr)!!))
            .isAtLeast(5)
        // ...and Isha comes earlier in the evening.
        assertThat(minutesBetween(france.timeFor(Prayer.Isha)!!, mwl.timeFor(Prayer.Isha)!!))
            .isAtLeast(5)
    }
}
