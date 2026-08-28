package org.muslim.app.feature.prayertimes.ui.location

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.location.GeoLocation
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.location.RegionNameResolver
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler

/**
 * Device-level regression for the location picker's "Use my current location"
 * action. It proves that a successful foreground GPS fix is persisted and
 * returns through the saved callback instead of closing the visible picker.
 */
@RunWith(AndroidJUnit4::class)
class LocationScreenGpsInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun grantForegroundLocation() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            targetContext.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @Test
    fun useCurrentLocationPersistsGpsFixAndStaysInThePickerFlow() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val locationProvider = mockk<LocationProvider>()
        coEvery { locationProvider.currentLocation() } returns GeoLocation(24.7136, 46.6753)
        val repository = mockk<PrayerSettingsRepository>()
        every { repository.settings } returns flowOf(PrayerSettings())
        coEvery { repository.save(any()) } returns Unit
        val scheduler = mockk<AdhanScheduler>(relaxed = true)
        val regionNameResolver = mockk<RegionNameResolver>()
        coEvery { regionNameResolver.resolve(any(), any()) } returns "Riyadh, Saudi Arabia"
        val timeZoneResolver = mockk<CoordinateTimeZoneResolver>()
        coEvery { timeZoneResolver.resolve(any(), any()) } returns "Asia/Riyadh"
        val viewModel = LocationViewModel(
            context = targetContext,
            repository = repository,
            locationProvider = locationProvider,
            scheduler = scheduler,
            regionNameResolver = regionNameResolver,
            coordinateTimeZoneResolver = timeZoneResolver,
        )
        var saved = false
        val gpsText = targetContext.getString(R.string.location_use_gps)

        composeRule.setContent {
            LocationScreen(
                onSaved = { saved = true },
                viewModel = viewModel,
            )
        }

        composeRule.onNodeWithText(gpsText).assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { saved }

        assertTrue(saved)
        coVerify(exactly = 1) {
            repository.save(match { settings ->
                settings.location?.latitude == 24.7136 &&
                    settings.location?.longitude == 46.6753 &&
                    settings.location?.timeZone == "Asia/Riyadh"
            })
        }
    }
}
