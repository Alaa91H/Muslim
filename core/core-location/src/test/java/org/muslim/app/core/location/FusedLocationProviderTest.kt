package org.muslim.app.core.location

import android.Manifest
import android.app.Application
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Regression coverage for the failure that previously force-closed the picker
 * when Google Play Services or an OEM location implementation failed during
 * provider creation. Every unavailable source must resolve to null so the UI
 * can show its existing recoverable GPS error instead of losing the process.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FusedLocationProviderTest {

    private val application: Application
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun clearLocationPermissions() {
        shadowOf(application).denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    @Test
    fun `construction defers fused client initialization until GPS is requested`() {
        var fusedFactoryCalls = 0
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = {
                fusedFactoryCalls += 1
                error("The factory must not run while the picker is being created")
            },
        )

        assertThat(fusedFactoryCalls).isEqualTo(0)

        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertThat(runBlocking { provider.currentLocation() }).isNull()
        assertThat(fusedFactoryCalls).isEqualTo(1)
    }

    @Test
    fun `fused and platform initialization failures return unavailable location instead of throwing`() {
        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        var fusedFactoryCalls = 0
        var platformFactoryCalls = 0
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = {
                fusedFactoryCalls += 1
                error("Simulated broken Google Play Services stack")
            },
            platformLocationManagerFactory = {
                platformFactoryCalls += 1
                error("Simulated broken OEM location service")
            },
        )

        val result = runBlocking { provider.currentLocation() }

        assertThat(result).isNull()
        // The lazy client failure is cached, so both fused attempts are safe and
        // no second platform initialization is needed after it fails.
        assertThat(fusedFactoryCalls).isEqualTo(1)
        assertThat(platformFactoryCalls).isEqualTo(1)
    }

    @Test
    fun `approximate permission accepts a valid platform fallback when fused is unavailable`() {
        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        val fallbackLocation = Location("test").apply {
            latitude = 24.7136
            longitude = 46.6753
            time = 1_000L
        }
        val locationManager = mockk<LocationManager>()
        every { locationManager.getLastKnownLocation(any()) } returns fallbackLocation
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = { throw IllegalStateException("Fused unavailable") },
            platformLocationManagerFactory = { locationManager },
        )

        val result = runBlocking { provider.currentLocation() }

        assertThat(result).isEqualTo(GeoLocation(latitude = 24.7136, longitude = 46.6753))
        verify(exactly = 3) { locationManager.getLastKnownLocation(any()) }
    }

    @Test
    fun `out of range platform fallback is rejected safely`() {
        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        val invalidFallback = Location("test").apply {
            latitude = 91.0
            longitude = 46.6753
            time = 1_000L
        }
        val locationManager = mockk<LocationManager>()
        every { locationManager.getLastKnownLocation(any()) } returns invalidFallback
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = { throw IllegalStateException("Fused unavailable") },
            platformLocationManagerFactory = { locationManager },
        )

        assertThat(runBlocking { provider.currentLocation() }).isNull()
    }

    @Test
    fun `Play Services listener registration failure returns unavailable location safely`() {
        shadowOf(application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val task = mockk<Task<Location>>()
        every { task.addOnSuccessListener(any()) } throws IllegalStateException("Listener registration failed")
        val fusedClient = mockk<FusedLocationProviderClient>()
        every { fusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns task
        every { fusedClient.lastLocation } returns task
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = { fusedClient },
            platformLocationManagerFactory = { null },
        )

        assertThat(runBlocking { provider.currentLocation() }).isNull()
    }

    @Test
    fun `missing foreground permission does not initialize any location source`() {
        var fusedFactoryCalls = 0
        var platformFactoryCalls = 0
        val provider = FusedLocationProvider.createForTesting(
            context = application,
            fusedClientFactory = {
                fusedFactoryCalls += 1
                mockk<FusedLocationProviderClient>()
            },
            platformLocationManagerFactory = {
                platformFactoryCalls += 1
                mockk<LocationManager>()
            },
        )

        val result = runBlocking { provider.currentLocation() }

        assertThat(result).isNull()
        assertThat(fusedFactoryCalls).isEqualTo(0)
        assertThat(platformFactoryCalls).isEqualTo(0)
    }
}
