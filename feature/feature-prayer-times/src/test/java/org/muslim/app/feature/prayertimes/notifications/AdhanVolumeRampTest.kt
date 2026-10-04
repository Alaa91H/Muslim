package org.muslim.app.feature.prayertimes.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class AdhanVolumeRampTest {
    @Test
    fun `ramp begins gently and reaches selected level`() {
        assertEquals(0.0f, AdhanVolumeRamp.level(0.0f, 0.8f), 0.001f)
        assertEquals(0.4f, AdhanVolumeRamp.level(0.5f, 0.8f), 0.001f)
        assertEquals(0.8f, AdhanVolumeRamp.level(1.0f, 0.8f), 0.001f)
    }

    @Test
    fun `ramp clamps invalid progress and target`() {
        assertEquals(0.0f, AdhanVolumeRamp.level(-1.0f, 0.8f), 0.001f)
        assertEquals(1.0f, AdhanVolumeRamp.level(2.0f, 2.0f), 0.001f)
    }

    @Test
    fun `ramp never exceeds target and remains muted for zero volume`() {
        assertEquals(0.0f, AdhanVolumeRamp.level(0.75f, 0.0f), 0.001f)
        assertEquals(0.5f, AdhanVolumeRamp.level(0.5f, 1.0f), 0.001f)
    }
}
