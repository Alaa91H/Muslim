package org.muslim.app.feature.prayertimes.ui.location

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Exercises the real compressed timezone boundary index on Android. This is
 * intentionally not mocked: the release crash happened inside zstd-jni while
 * constructing TimeZoneMap during the GPS save path.
 */
@RunWith(AndroidJUnit4::class)
class CoordinateTimeZoneResolverInstrumentationTest {
    @Test
    fun riyadhCoordinatesResolveThroughTheRealTimezoneIndex() = runBlocking {
        val zone = CoordinateTimeZoneResolver().resolve(24.7136, 46.6753)

        assertEquals("Asia/Riyadh", zone)
    }
}
