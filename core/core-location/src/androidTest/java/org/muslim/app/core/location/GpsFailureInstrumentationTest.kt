package org.muslim.app.core.location

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
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
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            ParcelFileDescriptor.AutoCloseInputStream(
                instrumentation.uiAutomation.executeShellCommand(
                    "pm grant ${context.packageName} ${Manifest.permission.ACCESS_FINE_LOCATION}",
                ),
            ).use { it.readBytes() }
        }
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
