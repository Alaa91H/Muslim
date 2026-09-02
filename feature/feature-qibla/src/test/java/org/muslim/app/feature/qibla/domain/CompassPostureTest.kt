package org.muslim.app.feature.qibla.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CompassPostureTest {
    @Test
    fun flatPhoneIsAccepted() {
        assertThat(CompassPosture.isLevel(pitchDegrees = 0f, rollDegrees = 0f)).isTrue()
        assertThat(CompassPosture.isLevel(pitchDegrees = 35f, rollDegrees = -35f)).isTrue()
    }

    @Test
    fun uprightPhoneIsRejected() {
        assertThat(CompassPosture.isLevel(pitchDegrees = 90f, rollDegrees = 0f)).isFalse()
        assertThat(CompassPosture.isLevel(pitchDegrees = -90f, rollDegrees = 0f)).isFalse()
    }

    @Test
    fun excessiveTiltAndNonFiniteValuesAreRejected() {
        assertThat(CompassPosture.isLevel(pitchDegrees = 60.1f, rollDegrees = 0f)).isFalse()
        assertThat(CompassPosture.isLevel(pitchDegrees = 0f, rollDegrees = -60.1f)).isFalse()
        assertThat(CompassPosture.isLevel(pitchDegrees = Float.NaN, rollDegrees = 0f)).isFalse()
        assertThat(CompassPosture.isLevel(pitchDegrees = 0f, rollDegrees = Float.POSITIVE_INFINITY)).isFalse()
    }
}
