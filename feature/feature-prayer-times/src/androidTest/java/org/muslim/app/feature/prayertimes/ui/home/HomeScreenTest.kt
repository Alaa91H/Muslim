package org.muslim.app.feature.prayertimes.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import org.muslim.app.core.ui.theme.AppTheme
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.common.prayer.PrayerTimesResult
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalTime

/**
 * Compose UI smoke test for the critical home screen (PROJECT_PROMPT.md §3.7).
 * Runs on a device/emulator: `./gradlew :feature:feature-prayer-times:connectedDebugAndroidTest`.
 */
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val now = System.currentTimeMillis()

    private fun buildViewModel(): HomeViewModel {
        val repository = mockk<PrayerSettingsRepository>()
        every { repository.settings } returns flowOf(
            PrayerSettings(
                location = SelectedLocation(
                    name = "Makkah",
                    latitude = 21.4225,
                    longitude = 39.8262,
                    timeZone = "Asia/Riyadh",
                ),
            )
        )

        val calculator = mockk<PrayerTimesCalculator>()
        every {
            calculator.compute(any(), any(), any(), any(), any(), any())
        } answers {
            val date = firstArg<LocalDate>()
            PrayerTimesResult(
                date = date,
                times = mapOf(
                    Prayer.Fajr to LocalTime.of(4, 30),
                    Prayer.Sunrise to LocalTime.of(6, 12),
                    Prayer.Dhuhr to LocalTime.of(12, 45),
                    Prayer.Asr to LocalTime.of(16, 20),
                    Prayer.Maghrib to LocalTime.of(19, 5),
                    Prayer.Isha to LocalTime.of(20, 30),
                ),
                // All prayers ~1h in the future so "next prayer" resolves to Fajr.
                epochMillis = Prayer.entries.associateWith { now + 3_600_000L },
            )
        }
        return HomeViewModel(repository, calculator, mockk(relaxed = true))
    }

    @Test
    fun homeScreen_showsLocation_nextPrayer_andPrayerTimes() {
        val viewModel = buildViewModel()
        composeRule.setContent {
            AppTheme(dynamicColor = false) {
                HomeScreen(onSelectLocation = {}, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("Makkah").assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.home_next_prayer)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.home_today_times)
        ).assertIsDisplayed()

        // All five prayers must be listed. The label can appear twice (the
        // next-prayer countdown header shows the same prayer name as its row),
        // so match all nodes and require the first one to be displayed.
        val activity = composeRule.activity
        listOf(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha).forEach { prayer ->
            composeRule.onAllNodesWithText(activity.getString(prayerLabelRes(prayer)))
                .onFirst()
                .assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_withoutLocation_promptsForLocation() {
        val repository = mockk<PrayerSettingsRepository>()
        every { repository.settings } returns flowOf(PrayerSettings(location = null))
        val calculator = mockk<PrayerTimesCalculator>()
        val viewModel = HomeViewModel(repository, calculator, mockk<AppPreferencesRepository>(relaxed = true))

        composeRule.setContent {
            AppTheme(dynamicColor = false) {
                HomeScreen(onSelectLocation = {}, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.home_select_location)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.home_location_unknown)
        ).assertIsDisplayed()
    }
}
