package org.muslim.app.core.location

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device-level regression for the process-killing GPS path. The test grants
 * foreground location, makes both platform client factories throw, and proves
 * that the real Android process receives an unavailable location rather than
 * an uncaught exception.
 */
@RunWith(AndroidJUnit4::class)
class GpsFailureInstrumentationTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun grantForegroundLocation() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @After
    fun revokeForegroundLocation() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.revokeRuntimePermission(
            context.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @Test
    fun brokenGpsServicesDoNotCrashTheAndroidProcess() {
        var fusedFactoryCalls = 0
        var platformFactoryCalls = 0
        val provider = FusedLocationProvider.createForTesting(
            context = context,
            fusedClientFactory = {
                fusedFactoryCalls += 1
                error("Simulated Google Play Services initialization failure")
            },
            platformLocationManagerFactory = {
                platformFactoryCalls += 1
                error("Simulated OEM LocationManager initialization failure")
            },
        )

        assertNull(runBlocking { provider.currentLocation() })
        assertEquals(1, fusedFactoryCalls)
        assertEquals(1, platformFactoryCalls)
    }
}
