package org.muslim.app.feature.prayertimes.ui.location

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.location.GeoLocation
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.location.RegionNameResolver
import org.muslim.app.feature.prayertimes.notifications.AdhanScheduler

/**
 * Verifies that a GPS-click failure remains inside the picker and becomes the
 * localized recoverable error state, rather than escaping a ViewModel coroutine
 * and force-closing the process.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelGpsFailureTest {

    @Test
    fun `GPS provider failure is surfaced as a recoverable picker error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } throws IllegalStateException("Provider unavailable")
            val viewModel = newViewModel(locationProvider = locationProvider)

            viewModel.useGps()
            advanceUntilIdle()

            assertThat(viewModel.messages.value)
                .isEqualTo(LocationViewModel.Message.Error("gps_failed"))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `reverse geocoding failure after a valid GPS fix remains a recoverable picker error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } returns GeoLocation(24.7136, 46.6753)
            val regionNameResolver = mockk<RegionNameResolver>()
            coEvery { regionNameResolver.resolve(any(), any()) } throws IllegalStateException("Geocoder unavailable")
            val timeZoneResolver = mockk<CoordinateTimeZoneResolver>()
            coEvery { timeZoneResolver.resolve(any(), any()) } returns "Asia/Riyadh"
            val viewModel = newViewModel(
                locationProvider = locationProvider,
                regionNameResolver = regionNameResolver,
                timeZoneResolver = timeZoneResolver,
            )

            viewModel.useGps()
            advanceUntilIdle()

            assertThat(viewModel.messages.value)
                .isEqualTo(LocationViewModel.Message.Error("gps_failed"))
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun newViewModel(
        locationProvider: LocationProvider = mockk(),
        regionNameResolver: RegionNameResolver = mockk(),
        timeZoneResolver: CoordinateTimeZoneResolver = mockk(),
    ): LocationViewModel = LocationViewModel(
        context = mockk<Context>(relaxed = true),
        repository = mockk<PrayerSettingsRepository>(relaxed = true),
        locationProvider = locationProvider,
        scheduler = mockk<AdhanScheduler>(relaxed = true),
        regionNameResolver = regionNameResolver,
        coordinateTimeZoneResolver = timeZoneResolver,
    )
}
