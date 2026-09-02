package org.muslim.app.feature.prayertimes.ui.location

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.muslim.app.core.datastore.prayer.PrayerSettings
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

    @Test
    fun `concurrent GPS clicks are serialized and both remain recoverable`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val activeRequests = AtomicInteger(0)
            val maximumConcurrentRequests = AtomicInteger(0)
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } coAnswers {
                val active = activeRequests.incrementAndGet()
                maximumConcurrentRequests.set(maxOf(maximumConcurrentRequests.get(), active))
                delay(100)
                activeRequests.decrementAndGet()
                GeoLocation(24.7136, 46.6753)
            }
            val regionNameResolver = mockk<RegionNameResolver>()
            coEvery { regionNameResolver.resolve(any(), any()) } returns "Riyadh, Saudi Arabia"
            val timeZoneResolver = mockk<CoordinateTimeZoneResolver>()
            coEvery { timeZoneResolver.resolve(any(), any()) } returns "Asia/Riyadh"
            val repository = mockk<PrayerSettingsRepository>(relaxed = true).also {
                every { it.settings } returns flowOf(PrayerSettings())
            }
            val viewModel = newViewModel(
                locationProvider = locationProvider,
                repository = repository,
                regionNameResolver = regionNameResolver,
                timeZoneResolver = timeZoneResolver,
            )

            viewModel.useGps()
            viewModel.useGps()
            advanceUntilIdle()

            assertThat(maximumConcurrentRequests.get()).isEqualTo(1)
            assertThat(viewModel.messages.value).isEqualTo(LocationViewModel.Message.Saved)
            coVerify(exactly = 2) { repository.save(any()) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `GPS location remains saved when a derived scheduler refresh fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } returns GeoLocation(24.7136, 46.6753)
            val repository = mockk<PrayerSettingsRepository>()
            every { repository.settings } returns flowOf(PrayerSettings())
            coEvery { repository.save(any()) } just runs
            val scheduler = mockk<AdhanScheduler>()
            every { scheduler.schedule(any()) } throws IllegalStateException("Alarm manager unavailable")
            val regionNameResolver = mockk<RegionNameResolver>()
            coEvery { regionNameResolver.resolve(any(), any()) } returns "Riyadh, Saudi Arabia"
            val timeZoneResolver = mockk<CoordinateTimeZoneResolver>()
            coEvery { timeZoneResolver.resolve(any(), any()) } returns "Asia/Riyadh"
            val viewModel = newViewModel(
                locationProvider = locationProvider,
                repository = repository,
                scheduler = scheduler,
                regionNameResolver = regionNameResolver,
                timeZoneResolver = timeZoneResolver,
            )

            viewModel.useGps()
            advanceUntilIdle()

            assertThat(viewModel.messages.value).isEqualTo(LocationViewModel.Message.Saved)
            coVerify(exactly = 1) { repository.save(match { it.location?.timeZone == "Asia/Riyadh" }) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun newViewModel(
        locationProvider: LocationProvider = mockk(),
        repository: PrayerSettingsRepository = mockk<PrayerSettingsRepository>(relaxed = true).also {
            every { it.settings } returns flowOf(PrayerSettings())
        },
        scheduler: AdhanScheduler = mockk<AdhanScheduler>(relaxed = true),
        regionNameResolver: RegionNameResolver = mockk(),
        timeZoneResolver: CoordinateTimeZoneResolver = mockk(),
    ): LocationViewModel = LocationViewModel(
        context = mockk<Context>(relaxed = true),
        repository = repository,
        locationProvider = locationProvider,
        scheduler = scheduler,
        regionNameResolver = regionNameResolver,
        coordinateTimeZoneResolver = timeZoneResolver,
    )
}
