package org.example.islamicapp.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.core.common.prayer.AsrMethod
import org.example.islamicapp.core.common.prayer.CalculationMethod
import org.example.islamicapp.core.common.prayer.HighLatitudeRule
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.core.common.prayer.PrayerAdjustments
import org.example.islamicapp.core.common.prayer.PrayerParameters
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Validation of the calculation engine.
 *
 * Expected values are the official test vectors of the open-source Adhan
 * library (which this engine's algorithm is ported from) plus rows of
 * published government prayer tables (Umm al-Qura for Makkah, Qatar's
 * Ministry of Awqaf, moonsighting.com for London) with their documented
 * ±1–2 minute variance.
 */
class PrayerTimesCalculatorTest {

    private val calculator = PrayerTimesCalculator()

    private fun assertTimes(
        date: LocalDate,
        lat: Double,
        lng: Double,
        zone: String,
        method: CalculationMethod,
        asr: AsrMethod = AsrMethod.Standard,
        adjustments: PrayerAdjustments = PrayerAdjustments(),
        highLatitudeRule: HighLatitudeRule? = null,
        expected: Map<Prayer, String>,
    ) {
        val result = calculator.compute(
            date = date,
            coordinates = Coordinates(lat, lng),
            parameters = PrayerParameters.of(method).copy(highLatitudeRule = highLatitudeRule),
            timeZone = ZoneId.of(zone),
            asrMethod = asr,
            userAdjustments = adjustments,
        )
        assertThat(result.isValid).isTrue()
        for ((prayer, expectedTime) in expected) {
            val actual = result.timeFor(prayer)
            assertThat(actual)
                .isEqualTo(LocalTime.parse(expectedTime))
        }
    }

    // ---- Exact vectors from Adhan's PrayerTimesTest (tolerance: 1 minute) ----

    @Test
    fun `ISNA Hanafi - Raleigh NC 2015-07-12`() {
        assertTimes(
            LocalDate.of(2015, 7, 12), 35.7750, -78.6336, "America/New_York",
            CalculationMethod.NorthAmerica, AsrMethod.Hanafi,
            expected = mapOf(
                Prayer.Fajr to "04:42", Prayer.Sunrise to "06:08", Prayer.Dhuhr to "13:21",
                Prayer.Asr to "18:22", Prayer.Maghrib to "20:32", Prayer.Isha to "21:57",
            ),
        )
    }

    @Test
    fun `MWL - Raleigh NC 2015-12-01`() {
        assertTimes(
            LocalDate.of(2015, 12, 1), 35.7750, -78.6336, "America/New_York",
            CalculationMethod.MuslimWorldLeague,
            expected = mapOf(
                Prayer.Fajr to "05:35", Prayer.Sunrise to "07:06", Prayer.Dhuhr to "12:05",
                Prayer.Asr to "14:42", Prayer.Maghrib to "17:01", Prayer.Isha to "18:26",
            ),
        )
    }

    @Test
    fun `MWL with manual +10 minute adjustments - Raleigh NC 2015-12-01`() {
        val adjustments = PrayerAdjustments(fajr = 10, sunrise = 10, dhuhr = 10, asr = 10, maghrib = 10, isha = 10)
        assertTimes(
            LocalDate.of(2015, 12, 1), 35.7750, -78.6336, "America/New_York",
            CalculationMethod.MuslimWorldLeague,
            adjustments = adjustments,
            expected = mapOf(
                Prayer.Fajr to "05:45", Prayer.Sunrise to "07:16", Prayer.Dhuhr to "12:15",
                Prayer.Asr to "14:52", Prayer.Maghrib to "17:11", Prayer.Isha to "18:36",
            ),
        )
    }

    @Test
    fun `Moonsighting - Raleigh NC 2016-01-31`() {
        assertTimes(
            LocalDate.of(2016, 1, 31), 35.7750, -78.6336, "America/New_York",
            CalculationMethod.MoonsightingCommittee,
            expected = mapOf(
                Prayer.Fajr to "05:48", Prayer.Sunrise to "07:16", Prayer.Dhuhr to "12:33",
                Prayer.Asr to "15:20", Prayer.Maghrib to "17:43", Prayer.Isha to "19:05",
            ),
        )
    }

    @Test
    fun `Moonsighting Hanafi high latitude - Oslo 2016-01-01`() {
        assertTimes(
            LocalDate.of(2016, 1, 1), 59.9094, 10.7349, "Europe/Oslo",
            CalculationMethod.MoonsightingCommittee, AsrMethod.Hanafi,
            expected = mapOf(
                Prayer.Fajr to "07:34", Prayer.Sunrise to "09:19", Prayer.Dhuhr to "12:25",
                Prayer.Asr to "13:36", Prayer.Maghrib to "15:25", Prayer.Isha to "17:02",
            ),
        )
    }

    @Test
    fun `Diyanet Turkey - Istanbul 2020-04-16`() {
        assertTimes(
            LocalDate.of(2020, 4, 16), 41.005616, 28.976380, "Europe/Istanbul",
            CalculationMethod.Turkey,
            expected = mapOf(
                Prayer.Fajr to "04:44", Prayer.Sunrise to "06:16", Prayer.Dhuhr to "13:09",
                Prayer.Asr to "16:53", Prayer.Maghrib to "19:52", Prayer.Isha to "21:19",
            ),
        )
    }

    @Test
    fun `Egyptian - Cairo 2020-01-01`() {
        assertTimes(
            LocalDate.of(2020, 1, 1), 30.028703, 31.249528, "Africa/Cairo",
            CalculationMethod.Egyptian,
            expected = mapOf(
                Prayer.Fajr to "05:18", Prayer.Sunrise to "06:51", Prayer.Dhuhr to "11:59",
                Prayer.Asr to "14:47", Prayer.Maghrib to "17:06", Prayer.Isha to "18:29",
            ),
        )
    }

    @Test
    fun `Moonsighting - Oakville ON 2021-01-01`() {
        assertTimes(
            LocalDate.of(2021, 1, 1), 43.494, -79.844, "America/New_York",
            CalculationMethod.MoonsightingCommittee, AsrMethod.Hanafi,
            expected = mapOf(
                Prayer.Fajr to "06:16", Prayer.Sunrise to "07:52", Prayer.Dhuhr to "12:28",
                Prayer.Asr to "15:12", Prayer.Maghrib to "16:57", Prayer.Isha to "18:27",
            ),
        )
    }

    // ---- High latitude rules - Edinburgh 2020-06-15 (MWL) ----

    @Test
    fun `High latitude - Middle of the night - Edinburgh 2020-06-15`() {
        assertTimes(
            LocalDate.of(2020, 6, 15), 55.983226, -3.216649, "Europe/London",
            CalculationMethod.MuslimWorldLeague,
            highLatitudeRule = HighLatitudeRule.MiddleOfTheNight,
            expected = mapOf(
                Prayer.Fajr to "01:14", Prayer.Sunrise to "04:26", Prayer.Dhuhr to "13:14",
                Prayer.Asr to "17:46", Prayer.Maghrib to "22:01", Prayer.Isha to "01:14",
            ),
        )
    }

    @Test
    fun `High latitude - Seventh of the night - Edinburgh 2020-06-15`() {
        assertTimes(
            LocalDate.of(2020, 6, 15), 55.983226, -3.216649, "Europe/London",
            CalculationMethod.MuslimWorldLeague,
            highLatitudeRule = HighLatitudeRule.SeventhOfTheNight,
            expected = mapOf(
                Prayer.Fajr to "03:31", Prayer.Sunrise to "04:26", Prayer.Dhuhr to "13:14",
                Prayer.Asr to "17:46", Prayer.Maghrib to "22:01", Prayer.Isha to "22:56",
            ),
        )
    }

    @Test
    fun `High latitude - Twilight angle - Edinburgh 2020-06-15`() {
        assertTimes(
            LocalDate.of(2020, 6, 15), 55.983226, -3.216649, "Europe/London",
            CalculationMethod.MuslimWorldLeague,
            highLatitudeRule = HighLatitudeRule.TwilightAngle,
            expected = mapOf(
                Prayer.Fajr to "02:31", Prayer.Sunrise to "04:26", Prayer.Dhuhr to "13:14",
                Prayer.Asr to "17:46", Prayer.Maghrib to "22:01", Prayer.Isha to "23:50",
            ),
        )
    }

    // ---- Official government tables (documented variance ±1-2 min) ----

    @Test
    fun `official table - Makkah Umm al-Qura 2016-01-05`() {
        assertTimesWithin(
            LocalDate.of(2016, 1, 5), 21.427009, 39.828685, "Asia/Riyadh",
            CalculationMethod.UmmAlQura,
            expected = mapOf(
                Prayer.Fajr to "05:38", Prayer.Sunrise to "07:00", Prayer.Dhuhr to "12:26",
                Prayer.Asr to "15:31", Prayer.Maghrib to "17:52", Prayer.Isha to "19:22",
            ),
            toleranceMinutes = 2,
        )
    }

    @Test
    fun `official table - Doha Qatar 2016-01-01`() {
        assertTimesWithin(
            LocalDate.of(2016, 1, 1), 25.283897, 51.528770, "Asia/Riyadh",
            CalculationMethod.Qatar,
            expected = mapOf(
                Prayer.Fajr to "04:58", Prayer.Sunrise to "06:19", Prayer.Dhuhr to "11:37",
                Prayer.Asr to "14:35", Prayer.Maghrib to "16:55", Prayer.Isha to "18:25",
            ),
            toleranceMinutes = 2,
        )
    }

    @Test
    fun `official table - London moonsighting 2016-01-01`() {
        assertTimesWithin(
            LocalDate.of(2016, 1, 1), 51.507194, -0.116711, "Europe/London",
            CalculationMethod.MoonsightingCommittee, AsrMethod.Hanafi,
            expected = mapOf(
                Prayer.Fajr to "06:25", Prayer.Sunrise to "08:06", Prayer.Dhuhr to "12:09",
                Prayer.Asr to "14:15", Prayer.Maghrib to "16:05", Prayer.Isha to "17:38",
            ),
            toleranceMinutes = 2,
        )
    }

    // ---- Degenerate days at extreme latitudes ----

    @Test
    fun `extreme location - Utqiagvik polar night is invalid`() {
        val result = calculator.compute(
            LocalDate.of(2018, 1, 1),
            Coordinates(71.275009, -156.761368),
            PrayerParameters.of(CalculationMethod.MuslimWorldLeague),
            ZoneId.of("America/Anchorage"),
        )
        assertThat(result.isValid).isFalse()
    }

    @Test
    fun `extreme location - Utqiagvik March is valid`() {
        val result = calculator.compute(
            LocalDate.of(2018, 3, 1),
            Coordinates(71.275009, -156.761368),
            PrayerParameters.of(CalculationMethod.MuslimWorldLeague),
            ZoneId.of("America/Anchorage"),
        )
        assertThat(result.isValid).isTrue()
        assertThat(result.timeFor(Prayer.Fajr)).isNotNull()
    }

    // ---- helpers ----

    private fun assertTimesWithin(
        date: LocalDate,
        lat: Double,
        lng: Double,
        zone: String,
        method: CalculationMethod,
        asr: AsrMethod = AsrMethod.Standard,
        expected: Map<Prayer, String>,
        toleranceMinutes: Int,
    ) {
        val result = calculator.compute(
            date = date,
            coordinates = Coordinates(lat, lng),
            parameters = PrayerParameters.of(method),
            timeZone = ZoneId.of(zone),
            asrMethod = asr,
        )
        assertThat(result.isValid).isTrue()
        for ((prayer, expectedTime) in expected) {
            val expectedMinutes = LocalTime.parse(expectedTime).toSecondOfDay() / 60
            val actualMinutes = result.timeFor(prayer)!!.toSecondOfDay() / 60
            assertThat(actualMinutes)
                .isWithin(toleranceMinutes).of(expectedMinutes)
        }
    }
}
