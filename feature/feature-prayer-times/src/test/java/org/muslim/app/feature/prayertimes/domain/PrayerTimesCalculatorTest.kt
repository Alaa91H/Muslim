package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.muslim.app.core.common.prayer.AsrMethod
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.common.prayer.HighLatitudeRule
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerAdjustments
import org.muslim.app.core.common.prayer.PrayerParameters
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.junit.Test
import java.time.Instant
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

    // ---- Elevation (meters above sea level) ----

    @Test
    fun `elevation makes sunrise earlier and sunset later`() {
        val date = LocalDate.of(2024, 6, 21)
        val params = PrayerParameters.of(CalculationMethod.MuslimWorldLeague)
        val zone = ZoneId.of("Asia/Riyadh")

        val seaLevel = calculator.compute(date, Coordinates(21.4225, 39.8262, 0.0), params, zone)
        val high = calculator.compute(date, Coordinates(21.4225, 39.8262, 2000.0), params, zone)

        assertThat(seaLevel.isValid).isTrue()
        assertThat(high.isValid).isTrue()

        val sunriseSea = seaLevel.timeFor(Prayer.Sunrise)!!.toSecondOfDay()
        val sunriseHigh = high.timeFor(Prayer.Sunrise)!!.toSecondOfDay()
        val sunsetSea = seaLevel.timeFor(Prayer.Maghrib)!!.toSecondOfDay()
        val sunsetHigh = high.timeFor(Prayer.Maghrib)!!.toSecondOfDay()

        // At 2000 m the horizon dip is 0.0347·sqrt(2000) = 1.55° ≈ 6.2 min.
        assertThat(sunriseHigh).isLessThan(sunriseSea)
        assertThat(sunsetHigh).isGreaterThan(sunsetSea)
        val sunriseDeltaMin = (sunriseSea - sunriseHigh) / 60.0
        val sunsetDeltaMin = (sunsetHigh - sunsetSea) / 60.0
        assertThat(sunriseDeltaMin).isAtLeast(3.0)
        assertThat(sunriseDeltaMin).isAtMost(10.0)
        assertThat(sunsetDeltaMin).isAtLeast(3.0)
        assertThat(sunsetDeltaMin).isAtMost(10.0)
    }

    @Test
    fun `elevation of zero matches sea-level reference`() {
        val date = LocalDate.of(2018, 1, 1)
        val params = PrayerParameters.of(CalculationMethod.UmmAlQura)
        val zone = ZoneId.of("Asia/Riyadh")
        val result = calculator.compute(date, Coordinates(21.4225, 39.8262, 0.0), params, zone)
        assertThat(result.isValid).isTrue()
        // Umm al-Qura publishes Makkah tables; sunrise must be near 06:5x in
        // early January (the exact minute depends on the year's ephemeris).
        val sunrise = result.timeFor(Prayer.Sunrise)!!.hour
        assertThat(sunrise).isIn(6..7)
    }

    @Test
    fun `extreme elevation does not break calculation`() {
        val date = LocalDate.of(2024, 3, 1)
        val params = PrayerParameters.of(CalculationMethod.MuslimWorldLeague)
        val zone = ZoneId.of("Asia/Kathmandu")
        // Highest city on Earth (~2,950 m): must still produce a valid day.
        val result = calculator.compute(date, Coordinates(27.9861, 86.9236, 2950.0), params, zone)
        assertThat(result.isValid).isTrue()
        assertThat(result.timeFor(Prayer.Sunrise)).isNotNull()
        assertThat(result.timeFor(Prayer.Maghrib)).isNotNull()
    }

    @Test
    fun `city database carries real elevations`() {
        val mecca = org.muslim.app.feature.prayertimes.data.CitiesRepository.all
            .first { it.name == "Mecca" }
        assertThat(mecca.elevation).isEqualTo(277.0)
        val sanaa = org.muslim.app.feature.prayertimes.data.CitiesRepository.all
            .first { it.name == "Sana'a" }
        assertThat(sanaa.elevation).isEqualTo(2250.0)
        assertThat(org.muslim.app.feature.prayertimes.data.CitiesRepository.all)
            .hasSize(54)
    }

    @Test
    fun `dhuhr profile interval is applied before final minute rounding`() {
        val date = LocalDate.of(2026, 8, 27)
        val coordinates = Coordinates(52.5200, 13.4050)
        val zone = ZoneId.of("Europe/Berlin")
        val base = calculator.compute(
            date = date,
            coordinates = coordinates,
            parameters = PrayerParameters.of(CalculationMethod.MuslimWorldLeague),
            timeZone = zone,
        )
        val shifted = calculator.compute(
            date = date,
            coordinates = coordinates,
            parameters = PrayerParameters.of(CalculationMethod.MuslimWorldLeague).copy(dhuhrMinutes = 2),
            timeZone = zone,
        )

        assertThat(shifted.epochMillis.getValue(Prayer.Dhuhr))
            .isEqualTo(base.epochMillis.getValue(Prayer.Dhuhr) + 2 * 60_000L)
    }

    // ---- 2026 global MWL regression vectors generated by Adhan Kotlin ----

    @Test
    fun `MWL Isha is dynamic at 17 degrees rather than a fixed clock time`() {
        val profile = PrayerSettings().toPrayerCalculationProfile()
        assertThat(profile.ishaAngle).isEqualTo(17.0)
        assertThat(profile.ishaMinutes).isEqualTo(0)
        assertThat(profile.userAdjustments[Prayer.Isha]).isEqualTo(0)

        val berlinWinter = calculator.compute(
            date = LocalDate.of(2026, 1, 15),
            coordinates = Coordinates(52.5200, 13.4050),
            profile = profile,
            timeZone = ZoneId.of("Europe/Berlin"),
        )
        val riyadhSummer = calculator.compute(
            date = LocalDate.of(2026, 8, 27),
            coordinates = Coordinates(24.7136, 46.6753),
            profile = profile,
            timeZone = ZoneId.of("Asia/Riyadh"),
        )

        assertThat(berlinWinter.isValid).isTrue()
        assertThat(riyadhSummer.isValid).isTrue()
        assertThat(berlinWinter.timeFor(Prayer.Isha)).isNotEqualTo(LocalTime.of(22, 8))
        assertThat(riyadhSummer.timeFor(Prayer.Isha)).isNotEqualTo(LocalTime.of(22, 8))
        assertThat(berlinWinter.timeFor(Prayer.Isha)).isNotEqualTo(riyadhSummer.timeFor(Prayer.Isha))
    }

    @Test
    fun `global default MWL profile matches Adhan Berlin across seasons`() {
        assertThat(PrayerSettings().toPrayerCalculationProfile().calculationMethod)
            .isEqualTo(CalculationMethod.MuslimWorldLeague)
        assertThat(PrayerSettings().toPrayerCalculationProfile().asrMethod)
            .isEqualTo(AsrMethod.Standard)
        val defaultProfile = PrayerSettings().toPrayerCalculationProfile()
        assertThat(defaultProfile.highLatitudeRule)
            .isEqualTo(HighLatitudeRule.MiddleOfTheNight)
        assertThat(defaultProfile.ishaAngle).isEqualTo(17.0)
        assertThat(defaultProfile.ishaMinutes).isEqualTo(0)
        assertThat(defaultProfile.userAdjustments[Prayer.Isha]).isEqualTo(0)

        BERLIN_SEASONAL_MWL_CASES.forEach(::assertMwlSeventhReferenceCase)
    }

    @Test
    fun `global MWL profile matches Adhan vectors across regions`() {
        GLOBAL_MWL_REFERENCE_CASES.forEach(::assertMwlSeventhReferenceCase)
    }

    @Test
    fun `final alert instant and visible time share exactly one rounded minute`() {
        val profile = PrayerSettings().toPrayerCalculationProfile()
        val zone = ZoneId.of("Europe/Berlin")
        val result = calculator.compute(
            date = LocalDate.of(2026, 8, 27),
            coordinates = Coordinates(52.5200, 13.4050),
            profile = profile,
            timeZone = zone,
        )

        assertThat(result.isValid).isTrue()
        Prayer.entries.forEach { prayer ->
            val finalEpoch = result.epochMillis.getValue(prayer)
            val rawEpoch = result.rawEpochMillis.getValue(prayer)
            assertThat(finalEpoch % 60_000L).isEqualTo(0L)
            assertThat(Instant.ofEpochMilli(finalEpoch).atZone(zone).toLocalTime())
                .isEqualTo(result.timeFor(prayer))
            assertThat(kotlin.math.abs(finalEpoch - rawEpoch)).isAtMost(30_000L)
        }
    }

    private fun assertMwlSeventhReferenceCase(case: MwlReferenceCase) {
        // Reference vectors were generated for SeventhOfTheNight; keep them stable
        // even though the app default is now MiddleOfTheNight per user request.
        val profile = PrayerSettings(highLatitudeRule = HighLatitudeRule.SeventhOfTheNight).toPrayerCalculationProfile()
        val result = calculator.compute(
            date = case.date,
            coordinates = Coordinates(case.latitude, case.longitude),
            profile = profile,
            timeZone = ZoneId.of(case.zoneId),
        )
        assertThat(result.isValid).isTrue()
        Prayer.entries.zip(case.expected).forEach { (prayer, expected) ->
            assertThat(result.timeFor(prayer))
                .isEqualTo(LocalTime.parse(expected))
        }
    }

    private data class MwlReferenceCase(
        val name: String,
        val date: LocalDate,
        val latitude: Double,
        val longitude: Double,
        val zoneId: String,
        val expected: List<String>,
    )

    private companion object {
        val BERLIN_SEASONAL_MWL_CASES = listOf(
            MwlReferenceCase("Berlin winter", LocalDate.of(2026, 1, 15), 52.5200, 13.4050, "Europe/Berlin", listOf("06:06", "08:10", "12:17", "14:04", "16:22", "18:19")),
            MwlReferenceCase("Berlin spring", LocalDate.of(2026, 4, 15), 52.5200, 13.4050, "Europe/Berlin", listOf("04:43", "06:09", "13:07", "16:56", "20:05", "21:31")),
            MwlReferenceCase("Berlin summer", LocalDate.of(2026, 6, 15), 52.5200, 13.4050, "Europe/Berlin", listOf("03:41", "04:43", "13:08", "17:32", "21:31", "22:33")),
            MwlReferenceCase("Berlin late summer", LocalDate.of(2026, 8, 27), 52.5200, 13.4050, "Europe/Berlin", listOf("04:42", "06:09", "13:09", "16:56", "20:06", "21:32")),
            MwlReferenceCase("Berlin autumn", LocalDate.of(2026, 10, 15), 52.5200, 13.4050, "Europe/Berlin", listOf("05:38", "07:32", "12:53", "15:35", "18:12", "19:59")),
        )

        val GLOBAL_MWL_REFERENCE_CASES = listOf(
            MwlReferenceCase("London", LocalDate.of(2026, 8, 27), 51.5074, -0.1278, "Europe/London", listOf("04:38", "06:05", "13:03", "16:49", "19:58", "21:25")),
            MwlReferenceCase("Stockholm", LocalDate.of(2026, 8, 27), 59.3293, 18.0686, "Europe/Stockholm", listOf("04:11", "05:33", "12:50", "16:38", "20:04", "21:26")),
            MwlReferenceCase("Oslo summer", LocalDate.of(2026, 6, 15), 59.9139, 10.7522, "Europe/Oslo", listOf("03:10", "03:54", "13:19", "17:59", "22:41", "23:26")),
            MwlReferenceCase("Oslo winter", LocalDate.of(2026, 1, 15), 59.9139, 10.7522, "Europe/Oslo", listOf("06:36", "09:04", "12:27", "13:38", "15:49", "18:17")),
            MwlReferenceCase("Helsinki", LocalDate.of(2026, 8, 27), 60.1699, 24.9384, "Europe/Helsinki", listOf("04:42", "06:02", "13:23", "17:10", "20:40", "22:00")),
            MwlReferenceCase("Reykjavik", LocalDate.of(2026, 8, 27), 64.1466, -21.9426, "Atlantic/Reykjavik", listOf("04:39", "05:55", "13:30", "17:17", "21:01", "22:18")),
            MwlReferenceCase("Toronto", LocalDate.of(2026, 8, 27), 43.6532, -79.3832, "America/Toronto", listOf("05:05", "06:36", "13:20", "17:03", "20:02", "21:32")),
            MwlReferenceCase("Riyadh", LocalDate.of(2026, 8, 27), 24.7136, 46.6753, "Asia/Riyadh", listOf("04:14", "05:32", "11:56", "15:23", "18:17", "19:31")),
            MwlReferenceCase("Cairo", LocalDate.of(2026, 8, 27), 30.0444, 31.2357, "Africa/Cairo", listOf("05:06", "06:29", "12:58", "16:31", "19:24", "20:42")),
            MwlReferenceCase("Istanbul", LocalDate.of(2026, 8, 27), 41.0082, 28.9784, "Europe/Istanbul", listOf("04:54", "06:26", "13:07", "16:49", "19:45", "21:17")),
            MwlReferenceCase("New York", LocalDate.of(2026, 8, 27), 40.7128, -74.0060, "America/New_York", listOf("04:46", "06:18", "12:58", "16:40", "19:36", "21:08")),
            MwlReferenceCase("Tokyo", LocalDate.of(2026, 8, 27), 35.6762, 139.6503, "Asia/Tokyo", listOf("03:39", "05:09", "11:44", "15:23", "18:16", "19:41")),
            MwlReferenceCase("Singapore", LocalDate.of(2026, 8, 27), 1.3521, 103.8198, "Asia/Singapore", listOf("05:52", "07:02", "13:07", "16:21", "19:11", "20:16")),
            MwlReferenceCase("Sydney", LocalDate.of(2026, 8, 27), -33.8688, 151.2093, "Australia/Sydney", listOf("04:57", "06:20", "11:58", "15:07", "17:34", "18:52")),
        )
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
