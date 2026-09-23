package org.muslim.app.feature.prayertimes.ui.location

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.location.GeoLocation
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.location.RegionNameResolver
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.notifications.AdhanDeliveryJournal
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
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.uiAutomation.grantRuntimePermission(
                targetContext.packageName,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            ParcelFileDescriptor.AutoCloseInputStream(
                instrumentation.uiAutomation.executeShellCommand(
                    "pm grant ${targetContext.packageName} ${Manifest.permission.ACCESS_FINE_LOCATION}",
                ),
            ).use { it.readBytes() }
        }
    }

    @Test
    fun useCurrentLocationPersistsGpsFixAndStaysInThePickerFlow() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val locationProvider = object : LocationProvider {
            override suspend fun currentLocation(): GeoLocation = GeoLocation(24.7136, 46.6753)
        }
        val repository = PrayerSettingsRepository(targetContext)
        runBlocking { repository.save(PrayerSettings(adhanEnabled = false)) }
        val viewModelContext = object : ContextWrapper(targetContext) {
            override fun getApplicationContext(): Context = this

            override fun startForegroundService(service: Intent): ComponentName? =
                service.component
        }
        val scheduler = AdhanScheduler(
            context = targetContext,
            calculator = PrayerTimesCalculator(),
            deliveryJournal = AdhanDeliveryJournal(targetContext),
        )
        val regionNameResolver = object : RegionNameResolver {
            override suspend fun resolve(latitude: Double, longitude: Double): String =
                "Riyadh, Saudi Arabia"
        }
        val viewModel = LocationViewModel(
            context = viewModelContext,
            repository = repository,
            locationProvider = locationProvider,
            scheduler = scheduler,
            regionNameResolver = regionNameResolver,
            coordinateTimeZoneResolver = CoordinateTimeZoneResolver(),
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
        val persisted = runBlocking { repository.settings.first() }
        assertEquals(24.7136, persisted.location?.latitude ?: Double.NaN, 0.0)
        assertEquals(46.6753, persisted.location?.longitude ?: Double.NaN, 0.0)
        assertEquals("Asia/Riyadh", persisted.location?.timeZone)
    }
}
