package org.muslim.app.feature.qibla.mosques

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.location.GeoLocation
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.feature.qibla.data.MosquePlace
import org.muslim.app.feature.qibla.data.NearbyMosque
import org.muslim.app.feature.qibla.data.NearbyMosqueCache
import org.muslim.app.feature.qibla.data.NearbyMosqueRepository

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyMosquesViewModelTest {

    @Test
    fun `permission and unavailable location become explicit recoverable states`() = runTest {
        withMainDispatcher {
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } returns null
            val viewModel = newViewModel(locationProvider = locationProvider)

            viewModel.onPermissionDenied()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.PermissionDenied)

            viewModel.refresh()
            advanceUntilIdle()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.LocationUnavailable)
        }
    }

    @Test
    fun `loading transitions to success after network places are locally prepared`() = runTest {
        withMainDispatcher {
            val location = GeoLocation(24.7136, 46.6753)
            val rawPlaces = listOf(MosquePlace(1, "node", "An-Nur", latitude = 24.714, longitude = 46.675))
            val rendered = listOf(NearbyMosque(rawPlaces.single(), 78.0))
            val gate = CompletableDeferred<List<MosquePlace>>()
            val repository = defaultRepository().also {
                coEvery { it.searchAndCache(location, 5, any()) } coAnswers { gate.await() }
                every { it.nearbyFor(location, rawPlaces, 5) } returns rendered
            }
            val viewModel = newViewModel(locationProvider = locationProvider(location), repository = repository)

            viewModel.refresh()
            advanceUntilIdle()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.LoadingMosques(emptyList()))

            gate.complete(rawPlaces)
            advanceUntilIdle()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.Success(rendered))
        }
    }

    @Test
    fun `empty and request failure map to explicit list states`() = runTest {
        withMainDispatcher {
            val location = GeoLocation(24.7136, 46.6753)
            val emptyRepository = defaultRepository().also {
                coEvery { it.searchAndCache(location, 5, any()) } returns emptyList()
                every { it.nearbyFor(location, emptyList(), 5) } returns emptyList()
            }
            val emptyViewModel = newViewModel(locationProvider(location), emptyRepository)
            emptyViewModel.refresh()
            advanceUntilIdle()
            assertThat(emptyViewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.Empty)

            val failedRepository = defaultRepository().also {
                coEvery { it.searchAndCache(location, 5, any()) } throws IllegalStateException("offline")
            }
            val failedViewModel = newViewModel(locationProvider(location), failedRepository)
            failedViewModel.refresh()
            advanceUntilIdle()
            assertThat(failedViewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.Error)
        }
    }

    @Test
    fun `cached locations render immediately without stale distances while a new fix is pending`() = runTest {
        withMainDispatcher {
            val rawPlaces = listOf(MosquePlace(2, "node", "Saved mosque", latitude = 24.714, longitude = 46.675))
            val cache = NearbyMosqueCache(rawPlaces, 24.7136, 46.6753, 5)
            val locationGate = CompletableDeferred<GeoLocation?>()
            val locationProvider = mockk<LocationProvider>()
            coEvery { locationProvider.currentLocation() } coAnswers { locationGate.await() }
            val repository = defaultRepository().also {
                every { it.cacheFrom(any()) } returns cache
            }
            val viewModel = newViewModel(locationProvider, repository)

            viewModel.refresh()
            runCurrent()

            assertThat(viewModel.presentation.value.state)
                .isEqualTo(NearbyMosquesUiState.LoadingLocation(rawPlaces))
            verify(exactly = 0) { repository.nearbyFor(any(), any(), any()) }

            locationGate.complete(null)
            advanceUntilIdle()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.LocationUnavailable)
        }
    }

    @Test
    fun `cached places are rendered offline with distances recalculated from current location`() = runTest {
        withMainDispatcher {
            val location = GeoLocation(24.7136, 46.6753)
            val rawPlaces = listOf(MosquePlace(3, "way", "Cached mosque", latitude = 24.714, longitude = 46.675))
            val cache = NearbyMosqueCache(rawPlaces, location.latitude, location.longitude, 5)
            val rendered = listOf(NearbyMosque(rawPlaces.single(), 78.0))
            val repository = defaultRepository().also {
                every { it.cacheFrom(any()) } returns cache
                every { it.nearbyFor(location, rawPlaces, 5) } returns rendered
                every { it.distanceMeters(any<GeoLocation>(), any<GeoLocation>()) } returns 0.0
                every { it.isFresh(any(), any()) } returns false
                coEvery { it.searchAndCache(location, 5, any()) } throws IllegalStateException("offline")
            }
            val viewModel = newViewModel(locationProvider(location), repository)

            viewModel.refresh()
            advanceUntilIdle()

            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.OfflineCache(rendered))
            coVerify(exactly = 1) { repository.searchAndCache(location, 5, any()) }
        }
    }

    @Test
    fun `fresh cache skips network while a radius change persists and refreshes`() = runTest {
        withMainDispatcher {
            val location = GeoLocation(24.7136, 46.6753)
            val rawPlaces = listOf(MosquePlace(4, "relation", "Fresh mosque", latitude = 24.714, longitude = 46.675))
            val cache = NearbyMosqueCache(rawPlaces, location.latitude, location.longitude, 5)
            val rendered = listOf(NearbyMosque(rawPlaces.single(), 78.0))
            val repository = defaultRepository().also {
                every { it.cacheFrom(any()) } returns cache
                every { it.nearbyFor(location, rawPlaces, 5) } returns rendered
                every { it.distanceMeters(any<GeoLocation>(), any<GeoLocation>()) } returns 0.0
                every { it.isFresh(any(), any()) } returns true
                coEvery { it.setRadius(10) } just runs
            }
            val viewModel = newViewModel(locationProvider(location), repository)

            viewModel.refresh()
            advanceUntilIdle()
            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.Success(rendered))
            coVerify(exactly = 0) { repository.searchAndCache(any(), any(), any()) }

            viewModel.selectRadius(10)
            advanceUntilIdle()
            coVerify(exactly = 1) { repository.setRadius(10) }
        }
    }

    @Test
    fun `deactivate cancels an active network request when leaving mosques tab`() = runTest {
        withMainDispatcher {
            val location = GeoLocation(24.7136, 46.6753)
            val repository = defaultRepository().also {
                coEvery { it.searchAndCache(location, 5, any()) } coAnswers { awaitCancellation() }
            }
            val viewModel = newViewModel(locationProvider(location), repository)

            viewModel.refresh()
            advanceUntilIdle()
            viewModel.deactivate()
            advanceUntilIdle()

            assertThat(viewModel.presentation.value.state).isEqualTo(NearbyMosquesUiState.LoadingMosques(emptyList()))
        }
    }

    private fun locationProvider(location: GeoLocation): LocationProvider = mockk {
        coEvery { currentLocation() } returns location
    }

    private fun defaultRepository(): NearbyMosqueRepository = mockk(relaxed = true) {
        every { cacheFrom(any()) } returns null
    }

    private fun newViewModel(
        locationProvider: LocationProvider = mockk(),
        repository: NearbyMosqueRepository = defaultRepository(),
        preferencesRepository: AppPreferencesRepository = mockk(),
    ): NearbyMosquesViewModel {
        every { preferencesRepository.preferences } returns flowOf(AppPreferences())
        return NearbyMosquesViewModel(locationProvider, repository, preferencesRepository)
    }

    private suspend fun <T> kotlinx.coroutines.test.TestScope.withMainDispatcher(block: suspend () -> T): T {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
