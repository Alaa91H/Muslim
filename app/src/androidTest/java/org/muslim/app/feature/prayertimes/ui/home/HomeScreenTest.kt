package org.muslim.app.feature.prayertimes.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import org.muslim.app.core.ui.theme.AppTheme
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerCompletionRepository
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.SelectedLocation
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes
import org.junit.Rule
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking

/**
 * Compose UI smoke test for the critical home screen (PROJECT_PROMPT.md §3.7).
 * Runs on a device/emulator: `./gradlew :feature:feature-prayer-times:connectedDebugAndroidTest`.
 */
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun buildViewModel(settings: PrayerSettings): HomeViewModel {
        val repository = PrayerSettingsRepository(targetContext)
        runBlocking { repository.save(settings) }
        return HomeViewModel(
            settingsRepository = repository,
            completionRepository = PrayerCompletionRepository(targetContext),
            calculator = PrayerTimesCalculator(),
            appPreferencesRepository = AppPreferencesRepository(targetContext),
        )
    }

    @Test
    fun homeScreen_showsLocation_nextPrayer_andPrayerTimes() {
        val viewModel = buildViewModel(
            PrayerSettings(
                location = SelectedLocation(
                    name = "Makkah",
                    latitude = 21.4225,
                    longitude = 39.8262,
                    timeZone = "Asia/Riyadh",
                ),
            ),
        )
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
        ).performScrollTo().assertIsDisplayed()

        // All five prayers must be listed. The label can appear twice (the
        // next-prayer countdown header shows the same prayer name as its row),
        // so match all nodes and require the first one to be displayed.
        val activity = composeRule.activity
        listOf(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha).forEach { prayer ->
            composeRule.onAllNodesWithText(activity.getString(prayerLabelRes(prayer)))
                .onFirst()
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_withoutLocation_promptsForLocation() {
        val viewModel = buildViewModel(PrayerSettings(location = null))

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
